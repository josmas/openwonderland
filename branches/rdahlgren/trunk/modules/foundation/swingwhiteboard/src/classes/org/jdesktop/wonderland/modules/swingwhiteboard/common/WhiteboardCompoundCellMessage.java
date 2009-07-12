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
import java.util.LinkedList;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction.Action;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.cell.CellID;


/**
 * A compounding Cell Message that coalesces messages of the same type
 * (in this case with the same action) but differ only in other properties
 * (position of the actions)
 *
 * @author nsimpson
 */

@ExperimentalAPI
public class WhiteboardCompoundCellMessage extends WhiteboardCellMessage {
    
    private static final Logger logger =
            Logger.getLogger(WhiteboardCompoundCellMessage.class.getName());
    
    private LinkedList<Point> positions = new LinkedList<Point>();
    
    public WhiteboardCompoundCellMessage(WhiteboardCellMessage src) {
	super(src.getClientID(), src.getCellID());
        this.actionType = src.actionType;        
        this.action = src.action;
        this.color = src.color;
        this.command = src.command;
        addPosition(src.getPosition());
    }
    
    public WhiteboardCompoundCellMessage(BigInteger clientID, CellID cellID, Action action) {
        super(clientID, cellID, action);
    }
    
    public WhiteboardCompoundCellMessage(BigInteger clientID, CellID cellID, Action action, Point position) {
        super(clientID, cellID, action);
        setPosition(position);
    }
    
    public WhiteboardCompoundCellMessage(BigInteger clientID, CellID cellID, Action action, Command command) {
        super(clientID, cellID, action, command);
    }
    
    public WhiteboardCompoundCellMessage(BigInteger clientID, CellID cellID, Action action, Color color) {
        super(clientID, cellID, action, color);
    }
    
    /**
     * Add an (x, y) position of an action
     * @param position the (x, y) position of the action
     */
    @Override
    public void setPosition(Point position) {
        positions.add(position);
    }
    
    /**
     * Get the last (x, y) position of the last action
     * @return the (x, y) position
     */
    @Override
    public Point getPosition() {
        return positions.getLast();
    }
    
    /**
     * Add a new position
     * @param position the (x, y) position of the action
     */
    public void addPosition(Point position) {
        positions.add(position);
    }
    
    /**
     * Set the (x, y) positions of all of the actions
     * @param a list of (x, y) positions
     */
    public void setPositions(LinkedList<Point> positions) {
        this.positions = positions;
    }
    
    /**
     * Get the (x, y) positions of all of the actions
     * @return a list of (x, y) positions
     */
    public LinkedList<Point> getPositions() {
        return positions;
    }
    
    /**
     * Get a string representation of the whiteboard cell message
     * @return a the cell message as as String
     */
    @Override
    public String toString() {
        return getCellID() + ", " + getActionType() + ", " + getAction() 
        + ", " + getCommand() + ", " + getColor() + ", " + getPositions();
    }
}
