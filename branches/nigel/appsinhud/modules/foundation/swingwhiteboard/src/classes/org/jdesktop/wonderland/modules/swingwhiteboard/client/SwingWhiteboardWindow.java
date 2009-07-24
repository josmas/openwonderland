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
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import java.awt.Color;
import java.awt.Point;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import com.jme.math.Vector2f;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.math.BigInteger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardAction;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCellMessage;
import org.jdesktop.wonderland.modules.swingwhiteboard.common.WhiteboardCommand.Command;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.appbase.client.cell.App2DCell;

/**
 *
 * The window for the whiteboard.
 *
 * @author deronj
 */
@ExperimentalAPI
public class SwingWhiteboardWindow extends WindowSwing {

    private SwingWhiteboardComponent commComponent;
    private WhiteboardCellMessage msg;

    SwingWhiteboardDrawingPanel drawingPanel;
    private int penX,  penY;

    private SwingWhiteboardCell cell;

    private CellID cellID;

    /**
     * Create a new instance of SwingWhiteboardWindow.
     *
     * @param cell The cell in which the whiteboard app is displayed.
     * @param app The whiteboard app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param commComponent The communications component for communicating with the server.
     */
    public SwingWhiteboardWindow(SwingWhiteboardCell cell, App2D app, int width, int height, 
                                 boolean topLevel, Vector2f pixelScale, 
                                 SwingWhiteboardComponent commComponent)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.cell = cell;
        this.commComponent = commComponent;

        setTitle("Swing Whiteboard Window");

        SwingWhiteboardPanel panel = new SwingWhiteboardPanel(this);

	// Parent to Wonderland main window for proper focus handling 
        JmeClientMain.getFrame().getCanvas3DPanel().add(panel);

        setComponent(panel);

        cellID = cell.getCellID();

        drawingPanel = panel.getDrawingPanel();
        drawingPanel.addMouseMotionListener(new MouseMotionListener() {

            public void mouseMoved(MouseEvent e) {
                move(e.getPoint());
            }

            public void mouseDragged(MouseEvent e) {
                drag(e.getPoint());
            }
        });
    }

    /**
     * Called from the GUI to set the pen color.
     */
    public void setPenColor(Color color) {
	drawingPanel.setPenColor(color);

        // Notify other clients
        msg = new WhiteboardCellMessage(cell.getClientID(), cellID, WhiteboardAction.SET_COLOR, color);
        commComponent.sendMessage(msg);
    }

    /**
     * Called from the GUI to erase the drawin panel.
     */
    public void erase() {

	drawingPanel.setEraseAction();
	drawingPanel.repaint();

        // Notify other clients
        msg = new WhiteboardCellMessage(cell.getClientID(), cellID, WhiteboardAction.EXECUTE_COMMAND,  
                                        Command.ERASE);
        commComponent.sendMessage(msg);
    }

    /**
     * Move the pen.
     */
    void move(Point loc) {
        penX = loc.x;
        penY = loc.y;

        // notify other clients
        msg = new WhiteboardCellMessage(cell.getClientID(), cellID, WhiteboardAction.MOVE_TO, loc);
        commComponent.sendMessage(msg);
    }

    /**
     * Drag the pen.
     */
    void drag(Point loc) {

	drawingPanel.setDrawLineAction(penX, penY, loc.x, loc.y);
	drawingPanel.repaint();
        penX = loc.x;
        penY = loc.y;

        // notify other clients
        msg = new WhiteboardCellMessage(cell.getClientID(), cellID,
                                        WhiteboardAction.DRAG_TO, loc);
        commComponent.sendMessage(msg);
    }
}
