package at.uibk.dps.ee.docker.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.JsonObject;

/**
 * The {@link ContainerInputManager} performs the file operations necessary to
 * mount data as an input to containers.
 * 
 * @author Fedor Smirnov
 */
public class ContainerInputManager {

  protected final AtomicInteger suffix = new AtomicInteger(0);

  /**
   * Creates a temporal input file with the contents of the provided json object.
   * 
   * @param jsonObject the provided json object
   * @return the suffix for the created file
   */
  public int createHostInputFile(JsonObject jsonObject) {
    int cur_suffix = suffix.getAndAdd(1);
    String path = "./" + ConstantsManager.inputFileName + cur_suffix + ".json";
    try {
      Files.writeString(Path.of(path), jsonObject.toString());
      return cur_suffix;
    } catch (IOException e) {
      throw new IllegalStateException("Could not write container input file");
    }
  }

  /**
   * Deletes the temporal file on the host
   * 
   * @param suffix the suffix of the file to delete
   */
  public void deleteHostInputFile(int suffix) {
    File file = new File("./" + ConstantsManager.inputFileName + suffix + ".json");
    file.delete();
  }



  public static void main(String[] args) throws IOException {

//    JsonArray array = new JsonArray();
//    int num = 10000000;
//
//    for (int i = 0; i < num; i++) {
//      array.add("string" + i);
//    }
//
//    JsonObject input = new JsonObject();
//    input.add("input", array);
//
//    ContainerManagerExec containerExec = new ContainerManagerExec();
//
//    JsonObject result = containerExec.runImage("fedor89/iterateinput", input);
//    System.out.println(result.toString());

  }

}
