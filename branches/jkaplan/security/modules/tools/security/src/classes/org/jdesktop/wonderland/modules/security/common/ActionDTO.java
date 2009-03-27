/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.security.common;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.common.security.Action;

/**
 * Encapsulate an action into XML form
 * @author jkaplan
 */
@XmlRootElement(name = "action")
public class ActionDTO implements Serializable, Comparable {
    private static final Logger logger =
            Logger.getLogger(ActionDTO.class.getName());

    private String clazz;
    private String name;
    private String parent;
    private String displayName;
    private String toolTip;

    public ActionDTO() {
    }

    public ActionDTO(Action action) {
        this.clazz = action.getClass().getName();
        this.name = action.getName();
        this.parent = action.getParent();
        this.displayName = action.getDisplayName();
        this.toolTip = action.getToolTip();
    }

    @XmlElement
    public String getActionClass() {
        return clazz;
    }

    public void setActionClass(String clazz) {
        this.clazz = clazz;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @XmlElement
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @XmlElement
    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    @XmlTransient
    public Action getAction() {
        try {
            Class c = getClass().getClassLoader().loadClass(getActionClass());
            return (Action) c.newInstance();
        } catch (InstantiationException ex) {
            logger.log(Level.SEVERE, "Error creating action", ex);
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, "Error creating action", ex);
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Error creating action", ex);
        }

        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ActionDTO other = (ActionDTO) obj;
        if ((this.clazz == null) ? (other.clazz != null) : !this.clazz.equals(other.clazz)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object o) {
        if (!(o instanceof ActionDTO)) {
            return 0;
        }

        ActionDTO oa = (ActionDTO) o;
        return getActionClass().compareTo(oa.getActionClass());
    }
}
