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
  public static final String routeRunFunction = "/run/";
  public static final String routeStartContainer = "/start/";
  public static final String routeRemoveContainer = "/remove/";

  // Json keys
  public static final String jsonKeyImageName = "image";
  public static final String jsonKeyContainer = "container_id";


  /**
   * No constructor.
   */
  private ConstantsServerContainer() {}

}
