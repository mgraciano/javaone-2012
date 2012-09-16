/*
 * Copyright (c) 2012, Michael Nascimento Santos & Michel Graciano.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the project's authors nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND/OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
