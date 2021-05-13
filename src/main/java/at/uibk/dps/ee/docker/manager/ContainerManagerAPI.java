package at.uibk.dps.ee.docker.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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

  @Inject
  public ContainerManagerAPI(String unix_pathname) {
    this.docker = new UnixDocker(new File(unix_pathname));
    this.images = docker.images();
    this.containers = docker.containers();
  }

  @Inject
  public ContainerManagerAPI(URI uri) {
    this.docker = new TcpDocker(uri);
    this.images = docker.images();
    this.containers = docker.containers();
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
  public JsonObject runImage(String imageName, JsonObject functionInput) {
    try {
      final Container container = containers.create(Json.createObjectBuilder()
        .add("Image", imageName)
        .add("Cmd", functionInput.toString()).build());

      container.start();
      container.waitOn("not-running");
      final String output = container.logs().stdout().toString();
      container.remove();

      return (JsonObject) JsonParser.parseString(output);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new JsonObject();
  }

}
