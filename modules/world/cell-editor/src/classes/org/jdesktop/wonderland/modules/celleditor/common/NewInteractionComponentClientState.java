/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.common;

import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;

/**
 * client state for new interactions
 * 
 * @author Abhishek Upadhyay
 */
public class NewInteractionComponentClientState extends CellComponentClientState {

    private boolean highlightEnable = false;
    private float red = 1;
    private float green = 1;
    private float blue = 0;
    private boolean cursorEnable = false;
    private boolean standardCursor = true;
    private String cursorFilePath = "";

    public boolean isHighlightEnable() {
        return highlightEnable;
    }

    public void setHighlightEnable(boolean highlightEnable) {
        this.highlightEnable = highlightEnable;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public boolean isCursorEnable() {
        return cursorEnable;
    }

    public void setCursorEnable(boolean cursorEnable) {
        this.cursorEnable = cursorEnable;
    }

    public boolean isStandardCursor() {
        return standardCursor;
    }

    public void setStandardCursor(boolean standardCursor) {
        this.standardCursor = standardCursor;
    }

    public String getCursorFilePath() {
        return cursorFilePath;
    }

    public void setCursorFilePath(String cursorFilePath) {
        this.cursorFilePath = cursorFilePath;
    }
}
