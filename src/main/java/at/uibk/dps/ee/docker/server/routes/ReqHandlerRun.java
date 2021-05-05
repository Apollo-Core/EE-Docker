package at.uibk.dps.ee.docker.server.routes;

import com.google.gson.JsonParser;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.server.ConstantsServerContainer;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * The handler for the request to run a Docker image.
 * 
 * @author Fedor Smirnov
 */
public class ReqHandlerRun implements Handler<RoutingContext> {

  protected final ContainerManager manager;

  /**
   * Default constructor.
   * 
   * @param manager the manager executing the container operations.
   */
  public ReqHandlerRun(ContainerManager manager) {
    this.manager = manager;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject vertJson = ctx.getBodyAsJson();
    String imageToRun = vertJson.getString(ConstantsServerContainer.jsonKeyImageName);
    String input = vertJson.getString(ConstantsServerContainer.jsonKeyRunInput);
    com.google.gson.JsonObject gsonJsonResult =
        manager.runImage(imageToRun, (com.google.gson.JsonObject) JsonParser.parseString(input));
    JsonObject responseBody = new JsonObject();
    responseBody.put(ConstantsServerContainer.jsonKeyRunResult, gsonJsonResult.toString());
    response.setStatusCode(200).end(responseBody.toString());
  }



}