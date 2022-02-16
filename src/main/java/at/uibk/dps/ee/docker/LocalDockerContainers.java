package at.uibk.dps.ee.docker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.docker.manager.ContainerManager;
import at.uibk.dps.ee.docker.manager.ContainerManagerProvider;
import at.uibk.dps.ee.guice.init_term.ManagedComponent;
import at.uibk.dps.ee.model.graph.MappingsConcurrent;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
import at.uibk.dps.ee.model.properties.PropertyServiceMappingLocal;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * Class used to initialize and remove Docker containers on the resource used by
 * this Apollo instance.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class LocalDockerContainers implements ManagedComponent {

  protected final MappingsConcurrent mappings;
  protected final ContainerManager containerManager;
  protected final Set<String> images = new HashSet<>();

  /**
   * Injection constructor.
   * 
   * @param specProvider the spec provider.
   */
  @Inject
  public LocalDockerContainers(final SpecificationProvider specProvider,
      final ContainerManagerProvider containerManagerProv) {
    this.mappings = specProvider.getMappings();
    this.containerManager = containerManagerProv.getContainerManager();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Future<String> initialize() {
    Promise<String> resultPromise = Promise.promise();
    List<Future> imageInitFutures = new ArrayList<>();
    for (Mapping<Task, Resource> mapping : mappings) {
      if (PropertyServiceMapping.getEnactmentMode(mapping).equals(EnactmentMode.Local)) {
        String image = PropertyServiceMappingLocal.getImageName(mapping);
        imageInitFutures.add(containerManager.initImage(image));
      }
    }
    CompositeFuture.join(imageInitFutures).onComplete(asyncRes -> {
      imageInitFutures.forEach(imageFuture -> images.add((String) imageFuture.result()));
      resultPromise.complete("Images initialized");
    });
    return resultPromise.future();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Future<String> terminate() {
    Promise<String> resultPromise = Promise.promise();
    List<Future> closingFutures = images.stream().map(imageStr -> containerManager.closeImage(imageStr)).collect(Collectors.toList());
    CompositeFuture.join(closingFutures).onComplete(asyncRes ->{
      resultPromise.complete("Images closed");
    });
    return resultPromise.future();
  }
}
