/*
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
package org.jdesktop.wonderland.modules.hud.client;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.hud.HUDDialog;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 * A simple dialog for requesting a text value from the user.
 *
 * @author nsimpson
 */
public class HUDDialogComponent extends HUDComponent2D implements HUDDialog {

    private static final Logger logger = Logger.getLogger(HUDDialogComponent.class.getName());
    private HUDInputDialogImpl dialogImpl;

    public HUDDialogComponent() {
        super();
        initializeDialog();
    }

    public HUDDialogComponent(Cell cell) {
        this();
        setCell(cell);
    }

    public HUDDialogComponent(String label) {
        this();
        dialogImpl.setLabelText(label);
    }

    public HUDDialogComponent(String label, String value, Cell cell) {
        this(label);
        setCell(cell);
        dialogImpl.setValueText(value);
    }

    /**
     * Create the dialog components
     */
    private void initializeDialog() {
        if (dialogImpl == null) {
            dialogImpl = new HUDInputDialogImpl(null, false);
            component = (JComponent) dialogImpl.getContentPane();
            Dimension size = dialogImpl.getPreferredSize();
            setBounds(0, 0, size.width, size.height);
            JmeClientMain.getFrame().getCanvas3DPanel().add(component);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setLabelText(String text) {
        dialogImpl.setLabelText(text);
    }

    /**
     * {@inheritDoc}
     */
    public String getLabelText() {
        return dialogImpl.getLabelText();
    }

    /**
     * {@inheritDoc}
     */
    public void setValueText(String text) {
        dialogImpl.setValueText(text);
    }

    /**
     * {@inheritDoc}
     */
    public String getValueText() {
        return dialogImpl.getValueText();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        dialogImpl.addPropertyChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        dialogImpl.removePropertyChangeListener(listener);
    }
}
