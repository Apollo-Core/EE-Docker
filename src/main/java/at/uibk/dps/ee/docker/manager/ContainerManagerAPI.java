package at.uibk.dps.ee.docker.manager;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;

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
  private final String hostUri;

  private Map<String, Integer> functions = new HashMap<>();

  @Inject
  public ContainerManagerAPI(String unix_pathname, String uri) {
    this.docker = new UnixDocker(new File(unix_pathname));
    this.images = docker.images();
    this.containers = docker.containers();
    this.hostUri = uri;
  }

  @Inject
  public ContainerManagerAPI(String uri, int port) {
    this.docker = new TcpDocker(URI.create("http://" + uri + ":" + port));
    this.images = docker.images();
    this.containers = docker.containers();
    this.hostUri = uri;
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
    Optional<Integer> port = Optional.ofNullable(this.functions.get(imageName));

    if (port.isEmpty()) {
      return JsonParser.parseString("{ \"error\": \"Function not available!\" }").getAsJsonObject();
    }

    try (CloseableHttpClient client = HttpClients.createDefault()) {

      HttpGet request = new HttpGet("http://" + this.hostUri + ":" + port.get());
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
      final int port = 8800 + this.functions.size();

      StringReader reader = new StringReader("{ \"PortBindings\": { \"8080/tcp\": [ { \"HostIp\": \"\", \"HostPort\": \""
        + port + "\" } ] } }");
      JsonReader jsonReader = Json.createReader(reader);
      javax.json.JsonObject hostConfig = jsonReader.readObject();
      jsonReader.close();

      javax.json.JsonObject exposedPorts = Json.createObjectBuilder()
        .add("8080/tcp", JsonValue.EMPTY_JSON_OBJECT)
        .build();

      Container container = containers.create(Json.createObjectBuilder()
        .add("Image", imageName)
        .add("ExposedPorts", exposedPorts)
        .add("HostConfig", hostConfig)
        .build());

      container.start();
      this.functions.put(imageName, port);

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
