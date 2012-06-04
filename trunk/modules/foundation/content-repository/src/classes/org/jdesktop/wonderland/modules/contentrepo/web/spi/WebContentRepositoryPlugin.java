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
package org.jdesktop.wonderland.modules.contentrepo.web.spi;

import org.jdesktop.wonderland.client.jme.WonderlandURLStreamHandlerFactory;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.webserver.WebLibPlugin;
import org.jdesktop.wonderland.webserver.WonderlandAppServer;

/**
 * Plugin for registering content handlers to point to the web content
 * repository on the web server.
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
@Plugin
public class WebContentRepositoryPlugin implements WebLibPlugin {
    private static final String HANDLER_PACKAGE =
            WebContentRepositoryPlugin.class.getPackage().getName() + ".protocols";
    
    public void initialize(WonderlandAppServer was) {
        WonderlandURLStreamHandlerFactory.setHandlerPackage(HANDLER_PACKAGE);
    }   
}
