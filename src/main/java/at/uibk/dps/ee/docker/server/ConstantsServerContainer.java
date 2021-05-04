package at.uibk.dps.ee.docker.server;

/**
 * Class containing the constants relevant for the Apollo server.
 * 
 * @author Fedor Smirnov
 */
public final class ConstantsServerContainer {


  // Route names
  public static final String routePullImage = "/pull/";
  public static final String routeRunImage = "/run/";
  
  // Json keys
  public static final String jsonKeyImageName = "image";
  
  
  /**
   * No constructor.
   */
  private ConstantsServerContainer() {
  }
  
}
