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
package org.jdesktop.wonderland.common.app.base;

import java.util.UUID;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * @author deronj
 */

@ExperimentalAPI
public class AppTypeCellConfig extends CellConfig {

    // TODO: For local testing. Until xremwin module becomes part of the standard art 
    private String baseUrl;

    private String appTypeServerClassName;
    private String appTypeClientClassName;
    private String serverJar;
    private String clientJar;

    public AppTypeCellConfig () {}

    public AppTypeCellConfig (String baseUrl, String appTypeServerClassName, 
			      String appTypeClientClassName, String serverJar,
			      String clientJar) {
        this.baseUrl = baseUrl;
        this.appTypeServerClassName = appTypeServerClassName;
        this.appTypeClientClassName = appTypeClientClassName;
	this.serverJar = serverJar;
	this.clientJar = clientJar;
    }

    public void setBaseUrl (String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl () {
        return baseUrl;
    }

    public void setAppTypeServerClassName (String appTypeServerClassName) {
        this.appTypeServerClassName = appTypeServerClassName;
    }
    
    public String getAppTypeServerClassName () {
        return appTypeServerClassName;
    }

    public void setAppTypeClientClassName (String appTypeClientClassName) {
        this.appTypeClientClassName = appTypeClientClassName;
    }
    
    public String getAppTypeClientClassName () {
        return appTypeClientClassName;
    }

    public void setServerJar (String serverJar) {
        this.serverJar = serverJar;
    }
    
    public String getServerJar () {
        return serverJar;
    }

    public void setClientJar (String clientJar) {
        this.clientJar = clientJar;
    }
    
    public String getClientJar () {
        return clientJar;
    }
}
