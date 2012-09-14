package mgt;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class DetailExtension implements Extension {
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat,
            final BeanManager bm) throws Exception {
    }
}
