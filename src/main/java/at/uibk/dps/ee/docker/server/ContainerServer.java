package at.uibk.dps.ee.docker.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.server.routes.ReqHandlerRemove;
import at.uibk.dps.ee.docker.server.routes.ReqHandlerRun;
import at.uibk.dps.ee.docker.server.routes.ReqHandlerStart;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * The {@link ContainerServer} is used as an access point to the manager of the
 * local Docker containers.
 *
 * @author Fedor Smirnov, Lukas Dötlinger
 */
public class ContainerServer {

  protected final Logger logger = LoggerFactory.getLogger(ContainerServer.class);

  protected final Router router;
  protected final HttpServer server;
  protected final ContainerManager manager;

  /**
   * Default constructor.
   *
   * @param vertx the vertx instance
   */
  public ContainerServer(Vertx vertx, ContainerManager manager) {
    this.router = Router.router(vertx);
    this.server = vertx.createHttpServer();
    this.manager = manager;
    configureRoutes();
  }

  /**
   * Configures the handlers for the routes served by the server.
   */
  protected final void configureRoutes() {
    final Route runRoute =
        router.route(ConstantsServerContainer.routeRunFunction).method(HttpMethod.POST).handler(BodyHandler.create());
    ReqHandlerRun handlerRun = new ReqHandlerRun(manager);
    runRoute.blockingHandler(handlerRun::handle);

    final Route startRoute =
        router.route(ConstantsServerContainer.routeStartContainer).method(HttpMethod.POST).handler(BodyHandler.create());
    ReqHandlerStart handlerStart = new ReqHandlerStart(manager);
    startRoute.blockingHandler(handlerStart::handle);

    final Route removeRoute =
        router.route(ConstantsServerContainer.routeRemoveContainer).method(HttpMethod.POST).handler(BodyHandler.create());
    ReqHandlerRemove handlerRemove = new ReqHandlerRemove(manager);
    removeRoute.blockingHandler(handlerRemove::handle);
  }

  public void start() {
    logger.info("Container manager listening to port " + ConstantsServerContainer.port);
    server.requestHandler(router).listen(ConstantsServerContainer.port);
  }
}
