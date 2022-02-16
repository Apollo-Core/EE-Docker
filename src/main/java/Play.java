
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI.UsedOperatingSystem;
import at.uibk.dps.ee.docker.server.ContainerServer;
import at.uibk.dps.ee.guice.starter.VertxProvider;
import ch.qos.logback.classic.util.ContextInitializer;

/**
 * This class is an example usage of Docker via the java api.
 *
 * @author Fedor Smirnov, Lukas Dötlinger
 */
public class Play {

  public static void main(String[] args) throws Exception {

    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logging/config/logback.xml");
    VertxProvider vProv = new VertxProvider();

    UsedOperatingSystem usedOs = UsedOperatingSystem.Unix;

    ContainerManager manager = new ContainerManagerDockerAPI(vProv, usedOs);

    ContainerServer server = new ContainerServer(vProv.getVertx(), manager);
    server.start();
  }
}
