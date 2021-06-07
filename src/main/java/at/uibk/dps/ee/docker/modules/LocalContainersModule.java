package at.uibk.dps.ee.docker.modules;

import at.uibk.dps.ee.core.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerExec;
import at.uibk.dps.ee.guice.modules.FunctionModule;

/**
 * Module for the configuration of the local modules.
 * 
 * @author Fedor Smirnov
 */
public class LocalContainersModule extends FunctionModule {

  @Override
  protected void config() {
    bind(ContainerManager.class).to(ContainerManagerExec.class);
  }
}
