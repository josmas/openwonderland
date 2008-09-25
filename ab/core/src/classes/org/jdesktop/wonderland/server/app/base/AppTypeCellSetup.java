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

package org.jdesktop.wonderland.server.app.base;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.jdesktop.wonderland.server.cell.setup.BasicCellSetup;

/**
 * The WFS setup class for AppTypeCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="app-type-cell")
public class AppTypeCellSetup extends BasicCellSetup implements Serializable {
    
    /** The name of the app type class on the server side. */
    @XmlElement(name="appTypeServerClassName")
    public String appTypeServerClassName = null;
    
    /** The name of the app type class on the client side. */
    @XmlElement(name="appTypeClientClassName")
    public String appTypeClientClassName = null;
    
    /** The name of the server jar for the app type */
    @XmlElement(name="serverJar")
    public String serverJar = null;

    /** The name of the client jar for the app type */
    @XmlElement(name="clientJar")
    public String clientJar = null;
    
    // TODO: For local testing. Until whiteboard module becomes part of the standard art
    /** The The base URL */
    @XmlElement(name="baseUrl")
    public String baseUrl = null;
    
    /** Default constructor */
    public AppTypeCellSetup() {}
    
    @XmlTransient public String getAppTypeServerClassName () {
        return appTypeServerClassName;
    }
    
    public void setAppTypeServerClassName (String appTypeServerClassName) {
        this.appTypeServerClassName = appTypeServerClassName;
    }
    
    @XmlTransient public String getAppTypeClientClassName () {
        return appTypeClientClassName;
    }
    
    public void setAppTypeClientClassName (String appTypeClientClassName) {
        this.appTypeClientClassName = appTypeClientClassName;
    }
    
    @XmlTransient public String getServerJar () {
        return serverJar;
    }
    
    public void setServerJar (String serverJar) {
        this.serverJar = serverJar;
    }
    
    @XmlTransient public String getClientJar () {
        return clientJar;
    }
    
    public void setClientJar (String clientJar) {
        this.clientJar = clientJar;
    }
    
    // TODO: For local testing. Until whiteboard module becomes part of the standard art
    @XmlTransient public String getBaseUrl () {
        return baseUrl;
    }
    
    // TODO: For local testing. Until whiteboard module becomes part of the standard art
    public void setBaseUrl (String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.server.app.base.AppTypeCellMO";
    }
    
    /**
     * Returns a string representation of this class.
     *
     * @return The setup information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [AppTypeCellSetup]: " +
	    "appTypeServerClassName = " + appTypeServerClassName +
	    "appTypeClientClassName = " + appTypeClientClassName +
	    "serverJar = " + serverJar +
	    "clientJar = " + clientJar;
    }
}
