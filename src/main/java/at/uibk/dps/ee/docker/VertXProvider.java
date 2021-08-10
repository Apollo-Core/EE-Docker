package at.uibk.dps.ee.docker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;

/**
 * Class to replace the vertx provider from the core project (since not yet present in the preVertX state)
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class VertXProvider {

  protected final Vertx vertx;
  
  @Inject
  public VertXProvider() {
    this.vertx = Vertx.vertx();
  }

  public Vertx getVertX() {
    return this.vertx;
  }
  
}
