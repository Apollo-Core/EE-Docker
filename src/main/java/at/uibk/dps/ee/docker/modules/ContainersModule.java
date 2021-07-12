package at.uibk.dps.ee.docker.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import at.uibk.dps.ee.docker.LocalResourcesDocker;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI;
import at.uibk.dps.ee.docker.manager.ContainerManagerExec;
import at.uibk.dps.ee.guice.modules.ResourceModule;

/**
 * Module for the configuration of the local modules.
 * 
 * @author Fedor Smirnov
 */
public class ContainersModule extends ResourceModule {

  /**
   * Specifies different types of Docker managers.
   * 
   * @author Fedor Smirnov
   *
   */
  protected enum DockerManager {
    Exec, API
  }

  @Order(1)
  @Info("The configured Docker manager")
  public DockerManager dockerManager = DockerManager.API;

  @Override
  protected void config() {
    if (dockerManager.equals(DockerManager.API)) {
      bind(ContainerManager.class).to(ContainerManagerDockerAPI.class);
    } else if (dockerManager.equals(DockerManager.Exec)) {
      bind(ContainerManager.class).to(ContainerManagerExec.class);
    } else {
      throw new IllegalArgumentException("Unknown manager option: " + dockerManager);
    }
    addLocalResources(LocalResourcesDocker.class);
  }

  public DockerManager getDockerManager() {
    return dockerManager;
  }

  public void setDockerManager(DockerManager dockerManager) {
    this.dockerManager = dockerManager;
  }
}
