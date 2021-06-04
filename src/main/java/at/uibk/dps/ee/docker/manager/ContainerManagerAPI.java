package at.uibk.dps.ee.docker.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import com.amihaiemil.docker.Container;
import com.amihaiemil.docker.Containers;
import com.amihaiemil.docker.Docker;
import com.amihaiemil.docker.Images;
import com.amihaiemil.docker.TcpDocker;
import com.amihaiemil.docker.UnexpectedResponseException;
import com.amihaiemil.docker.UnixDocker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;


/**
 * A {@link ContainerManager} based on the Docker API by amihaiemil
 * (https://github.com/amihaiemil/docker-java-api).
 *
 * @author Lukas Dötlinger
 */
@Singleton
public class ContainerManagerAPI implements ContainerManager {

  private final Docker docker;
  private final Images images;
  private final Containers containers;

  private Map<String, Integer> functions = new HashMap<>();

  /**
   * Injection constructor.
   */
  @Inject
  public ContainerManagerAPI() {
    this.docker = getDocker();
    this.images = docker.images();
    this.containers = docker.containers();
  }

  /**
   * Creates the docker object using the host address matching the current
   * operating system.
   * 
   * @return the docker object using the host address matching the current
   *         operating system
   */
  protected final Docker getDocker() {
    if (System.getProperty("os.name").equals("Windows")) {
      try {
        return new TcpDocker(new URI(ConstantsManager.dockerHostWindows));
      } catch (URISyntaxException e) {
        throw new IllegalStateException("Incorrect Docker Host for Windows.", e);
      }
    } else {
      System.out.println("we are on linux");
      return new UnixDocker(new File(ConstantsManager.dockerHostUnix));
    }
  }

  @Override
  public void pullImage(String imageName) {
    try {
      this.images.pull(imageName, "latest");
    } catch (UnexpectedResponseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public JsonObject runFunction(String imageName, JsonObject functionInput) {
    final int port = functions.get(imageName);

    try (CloseableHttpClient client = HttpClients.createDefault()) {

      HttpGet request = new HttpGet("http://host.docker.internal:" + port);
      request.setHeader("Accept", "application/json");
      request.setHeader("Content-type", "application/json");
      request.setEntity(new StringEntity(functionInput.toString()));

      CloseableHttpResponse response = client.execute(request);
      final String responseString = EntityUtils.toString(response.getEntity());

      return (JsonObject) JsonParser.parseString(responseString);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new JsonObject();
  }

  @Override
  public String startContainer(String imageName) {
    try {
      final int port = 8800 + functions.size();

      final javax.json.JsonObject hostConfig = Json.createObjectBuilder().add("PortBindings",
          Json.createObjectBuilder().add("8080/tcp", Json.createArrayBuilder().add(
              Json.createObjectBuilder().add("HostPort", port).add("HostIp", "127.0.0.1").build()))
              .build())
          .build();

      final javax.json.JsonObject exposedPorts =
          Json.createObjectBuilder().add("8080/tcp", Json.createObjectBuilder().build()).build();

      final Container container =
          containers.create(Json.createObjectBuilder().add("Image", imageName)
              // .add("ExposedPorts", Json.createParser(new StringReader("{\"8080/tcp\": {
              // }}")).getObject())
              // .add("HostConfig", Json.createParser(new StringReader("\"PortBindings\":
              // {\"8080/tcp\": [{ \"HostPort\": \"8801\"}]}")).getObject())
              // .add("ExposedPorts", Json.createObjectBuilder()
              // .add("8080/tcp", JsonValue.EMPTY_JSON_OBJECT)
              // .build())
              // .add("HostConfig", Json.createObjectBuilder()
              // .add("PortBindings", Json.createObjectBuilder()
              // .add("8080/tcp", Json.createObjectBuilder()
              // .add("HostPort", port)
              // .build())
              // .build())
              // .build())
              .add("ExposedPorts", exposedPorts).add("HostConfig", hostConfig).build());

      container.start();
      functions.put(imageName, port);

      return container.containerId();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void removeContainer(String containerId) {
    final Container container = containers.get(containerId);
    try {
      container.remove();
    } catch (UnexpectedResponseException | IOException e) {
      e.printStackTrace();
    }
  }

}
