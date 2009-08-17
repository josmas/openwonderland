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
package org.jdesktop.wonderland.modules.swingwhiteboard.common;

import java.awt.Color;
import java.awt.Point;
import java.math.BigInteger;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction.Action;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardActionType.ActionType;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;


/**
 * A Cell Message that carries whiteboard actions
 *
 * @author nsimpson
 */

@ExperimentalAPI
public class WhiteboardCellMessage extends CellMessage {
    
    private static final Logger logger = Logger.getLogger(WhiteboardCellMessage.class.getName());
    
    protected BigInteger clientID;
    protected ActionType actionType;
    protected Action action = WhiteboardAction.NO_ACTION;
    protected Point position;
    protected Command command;
    protected Color color;
    
    public WhiteboardCellMessage(BigInteger clientID, CellID cellID) {
        super(cellID);
	this.clientID = clientID;
    }
    
    public WhiteboardCellMessage(BigInteger clientID, CellID cellID, Action action) {
        super(cellID);
	this.clientID = clientID;
        this.action = action;
    }
    
    public WhiteboardCellMessage(BigInteger clientID, CellID cellID, Action action, Point position) {
        super(cellID);
	this.clientID = clientID;
        this.action = action;
        this.position = position;
    }
    
    public WhiteboardCellMessage(BigInteger clientID, CellID cellID, Action action, Command command) {
        super(cellID);
	this.clientID = clientID;
        this.action = action;
        this.command = command;
        actionType = WhiteboardActionType.COMMAND;
    }
    
    public WhiteboardCellMessage(BigInteger clientID, CellID cellID, Action action, Color color) {
        super(cellID);
	this.clientID = clientID;
        this.action = action;
        this.color = color;
        actionType = WhiteboardActionType.COLOR;
    }
    
    public void setClientID(BigInteger clientID) {
        this.clientID = clientID;
    }

    public BigInteger getClientID() {
        return clientID;
    }

    /**
     * Set the action
     * @param action the action
     */
    public void setAction(Action action) {
        this.action = action;
    }
    
    /**
     * Get the action
     * @return the action
     */
    public Action getAction() {
        return action;
    }
    
    /**
     * Get the type of the action
     * @return the action type
     */
    public ActionType getActionType() {
        return actionType;
    }
    
    /**
     * Set the (x, y) position of the page
     * @param position the (x, y) position of the page
     */
    public void setPosition(Point position) {
        this.position = position;
    }
    
    /**
     * Get the (x, y) position of the page
     * @return the (x, y) position
     */
    public Point getPosition() {
        return position;
    }
    
    /**
     * Set the active command
     * @param command the new command
     */
    public void setCommand(Command command) {
        this.command = command;
    }
    
    /**
     * Get the active command
     * @return the active command
     */
    public Command getCommand() {
        return command;
    }
    
    /**
     * Set the color
     * @param color the new color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Get the color
     * @return the current color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Get a string representation of the whiteboard cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return getClientID() + ", " + getCellID() + ", " + getActionType() + ", " + getAction() 
        + ", " + getCommand() + ", " + getColor() + ", " + getPosition();
    }
}
