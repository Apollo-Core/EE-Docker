import java.net.URI;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerAPI;
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

    // For Windows, TCP connection is needed.
    //ContainerManager manager = new ContainerManagerAPI(URI.create("http://localhost:2375"));

    // For Unix, using system sockets is recommended.
    //ContainerManager manager = new ContainerManagerAPI("/var/run/docker.sock");

    ContainerManager manager = new ContainerManagerDockerAPI("localhost:2375");

    ContainerServer server = new ContainerServer(vertx, manager);
    server.start();
  }
}
