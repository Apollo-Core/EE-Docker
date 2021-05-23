import java.net.URI;
import java.util.List;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerAPI;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI;
import at.uibk.dps.ee.docker.manager.ContainerManagerExec;
import at.uibk.dps.ee.docker.server.ContainerServer;
import ch.qos.logback.classic.util.ContextInitializer;
import io.vertx.core.Vertx;

/**
 * This class is an example usage of Docker via the java api.
 *
 * @author Fedor Smirnov, Lukas Dötlinger
 */
public class Play {

  protected static final String imageName = "fedor89/addition";
  protected static final String containerName = "additionContainer7";
  protected static List<String> command;

  public static void main(String[] args) throws Exception {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "./logging/config/logback.xml");
    Vertx vertx = Vertx.vertx();

    // For Windows, TCP connection is needed.
    ContainerManager manager = new ContainerManagerAPI(URI.create("http://host.docker.internal:2375"));

    // For Unix, using system sockets is recommended.
    //ContainerManager manager = new ContainerManagerAPI("/var/run/docker.sock");

    //ContainerManager manager = new ContainerManagerDockerAPI();

    //ContainerManager manager = new ContainerManagerExec();

    ContainerServer server = new ContainerServer(vertx, manager);
    server.start();
  }


  /**
   * Method which creates the container with the command, starts it, retrieves the
   * logs (the result), and then stops the container.
   *
   * @param dockerClient: we assume that this is configured once
   */
//  protected static void createAndStart(DockerClient dockerClient) {
//    Instant start = Instant.now();
//
//    CreateContainerResponse container =
//        dockerClient.createContainerCmd(imageName).withName(containerName).withCmd(command).exec();
//
//    System.out.println("Container creation");
//    System.out.println(Duration.between(start, Instant.now()).toMillis());
//
//    dockerClient.startContainerCmd(container.getId()).exec();
//
//    // wait for container to complete
//    WaitContainerResultCallback waitCallBack = new WaitContainerResultCallback();
//    dockerClient.waitContainerCmd(container.getId()).exec(waitCallBack);
//    try {
//      waitCallBack.awaitCompletion();
//    } catch (InterruptedException e1) {
//      throw new IllegalStateException("Interrupted");
//    }
//
//    System.out.println("Container running");
//    System.out.println(Duration.between(start, Instant.now()).toMillis());
//
//
//    final List<String> logs = new ArrayList<>();
//
//    LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId());
//    logContainerCmd.withStdOut(true).withStdErr(true);
//
//    try {
//      logContainerCmd.exec(new ResultCallback.Adapter<>() {
//        @Override
//        public void onNext(Frame item) {
//          logs.add(item.toString());
//        }
//      }).awaitCompletion();
//    } catch (InterruptedException e) {
//      throw new IllegalStateException("such exception");
//    }
//
//    String stdPutString = logs.get(0);
//    String jsonString = stdPutString.substring(8);
//    JsonObject jsonO = (JsonObject) JsonParser.parseString(jsonString);
//
//    System.out.println(jsonO.toString());
//    dockerClient.removeContainerCmd(container.getId()).exec();
//
//    long duration = Duration.between(start, Instant.now()).toMillis();
//    System.out.println(duration + " ms");
//  }

}
