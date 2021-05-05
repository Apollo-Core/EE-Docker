package at.uibk.dps.ee.docker.server;

/**
 * Class containing the constants relevant for the Apollo server.
 * 
 * @author Fedor Smirnov
 */
public final class ConstantsServerContainer {

  // Server config
  public static final int port = 5055;

  // Route names
  public static final String routePullImage = "/pull/";
  public static final String routeRunImage = "/run/";

  // Json keys
  public static final String jsonKeyImageName = "image";
  public static final String jsonKeyRunInput = "input";
  public static final String jsonKeyRunResult = "result";


  /**
   * No constructor.
   */
  private ConstantsServerContainer() {}

}
