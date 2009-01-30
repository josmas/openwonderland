/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.common.cell.messages;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 * A message class to add a component to the cell, given the components server-
 * side state
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellAddComponentMessage extends CellMessage {

//    private CellComponentServerState serverState = null;

//    public CellAddComponentMessage(CellID cellID, CellComponentServerState serverState) {
//        super(cellID);
//        this.serverState = serverState;
//    }
//
//    public CellComponentServerState getServerState() {
//        return serverState;
//    }
//
//    public void setServerState(CellComponentServerState serverState) {
//        this.serverState = serverState;
//    }

    private String componentClassName = null;

    public CellAddComponentMessage(CellID cellID, String name) {
        super(cellID);
        componentClassName = name;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }

    
}
