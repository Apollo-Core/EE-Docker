package at.uibk.dps.ee.docker.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.docker.manager.ContainerManagerDockerAPI.UsedOperatingSystem;
import at.uibk.dps.ee.guice.starter.VertxProvider;

/**
 * Used to inject the manager capable of managing Docker containers and images.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class ContainerManagerProvider {

  protected final ContainerManager manager;

  /**
   * Injection constructor.
   * 
   * @param managerType the manager type to use
   * @param vProv the vertX provider
   */
  @Inject
  public ContainerManagerProvider(final UsedOperatingSystem usedOs,
      final VertxProvider vProv) {
    this.manager = new ContainerManagerDockerAPI(vProv, usedOs);
  }

  public ContainerManager getContainerManager() {
    return manager;
  }
}
