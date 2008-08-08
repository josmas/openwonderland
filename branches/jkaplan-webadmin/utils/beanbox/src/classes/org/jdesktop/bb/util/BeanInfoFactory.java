/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.jdesktop.bb.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;

import java.beans.*;

import java.lang.reflect.Method;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A wrapper for the Introspector to return instances
 * of BeanInfos.
 *
 * This class also contains some convenience methods for getting access to 
 * BeanInfo specfic values for keys like "isContainer",
 * "containerDelegate" and "proxyComponent"
 *
 * Also contains some utility methods for using reflection to invoke methods.
 *
 * @version 1.11 02/27/02
 * @author  Mark Davidson
 */
public class BeanInfoFactory  {

    private static HashMap methodCache = new HashMap();

    /**
     * Helper method which uses reflection to search for the methodName
     * on the target with no arguments.
     */
    public static Object executeMethod(String methodName, Object source) {
	Object result = null;
	String key = makeKey(source.getClass(), methodName, null);
	Method method = (Method)methodCache.get(key);
	try {
	    if (method == null) {
		method = source.getClass().getMethod(methodName, null);
		methodCache.put(key, method);
	    }
	    result = method.invoke(source, null);
	} catch (Exception ex) {
	    // Fall through
	}
	return result;
    }

    /**
     * Helper method which uses reflection to search for the methodName
     * on the target with a single arg type as a parameter. 
     * The Class hierarchy of the arg is traversed untill the method
     * is found.
     * <p>
     * For example, we wish to exectute Container.add(Component).
     * The source object would be a Container instance, the argument
     * would be an instance of a Component and the method name would
     * be "add". If a JButton is used the component, the hierarchy will be
     * walked:
     * JButton -> JComponent -> Container -> Component until the add method
     * on Container is found.
     *
     * @param methodName name of the method to execute
     * @param source object in which to invoke the method
     * @param arg object which is the argument
     * @return the result of invocation or null if it hasn't been invoked
     *         or if the invocation produces a null result
     */
    public static Object executeMethod(String methodName, Object source, 
				       Object arg) {
	Object result = null;
	for (Class argType = arg.getClass(); argType != null; 
	     argType = argType.getSuperclass()) {
	    try {
		String key = makeKey(source.getClass(), methodName, argType);
		Method method = (Method)methodCache.get(key);
		if (method == null) {
		    method = source.getClass().getMethod(methodName,
						new Class[] { argType });
		    methodCache.put(key, method);
		}
		result = method.invoke(source, new Object[] { arg });
		break;
	    } catch (Exception ex) {
		// Fall through
	    }
	}
	return result;
    }

    /**
     * Helper method which uses reflection to execute a method with a 
     * single argument.
     * Once the method is found which satifies the parameterr, that method
     * is stored in a cache.
     *
     * @param methodName name of the method to execute
     * @param source object in which to invoke the method
     * @param arg object which is the argument
     * @param argType type of the argument object.
     * @return the result of invocation or null if it hasn't been invoked
     *         or if the invocation produces a null result
     */
    public static Object executeMethod(String methodName, Object source, 
				       Object arg, Class argType) {
	Object result = null;
	try {
	    String key = makeKey(source.getClass(), methodName, argType);
	    Method method = (Method)methodCache.get(key);
		if (method == null) {
		    method = source.getClass().getMethod(methodName,
							 new Class[] { argType });
		    methodCache.put(key, method);
		}
		result = method.invoke(source, new Object[] { arg });
	} catch (Exception ex) {
		// Fall through
	}
	return result;
    }


    /**
     * Creates a key for the method cache
     */
    private static String makeKey(Class src, String methodName, Class arg) {
	StringBuffer key = new StringBuffer(src.getName()).append('+');
	key.append(methodName).append('+');
	if (arg != null) {
	    key.append(arg.getName());
	}
	return key.toString();
    }

    /** 
     * Retrieves the BeanInfo for a Class
     */
    public static BeanInfo getBeanInfo(Class cls)  {
        BeanInfo beanInfo = null;
        
	try {
	    beanInfo = Introspector.getBeanInfo(cls);
	} catch (IntrospectionException ex) {
	    // XXX - should handle this better.
	    ex.printStackTrace();
	}
	return beanInfo;
    }

    // These helper methods are also in java.beans.MetaData.

    public static void setBeanAttribute(Class type, String attribute, Object value) {
        getBeanInfo(type).getBeanDescriptor().setValue(attribute, value);
    }

    public static Object getBeanAttribute(Class type, String attribute) {
	return getBeanInfo(type).getBeanDescriptor().getValue(attribute);
    }

    public static PropertyDescriptor getPropertyDescriptor(Class type, String propertyName) {
        BeanInfo info = getBeanInfo(type);
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        // System.out.println("Searching for: " + propertyName + " in " + type);
        for(int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor pd  = propertyDescriptors[i];
            if (propertyName.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }

    public static void setPropertyAttribute(Class type, String property, String attribute, Object value) {
        PropertyDescriptor pd = getPropertyDescriptor(type, property);
        if (pd == null) {
            System.err.println("Warning: property " + property + " is not defined on " + type);
            return;
        }
        pd.setValue(attribute, value);
    }


    /** 
     * Retrieves the BeanInfo icon for the class.
     */
    public static Icon getIcon(Class cls)  {
	// Note: This should be a static method except that
	// the inner classes can't be static.
	Icon icon = null;
            
	if (cls == null)
	    return null;
               
	BeanInfo info = BeanInfoFactory.getBeanInfo(cls);
            
	if (info != null)  {
	    Image image = info.getIcon(BeanInfo.ICON_COLOR_16x16);
	    if (image != null)  {
		icon = new ImageIcon(image);
	    } else {
		return getIcon(cls.getSuperclass());
	    }
	}
	return icon;
    }

    /**
     * Determines if the object is a Swing container like JScrollPane
     * @param obj Object to test.
     * @return true if the object is a Swing container.
     */
    public static boolean isContainer(Object obj) {
	// Determine if this target is a swing container.
	BeanInfo info = BeanInfoFactory.getBeanInfo(obj.getClass());
	Boolean flag = Boolean.FALSE;
	if (info != null)  {
	    BeanDescriptor desc = info.getBeanDescriptor();

	    flag = (Boolean)desc.getValue("isContainer");
	    if (flag == null) {
		flag = Boolean.FALSE;
	    }
	}
	return flag.booleanValue();
    }

    /**
     * Returns the container delegate for the object. The container delegate
     * is the actual container for which objects are added to for Swing
     * containers. For example, The JViewport is the container delegate
     * for the JScrollPane.
     *
     * @return container delegate or null if there is no container delegate.
     */
    public static Container getContainerDelegate(Object obj) {
	Container container = null;
	BeanInfo info = BeanInfoFactory.getBeanInfo(obj.getClass());
	if (info != null)  {
	    BeanDescriptor desc = info.getBeanDescriptor();
	    // If the target is a Swing container then get the container
	    // delegate and use that component as the container to add.
	    String methodName = (String)desc.getValue("containerDelegate");
	    if (methodName != null)  {
		try {
		    Method method = obj.getClass().getMethod(methodName,
								new Class[]{});
		    Object delegate = method.invoke(obj, new Object[]{});
		    container = (Container)delegate;
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	return container;
    }


    /**
     * Returns the visual proxy for the object
     * A visual proxy is a visual component which represents
     * a non visual component.
     *
     * @return an instance of the visual proxy component or null if a proxy doesn't
     *         exist.
     */
    public static Component getProxyComponent(Object obj) {
	Component comp = null;
	BeanInfo info = BeanInfoFactory.getBeanInfo(obj.getClass());
	if (info != null)  {
	    BeanDescriptor desc = info.getBeanDescriptor();
	    // If the target is a Swing container then get the container
	    // delegate and use that component as the container to add.
	    String className = (String)desc.getValue("proxyComponent");
	    if (className != null)  {
		try {
		    Object proxy = Beans.instantiate(obj.getClass().getClassLoader(),
						     className);
		    comp = (Component)proxy;
		    comp.setSize(comp.getPreferredSize());
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}
	return comp;
	
    }
							  
}
