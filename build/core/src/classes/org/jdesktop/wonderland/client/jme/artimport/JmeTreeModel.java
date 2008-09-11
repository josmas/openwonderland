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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import java.awt.Component;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author paulby
 */
class JmeTreeModel implements TreeModel {

    private Spatial root;
    private LinkedList<TreeModelListener> modelListeners = new LinkedList();

    public JmeTreeModel(Spatial node) {
        root = node;
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        return ((Node)parent).getChild(index);
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Node)
            return ((Node)parent).getQuantity();
        else 
            return 0;
    }

    public boolean isLeaf(Object node) {
        return (getChildCount(node)==0);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((Node)parent).getChildIndex((Node)child);
    }

    public void addTreeModelListener(TreeModelListener l) {
        modelListeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        modelListeners.remove(l);
    }
}

class JmeTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                           Object value,
                                           boolean selected,
                                           boolean expanded,
                                           boolean leaf,
                                           int row,
                                           boolean hasFocus) {
        String name ="";
        if ((value instanceof Spatial)) {
            name = ((Spatial)value).getName();
            if (name==null)
                name="";
        }
        return new JLabel(getTrimmedClassname(value)+":"+name);
    }       

    /**
     * Return the classname of the object, trimming off the package name
     * @param o
     * @return
     */
    private String getTrimmedClassname(Object o) {
        String str = o.getClass().getName();

        return str.substring(str.lastIndexOf('.')+1);
    }
}