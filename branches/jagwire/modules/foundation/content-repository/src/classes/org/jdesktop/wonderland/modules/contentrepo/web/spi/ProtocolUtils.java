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

import javax.servlet.ServletContext;

/**
 * Utilities for using specific protocols in the web server
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ProtocolUtils {
    private static final ThreadLocal<ServletContext> context =
            new ThreadLocal<ServletContext>();
    
    /**
     * Set the current servlet context
     * @param context the current servlet context
     */
    public static void setContext(ServletContext ctx) {
        context.set(ctx);
    }
    
    /**
     * Get the current servlet context
     * @return the current context
     */
    public static ServletContext getContext() {
        return context.get();
    }
}
