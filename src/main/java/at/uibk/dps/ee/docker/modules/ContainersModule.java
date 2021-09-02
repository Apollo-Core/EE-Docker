package at.uibk.dps.ee.docker.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.config.annotations.Required;
import at.uibk.dps.ee.docker.LocalResourcesDocker;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI.UsedOperatingSystem;
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

  @Order(2)
  @Info("The operating system configured on the device running the containers.")
  @Required(property = "dockerManager", elements = "DockerApi")
  public UsedOperatingSystem usedOs = UsedOperatingSystem.Unix;

  @Override
  protected void config() {
    bind(ContainerManagerProvider.class).to(ContainerManagerProviderDocker.class);
    bind(DockerManager.class).toInstance(dockerManager);
    addLocalResources(LocalResourcesDocker.class);
    bind(UsedOperatingSystem.class).toInstance(usedOs);
  }

  public DockerManager getDockerManager() {
    return dockerManager;
  }

  public void setDockerManager(DockerManager dockerManager) {
    this.dockerManager = dockerManager;
  }

  public UsedOperatingSystem getUsedOs() {
    return usedOs;
  }

  public void setUsedOs(UsedOperatingSystem usedOs) {
    this.usedOs = usedOs;
  }
}
