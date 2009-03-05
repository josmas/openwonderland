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
package org.jdesktop.wonderland.common.security;

import java.io.Serializable;

/**
 * An action that a user can perform on a resource
 * @author jkaplan
 */
public class Action implements Serializable {
    /** actions are uniquely identified by name */
    private String name;

    /** the name of the parent for this action */
    private String parent;

    /** the display name for this action */
    private String displayName;

    /** the tool-tip for this action */
    private String toolTip;

    /**
     * Create a new top-level action with the given name
     * @param name the name of the action to created
     */
    public Action(String name) {
        this (name, null);
    }

    /**
     * Create a new action with the given name and parent
     * @param name the name of the action to create
     * @param parent the name of this action's parent
     */
    public Action(String name, String parent) {
        this (name, parent, null, null);
    }

    /**
     * Create a new action with the given name and parent
     * @param name the name of the action to create
     * @param parent the name of this action's parent
     */
    public Action(String name, String parent, String displayName,
                  String toolTip)
    {
        this.name        = name;
        this.parent      = parent;
        this.displayName = displayName;
        this.toolTip     = toolTip;
    }

    /**
     * Get this action's name
     * @return the action's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parent of this action.  By default, an action is assigned
     * the same value as its parent.
     * @return the name of this action's parent, or null if this action
     * is the top-level
     */
    public String getParent() {
        return parent;
    }

    /**
     * Get the name to display for this action. 
     * @return the display name, or null if the display name is the
     * same as the name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the tool tip associated with this action
     * @return the tool tip for this action, or null if the action has no
     * tool tip
     */
    public String getToolTip() {
        return toolTip;
    }

    /**
     * Two actions are the same if they have the same name
     * @param obj the object to compare to
     * @return true if the object is an Action with the same name as this one
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Action other = (Action) obj;
        if ((this.name == null) ? (other.name != null) : 
            !this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
