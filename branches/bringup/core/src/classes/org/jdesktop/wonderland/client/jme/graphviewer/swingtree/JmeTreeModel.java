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
package org.jdesktop.wonderland.client.jme.graphviewer.swingtree;

import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author paulby
 */
class JmeTreeModel implements TreeModel {
    
    private RootNode root;
    
    private ArrayList<TreeModelListener> listeners;
    
    public JmeTreeModel() {
        root = new RootNode();
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Node) {
            return ((Node)parent).getChild(index);
        } else if (parent instanceof SceneElement) { 
            return null;
        } else if (parent instanceof RootNode) {
            return ((RootNode)parent).getChild(index);
        } else {
            System.err.println("Unimplemented node "+parent.getClass().getName());
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Node) {
            ArrayList children = ((Node)parent).getChildren();
            if (children==null)
                return 0;
            return children.size();
        } else if (parent instanceof SceneElement) { 
            return 0;
        } else if (parent instanceof RootNode) {
            return ((RootNode)parent).getChildCount();
        } else {
            System.err.println("Unimplemented node "+parent.getClass().getName());
        }
        return 0;
    }

    public boolean isLeaf(Object node) {
        if (getChildCount(node)==0)
            return true;
        return false;
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        System.err.println("JmeTreeModel.valueForPathChanged not implemented");
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof Node) {
            return ((Node)parent).getChildren().size();
        } else if (parent instanceof RootNode) {
            return ((RootNode)parent).getIndexOfChild((SceneElement)child);
        } else {
            System.err.println("Unimplemented getIndexOfChild node "+parent.getClass().getName());
        }
        return 0;
    }

    public void addTreeModelListener(TreeModelListener l) {
        if (listeners==null)
            listeners = new ArrayList();
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTreeNodeInserted(Object changeRoot) {
        if (listeners==null)
            return;
        TreeModelEvent evt = new TreeModelEvent(this, new Object[] {changeRoot});
        for(TreeModelListener l : listeners)
            l.treeNodesInserted(evt);
    }
    
    private void fireTreeNodeRemoved(Object changeRoot) {
        if (listeners==null)
            return;
        TreeModelEvent evt = new TreeModelEvent(this, new Object[] {changeRoot});
        for(TreeModelListener l : listeners)
            l.treeNodesRemoved(evt);
    }
    
    private void fireTreeStructureChanged(Object changeRoot) {
        if (listeners==null)
            return;
        TreeModelEvent evt = new TreeModelEvent(this, new Object[] {changeRoot});
        for(TreeModelListener l : listeners)
            l.treeStructureChanged(evt);
    }
    
    class RootNode {
        private ArrayList<SceneElement> children = new ArrayList();
        
        public void RootNode() {
            
        }
        
        public void addChild(SceneElement child) {
            children.add(child);
            fireTreeNodeInserted(this);
        }
        
        public void removeChild(SceneElement child) {
            children.remove(child);
            fireTreeNodeRemoved(this);
        }
        
        public int getChildCount() {
            return children.size();
        }
        
        public SceneElement getChild(int index) {
            return children.get(index);
        }
        
        public int getIndexOfChild(SceneElement child) {
            int index = 0;
            while(children.get(index)!=child)
                index++;
            
            return index;
        }
    }
}
