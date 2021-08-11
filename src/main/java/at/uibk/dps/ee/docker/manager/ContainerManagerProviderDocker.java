package at.uibk.dps.ee.docker.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.core.ContainerManager;
import at.uibk.dps.ee.guice.container.ContainerManagerProvider;
import at.uibk.dps.ee.guice.starter.VertxProvider;

/**
 * Used to inject the manager capable of managing Docker containers and images.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class ContainerManagerProviderDocker implements ContainerManagerProvider {

  /**
   * Different types of docker container managers.
   * 
   * @author Fedor Smirnov
   */
  public enum DockerManager {
    Exec, DockerApi
  }

  protected final ContainerManager manager;

  /**
   * Injection constructor.
   * 
   * @param managerType the manager type to use
   * @param vProv the vertX provider
   */
  @Inject
  public ContainerManagerProviderDocker(final DockerManager managerType,
      final VertxProvider vProv) {
    switch (managerType) {
      case Exec:
        this.manager = new ContainerManagerExec();
        break;
      case DockerApi:
        this.manager = new ContainerManagerDockerAPI(vProv);
        break;
      default:
        throw new IllegalArgumentException("Unknown type of docker manager: " + managerType.name());
    }
  }

  @Override
  public ContainerManager getContainerManager() {
    return manager;
  }
}
