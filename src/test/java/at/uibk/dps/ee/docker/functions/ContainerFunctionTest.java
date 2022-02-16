package at.uibk.dps.ee.docker.functions;

import com.google.gson.JsonObject;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.enactables.FactoryInputUser;
import at.uibk.dps.ee.model.properties.PropertyServiceMappingLocal;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerFunctionTest {

  @Test
  public void test() {

    Task task = new Task("task");
    Resource res = new Resource("res");
    Mapping<Task, Resource> map =
        PropertyServiceMappingLocal.createMappingLocal(task, res, "image");

    ContainerManager mockManager = mock(ContainerManager.class);
    Vertx mockVertx = mock(Vertx.class);
    String imageName = "image";
    ContainerFunction tested =
        new ContainerFunction(new FactoryInputUser(task, map), mockManager, mockVertx);

    // running the function
    JsonObject expectedResult = new JsonObject();
    Future<JsonObject> expected = Future.succeededFuture(expectedResult);
    JsonObject input = new JsonObject();
    when(mockManager.runImage(imageName, input)).thenReturn(expected);

    tested.processInput(input);

    verify(mockManager).runImage(imageName, input);
  }
}
