package at.uibk.dps.ee.docker.manager;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
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

@Singleton
public class ContainerManagerDockerAPI implements ContainerManager {

  private final DockerClientConfig config;
  private final DockerHttpClient clientHttp;
  private final DockerClient client;

  @Inject
  public ContainerManagerDockerAPI() {
    this.config = DefaultDockerClientConfig.createDefaultConfigBuilder()
      .withDockerHost("tcp://localhost:2375")
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
    Adapter<PullResponseItem> res = this.client.pullImageCmd(imageName).start();

    try {
      res.awaitCompletion();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public JsonObject runImage(String imageName, JsonObject functionInput) {
    CreateContainerResponse container = this.client.createContainerCmd(imageName)
      .withCmd(functionInput.toString())
      .exec();

    this.client.startContainerCmd(container.getId()).exec();
    this.client.waitContainerCmd(container.getId()).start();

    ResultCallbackTemplate<?, Frame> log = this.client.logContainerCmd(container.getId())
      .withStdOut(true)
      .start();

    try {
      log.awaitCompletion();
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.client.removeContainerCmd(container.getId())
      .withForce(true)
      .exec();

    return (JsonObject) JsonParser.parseString(log.toString());
  }
}
