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

package org.jdesktop.wonderland.service;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import org.jdesktop.wonderland.service.modules.ModuleManager;
import org.jdesktop.wonderland.service.wfs.WFSManager;

/**
 *
 * @author jordanslott
 */
public class ServiceMain {

    public static void main(String args[]) throws IOException {
        ModuleManager mm = ModuleManager.getModuleManager();
        WFSManager wfsm = WFSManager.getWFSManager();
        
        HttpServer server = HttpServerFactory.create("http://localhost:9998/");
        server.start();
        
        System.out.println("Server running");
        System.out.println("Visit: http://localhost:9998/");
        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");   
        server.stop(0);
        System.out.println("Server stopped"); 
    }
}
