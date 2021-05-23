package at.uibk.dps.ee.docker.modules;

import org.opt4j.core.config.annotations.Info;
import org.opt4j.core.config.annotations.Order;
import org.opt4j.core.start.Constant;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerAPI;
import at.uibk.dps.ee.docker.server.ServerStarter;
import at.uibk.dps.ee.guice.modules.FunctionModule;

/**
 * Module for the configuration of the local modules.
 *
 * @author Fedor Smirnov
 */
public class LocalContainersModule extends FunctionModule {

  @Order(1)
  @Info("The port used by the container manager.")
  @Constant(value = "portNumber", namespace = ServerStarter.class)
  public int portNumber = 5055;

  @Override
  protected void config() {
    bind(ContainerManager.class).to(ContainerManagerAPI.class);
    bind(ServerStarter.class).asEagerSingleton();
  }

  public int getPortNumber() {
    return portNumber;
  }

  public void setPortNumber(int portNumber) {
    this.portNumber = portNumber;
  }
}
