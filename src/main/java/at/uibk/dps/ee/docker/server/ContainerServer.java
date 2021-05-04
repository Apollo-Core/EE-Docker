package at.uibk.dps.ee.docker.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.server.routes.ReqHandlerPull;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

/**
 * The {@link ContainerServer} is used as an access point to the manager of the
 * local Docker containers.
 * 
 * @author Fedor Smirnov
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
    final Route pullRoute = router.route(ConstantsServerContainer.routePullImage).method(HttpMethod.POST);
    ReqHandlerPull handlerPull = new ReqHandlerPull(manager);
    pullRoute.blockingHandler(handlerPull::handle);
  }

}
