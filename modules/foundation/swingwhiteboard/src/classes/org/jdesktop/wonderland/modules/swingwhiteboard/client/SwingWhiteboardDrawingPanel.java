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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * All drawing must take place in the paint method of the 
 * drawing panel.
 */

public class SwingWhiteboardDrawingPanel extends JPanel {

    public enum Action {

        NULL, ERASE, DRAW_LINE
    };
    private Action action = Action.NULL;
    private Color penColor = Color.BLACK;
    private int x0,  y0;
    private int x1,  y1;

    public void setEraseAction() {
        action = Action.ERASE;
    }

    public void setDrawLineAction(int x0, int y0, int x1, int y1) {
        action = Action.DRAW_LINE;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public void setPenColor(Color penColor) {
        this.penColor = penColor;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        switch (action) {

            case ERASE:
                g2d.setBackground(Color.WHITE);
                g2d.clearRect(0, 0, getWidth(), getHeight());
                break;

            case DRAW_LINE:
                g.setColor(penColor);
                g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x0, y0, x1, y1);
		break;
        }
        action = Action.NULL;
    }
}
