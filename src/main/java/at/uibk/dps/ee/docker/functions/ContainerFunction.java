package at.uibk.dps.ee.docker.functions;

import com.google.gson.JsonObject;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.enactables.FactoryInputUser;
import at.uibk.dps.ee.enactables.FunctionAbstract;
import at.uibk.dps.ee.model.properties.PropertyServiceMappingLocal;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * The {@link ContainerFunction} is used to execute function within local Docker
 * containers.
 * 
 * @author Fedor Smirnov
 */
public class ContainerFunction extends FunctionAbstract {

  protected final ContainerManager containerManager;
  protected final String imageName;

  protected final Vertx vertx;

  /**
   * Injection constructor.
   * 
   */
  public ContainerFunction(final FactoryInputUser input, final ContainerManager containerManager,
      final Vertx vertx) {
    super(input.getTask(), input.getMapping());
    this.containerManager = containerManager;
    this.vertx = vertx;
    this.imageName = PropertyServiceMappingLocal.getImageName(input.getMapping());
  }

  @Override
  public Future<JsonObject> processVerifiedInput(final JsonObject input) {
    return containerManager.runImage(imageName, input);
  }
}
