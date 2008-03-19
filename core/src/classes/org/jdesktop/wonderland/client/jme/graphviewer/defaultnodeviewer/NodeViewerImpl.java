/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.jme.graphviewer.defaultnodeviewer;

import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DebugGraphics;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;
import org.jdesktop.wonderland.client.jme.graphviewer.NodeViewer;

/**
 *
 * @author paulby
 */
public class NodeViewerImpl implements NodeViewer {

    private JPanel viewerPanel;
    private LinkedList<JmeInfoPanel> panels;
    
    private String packageName = "org.jdesktop.wonderland.client.jme.graphviewer.defaultnodeviewer";
    
    
    public NodeViewerImpl() {
        viewerPanel = new JPanel();
        viewerPanel.setLayout(new BoxLayout(viewerPanel, BoxLayout.Y_AXIS));
        viewerPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
    }
    
    public JPanel getNodeViewerPanel() {
        return viewerPanel;
    }

    public void setNode(SceneElement node) {
        if (node==null) {
            // Clear the panel
            viewerPanel.removeAll();
        } else {
            Class[] classHierarchy = getClassHierarchy(node.getClass());
            LinkedList<JmeInfoPanel> list = createPanelList(classHierarchy);
            
            // We should not be removing all panels but instead only
            // add/remove those that have changed. Then we should call setElement
            // on the entire list.
            viewerPanel.removeAll();
            for(JmeInfoPanel p : list) {
                p.setElement(node);
                viewerPanel.add(p);
            }
            viewerPanel.revalidate();
        }
        
    }
    
    /**
     * Given a set of classes create a list of panel renderers for those
     * classes.
     * @param classes
     * @return
     */
    private LinkedList<JmeInfoPanel> createPanelList(Class[] classes) {
        LinkedList<JmeInfoPanel> ret = new LinkedList();
        
        for(Class clazz : classes) {
            try {
                String panelClassName = packageName + "." + getTrimmedClassName(clazz)+"Panel";
                Class panelClass = Class.forName(panelClassName);
                ret.addFirst((JmeInfoPanel)panelClass.newInstance());
            } catch (InstantiationException ex) {
                Logger.getLogger(NodeViewerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(NodeViewerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(NodeViewerImpl.class.getName()).warning("No panel for "+clazz);
            }
        }
                
        return ret;
    }
    
    /**
     * Returns the classname without leading package name
     * @param clazz
     * @return
     */
    private String getTrimmedClassName(Class clazz) {
        String ret = clazz.getName();
        int dot = ret.lastIndexOf('.');
        if (dot==0)
            return ret;
        return ret.substring(dot+1);
    }
    
    /**
     * Return the array of superclasses of the supplied Class. The supplied 
     * class will be the last element in the array the first superclass will be
     * the first element. The list does not include Object.class
     * 
     * @param clazz
     * @return
     */
    private Class[] getClassHierarchy(Class clazz) {
        LinkedList<Class> hierarchy = new LinkedList();
        Class c = clazz;
        while(c!=Object.class) {
            hierarchy.add(c);
            c = c.getSuperclass();
        }
        
        return hierarchy.toArray(new Class[hierarchy.size()]);
    }
    
    

}
