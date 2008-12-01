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
package org.jdesktop.wonderland.client.jme;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;

/**
 * Provides a mechansim for Avatar renderer modules to register themselves
 * with the core system.
 *
 * @author paulby
 */
public class AvatarRenderManager {

    private static AvatarRenderManager avatarRenderMgr = new AvatarRenderManager();
    private HashMap<String, Class<? extends CellRenderer>> rendererClasses = new HashMap();

    private AvatarRenderManager() {

    }

    /**
     * Get the avatar render manager
     * @return
     */
    public static AvatarRenderManager getAvatarRenderManager() {
        return avatarRenderMgr;
    }

    public void registerRenderer(Class<? extends CellRenderer> rendererClass) {
        rendererClasses.put(rendererClass.getName(), rendererClass);
    }

    /**
     * Instantiate and return a renderer of the specified class
     *
     * @param rendererClass
     * @return
     */
    public CellRenderer createRenderer(String rendererClass, Cell cell) throws RendererUnavailable {
       try {

            Class<? extends CellRenderer> clazz = rendererClasses.get(rendererClass);
            Constructor con = clazz.getConstructor(Cell.class);
            return (CellRenderer) con.newInstance(cell);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Failed to created Renderer because ", e);
            e.printStackTrace();
            throw new RendererUnavailable(rendererClass);
        }
    }

    public class RendererUnavailable extends Exception {
        public RendererUnavailable(String msg) {
            super(msg);
        }
    }
}
