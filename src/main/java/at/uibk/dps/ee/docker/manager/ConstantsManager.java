package at.uibk.dps.ee.docker.manager;

/**
 * Class containing the constants used by container managers.
 * 
 * @author Fedor Smirnov
 */
public final class ConstantsManager {

  // Docker commands
  public static final String dockerCommandInspect = "docker inspect ";
  public static final String dockerCommandPull = "docker pull ";
  public static final String dockerCommandRun = "docker run ";
  
  /**
   * No constructor.
   */
  private ConstantsManager() {}
}
