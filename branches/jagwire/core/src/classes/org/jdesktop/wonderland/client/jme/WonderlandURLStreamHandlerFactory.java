/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.client.jme;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Setup a URL stream handler for the Wonderland protocols.  This
 * works around the fact that URL will only find handlers
 * loaded in the system classloader, and in webstart the
 * handlers we need (wonderland.protocol.*) are in the jnlp classloader.
 *
 * Note if we return null here, it will go on to try the normal
 * mechanisms defined in URL
 * @author jkaplan
 */
public class WonderlandURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private static final Logger logger =
            Logger.getLogger(WonderlandURLStreamHandlerFactory.class.getName());

    private static final String DEFAULT_PACKAGE = "org.jdesktop.wonderland.client.protocols";
    private static String handlerPackage = DEFAULT_PACKAGE;
    
    public static void setHandlerPackage(String handlerPackage) {
        WonderlandURLStreamHandlerFactory.handlerPackage = handlerPackage;
    }
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler u = null;


        // hardcode the list of protocol handelers.  We do this because
        // trying to load a handler class that doesn't exist using
        // Class.forName() throws a ClassCircularityError.
        String[] protocols = {
            "wla", "wlj", "wltexture", "wlzip", "wlcontent", "wlhttp"
        };
        for (String p : protocols) {
            if (p.equalsIgnoreCase(protocol)) {
                String className = handlerPackage + "." + p + ".Handler";
                try {
                    Class clazz = Class.forName(className);
                    u = (URLStreamHandler) clazz.newInstance();
                } catch (Exception ex) {
                    logger.log(Level.FINE, "Error loading Wonderland " +
                            "protocol handler " + className, ex);
                }
            }
        }

        return u;
    }
}
