package at.uibk.dps.ee.docker.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.core.ContainerManager;
import io.vertx.core.Future;

/**
 * A {@link ContainerManager} based on the exec command (directly calling the
 * docker commands).
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class ContainerManagerExec implements ContainerManager {

  protected final Logger logger = LoggerFactory.getLogger(ContainerManagerExec.class);
  protected final Runtime runtime;
  protected final ContainerInputManager inputManager = new ContainerInputManager();

  protected class CommandException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  @Inject
  public ContainerManagerExec() {
    this.runtime = Runtime.getRuntime();
  }

  @Override
  public void initImage(String imageName) {
    if (!checkWhetherImageExists(imageName)) {
      pullFromDocker(imageName);
    }
  }

  @Override
  public void closeImage(String imageName) {
    // Nothing to do here
  }
  
  @Override
  public Future<JsonObject> runImage(String imageName, JsonObject functionInput) {
    // create the temporal file on the host
    int suffix = inputManager.createHostInputFile(functionInput);

    StringBuffer commandBuffer = new StringBuffer();
    String hostPath = Path.of("").toAbsolutePath().toString();
    commandBuffer.append(ConstantsManager.dockerCommandRun).append(hostPath).append('/')
        .append(ConstantsManager.inputFileName + suffix + ".json").append(':')
        .append(ConstantsManager.containerSrcPath).append('/')
        .append(ConstantsManager.inputFileName + ".json").append(' ');

    String input = imageName;
    try {
      String runResult = executeCommand(commandBuffer.toString(), input);
      JsonObject jsonResult = JsonParser.parseString(runResult).getAsJsonObject();
      return Future.succeededFuture(jsonResult);
    } catch (CommandException failedCommand) {
      logger.error("Image run failed. Image {}. Input {}.", imageName, input, failedCommand);
      throw new IllegalStateException(failedCommand);
    }finally {
      inputManager.deleteHostInputFile(suffix);
    }
  }

  /**
   * Pulls the image with the given name from DockerHub
   * 
   * @param imageName
   */
  protected void pullFromDocker(String imageName) {
    try {
      executeCommand(ConstantsManager.dockerCommandPull, imageName);
    } catch (CommandException failedCommand) {
      logger.error("Image pull failed. Image {}", imageName, failedCommand);
      throw new IllegalStateException(failedCommand);
    }
  }

  /**
   * Checks whether the image with the given name exists.
   * 
   * @param imageName
   * @return
   */
  protected boolean checkWhetherImageExists(String imageName) {
    try {
      executeCommand(ConstantsManager.dockerCommandInspect, imageName);
      return true;
    } catch (CommandException failedCommand) {
      return false;
    }
  }

  /**
   * Executed the given command with the given input and returns the command
   * output as a string.
   * 
   * @param command the command to execute
   * @param input the command input
   * @return the output of the command execution
   * @throws IOException
   * @throws InterruptedException
   * @throws CommandException
   */
  protected String executeCommand(String command, String input) throws CommandException {
    try {
      Process pr = runtime.exec(command + input);
      // int exitCode = pr.waitFor();
      // if (exitCode != 0) {
      // throw new CommandException();
      // }
      BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      StringBuffer buffer = new StringBuffer();
      String line = buf.readLine();
      while (line != null) {
        buffer.append(line);
        line = buf.readLine();
      }
      return buffer.toString();
    } catch (IOException exc) {
      logger.error("Exception executing command " + command + " with input " + input, exc);
      throw new IllegalStateException(exc);
    }
  }
}
