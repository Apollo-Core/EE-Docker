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

  // Docker Engine Connections
  public static final String localhost = "localhost";
  public static final String defaultDockerInternalUri = "host.docker.internal";
  public static final String defaultDockerUnixSocketLocation = "/var/run/docker.sock";
  public static final int defaultDockerHTTPPort = 2375;

  public static final String dockerNetwork = "ee-docker-network";

  /**
   * No constructor.
   */
  private ConstantsManager() {}
}
