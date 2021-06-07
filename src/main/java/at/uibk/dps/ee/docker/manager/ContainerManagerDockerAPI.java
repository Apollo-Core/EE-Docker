package at.uibk.dps.ee.docker.manager;

import java.io.IOException;
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
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

  private final DockerClientConfig config;
  private final DockerHttpClient clientHttp;
  private final DockerClient client;
  private final String hostUri;

  private Map<String, Integer> functions = new HashMap<>();

  @Inject
  public ContainerManagerDockerAPI(String uri, int port) {
    this.hostUri = uri;

    this.config = DefaultDockerClientConfig.createDefaultConfigBuilder()
      .withDockerHost("tcp://" + uri + ":" + port)
      .withDockerTlsVerify(false)
      .build();

    this.clientHttp = new ApacheDockerHttpClient.Builder()
      .dockerHost(config.getDockerHost())
      .sslConfig(config.getSSLConfig())
      .maxConnections(100)
      .build();

    Request request = Request.builder()
      .method(Request.Method.GET)
      .path("/_ping")
      .build();

    try (Response response = clientHttp.execute(request)) {
      assert(response.getStatusCode() == 200);
    }

    this.client = DockerClientImpl.getInstance(config, clientHttp);
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
        URI.create("http://" + imageName.replaceAll("/", "-") + ":8080"))
      .header("accept", "application/json")
      .build();

    try {
      var response = client.send(request, BodyHandlers.ofString());
      return (JsonObject) JsonParser.parseString(response.body());
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    return JsonParser.parseString("{ \"error\": \"Running function failed\" }").getAsJsonObject();
  }

  @Override
  public String startContainer(String imageName) {
    final int port = 8800 + functions.size();

    HostConfig hostConfig = HostConfig.newHostConfig()
      .withNetworkMode(ConstantsManager.dockerNetwork)
      .withPortBindings(PortBinding.parse(port + ":8080/tcp"));

    CreateContainerResponse container = this.client.createContainerCmd(imageName)
      .withExposedPorts(ExposedPort.tcp(8080))
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
