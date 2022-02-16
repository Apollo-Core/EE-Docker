package at.uibk.dps.ee.docker.server;

import org.opt4j.core.start.Constant;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.docker.manager.ContainerManagerProvider;
import io.vertx.core.Vertx;

/**
 * The {@link ServerStarter} enables an automated startup of the
 * {@link ContainerServer} during the Guice configuration. In the middle- to
 * long run, the container server should be transformed into a verticle, which
 * is connected to Apollo via the event bus. This class should then become
 * obsolete.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class ServerStarter {

  /**
   * Injection constructor.
   * 
   * @param portNumber The port which will be used by the container manager.
   * @param manager the container manager to use
   */
  @Inject
  public ServerStarter(
      @Constant(value = "portNumber", namespace = ServerStarter.class) int portNumber,
      ContainerManagerProvider managerProv) {
    Vertx vertx = Vertx.vertx();
    ContainerServer server = new ContainerServer(vertx, managerProv.getContainerManager());
    server.start();
  }
}
