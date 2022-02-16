package at.uibk.dps.ee.docker.functions;

import java.util.Set;
import com.google.inject.Inject;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerProvider;
import at.uibk.dps.ee.enactables.FactoryInputUser;
import at.uibk.dps.ee.enactables.FunctionFactoryUser;
import at.uibk.dps.ee.enactables.decorators.FunctionDecoratorFactory;
import at.uibk.dps.ee.guice.starter.VertxProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
import io.vertx.core.Vertx;

/**
 * The {@link FunctionFactoryLocalDocker} provides the functions modeling function
 * execution within local containers.
 * 
 * @author Fedor Smirnov
 */
public class FunctionFactoryLocalDocker extends FunctionFactoryUser {

  protected final ContainerManager containerManager;
  protected final Vertx vertx;

  /**
   * Injection constructor.
   * 
   * @param decoratorFactories the factories for the decorators which are used to
   *        wrap the created functions
   */
  @Inject
  public FunctionFactoryLocalDocker(final Set<FunctionDecoratorFactory> decoratorFactories,
      final ContainerManagerProvider containerManagerProvider, final VertxProvider vProv) {
    super(decoratorFactories);
    this.containerManager = containerManagerProvider.getContainerManager();
    this.vertx = vProv.getVertx();
  }

  @Override
  protected ContainerFunction makeActualFunction(final FactoryInputUser input) {
    return new ContainerFunction(input, containerManager, vertx);
  }

  @Override
  public boolean isApplicable(FactoryInputUser factoryInput) {
    return PropertyServiceMapping.getEnactmentMode(factoryInput.getMapping())
        .equals(EnactmentMode.Local);
  }
}
