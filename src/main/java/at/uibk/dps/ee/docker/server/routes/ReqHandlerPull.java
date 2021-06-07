package at.uibk.dps.ee.docker.server.routes;

import at.uibk.dps.ee.core.ContainerManager;
import at.uibk.dps.ee.docker.server.ConstantsServerContainer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * The handler for the request to pull a Docker image.
 * 
 * @author Fedor Smirnov
 */
public class ReqHandlerPull implements Handler<RoutingContext> {

  protected final ContainerManager manager;

  /**
   * Default constructor.
   * 
   * @param manager the manager executing the container operations.
   */
  public ReqHandlerPull(ContainerManager manager) {
    this.manager = manager;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject json = ctx.getBodyAsJson();
    String imageName = json.getString(ConstantsServerContainer.jsonKeyImageName);
    manager.pullImage(imageName);
    response.setStatusCode(201).end();
  }
}
