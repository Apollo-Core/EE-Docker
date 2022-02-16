package at.uibk.dps.ee.docker.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.config.annotations.Required;
import at.uibk.dps.ee.docker.LocalDockerContainers;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI.UsedOperatingSystem;
import at.uibk.dps.ee.guice.modules.EeModule;

/**
 * Module for the configuration of the local modules.
 * 
 * @author Fedor Smirnov
 */
public class LocalDockerModule extends EeModule{

  @Order(1)
  @Info("The operating system configured on the device running the containers.")
  @Required(property = "dockerManager", elements = "DockerApi")
  public UsedOperatingSystem usedOs = UsedOperatingSystem.Unix;

  @Override
  protected void config() {
    addManagedComponent(LocalDockerContainers.class);
    bind(UsedOperatingSystem.class).toInstance(usedOs);
  }

  public UsedOperatingSystem getUsedOs() {
    return usedOs;
  }

  public void setUsedOs(UsedOperatingSystem usedOs) {
    this.usedOs = usedOs;
  }
}
