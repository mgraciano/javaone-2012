package mgt;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import mgt.details.BeanDetailsInterceptorBinding;
import org.jboss.solder.reflection.annotated.AnnotatedTypeBuilder;

public class DetailExtension implements Extension {
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        class BeanDetailsInterceptorBindingLiteral extends AnnotationLiteral<BeanDetailsInterceptorBinding>
                implements BeanDetailsInterceptorBinding {
        };

        final AnnotatedTypeBuilder builder = new AnnotatedTypeBuilder().
                readFromType(pat.getAnnotatedType(), true).
                addToClass(new BeanDetailsInterceptorBindingLiteral());
        pat.setAnnotatedType(builder.create());
    }
}
