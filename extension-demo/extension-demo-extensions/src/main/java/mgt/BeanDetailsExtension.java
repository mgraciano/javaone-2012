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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import mgt.details.BeanDetailsController;
import mgt.details.BeanDetailsInterceptorBinding;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

public class BeanDetailsExtension implements Extension {
    <T> void processAnnotatedType(@Observes final ProcessAnnotatedType<T> pat) {
        class BeanDetailsInterceptorBindingLiteral extends AnnotationLiteral<BeanDetailsInterceptorBinding>
                implements BeanDetailsInterceptorBinding {
        };

        final AnnotatedTypeBuilder builder = new AnnotatedTypeBuilder().
                readFromType(pat.getAnnotatedType(), true).
                addToClass(new BeanDetailsInterceptorBindingLiteral());
        pat.setAnnotatedType(builder.create());
    }

    void beforeShutdown(@Observes final BeforeShutdown bs, final MBeanServer mbs,
            final BeanDetailsController controller) throws Exception {
        for (ObjectName objectName : controller.getRegisteredObjectNames()) {
            mbs.unregisterMBean(objectName);
        }
    }
}
