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

package org.jdesktop.wonderland.modules.appbase.server;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;

/**
 * The WFS setup class for AppConventionalCellMO.
 * 
 * @author deronj
 */
@XmlRootElement(name="app-conventional-cell")
public class AppConventionalCellSetup extends BasicCellSetup implements Serializable {
    
    /** The master host. */
    @XmlElement(name="masterHost")
    public String masterHost = null;
    
    /** The name of the app. */
    @XmlElement(name="appName")
    public String appName = null;

    /* The platform-specific app startup command. */
    @XmlElement(name="command")
    public String command= null;
    
    /* The pixel scale. */
    @XmlElement(name="pixelScale")
    public Vector2f pixelScale = null;

    /** Default constructor */
    public AppConventionalCellSetup() {}
    
    /**
     * Returns the master host.
     * @return The master host.
     */
    @XmlTransient public String getMasterHost () {
        return masterHost;
    }
    
    /**
     * Sets the master host. If null, then this property will not be written
     * out to the file.
     * 
     * @param masterHost The name of the master host.
     */
    public void setMasterHost (String masterHost) {
        this.masterHost = masterHost;
    }
    
    /**
     * Returns the app name.
     * @return The app name.
     */
    @XmlTransient public String getAppName () {
        return appName;
    }
    
    /**
     * Sets the appName. If null, then this property will not be written
     * out to the file.
     * 
     * @param appName The name of the app.
     */
    public void setAppName (String appName) {
        this.appName = appName;
    }

    /**
     * Returns the platform-specific app startup command.
     * @return command The command.
     */
    @XmlTransient public String getCommand () {
        return command;
    }
    
    /**
     * Sets the platform-specific app startup command. If null, then this property will not be written
     * out to the file.
     * 
     * @param command The command.
     */
    public void setCommand (String command) {
        this.command = command;
    }
    

    /**
     * Returns the pixel scale.
     * @return The pixel scale.
     */
    @XmlTransient public Vector2f getPixelScale () {
        return pixelScale;
    }
    
    /**
     * Sets the pixel scale. If null, then this property will not be written
     * out to the file.
     * 
     * @param pixelScale The pixel scale.
     */
    public void setPixelScale (Vector2f pixelScale) {
        this.pixelScale = pixelScale;
    }
    
    public String getServerClassName() {
        return "org.jdesktop.wonderland.modules.appbase.server.AppConventionalCellMO";
    }
    
    /**
     * Returns a string representation of this class.
     *
     * @return The setup information as a string.
     */
    @Override
    public String toString() {
        return super.toString() + " [AppConventionalCellSetup]: " +
	    "masterHost = " + masterHost +
	    "appName = " + appName +
	    "command = " + command +
	    "pixelScale = " + pixelScale;
    }
}
