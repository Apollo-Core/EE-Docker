package at.uibk.dps.ee.docker.manager;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;

/**
 * Interface used to manage the state of the docker containers.
 *
 * @author Fedor Smirnov
 */
public interface ContainerManager {

  /**
   * Initializes the given image during the configuration of Apollo.
   *
   * @param imageName the image to init
   */
  void initImage(String imageName);

  /**
   * Runs a function from a provided image using some input.
   *
   * @param imageName the name of the image containing the function
   * @param functionInput the function input
   * @return the function output as a Json Object
   */
  JsonObject runImage(String imageName, JsonObject functionInput);

  /**
   * Performs the image operations necessary before shutting down.
   *
   * @param imageName the image to close
   */
  void closeImage(String imageName);
}
