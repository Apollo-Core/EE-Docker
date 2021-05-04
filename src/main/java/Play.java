import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * This class is an example usage of Docker via the java api.
 * 
 * @author Fedor Smirnov
 */
public class Play {

  protected static final String imageName = "fedor89/addition";;
  protected static final String containerName = "additionContainer7";
  protected static List<String> command;

  public static void main(String[] args) throws Exception {

    // Instant start = Instant.now();
    //
    //
    //
    // JsonObject json = new JsonObject();
    // json.add("firstSummand", new JsonPrimitive(3));
    // json.add("secondSummand", new JsonPrimitive(4));
    //
    // command = new ArrayList<>();
    // command.add("java");
    // command.add("-jar");
    // command.add("addition.jar");
    // command.add(json.toString());
    //
    // DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    //
    // System.out.println("Docker client");
    // System.out.println(Duration.between(start, Instant.now()).toMillis());

    runByExec();

    // createAndStart(dockerClient);
  }

  /**
   * Runs the container via the exec command.
   * 
   * @throws IOException
   * @throws InterruptedException
   */
  protected static void runByExec() throws IOException, InterruptedException {
    Instant start = Instant.now();

    JsonObject object = new JsonObject();
    object.add("firstSummand", new JsonPrimitive(3));
    object.add("secondSummand", new JsonPrimitive(13));

    String command =
        "docker run fedor89/addition java -jar addition.jar \"{\"firstSummand\":5, \"secondSummand\": 6}\"";
    String command2 = "docker run fedor89/addition java -jar addition.jar " + object.toString();
    Runtime runtime = Runtime.getRuntime();

    Process pr = runtime.exec(command2);
    pr.waitFor();
    if (pr.exitValue() == 1) {
      System.err.println("exited with 1");
      return;
    }
    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    String line = "";
    while ((line = buf.readLine()) != null) {
      System.out.println(line);


    }
    System.out.println(Duration.between(start, Instant.now()).toMillis());

  }


  /**
   * Method which creates the container with the command, starts it, retrieves the
   * logs (the result), and then stops the container.
   * 
   * @param dockerClient: we assume that this is configured once
   */
  protected static void createAndStart(DockerClient dockerClient) {
    Instant start = Instant.now();

    CreateContainerResponse container =
        dockerClient.createContainerCmd(imageName).withName(containerName).withCmd(command).exec();

    System.out.println("Container creation");
    System.out.println(Duration.between(start, Instant.now()).toMillis());

    dockerClient.startContainerCmd(container.getId()).exec();

    // wait for container to complete
    WaitContainerResultCallback waitCallBack = new WaitContainerResultCallback();
    dockerClient.waitContainerCmd(container.getId()).exec(waitCallBack);
    try {
      waitCallBack.awaitCompletion();
    } catch (InterruptedException e1) {
      throw new IllegalStateException("Interrupted");
    }

    System.out.println("Container running");
    System.out.println(Duration.between(start, Instant.now()).toMillis());


    final List<String> logs = new ArrayList<>();

    LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(container.getId());
    logContainerCmd.withStdOut(true).withStdErr(true);

    try {
      logContainerCmd.exec(new ResultCallback.Adapter<>() {
        @Override
        public void onNext(Frame item) {
          logs.add(item.toString());
        }
      }).awaitCompletion();
    } catch (InterruptedException e) {
      throw new IllegalStateException("such exception");
    }

    String stdPutString = logs.get(0);
    String jsonString = stdPutString.substring(8);
    JsonObject jsonO = (JsonObject) JsonParser.parseString(jsonString);

    System.out.println(jsonO.toString());
    dockerClient.removeContainerCmd(container.getId()).exec();

    long duration = Duration.between(start, Instant.now()).toMillis();
    System.out.println(duration + " ms");
  }

}
