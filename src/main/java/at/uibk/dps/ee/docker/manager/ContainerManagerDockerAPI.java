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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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

    if (System.getProperty("os.name").equals("Windows") || System.getenv().get("HOST_CONNECTION_TYPE").equals("tcp")) {
      config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("tcp://" + ConstantsManager.getDockerUri() + ":" + ConstantsManager.defaultDockerHTTPPort)
        .withDockerTlsVerify(false)
        .build();
    } else {
      System.out.println("We are on Linux.");
      config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .build();
    }

    var clientHttp = new ApacheDockerHttpClient.Builder()
      .dockerHost(config.getDockerHost())
      .sslConfig(config.getSSLConfig())
      .maxConnections(100)
      .build();

    return DockerClientImpl.getInstance(config, clientHttp);
  }

  @Override
  public void pullImage(String imageName) {
    ResultCallback.Adapter<PullResponseItem> res = this.client.pullImageCmd(imageName).start();

    try {
      res.awaitCompletion();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public JsonObject runFunction(String imageName, JsonObject functionInput) {
    Optional<Integer> port = Optional.ofNullable(this.functions.get(imageName));

    if (port.isEmpty()) {
      return JsonParser.parseString("{ \"error\": \"Function not available!\" }").getAsJsonObject();
    }

    var client = HttpClient.newHttpClient();

    var request = HttpRequest.newBuilder(
        URI.create("http://" + imageName.replaceAll("/", "-") + ":" + ConstantsManager.defaultFunctionPort))
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

  @Override
  public String startContainer(String imageName) {
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
    return containerId;
  }

  @Override
  public void removeContainer(String containerId) {
    this.client.removeContainerCmd(containerId)
      .withForce(true)
      .exec();
  }
}
