package mgt;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import mgt.overview.CDIDeploymentOverview;
import mgt.qualifiers.QualifierMBean;

public class OverviewExtension implements Extension {
    private static final String QUALIFIERS_OBJECT_NAME =
            "cdi-demo:type=monitoring,name=qualifiers";
    private final Set<ObjectName> registeredObjectNames = new HashSet<>();
    private final Map<Class<? extends Annotation>, QualifierMBean> qualifiers =
            new HashMap<>();
    private final Set<Class<? extends Annotation>> interceptorsBinding =
            new HashSet<>();

    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat,
            final BeanManager bm) throws Exception {
        for (Annotation ann : pat.getAnnotatedType().getAnnotations()) {
            final Class<? extends Annotation> annotationType = ann.
                    annotationType();
            if (bm.isQualifier(annotationType)) {
                QualifierMBean qmb = qualifiers.get(annotationType);
                if (qmb == null) {
                    qualifiers.put(annotationType, qmb = new QualifierMBean(
                            annotationType));
                }

                qmb.incrementBeansCount();
            } else if (bm.isInterceptorBinding(annotationType)) {
                interceptorsBinding.add(annotationType);
            }
        }
    }

    <X> void processInjectionTarget(
            @Observes final ProcessInjectionTarget<X> pit) {
        for (InjectionPoint ip : pit.getInjectionTarget().getInjectionPoints()) {
            for (Annotation ann : ip.getQualifiers()) {
                final Class<? extends Annotation> annotationType = ann.
                        annotationType();
                QualifierMBean qmb = qualifiers.get(annotationType);
                if (qmb == null) {
                    qualifiers.put(annotationType, qmb = new QualifierMBean(
                            annotationType));
                }

                qmb.incrementInjectionPointsCount();
            }
        }
    }

    void afterDeploymentValidation(@Observes final AfterDeploymentValidation adv)
            throws Exception {
        final CDIDeploymentOverview overviewBean = new CDIDeploymentOverview();
        final ObjectName overviewBeanName = new ObjectName(
                "cdi-demo:type=monitoring,name=overview");

        overviewBean.setQualifiersCount(qualifiers.size());
        overviewBean.setInterceptorsBindingCount(interceptorsBinding.size());

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(overviewBean, overviewBeanName);
        registeredObjectNames.add(overviewBeanName);

        for (Map.Entry<Class<? extends Annotation>, QualifierMBean> entry :
                qualifiers.entrySet()) {
            final Class<? extends Annotation> ann = entry.getKey();
            final QualifierMBean qualifierMBean = entry.getValue();
            final ObjectName qmbName = new ObjectName(QUALIFIERS_OBJECT_NAME +
                    ",qualifier=" + ann.getSimpleName());

            mbs.registerMBean(qualifierMBean, qmbName);
            registeredObjectNames.add(qmbName);
        }
    }

    void beforeShutdown(@Observes BeforeShutdown bs) throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (ObjectName objectName : registeredObjectNames) {
            mbs.unregisterMBean(objectName);
        }
    }
}
