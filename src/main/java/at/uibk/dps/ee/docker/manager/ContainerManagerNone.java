package at.uibk.dps.ee.docker.manager;

import com.google.gson.JsonObject;
import io.vertx.core.Future;

/**
 * To prevent build errors when no manager needed.
 * 
 * @author Fedor Smirnov
 */
public class ContainerManagerNone implements ContainerManager {

  @Override
  public void initImage(String imageName) {
    throw new IllegalStateException("Should never be called");

  }

  @Override
  public Future<JsonObject> runImage(String imageName, JsonObject functionInput) {
    return Future.failedFuture("Should never be called.");
  }

  @Override
  public void closeImage(String imageName) {
    throw new IllegalStateException("Should never be called");
  }
}
