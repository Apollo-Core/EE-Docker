  
import at.uibk.dps.ee.docker.VertXProvider;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI;
import at.uibk.dps.ee.docker.server.ContainerServer;
import ch.qos.logback.classic.util.ContextInitializer;

/**
 * This class is an example usage of Docker via the java api.
 *
 * @author Fedor Smirnov, Lukas Dötlinger
 */
public class Play {

  public static void main(String[] args) throws Exception {

    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logging/config/logback.xml");
    VertXProvider vProv = new VertXProvider();

    ContainerManager manager = new ContainerManagerDockerAPI(vProv);

    ContainerServer server = new ContainerServer(vProv.getVertX(), manager);
    server.start();
  }
}