/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.client;

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * plugin for the mouse cursor change
 * 
 * @author Abhishek Upadhyay
 */
@Plugin
public class MouseCursorPlugin extends BaseClientPlugin {

    private static CellHighlightManager cellEnterExitManager = null;
    private static MouseCursorManager mouseCursorManager = null;

    @Override
    public void initialize(ServerSessionManager loginInfo) {
        cellEnterExitManager = new CellHighlightManager();
        mouseCursorManager = new MouseCursorManager();
        super.initialize(loginInfo);
    }

    @Override
    protected void activate() {
        ClientContextJME.getInputManager().addGlobalEventListener(cellEnterExitManager);
        ClientContextJME.getInputManager().addGlobalEventListener(mouseCursorManager);
    }

    @Override
    protected void deactivate() {
        ClientContextJME.getInputManager().removeGlobalEventListener(cellEnterExitManager);
        ClientContextJME.getInputManager().addGlobalEventListener(mouseCursorManager);
    }

    public static MouseCursorManager getMouseCursorManager() {
        return mouseCursorManager;
    }
}
