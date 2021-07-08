package at.uibk.dps.ee.docker.manager;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * A {@link ContainerManager} based on the `docker-java` API.
 * (https://github.com/docker-java/docker-java).
 *
 * @author Lukas Dötlinger
 */
@Singleton
public class ContainerManagerDockerAPI implements ContainerManager {

  private final DockerClient client;

  private Map<String, Integer> functions = new HashMap<>();
  private Map<String, String> containers = new HashMap<>();

  @Inject
  public ContainerManagerDockerAPI() {
    this.client = getDockerClient();
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

    if (System.getProperty("os.name").equals("Windows") || System.getenv().getOrDefault("HOST_CONNECTION_TYPE", "tcp").equals("tcp")) {
      config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("tcp://" + ConstantsManager.getDockerUri() + ":" + ConstantsManager.defaultDockerHTTPPort)
        .withDockerTlsVerify(false)
        .build();
    } else {
      System.out.println("We are on Linux.");
      config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("unix://" + ConstantsManager.defaultDockerUnixSocketLocation)
        .build();
    }

    var clientHttp = new ApacheDockerHttpClient.Builder()
      .dockerHost(config.getDockerHost())
      .sslConfig(config.getSSLConfig())
      .maxConnections(100)
      .build();

    Request request = Request.builder()
      .method(Request.Method.GET)
      .path("/_ping")
      .build();

    try (Response response = clientHttp.execute(request)) {
      assertThat(response.getStatusCode(), is(200));
    }

    return DockerClientImpl.getInstance(config, clientHttp);
  }

  public void pullImage(String imageName) {
    ResultCallback.Adapter<PullResponseItem> res = this.client.pullImageCmd(imageName).start();

    try {
      res.awaitCompletion();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public JsonObject runImage(String imageName, JsonObject functionInput) {
    Optional<Integer> port = Optional.ofNullable(this.functions.get(imageName));

    if (port.isEmpty()) {
      return JsonParser.parseString("{ \"error\": \"Function not available!\" }").getAsJsonObject();
    }

    var client = HttpClient.newHttpClient();

    var request = HttpRequest.newBuilder(
        getContainerAddress(imageName, port.get()))
      .header("accept", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(functionInput.toString()))
      .build();

    try {
      var response = client.send(request, BodyHandlers.ofString());
      var reader = new JsonReader(new StringReader(response.body()));
      reader.setLenient(true);
      System.out.println(response.body());
      var json = (JsonObject) JsonParser.parseReader(reader);
      return json;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return JsonParser.parseString("{ \"error\": \"Running function failed\" }").getAsJsonObject();
  }

  /**
   * Finds the correct address of a function container depending on the application type.
   * For dockerized applications, the function name can be used directly, while running
   * on the local OS uses a specific port mapping.
   *
   * @return The correct URI for a function container.
   */
  protected final URI getContainerAddress(String imageName, Integer port) {
    if (System.getenv().containsKey("DOCKERIZED_APPLICATION")) {
      return URI.create("http://" + imageName.replaceAll("/", "-") + ":" + ConstantsManager.defaultFunctionPort);
    } else {
      return URI.create("http://" + ConstantsManager.localhost + ":" + port);
    }
  }

  @Override
  public void initImage(String imageName) {
    this.pullImage(imageName);

    final int port = 8800 + functions.size();

    HostConfig hostConfig = HostConfig.newHostConfig()
      .withNetworkMode(ConstantsManager.dockerNetwork)
      .withPortBindings(PortBinding.parse(port + ":" + ConstantsManager.defaultFunctionPort + "/tcp"));

    CreateContainerResponse container = this.client.createContainerCmd(imageName)
      .withExposedPorts(ExposedPort.tcp(ConstantsManager.defaultFunctionPort))
      .withHostConfig(hostConfig)
      .withName(imageName.replaceAll("/", "-"))
      .exec();

    String containerId = container.getId();
    this.client.startContainerCmd(containerId).exec();

    try {
      this.client.waitContainerCmd(container.getId()).start().awaitStarted();
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.functions.put(imageName, port);
    this.containers.put(imageName, containerId);
  }

  @Override
  public void closeImage(String imageName) {
    this.client.removeContainerCmd(this.containers.get(imageName))
      .withForce(true)
      .exec();
  }
}
