/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.server;

import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.modules.celleditor.common.NewInteractionComponentClientState;
import org.jdesktop.wonderland.modules.celleditor.common.NewInteractionComponentServerState;
import org.jdesktop.wonderland.server.cell.CellComponentMO;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 * server side class for new interactions
 * 
 * @author Abhishek Upadhyay
 */
public class NewInteractionComponentMO extends CellComponentMO {

    private boolean highlightEnable = false;
    private float red = 1;
    private float green = 1;
    private float blue = 0;
    private boolean cursorEnable = false;
    private boolean standardCursor = true;
    private String cursorFilePath = "";

    public NewInteractionComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    public CellComponentClientState getClientState(CellComponentClientState state,
            WonderlandClientID clientID,
            ClientCapabilities capabilities) {
        if (state == null) {
            state = new NewInteractionComponentClientState();
        }

        ((NewInteractionComponentClientState) state).setHighlightEnable(highlightEnable);
        ((NewInteractionComponentClientState) state).setRed(red);
        ((NewInteractionComponentClientState) state).setGreen(green);
        ((NewInteractionComponentClientState) state).setBlue(blue);
        ((NewInteractionComponentClientState) state).setCursorEnable(cursorEnable);
        ((NewInteractionComponentClientState) state).setCursorFilePath(cursorFilePath);
        ((NewInteractionComponentClientState) state).setStandardCursor(standardCursor);

        return super.getClientState(state, clientID, capabilities);
    }

    @Override
    public CellComponentServerState getServerState(CellComponentServerState state) {
        if (state == null) {
            state = new NewInteractionComponentServerState();
        }

        ((NewInteractionComponentServerState) state).setHighlightEnable(highlightEnable);
        ((NewInteractionComponentServerState) state).setRed(red);
        ((NewInteractionComponentServerState) state).setGreen(green);
        ((NewInteractionComponentServerState) state).setBlue(blue);
        ((NewInteractionComponentServerState) state).setCursorEnable(cursorEnable);
        ((NewInteractionComponentServerState) state).setCursorFilePath(cursorFilePath);
        ((NewInteractionComponentServerState) state).setStandardCursor(standardCursor);

        return super.getServerState(state);
    }

    @Override
    public void setServerState(CellComponentServerState state) {

        highlightEnable = ((NewInteractionComponentServerState) state).isHighlightEnable();
        red = ((NewInteractionComponentServerState) state).getRed();
        green = ((NewInteractionComponentServerState) state).getGreen();
        blue = ((NewInteractionComponentServerState) state).getBlue();
        cursorEnable = ((NewInteractionComponentServerState) state).isCursorEnable();
        standardCursor = ((NewInteractionComponentServerState) state).isStandardCursor();
        cursorFilePath = ((NewInteractionComponentServerState) state).getCursorFilePath();

        super.setServerState(state);
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.modules.celleditor.client.NewInteractionComponent";
    }
}
