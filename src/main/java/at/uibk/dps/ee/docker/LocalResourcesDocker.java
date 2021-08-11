package at.uibk.dps.ee.docker;

import java.util.HashSet;
import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.core.ContainerManager;
import at.uibk.dps.ee.core.LocalResources;
import at.uibk.dps.ee.guice.container.ContainerManagerProvider;
import at.uibk.dps.ee.model.graph.SpecificationProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping;
import at.uibk.dps.ee.model.properties.PropertyServiceMapping.EnactmentMode;
import at.uibk.dps.ee.model.properties.PropertyServiceMappingLocal;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
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
public class LocalResourcesDocker implements LocalResources {

  protected final Mappings<Task, Resource> mappings;
  protected final ContainerManager containerManager;
  protected final Set<String> images = new HashSet<>();

  /**
   * Injection constructor.
   * 
   * @param specProvider the spec provider.
   */
  @Inject
  public LocalResourcesDocker(final SpecificationProvider specProvider,
      final ContainerManagerProvider containerManagerProv) {
    this.mappings = specProvider.getMappings();
    this.containerManager = containerManagerProv.getContainerManager();
  }

  @Override
  public void init() {
    for (Mapping<Task, Resource> mapping : mappings) {
      if (PropertyServiceMapping.getEnactmentMode(mapping).equals(EnactmentMode.Local)) {
        String image = PropertyServiceMappingLocal.getImageName(mapping);
        containerManager.initImage(image);
        images.add(image);
      }
    }
  }

  @Override
  public void close() {
    images.forEach(image -> containerManager.closeImage(image));
  }
}
