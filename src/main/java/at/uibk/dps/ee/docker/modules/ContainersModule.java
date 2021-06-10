package at.uibk.dps.ee.docker.modules;

import at.uibk.dps.ee.docker.LocalResourcesDocker;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerExec;
import at.uibk.dps.ee.guice.modules.ResourceModule;

/**
 * Module for the configuration of the local modules.
 * 
 * @author Fedor Smirnov
 */
public class ContainersModule extends ResourceModule {

  @Override
  protected void config() {
    bind(ContainerManager.class).to(ContainerManagerExec.class);
    addLocalResources(LocalResourcesDocker.class);
  }
}
