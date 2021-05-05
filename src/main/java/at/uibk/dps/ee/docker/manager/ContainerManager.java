package at.uibk.dps.ee.docker.manager;

import com.google.gson.JsonObject;

/**
 * Class use to manage the state of the docker containers.
 * 
 * @author Fedor Smirnov
 */
public interface ContainerManager {

  /**
   * Pulls the image with the specified name.
   * 
   * @param imageName
   */
  void pullImage(String imageName);
  
  /**
   * Runs a container with the provided image and input
   * 
   * @param imageName the name of the container image
   * @param functionInput the function input.
   * @return the function output as a Json Object
   */ 
  JsonObject runImage(String imageName, JsonObject functionInput);
  
}
