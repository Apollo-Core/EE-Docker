package at.uibk.dps.ee.docker.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link ContainerInputManager} performs the file operations necessary to
 * mount data as an input to containers.
 * 
 * @author Fedor Smirnov
 */
public class ContainerInputManager {



  /**
   * Creates a temporal input file with the contents of the provided json object.
   * 
   * @param jsonObject the provided json object
   */
  public void createHostInputFile(JsonObject jsonObject) {
    String path = "./" + ConstantsManager.inputFileName;
    try {
      Files.writeString(Path.of(path), jsonObject.toString());
    } catch (IOException e) {
      throw new IllegalStateException("Could not write container input file");
    }
  }

  /**
   * Deletes the temporal file on the host
   */
  public void deleteHostInputFile() {
    File file = new File("./" + ConstantsManager.inputFileName);
    file.delete();
  }



  public static void main(String[] args) throws IOException {

    JsonArray array = new JsonArray();
    int num = 10000000;
    
    for (int i = 0; i < num; i++) {
      array.add("string" + i);
    }

    JsonObject input = new JsonObject();
    input.add("input", array);

    ContainerManagerExec containerExec = new ContainerManagerExec();
    
    JsonObject result = containerExec.runImage("fedor89/iterateinput", input);
    System.out.println(result.toString());
   
  }

}
