/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.common;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;

/**
 * server state for new interactions
 * 
 * @author Abhishek Upadhyay
 */
@XmlRootElement(name = "new-interaction-component")
@ServerState
public class NewInteractionComponentServerState extends CellComponentServerState
        implements Serializable {

    private boolean highlightEnable = false;
    private float red = 1;
    private float green = 1;
    private float blue = 0;
    private boolean cursorEnable = false;
    private boolean standardCursor = true;
    private String cursorFilePath = "";

    @XmlElement
    public boolean isHighlightEnable() {
        return highlightEnable;
    }

    public void setHighlightEnable(boolean highlightEnable) {
        this.highlightEnable = highlightEnable;
    }

    @XmlElement
    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    @XmlElement
    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    @XmlElement
    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    @XmlElement
    public boolean isCursorEnable() {
        return cursorEnable;
    }

    public void setCursorEnable(boolean cursorEnable) {
        this.cursorEnable = cursorEnable;
    }

    @XmlElement
    public boolean isStandardCursor() {
        return standardCursor;
    }

    public void setStandardCursor(boolean standardCursor) {
        this.standardCursor = standardCursor;
    }

    @XmlElement
    public String getCursorFilePath() {
        return cursorFilePath;
    }

    public void setCursorFilePath(String cursorFilePath) {
        this.cursorFilePath = cursorFilePath;
    }

    @Override
    public String getServerComponentClassName() {
        return "org.jdesktop.wonderland.modules.celleditor.server.NewInteractionComponentMO";
    }
}
