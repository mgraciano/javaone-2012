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
package mgt.details;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

@ApplicationScoped
public class BeanDetailsController {
    private static final String BEAN_METHOD_DETAILS_OBJECT_NAME =
            "cdi-demo:type=monitoring,name=beans";
    private final Set<ObjectName> registeredObjectNames = new HashSet<>();
    private final Map<Method, BeanMethodDetailsMBean> details = new HashMap<>();
    @Inject
    MBeanServer mbs;

    private ObjectName createObjectName(final Method method) throws
            MalformedObjectNameException {
        return new ObjectName(BEAN_METHOD_DETAILS_OBJECT_NAME + ",class=" +
                method.getDeclaringClass().getSimpleName() + ",method=" +
                method.getName());
    }

    private BeanMethodDetailsMBean lookupMBean(final Method method) throws
            MalformedObjectNameException, InstanceAlreadyExistsException,
            MBeanRegistrationException, NotCompliantMBeanException {
        BeanMethodDetailsMBean mBean = details.get(method);
        if (mBean == null) {
            mBean = new BeanMethodDetailsMBean();
            final ObjectName bmbName = createObjectName(method);
            mbs.registerMBean(mBean, bmbName);

            details.put(method, mBean);
            registeredObjectNames.add(bmbName);
        }

        return mBean;
    }

    public Set<ObjectName> getRegisteredObjectNames() {
        return registeredObjectNames;
    }

    Object proceedAndRegister(final InvocationContext ctx) throws Exception {
        final long startTime = System.nanoTime();
        final Object obj = ctx.proceed();
        final long delta = System.nanoTime() - startTime;

        final Method method = ctx.getMethod();
        final BeanMethodDetailsMBean mBean = lookupMBean(method);
        mBean.registerCall(delta);

        return obj;
    }
}
