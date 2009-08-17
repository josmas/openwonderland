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

import java.lang.reflect.Array;
import java.util.logging.Logger;
import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;

/**
 * Container for whiteboard cell data
 *
 * @author nsimpson,deronj
 */

@ExperimentalAPI
public class SwingWhiteboardCellClientState extends App2DCellClientState {

    private static final Logger logger = Logger.getLogger(SwingWhiteboardCellClientState.class.getName());
    
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final int GROWTH_DELTA = 256;
    
    private int dimensions[] = {0, 4};
    private int actions[][] = (int[][])Array.newInstance(int.class, dimensions);
    private int index = 0;
    private int preferredWidth = DEFAULT_WIDTH;
    private int preferredHeight = DEFAULT_HEIGHT;
    private String checksum;
    
    public SwingWhiteboardCellClientState () {
        this(null);
    }
    
    public SwingWhiteboardCellClientState (Vector2f pixelScale) {
	super(pixelScale);
    }
    
    /*
     * Get the checksum for the whiteboard
     * @return the checksum of the whiteboard
     */
    public String getChecksum() {
        return checksum;
    }
    
    /*
     * Set the checksum of the whiteboard
     * @param checksum the checksum of the whiteboard
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /*
     * Set the preferred width of the whiteboard
     * @param preferredWidth the preferred width in pixels
     */
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    /*
     * Get the preferred width of the whiteboard
     * @return the preferred width, in pixels
     */
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    /*
     * Set the preferred height of the whiteboard
     * @param preferredHeight the preferred height, in pixels
     */
    public void setPreferredHeight(int preferredHeight) {
        this.preferredHeight = preferredHeight;
    }
    
    /*
     * Get the preferred height of the whiteboard
     * @return the preferred height, in pixels
     */
    public int getPreferredHeight() {
        return preferredHeight;
    }
    
    /**
     * Add a message
     * @param msg the message to add
     */
    /* TODO: delete: I don't think this is used
    public void addMessage(WhiteboardCompoundCellMessage msg) {
        int cellID = (int)msg.getCellID().hashCode();
        int action = msg.getAction().ordinal();
        LinkedList<Point> positions = msg.getPositions();
        
        int newPositions = positions.size();
        
        if ((index + newPositions) > actions.length) {
            // need to grow action array
            double delta = (index + newPositions) - actions.length;
            int factor = (int)Math.ceil(delta/GROWTH_DELTA);
            delta = factor * GROWTH_DELTA;
            int dims[] = { (int)(actions.length + delta), 4 };
            int[][] newactions = (int[][])Array.newInstance(int.class, dims);
            System.arraycopy(actions, 0, newactions, 0, actions.length);
            actions = newactions;
        }
        
        Iterator<Point> iter = positions.iterator();
        while (iter.hasNext()) {
            Point point = (Point)iter.next();
            actions[index][0] = cellID;
            actions[index][1] = action;
            actions[index][2] = (int)point.getX();
            actions[index][3] = (int)point.getY();
            index++;
        }
        logger.info("whiteboard contains " + index + " actions");
    }
    */
    
    /**
     * Get the list of messages
     */
    /* TODO: delete: I don't think this is used
    public LinkedList<WhiteboardCompoundCellMessage> getMessages() {
        LinkedList<WhiteboardCompoundCellMessage> list = new LinkedList<WhiteboardCompoundCellMessage>();
        
        if (actions.length > 0) {
            for (int i = 0;i < actions.length;i++) {
                WhiteboardCompoundCellMessage msg = new WhiteboardCompoundCellMessage(clientID, 
										      new CellID(actions[i][0]));
                msg.setAction(Action.values()[actions[i][1]]);
                msg.setPosition(new Point(actions[i][2], actions[i][3]));
                
                list.addLast(msg);
            }
        }
        
        return list;
    }
    */
}
