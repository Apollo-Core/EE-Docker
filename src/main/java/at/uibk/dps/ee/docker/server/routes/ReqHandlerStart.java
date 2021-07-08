package at.uibk.dps.ee.docker.server.routes;

import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.server.ConstantsServerContainer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

/**
 * The handler for the request to start a Container with a given image.
 *
 * @author Lukas Dötlinger
 */
public class ReqHandlerStart implements Handler<RoutingContext> {

  private final ContainerManager manager;

  /**
   * Default constructor.
   *
   * @param manager the manager executing the container operations
   */
  public ReqHandlerStart(ContainerManager manager) {
    this.manager = manager;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject json = ctx.getBodyAsJson();
    String imageName = json.getString(ConstantsServerContainer.jsonKeyImageName);
    String containerId = manager.startContainer(imageName);
    response.setStatusCode(200).end(containerId);
  }
}
