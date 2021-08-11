package at.uibk.dps.ee.docker.server.routes;

import at.uibk.dps.ee.core.ContainerManager;
import at.uibk.dps.ee.docker.server.ConstantsServerContainer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

/**
 * The handler for the request to remove a Container.
 *
 * @author Lukas Dötlinger
 */
public class ReqHandlerRemove implements Handler<RoutingContext> {

  private final ContainerManager manager;

  /**
   * Default constructor.
   *
   * @param manager the manager executing the container operations
   */
  public ReqHandlerRemove(ContainerManager manager) {
    this.manager = manager;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject json = ctx.getBodyAsJson();
    String containerId = json.getString(ConstantsServerContainer.jsonKeyContainer);
    manager.closeImage(containerId);
    response.setStatusCode(200);
  }
}
