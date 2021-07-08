package at.uibk.dps.ee.docker.manager;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;

/**
 * Interface used to manage the state of the docker containers.
 * 
 * @author Fedor Smirnov
 */
@ImplementedBy(ContainerManagerNone.class)
public interface ContainerManager {

  /**
   * Initializes the given image during the configuration of Apollo.
   * 
   * @param imageName the image to init
   */
  void initImage(String imageName);
   * Pulls the image with the specified name.
   *
   * @param imageName
   */
  void pullImage(String imageName);

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
  JsonObject runFunction(String imageName, JsonObject functionInput);

  /**
   * Starts a specific image.
   *
   * @param imageName the image a container is created from
   * @return the name of the created container
   */
  String startContainer(String imageName);

  /**
   * Stops a container and removes it.
   *
   * @param containerId the id of the container to be removed
   */
  void removeContainer(String containerId);

}
