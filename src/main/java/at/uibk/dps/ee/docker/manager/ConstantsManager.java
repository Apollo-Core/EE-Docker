package at.uibk.dps.ee.docker.manager;

/**
 * Class containing the constants used by container managers.
 * 
 * @author Fedor Smirnov
 */
public final class ConstantsManager {

  // Container constants
  public static final String containerSrcPath = "/usr/src/app";
  public static final String inputFileName = "jsonInput.json";
  
  // Docker commands
  public static final String dockerCommandInspect = "docker inspect ";
  public static final String dockerCommandPull = "docker pull ";
  public static final String dockerCommandRun = "docker run --rm -v ";
  
  /**
   * No constructor.
   */
  private ConstantsManager() {}
}
