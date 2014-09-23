/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.client;

import java.awt.Color;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.celleditor.common.NewInteractionComponentClientState;

/**
 *
 * client side class for new interactions
 * 
 * @author Abhishek Upadhyay
 */
public class NewInteractionComponent extends CellComponent {

    private boolean highlightEnable = false;
    private float red = 1;
    private float green = 1;
    private float blue = 0;
    private boolean cursorEnable = false;
    private boolean standardCursor = true;
    private String cursorFilePath = "";

    public NewInteractionComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState state) {
        highlightEnable = ((NewInteractionComponentClientState) state).isHighlightEnable();
        red = ((NewInteractionComponentClientState) state).getRed();
        green = ((NewInteractionComponentClientState) state).getGreen();
        blue = ((NewInteractionComponentClientState) state).getBlue();
        cursorEnable = ((NewInteractionComponentClientState) state).isCursorEnable();
        cursorFilePath = ((NewInteractionComponentClientState) state).getCursorFilePath();
        standardCursor = ((NewInteractionComponentClientState) state).isStandardCursor();

        super.setClientState(state);

    }

    public boolean isHighlightEnable() {
        return highlightEnable;
    }

    public Color getHighlightColor() {
        return new Color(red, green, blue);
    }

    public boolean isCursorEnable() {
        return cursorEnable;
    }

    public boolean isStandardCursor() {
        return standardCursor;
    }

    public String getCursorFilePath() {
        return cursorFilePath;
    }
}
