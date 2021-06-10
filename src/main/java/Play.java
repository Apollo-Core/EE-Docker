import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI;
import at.uibk.dps.ee.docker.server.ContainerServer;
import ch.qos.logback.classic.util.ContextInitializer;
import io.vertx.core.Vertx;

/**
 * This class is an example usage of Docker via the java api.
 *
 * @author Fedor Smirnov, Lukas Dötlinger
 */
public class Play {

  public static void main(String[] args) throws Exception {

    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logging/config/logback.xml");
    Vertx vertx = Vertx.vertx();

    ContainerManager manager = new ContainerManagerDockerAPI();

    ContainerServer server = new ContainerServer(vertx, manager);
    server.start();
  }
}
