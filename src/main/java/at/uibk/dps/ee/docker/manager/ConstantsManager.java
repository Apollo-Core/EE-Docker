package at.uibk.dps.ee.docker.manager;

/**
 * Class containing the constants used by container managers.
 *
 * @author Fedor Smirnov
 */
public final class ConstantsManager {

  // Container constants
  public static final String containerSrcPath = "/var/www/java";
  public static final String inputFileName = "jsonInput";

  // Docker commands
  public static final String dockerCommandInspect = "docker inspect ";
  public static final String dockerCommandPull = "docker pull ";
  public static final String dockerCommandRun = "docker run --rm -v ";

  // Docker Engine Connections
  public static final String localhost = "localhost";
  public static final String defaultDockerInternalUri = "host.docker.internal";
  public static final String defaultDockerUnixSocketLocation = "/var/run/docker.sock";
  public static final int defaultDockerHTTPPort = 2375;

  public static final String dockerNetwork = "ee-docker-network";
  public static final int defaultFunctionPort = 8080;

  /**
   * No constructor.
   */
  private ConstantsManager() {}

  public static String getDockerUri() {
    if (System.getenv().containsKey("DOCKERIZED")) {
      return defaultDockerInternalUri;
    } else {
      return localhost;
    }
  }
}
