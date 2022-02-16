package at.uibk.dps.ee.docker.manager;

import com.google.gson.JsonObject;
import io.vertx.core.Future;


/**
 * Interface used to manage the state of the docker containers.
 *
 * @author Fedor Smirnov
 */
public interface ContainerManager {

  /**
   * Initializes the given image during the configuration of Apollo.
   *
   * @param imageName the name of the image to initialize
   *
   * @return future which is completed when the image initialization is done, with
   *         the image name as result
   */
  Future<String> initImage(String imageName);

  /**
   * Runs a function from a provided image using some input.
   *
   * @param imageName the name of the image containing the function
   * @param functionInput the function input
   * @return the function output as a Json Object
   */
  Future<JsonObject> runImage(String imageName, JsonObject functionInput);

  /**
   * Performs the image operations necessary before shutting down.
   *
   * @param imageName the image to close
   * @return a future which is completed when the image is closed
   */
  Future<String> closeImage(String imageName);
}
