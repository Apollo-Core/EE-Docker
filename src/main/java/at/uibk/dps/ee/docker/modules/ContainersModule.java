package at.uibk.dps.ee.docker.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import at.uibk.dps.ee.docker.LocalResourcesDocker;
import at.uibk.dps.ee.docker.manager.ContainerManagerProviderDocker;
import at.uibk.dps.ee.docker.manager.ContainerManagerProviderDocker.DockerManager;
import at.uibk.dps.ee.guice.container.ContainerManagerProvider;
import at.uibk.dps.ee.guice.modules.ResourceModule;

/**
 * Module for the configuration of the local modules.
 * 
 * @author Fedor Smirnov
 */
public class ContainersModule extends ResourceModule {

  @Order(1)
  @Info("The configured Docker manager")
  public DockerManager dockerManager = DockerManager.DockerApi;

  @Override
  protected void config() {
    bind(ContainerManagerProvider.class).to(ContainerManagerProviderDocker.class);
    bind(DockerManager.class).toInstance(dockerManager);
    addLocalResources(LocalResourcesDocker.class);
  }

  public DockerManager getDockerManager() {
    return dockerManager;
  }

  public void setDockerManager(DockerManager dockerManager) {
    this.dockerManager = dockerManager;
  }
}
