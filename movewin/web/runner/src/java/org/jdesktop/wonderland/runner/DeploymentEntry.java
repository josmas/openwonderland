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
package org.jdesktop.wonderland.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Store information for starting a single runners
 * @author jkaplan
 */
@XmlRootElement(name="DeploymentEntry")
public class DeploymentEntry {
    private String runnerName;
    private String runnerClass;
    private Properties runProps = new Properties();
    
    public DeploymentEntry() {
        // default no-arg constructor
    }
    
    public DeploymentEntry(String runnerName, String runnerClass) {
        this.runnerName = runnerName;
        this.runnerClass = runnerClass;
    }
    
    @XmlTransient
    public Properties getRunProps() {
        return runProps;
    }

    public void setRunProps(Properties runProps) {
        this.runProps = runProps;
    }

    @XmlElement(name="property")
    public Property[] getProperties() {
        Property[] out = new Property[getRunProps().size()];
        int i = 0;
        for (Entry<Object, Object> entry : getRunProps().entrySet()) {
            out[i++] = new Property(entry);
        }
        
        return out;
    }
    
    public void setProperties(Property[] properties) {
        Properties props = new Properties();
        for (Property prop : properties) {
            props.setProperty(prop.key, prop.value);
        }
        
        setRunProps(props);
    }
    
    @XmlElement
    public String getRunnerClass() {
        return runnerClass;
    }

    public void setRunnerClass(String runnerClass) {
        this.runnerClass = runnerClass;
    }

    @XmlElement
    public String getRunnerName() {
        return runnerName;
    }

    public void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeploymentEntry other = (DeploymentEntry) obj;
        if (this.runnerName != other.runnerName && (this.runnerName == null || !this.runnerName.equals(other.runnerName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.runnerName != null ? this.runnerName.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return getRunnerName() + " " + getRunnerClass() + " " + getRunProps().size();
    } 
    
    // internal representation of elements in the properties set
    protected static class Property {
        @XmlElement public String key;
        @XmlElement public String value;

        private Property() {} //Required by JAXB

        public Property(Map.Entry<Object, Object> entry) {
            this.key = (String) entry.getKey();
            this.value = (String) entry.getValue();
        }
    }
}
