package at.uibk.dps.ee.docker.manager;

import at.uibk.dps.ee.guice.starter.VertxProvider;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Singleton;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.opt4j.core.start.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link ContainerManager} based on the `docker-java` API.
 * (https://github.com/docker-java/docker-java).
 *
 * @author Lukas Dötlinger
 */
@Singleton
public class ContainerManagerDockerAPI implements ContainerManager {

  protected final Logger logger = LoggerFactory.getLogger(ContainerManagerDockerAPI.class);
  private final DockerClient client;

  protected final WebClient httpClient;
  protected final Vertx vertx;

  private Map<String, Integer> functions = new HashMap<>();
  private Map<String, String> containers = new HashMap<>();
  private int currentMaxPort = ConstantsManager.firstFunctionExposedPort + 1;

  protected final UsedOperatingSystem usedOs;

  /**
   * Enum for the configuration of the used OS
   * 
   * @author Fedor Smirnov
   */
  public enum UsedOperatingSystem {
    Windows, Unix
  }

  /**
   * Constructs an instance of ContainerManagerDockerAPI. Creates a connection to
   * a local Docker client and creates a network, iff it doen't already exist, to
   * be used by the function containers.
   */
  public ContainerManagerDockerAPI(VertxProvider vProv, @Constant(value = "usedOs",
      namespace = ContainerManagerDockerAPI.class) UsedOperatingSystem usedOs) {
    this.usedOs = usedOs;
    this.client = getDockerClient();
    try {
      this.client.createNetworkCmd().withName(ConstantsManager.dockerNetwork)
          .withCheckDuplicate(true).exec();
    } catch (Exception e) {
      logger.info(
          "Docker network for function containers already exits! Make sure this is set up correctly!");
    }

    // Check for already running functions.
    this.client.listContainersCmd().withShowAll(true)
        .withNetworkFilter(List.of(ConstantsManager.dockerNetwork)).exec().forEach(container -> {
          // Remove function container if container isn't running.
          if (!container.getState().equals("running")) {
            this.client.removeContainerCmd(container.getId()).withForce(true).exec();
            logger.info("Removed non-running existing function " + container.getImage());
            return;
          }

          var id = container.getId();
          containers.put(container.getImage(), container.getId());

          var hostPortSpec = Integer
              .parseInt(this.client.inspectContainerCmd(id).exec().getNetworkSettings().getPorts()
                  .getBindings().values().stream().findFirst().orElseThrow(RuntimeException::new)[0]
                      .getHostPortSpec());
          functions.put(container.getImage(), hostPortSpec);

          logger.info("Discovered function {} at port {} with id {}.", container.getImage(),
              hostPortSpec, id);
          this.currentMaxPort = Math.max(hostPortSpec, this.currentMaxPort);
        });

    this.vertx = vProv.getVertx();
    this.httpClient = WebClient.create(vertx);
  }

  /**
   * Creates the docker client object using the host address matching the current
   * operating system.
   *
   * @return the docker client object using the host address matching the current
   *         operating system
   */
  protected final DockerClient getDockerClient() {
    DockerClientConfig config;
    if (usedOs.equals(UsedOperatingSystem.Windows)) {
      logger.info("Using TCP connection to Docker Host.");
      config =
          DefaultDockerClientConfig
              .createDefaultConfigBuilder().withDockerHost("tcp://"
                  + ConstantsManager.getDockerUri() + ":" + ConstantsManager.defaultDockerHTTPPort)
              .withDockerTlsVerify(false).build();
    } else if (usedOs.equals(UsedOperatingSystem.Unix)) {
      logger.info("Using UNIX Socket for connecting to Docker Host.");
      config = DefaultDockerClientConfig.createDefaultConfigBuilder()
          .withDockerHost("unix://" + ConstantsManager.defaultDockerUnixSocketLocation).build();
    } else {
      throw new IllegalArgumentException("Unknown OS configured: " + usedOs.name());
    }
    var clientHttp = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig()).maxConnections(100).build();
    Request request = Request.builder().method(Request.Method.GET).path("/_ping").build();
    try (Response response = clientHttp.execute(request)) {
      if (response.getStatusCode() != 200) {
        throw new IllegalStateException("Ping to Docker API failed.");
      }
    }
    return DockerClientImpl.getInstance(config, clientHttp);
  }

  /**
   * Executes a function running inside a container with a given input.
   * @param imageName the name of the image containing the function
   * @param functionInput the function input
   *
   * @return a future to be completed with the function result
   */
  @Override
  public Future<JsonObject> runImage(String imageName, JsonObject functionInput) {
    Optional<Integer> port = Optional.ofNullable(this.functions.get(imageName));
    if (port.isEmpty()) {
      return Future.failedFuture("Function not available.");
    }

    Promise<JsonObject> resultPromise = Promise.promise();
    logger.info("Local function {} triggerred.", imageName);

    httpClient.postAbs(getContainerAddress(imageName, port.get()).toASCIIString())
        .sendJson(new io.vertx.core.json.JsonObject(functionInput.toString()))
        .onSuccess(asyncRes -> {
          logger.info("Local function {} finished.", imageName);
          JsonObject jsonResult =
              JsonParser.parseString(asyncRes.body().toString()).getAsJsonObject();
          resultPromise.complete(jsonResult);
        }).onFailure(failureThrowable -> {
          System.err.println(failureThrowable.getMessage());
        });
    return resultPromise.future();
  }

  /**
   * Finds the correct address of a function container depending on the
   * application type. For dockerized applications, the function name can be used
   * directly, while running on the local OS uses a specific port mapping.
   *
   * @return The correct URI for a function container.
   */
  protected final URI getContainerAddress(String imageName, Integer port) {
    if (System.getenv().containsKey("DOCKERIZED_APPLICATION")) {
      return URI.create(
          "http://" + imageName.replaceAll("/", "-") + ":" + ConstantsManager.defaultFunctionPort);
    } else {
      return URI.create("http://" + ConstantsManager.localhost + ":" + port);
    }
  }

  /**
   * Pulls an image from the registry selected at the system.
   * @param imageName which should be pulled
   *
   * @return a future to be completed if the pull was successful.
   */
  private Future<String> pullImage(String imageName) {
    Promise<String> pullPromise = Promise.promise();

    ResultCallback.Adapter<PullResponseItem> callback =
        new ResultCallback.Adapter<PullResponseItem>() {
          public void onNext(PullResponseItem pullResponse) {
            if (pullResponse.isErrorIndicated()) {
              logger.warn("Error pulling image " + imageName
                  + " from registry. Testing if image is available locally.");

              // Checks if image is available. Would throw another exception if it isn't.
              client.inspectImageCmd(imageName).exec();

              logger.warn("Image " + imageName + " is available locally!");
            } else if (pullResponse.isPullSuccessIndicated()) {
              logger.info("Pulled image {}.", imageName);
              logger.info("Returned pull status {}.", pullResponse.getStatus());
              pullPromise.complete(imageName);
            }
          }
        };

    logger.info("Pull command image {}", imageName);
    this.client.pullImageCmd(imageName).exec(callback);

    return pullPromise.future();
  }

  /**
   * @return next free port to be used by function container.
   */
  private synchronized int getNextPort() {
    return currentMaxPort++;
  }

  /**
   * Starts a container for a function image.
   * @param imageName the name of the image to be initialized
   *
   * @return a future to be completed if the image is running
   */
  @Override
  public Future<String> initImage(String imageName) {
    Promise<String> resultPromise = Promise.promise();

    // Return if an image is already running.
    if (this.client.listContainersCmd().exec().stream()
        .anyMatch(c -> c.getImage().equals(imageName))) {
      logger.info("Already running: " + imageName);
      resultPromise.complete(imageName);
      return resultPromise.future();
    }
    final int port = getNextPort();

    this.pullImage(imageName).onComplete(r -> {
      HostConfig hostConfig = HostConfig.newHostConfig()
          .withNetworkMode(ConstantsManager.dockerNetwork).withPortBindings(
              PortBinding.parse(port + ":" + ConstantsManager.defaultFunctionPort + "/tcp"));

      CreateContainerResponse container = this.client.createContainerCmd(imageName)
          .withExposedPorts(ExposedPort.tcp(ConstantsManager.defaultFunctionPort))
          .withHostConfig(hostConfig).withName(imageName.replaceAll("/", "-")).exec();

      logger.info("Starting container");
      String containerId = container.getId();
      this.client.startContainerCmd(containerId).exec();

      functions.put(imageName, port);
      waitOnContainer(resultPromise, containerId, imageName);
    });

    return resultPromise.future();
  }

  /**
   * Uses VertX timer to wait until a container is running.
   * @param promise to be completed if container is running
   * @param containerId of the container
   */
  public void waitOnContainer(Promise<String> promise, String containerId, String imageName) {
    var res = this.client.inspectContainerCmd(containerId).exec();

    if (Boolean.TRUE.equals(res.getState().getRunning())) {
      // Wait for the function process to start inside the container.
      vertx.setTimer(1000, e -> {
        logger.info("running!");
        containers.put(imageName, containerId);
        promise.complete(imageName);
      });
    } else {
      vertx.setTimer(50, e -> {
        logger.info("waiting .......");
        waitOnContainer(promise, containerId, imageName);
      });
    }
  }

  /**
   * Removes the container for an image.
   * @param imageName the image of the container
   *
   * @return a future
   */
  @Override
  public Future<String> closeImage(String imageName) {
    Promise<String> resultPromise = Promise.promise();

    logger.info("Removing container {}.", imageName);
    this.client.removeContainerCmd(this.containers.get(imageName)).withForce(true).exec();
    resultPromise.complete();

    return resultPromise.future();
  }
}
