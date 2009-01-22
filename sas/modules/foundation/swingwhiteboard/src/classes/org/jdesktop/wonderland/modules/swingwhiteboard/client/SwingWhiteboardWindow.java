/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.swingwhiteboard.client;

import java.awt.Color;
import java.awt.Point;
import org.jdesktop.wonderland.modules.appbase.client.App;
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

// TODO
import org.jdesktop.wonderland.modules.appbase.client.DrawingSurfaceBufferedImage;


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

    private JPanel drawingPanel;
    private Color penColor = Color.BLACK;
    private int penX,  penY;

    DrawingSurfaceBufferedImage surface;


    /**
     * Create a new instance of SwingWhiteboardWindow.
     *
     * @param app The whiteboard app which owns the window.
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param topLevel Whether the window is top-level (e.g. is decorated) with a frame.
     * @param pixelScale The size of the window pixels.
     * @param commComponent The communications component for communicating with the server.
     */
    public SwingWhiteboardWindow(App app, int width, int height, boolean topLevel, Vector2f pixelScale,
            SwingWhiteboardComponent commComponent)
            throws InstantiationException {
        super(app, width, height, topLevel, pixelScale);
        this.commComponent = commComponent;

        setTitle("Swing Whiteboard Window");

	surface = new DrawingSurfaceBufferedImage(width, height);

        SwingWhiteboardPanel panel = new SwingWhiteboardPanel(this);
        JmeClientMain.getFrame().getCanvas3DPanel().add(panel);
        setComponent(panel);

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
        System.err.println("Pen color = " + color);
        penColor = color;

        // Notify other clients
        msg = new WhiteboardCellMessage(getClientID(app), app.getCell().getCellID(),
                WhiteboardAction.SET_COLOR, color);
        commComponent.sendMessage(msg);
    }

    /**
     * Called from the GUI to erase the drawin panel.
     */
    public void erase() {
        System.err.println("erase");

	/**/
        Graphics2D gswing = (Graphics2D) drawingPanel.getGraphics();
        gswing.setBackground(Color.WHITE);
        gswing.clearRect(0, 0, drawingPanel.getWidth(), drawingPanel.getHeight());
	//	drawingPanel.repaint();
	/**/


	final DrawingSurfaceBufferedImage.DirtyTrackingGraphics g = 
			(DrawingSurfaceBufferedImage.DirtyTrackingGraphics) surface.getGraphics();
	g.executeAtomic(new Runnable () {
	    public void run () {
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, surface.getWidth(), surface.getHeight());
		g.addDirtyRectangle(0, 0, surface.getWidth(), surface.getHeight());
	    }
	});

        // Notify other clients
        msg = new WhiteboardCellMessage(getClientID(app), app.getCell().getCellID(),
                WhiteboardAction.EXECUTE_COMMAND, Command.ERASE);
        commComponent.sendMessage(msg);
    }

    /**
     * Move the pen.
     */
    void move(Point loc) {
        penX = loc.x;
        penY = loc.y;

        // notify other clients
        WhiteboardCellMessage msg = new WhiteboardCellMessage(getClientID(app), app.getCell().getCellID(),
                WhiteboardAction.MOVE_TO, loc);
        commComponent.sendMessage(msg);
    }

    /**
     * Drag the pen.
     */
    void drag(Point loc) {

	/**/
        Graphics2D gswing = (Graphics2D) drawingPanel.getGraphics();
        gswing.setColor(penColor);
        gswing.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gswing.drawLine(penX, penY, loc.x, loc.y);
        penX = loc.x;
        penY = loc.y;
	//	drawingPanel.repaint();
	/**/

	final DrawingSurfaceBufferedImage.DirtyTrackingGraphics g = 
			(DrawingSurfaceBufferedImage.DirtyTrackingGraphics) surface.getGraphics();

	final Point loc2 = loc;

	g.executeAtomic(new Runnable () {
	    public void run () {
		g.setColor(penColor);
		g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(penX, penY, loc2.x, loc2.y);
		penX = loc2.x;
		penY = loc2.y;
		// TODO: must damage everything
		System.err.println("Add dirty rect, wh = " +
				   surface.getWidth() + ", " + surface.getHeight());
		g.addDirtyRectangle(0, 0, surface.getWidth(), surface.getHeight());
	    }
	});

        // notify other clients
        WhiteboardCellMessage msg = new WhiteboardCellMessage(getClientID(app), app.getCell().getCellID(),
                WhiteboardAction.DRAG_TO, loc);
        commComponent.sendMessage(msg);
    }

    /**
     * Return the client id of this window's cell.
     */
    private BigInteger getClientID(App app) {
        return ((SwingWhiteboardCell) (app.getCell())).getClientID();
    }
}
