package mgt.details;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

public class BeanDetailsMBean implements DynamicMBean {
    @Override
    public Object getAttribute(String attribute) throws
            AttributeNotFoundException,
            MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttribute(Attribute attribute) throws
            AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
