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
package mgt.qualifiers;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

public class QualifierMBean implements DynamicMBean {
    public static final String BEANS_COUNT = "Number of annotated beans";
    public static final String INJECTION_POINTS_COUNT =
            "Number of injection points";
    private String className = this.getClass().getName();
    private MBeanInfo mbeanInfo = null;
    private int beansCount = 0;
    private int injectionPointsCount = 0;

    public QualifierMBean(final Class<? extends Annotation> annType) {
        buildDynamicMBeanInfo(annType);
    }

    private void buildDynamicMBeanInfo(final Class<? extends Annotation> annType) {
        final String description = "Detailed overview for " + annType.getName();

        final MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[2];
        final MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[1];
        final MBeanNotificationInfo[] notifications =
                new MBeanNotificationInfo[0];
        final MBeanOperationInfo[] operations = new MBeanOperationInfo[0];

        attributes[0] = new MBeanAttributeInfo(BEANS_COUNT, Integer.class.
                getName(), "How many beans are annotated with this qualifier.",
                true, false, false);
        attributes[1] =
                new MBeanAttributeInfo(INJECTION_POINTS_COUNT,
                Integer.class.getName(),
                "How many injection point do we have with this qualifier.", true,
                false, false);

        constructors[0] = new MBeanConstructorInfo("Default constructor.",
                this.getClass().getConstructors()[0]);

        mbeanInfo = new MBeanInfo(className, description, attributes,
                constructors, operations, notifications);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public AttributeList getAttributes(final String[] attributeNames) {
        if (attributeNames == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "attributeNames[] cannot be null"),
                    "Cannot invoke a getter of " + className);
        }

        final AttributeList resultList = new AttributeList();
        if (attributeNames.length == 0) {
            return resultList;
        }

        for (int i = 0; i < attributeNames.length; i++) {
            try {
                final Object value = getAttribute(attributeNames[i]);
                resultList.add(new Attribute(attributeNames[i], value));
            } catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.
                        getMessage(), e);
            }
        }
        return resultList;
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        if (attributes == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "AttributeList attributes cannot be null"),
                    "Cannot invoke a setter of " + className);
        }
        final AttributeList resultList = new AttributeList();

        if (attributes.isEmpty()) {
            return resultList;
        }

        for (Iterator i = attributes.iterator(); i.hasNext();) {
            final Attribute attr = (Attribute)i.next();
            try {
                setAttribute(attr);
                final String name = attr.getName();
                final Object value = getAttribute(name);
                resultList.add(new Attribute(name, value));
            } catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException e) {
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, e.
                        getMessage(), e);

            }
        }
        return resultList;
    }

    @Override
    public Object getAttribute(final String name) throws
            AttributeNotFoundException, MBeanException, ReflectionException {
        if (name == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException(
                    "Attribute name cannot be null"),
                    "Cannot invoke a getter of " + className +
                    " with null attribute name");
        }

        switch (name) {
            case BEANS_COUNT:
                return beansCount;
            case INJECTION_POINTS_COUNT:
                return injectionPointsCount;
            default:
                throw new AttributeNotFoundException("Cannot find " + name +
                        " attribute in " + className);
        }
    }

    @Override
    public void setAttribute(final Attribute attribute) throws
            AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        throw new AttributeNotFoundException("Read-only attribute.");
    }

    @Override
    public Object invoke(final String operationName, final Object params[],
            final String signature[]) throws MBeanException, ReflectionException {
        return null;
    }

    public void incrementBeansCount() {
        beansCount++;
    }

    public void incrementInjectionPointsCount() {
        injectionPointsCount++;
    }
}
