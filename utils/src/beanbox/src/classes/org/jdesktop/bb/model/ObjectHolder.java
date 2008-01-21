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

package org.jdesktop.bb.model;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.Beans;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;

import org.jdesktop.bb.util.BeanInfoFactory;

/**
 * The singleton instance of the class acts as a global repository and access to
 * the root object. This class manages the state
 * and manipulation of the root object and sends out events when it its changed.
 *
 * This class also manages associations between visual and non-visual objects.
 * For example, the Component representations which acts as a stand in for non
 * visual objects such as models can be managed here.
 *
 * This class also acts as a proxy for ComponentListeners and ContainerListeners.
 * Rather than register listeners on the designed components, the listener
 * association can be done through this class to simulate those events.
 *
 * This class is necessary because we don't want to add listeners to the root
 * object and all the other designed objects and we want all the information 
 * pertaining to the design and event
 * propagation resulting from the manipuation of the design in a central
 * repository.
 * 
 * @version 1.17 02/27/02
 * @author Mark Davidson
 */
public class ObjectHolder {
    
    // Root object. This is live root object which is to be manipulated 
    // by the designer. This is also the object which will be archived.
    private Object root;
    
    // Root container object. This is the object which may be the same as
    // the root object. If the root is a non-visual object, then this 
    // may represent a proxy of the root.
    private Container rootContainer;
    
    // Currently selected item (to be manipulated or edited).
    private Object selectedItem;
    
    private SwingPropertyChangeSupport changeSupport;
    
    // Association between components and component listeners
    private HashMap listenerTable;
    
    // These next two data structures and the getProxyComponent,
    // getProxyObject and un/registerProxy are meant to allow 
    // visual components to stand in for non-visual beans.
    
    // Association between key: object, value: visual-component
    private HashMap componentTable;
    // Association between key: visual-component, value: object
    private HashMap objectTable;    
    
    // Singleton instance of the Object holder.
    private static ObjectHolder objectHolder;
    
    /**
     * Returns the shared <code>ObjectHolder</code>
     *
     * @return shared ObjectHolder
     */
    public static ObjectHolder getInstance() {
        if (objectHolder == null) {
            objectHolder = new ObjectHolder();
        }
        return objectHolder;
    }
    
    /**
     * Sets the root object, sets the root Container property and
     * sets the design time property to true. The root object is the 
     * object which is the top level object in an object graph.
     * <p>
     * The root item will become the new selected item
     *
     * @see #setRootContainer
     * @beaninfo
     *       bound: true
     * description: The root object in the designer.
     */
    public void setRoot(Object root) {
        Object oldRoot = this.root;
        this.root = root;
        
        // Set the root container property. The root container
        // is the container that Components (or proxy components) 
        // will be added and removed
        Container container = null;
        if (isContainer(root)) {
            container = (Container)root;
        } else {
            // Get the proxy component and register it.
            Component comp = BeanInfoFactory.getProxyComponent(root);
            if (comp != null) {
                registerProxy(root, comp);
            }
            if (isContainer(getProxyComponent(root))) {
                container = (Container)getProxyComponent(root);
            }
        }
        if (container != null) {
            container = getRootContainer(container);
        }
        setRootContainer(container);
        
        if (!isComponent(oldRoot)) {
            // Unregister old proxy component
            Component comp = getProxyComponent(oldRoot);
            if (comp != null) {
                unregisterProxy(oldRoot, comp);
            }
        }
        
        if (!isComponent(root)) {
            createProxyGraph(root);
        }
        
        setDesignTime(false);
        if (changeSupport != null) {
            changeSupport.firePropertyChange("root", oldRoot, root);
        }
        setSelectedItem(root);
        setDesignTime(true);
    }
    
    /**
     * This is a big fat hack
     */
    private void createProxyGraph(Object root) {
        Object result = BeanInfoFactory.executeMethod("getChildren", root);
        if (result == null || !result.getClass().isArray()) {
            return;
        }
        Object[] children = (Object[])result;
        
        for (int i = 0; i < children.length; i++) {
            Component proxyComp = BeanInfoFactory.getProxyComponent(children[i]);
            if (proxyComp != null) {
                registerProxy(children[i], proxyComp);
                BeanInfoFactory.executeMethod("installUI", proxyComp, children[i]);
                add((Container)getProxyComponent(root), proxyComp);
            }
            
        }
    }
    
    
    /**
     * Gets the root object.
     */
    public Object getRoot() {
        return root;
    }
    
    /**
     * Set the root container. The root container is the actual container that
     * components are added and removed from. This can represent a proxy
     * proxy representation of the non-visual root or a containerDelegate
     * if the root is a top level Swing container.
     * <p>
     * If the current root is a Swing Container then the
     * the BeanInfo attributes "isContainer" and "containerDelegate"
     * are used to find the container.
     * <p>
     * If the current root is a non-visual component then the BeanInfo
     * attribute "proxyComponent"
     * 
     * @beaninfo
     *      bound: true
     * decription: The actual container that components are added and removed
     */
    public void setRootContainer(Container cont) {
        Container oldContainer = rootContainer;
        
        this.rootContainer = cont;
        if (changeSupport != null) {
            changeSupport.firePropertyChange("rootContainer", 
                    oldContainer, rootContainer);
        }	
    }
    
    /**
     * Returns the root container in which components are added 
     * removed or selected. 
     * 
     * @return a Container in which components are added or removed
     */
    public Container getRootContainer() {
        return rootContainer;
    }
    
    /**
     * Returns the root container in which components are added 
     * removed or selected. If this is a Swing Container then the
     * the SwingBeanInfo attributes "isContainer" and "containerDelegate"
     * are used.
     * 
     * @param cont a top level container like a JFrame or a JScrollPane
     * @return a Container in which components are added or removed
     */
    public Container getRootContainer(Container cont) {
        if (BeanInfoFactory.isContainer(cont)) {
            cont = BeanInfoFactory.getContainerDelegate(cont);
        }
        return cont;
    } 
    
    
    //
    // Utility methods
    //
    
    public static boolean isContainer(Object obj) {
        return (obj instanceof Container);
    }
    
    public static boolean isComponent(Object obj) {
        return (obj instanceof Component);
    }
    
    
    /**
     * Sets the current selected item. This is a bound property
     * 
     * @param newItem the object to select. This can be a Component, a non-visual bean
     *                but not a proxy component.
     * @beaninfo
     *     bound: true
     */
    public void setSelectedItem(Object newItem) {
        Object oldItem = this.selectedItem;
        this.selectedItem = newItem;
        
        if (isComponent(newItem)) {
            // The selected item can't be a proxy Component. So 
            // select the actual object first.
            this.selectedItem = getProxyObject((Component)newItem);
            if (this.selectedItem == null) {
                this.selectedItem = newItem;
            }
        }
        
        if (changeSupport != null) {
            changeSupport.firePropertyChange("selectedItem", oldItem, selectedItem);
        }
    }
    
    /**
     * Returns the current selected item
     * 
     * @return the selected item or null for no selection
     */
    public Object getSelectedItem() {
        return selectedItem;
    }
    
    /**
     * Returns the state of the designer. This calls the Beans.setDesignTime
     * property.
     * 
     * @param isDesignTime flag which indiciates the state of the designer.
     * @beaninfo 
     *        bound: true
     */
    public void setDesignTime(boolean isDesignTime) {
        // Set the mode to Design or Runtime.
        boolean oldState = Beans.isDesignTime();
        Beans.setDesignTime(isDesignTime);
        
        if (changeSupport != null) {
            changeSupport.firePropertyChange("designTime", Boolean.valueOf(oldState), 
                    Boolean.valueOf(isDesignTime));
        }
    }
    
    public boolean isDesignTime() {
        return Beans.isDesignTime();
    }
    
    /**
     * Adds the Object to the root Object. 
     * <p>
     * If the root object is a Container and the Object is a Component then a 
     * <code>ContainerEvent.COMPONENT_ADDED</code> event will be fired.
     * <p>
     * The added object will become the selected object.
     *
     * @param obj Object to add
     */
    public void add(Object obj) {
        if (isContainer(root) && isComponent(obj)) {
            // Add the object to the root container
            add(getRootContainer(), (Component)obj);
        } else {
            // Try to add to the root by looking for an "add" method which
            // takes the type of the obj type as the argument.
            BeanInfoFactory.executeMethod("add", root, obj);
            
            Component proxyComp = BeanInfoFactory.getProxyComponent(obj);
            if (proxyComp != null) {
                registerProxy(obj, proxyComp);
                BeanInfoFactory.executeMethod("installUI", proxyComp, obj);
                // The Calling method should execute this method and set
                // the correct bounds
                // add(getRootContainer(), proxyComp);
            }
            else {
                // Just send an "addBean"  message
                if (changeSupport != null) {
                    changeSupport.firePropertyChange("addBean", null, obj);
                }
            }
        }
        setSelectedItem(obj);
    }
    
    
    /**
     * Helper method which adds the Component to the Container and fires
     * a <code>ContainerEvent.COMPONENT_ADDED</code> event to registered
     * ContainerListeners.
     *
     * @param container Container for Component
     * @param comp Component to add
     */
    public void add(Container container, Component comp) {
        container.add(comp);
        fireComponentAdded(new ContainerEvent(container,
                ContainerEvent.COMPONENT_ADDED,
                comp));
    }
    
    /**
     * Removes the Object from the root Object.
     * <p>
     * If the root object is a Container and the Object is a Component then a
     * <code>ContainerEvent.COMPONENT_REMOVED</code> event will be fired.
     *
     * @param obj Object to remove
     */
    public void remove(Object obj) {
        if (isContainer(root) && isComponent(obj)) {
            // Remove the object from the root container delegate.
            remove(getRootContainer(), (Component)obj);
        } else {
            // Try to remove the obj from root by looking for a
            // "remove" method which takes the type of the obj
            //  type as the argument.
            BeanInfoFactory.executeMethod("remove", root, obj);
            
            Component proxyComp = getProxyComponent(obj);
            if (proxyComp != null) {
                remove(getRootContainer(), proxyComp);
                BeanInfoFactory.executeMethod("uninstallUI", proxyComp, obj);
                unregisterProxy(obj, proxyComp);
            } 
            else {
                // Just fire a removeBean event
                if (changeSupport != null) {
                    changeSupport.firePropertyChange("removeBean", null, obj);
                }
            }
        }
        setSelectedItem(getRootContainer());
    }
    
    /**
     * Removes the Component from the Container. Will fire a 
     * <code>ContainerEvent.COMPONENT_REMOVED</code> event.
     * <p>
     * The Container will become the new selected item
     *
     * @param comp Component to remove
     */
    public void remove(Container container, Component comp) {
        container.remove(comp);
        
        fireComponentRemoved(new ContainerEvent(container,
                ContainerEvent.COMPONENT_REMOVED,
                comp));
    }
    
    //
    // These next set of methods facilitates the association between 
    // non-visual Objects and Components which will stand in for them.
    // 
    
    /**
     * Retrieves the visual Component which is used to 
     * represents the non-visual object in the designer.
     * 
     * @param item a non-visual bean
     * @return the Component which is a stand in for the item or null
     */
    public Component getProxyComponent(Object item) {
        if (componentTable == null) {
            return null;
        }
        return (Component)componentTable.get(item);
    }
    
    /**
     * Returns the non-visual object which is used as the underlying
     * stand in for the item. This method is directly opposite to the
     * previous method.
     *
     * @param item a visual bean manipulated in the builder
     * @return the object it represents
     */
    public Object getProxyObject(Component item) {
        if (objectTable == null) {
            return null;
        }
        return objectTable.get(item);
    }
    
    /**
     * Tests to see whether the component represents a visual proxy.
     */
    public boolean isProxyComponent(Component comp) {
        return (getProxyObject(comp) != null);
    }
    
    /**
     * Return an iteration of objects which have a component association
     *
     * @return an Iterator which represents all the non-visual objects or
     *         null if there isn't any component associations.
     */
    public Iterator getProxyObjects() {
        if (componentTable != null) {
            return componentTable.keySet().iterator();
        }
        return null;
    }
    
    /**
     * Return an iteration of Component which have an object association
     * 
     * @return an Iterator of Components or null
     */
    public Iterator getProxyComponents() {
        if (objectTable != null) {
            return objectTable.keySet().iterator();
        }
        return null;
    }
    
    /**
     * Creates an association between the non visual Object and
     * a Component.
     * 
     * @param obj an Object which isn't a Component
     * @param comp a Component which represents the obj in a builder
     */
    public void registerProxy(Object obj, Component comp) {
        // Association between key: visual component, value: non-visual object
        if (objectTable == null) {
            objectTable = new HashMap();
        }
        objectTable.put(comp, obj);
        
        // Association between key: non-visual object, value: visual component
        if (componentTable == null) {
            componentTable = new HashMap();
        }
        componentTable.put(obj, comp);
    }
    
    /**
     * Removes the association between the non visual Object and
     * a Component.
     * 
     * @param obj an Object which isn't a Component
     * @param comp a Component which represents the obj in a builder
     */
    public void unregisterProxy(Object obj, Component comp) {
        if (objectTable != null && componentTable != null) {
            objectTable.remove(comp);
            componentTable.remove(obj);
        }
    }
    
    //
    // Listener registration and management methods.
    //
    
    private void addListener(Class listenerType, Object obj, 
            EventListener listener) {
        if (listenerTable == null) {
            listenerTable = new HashMap();
        }
        EventListenerList list = (EventListenerList)listenerTable.get(obj);
        if (list == null) {
            list = new EventListenerList();
            listenerTable.put(obj, list);
        }
        list.add(listenerType, listener);
    }
    
    private void removeListener(Class listenerType, Object obj,
            EventListener listener) {
        if (listenerTable != null && listener != null) {
            EventListenerList list = (EventListenerList)listenerTable.get(obj);
            if (list != null) {
                list.remove(listenerType, listener);
            }
        }	
    }
    
    private Object[] getListeners(Object obj) {
        EventListenerList list = (EventListenerList)listenerTable.get(obj);
        if (list != null) {
            return list.getListenerList();
        }
        return new Object[]{};
    }
    
    /**
     * This class acts as a proxy for ComponentEvents. We don't want to 
     * add listeners directly to the designed objects since but we are interested
     * in recieving notifications when the component changes. 
     * <p>
     * The events can be fired by calling the fire moved or resized methods.
     *
     * @param comp Component which represents the event.
     * @param l the listener to call
     * @see #fireComponentMoved
     * @see #fireComponentResized
     */
    public void addComponentListener(Component comp, ComponentListener l) {
        addListener(ComponentListener.class, comp, l);
    }
    
    /**
     * Removes a ComponentListener.
     */
    public void removeComponentListener(Component comp, ComponentListener l) {
        removeListener(ComponentListener.class, comp, l);
    }
    
    /**
     * This class acts as a proxy for ContainerEvents. We don't want to 
     * add listeners directly to the designed objects since but we are interested
     * in recieving notifications when the component changes. 
     * <p>
     * The events can be fired by calling the fire added or removed methods.
     *
     * @param l the listener to call
     * @see #fireComponentAdded
     * @see #fireComponentRemoved
     */
    public void addContainerListener(ContainerListener l) {
        addListener(ContainerListener.class, this, l);
    }
    
    /**
     * Removes a container listener.
     */
    public void removeContainerListener(ContainerListener l) {
        removeListener(ContainerListener.class, this, l);
    }
    
    public void fireComponentMoved(Component comp) {
        fireComponentMoved(comp, new ComponentEvent(comp, 
                ComponentEvent.COMPONENT_MOVED));
    }
    
    public void fireComponentMoved(Component comp, ComponentEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = getListeners(comp);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i] == ComponentListener.class ) {
                ((ComponentListener)listeners[i+1]).componentMoved(evt);
            }
        }
    }
    
    public void fireComponentResized(Component comp) {
        fireComponentResized(comp, new ComponentEvent(comp, 
                ComponentEvent.COMPONENT_RESIZED));
    }
    
    public void fireComponentResized(Component comp, ComponentEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = getListeners(comp);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i] == ComponentListener.class ) {
                ((ComponentListener)listeners[i+1]).componentResized(evt);
            }
        }
    }
    
    public void fireComponentAdded(ContainerEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = getListeners(this);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i] == ContainerListener.class ) {
                ((ContainerListener)listeners[i+1]).componentAdded(evt);
            }
        }
    }
    
    public void fireComponentRemoved(ContainerEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = getListeners(this);
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for ( int i = listeners.length-2; i>=0; i-=2 ) {
            if ( listeners[i] == ContainerListener.class ) {
                ((ContainerListener)listeners[i+1]).componentRemoved(evt);
            }
        }
    }
    
    /**
     * Adds a <code>PropertyChangeListener</code> to the listener list.
     * The listener is registered for all properties.
     * <p>
     * A <code>PropertyChangeEvent</code> will get fired in response
     * to setting a bound property, such as <code>setFont</code>,
     * <code>setBackground</code>, or <code>setForeground</code>.
     * <p>
     * Note that if the current component is inheriting its foreground,
     * background, or font from its container, then no event will be
     * fired in response to a change in the inherited property.
     *
     * @param listener  the <code>PropertyChangeListener</code> to be added
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(listener);
    }
    
    
    /**
     * Adds a <code>PropertyChangeListener</code> for a specific property.
     * The listener will be invoked only when a call on
     * <code>firePropertyChange</code> names that specific property.
     * <p>
     * If listener is <code>null</code>, no exception is thrown and no
     * action is performed.
     *
     * @param propertyName  the name of the property to listen on
     * @param listener  the <code>PropertyChangeListener</code> to be added
     */
    public synchronized void addPropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            changeSupport = new SwingPropertyChangeSupport(this);
        }
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }
    
    /**
     * Removes a <code>PropertyChangeListener</code> from the listener list.
     * This removes a <code>PropertyChangeListener</code> that was registered
     * for all properties.
     *
     * @param listener  the <code>PropertyChangeListener</code> to be removed
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport != null) {
            changeSupport.removePropertyChangeListener(listener);
        }
    }
    
    
    /**
     * Removes a <code>PropertyChangeListener</code> for a specific property.
     * If listener is <code>null</code>, no exception is thrown and no
     * action is performed.
     *
     * @param propertyName  the name of the property that was listened on
     * @param listener  the <code>PropertyChangeListener</code> to be removed
     */
    public synchronized void removePropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        if (listener == null) {
            return;
        }
        if (changeSupport == null) {
            return;
        }
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }
}
