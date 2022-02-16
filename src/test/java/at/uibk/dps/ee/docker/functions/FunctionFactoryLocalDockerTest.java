package at.uibk.dps.ee.docker.functions;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import at.uibk.dps.ee.core.function.EnactmentFunction;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerProvider;
import at.uibk.dps.ee.enactables.FactoryInputUser;
import at.uibk.dps.ee.enactables.decorators.FunctionDecoratorFactory;
import at.uibk.dps.ee.guice.starter.VertxProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ee.model.properties.PropertyServiceMappingLocal;
import io.vertx.core.Vertx;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FunctionFactoryLocalDockerTest {

  @Test
  public void test() {
    Set<FunctionDecoratorFactory> decorators = new HashSet<>();
    ContainerManager mockManager = mock(ContainerManager.class);
    ContainerManagerProvider manProv = mock(ContainerManagerProvider.class);
    when(manProv.getContainerManager()).thenReturn(mockManager);
    Vertx mockVertx = mock(Vertx.class);
    VertxProvider vProv = mock(VertxProvider.class);
    when(vProv.getVertx()).thenReturn(mockVertx);
    FunctionFactoryLocalDocker tested = new FunctionFactoryLocalDocker(decorators, manProv, vProv);

    Task task = PropertyServiceFunctionUser.createUserTask("task", "addition");
    Resource res = new Resource("r");
    Mapping<Task, Resource> map =
        PropertyServiceMappingLocal.createMappingLocal(task, res, "image");

    EnactmentFunction result = tested.makeFunction(new FactoryInputUser(task, map));
    assertTrue(result instanceof ContainerFunction);
  }
}
