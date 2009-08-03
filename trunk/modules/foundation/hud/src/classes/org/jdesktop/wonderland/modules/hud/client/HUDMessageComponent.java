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
import org.jdesktop.wonderland.client.hud.HUDMessage;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 * A dialog for displaying a message on the HUD
 * @author nsimpson
 */
public class HUDMessageComponent extends HUDComponent2D implements HUDMessage {

    private static final Logger logger = Logger.getLogger(HUDMessageComponent.class.getName());
    private HUDMessageImpl dialogImpl;
    protected String message;
    protected int rows = 1;

    public HUDMessageComponent() {
        super();
        setDecoratable(false);
        initializeDialog();
    }

    public HUDMessageComponent(String message) {
        this();
        setMessage(message);
    }

    public HUDMessageComponent(String message, int rows) {
        this(message);
        setRows(rows);
    }

    /**
     * Create the dialog components
     */
    private void initializeDialog() {
        if (dialogImpl == null) {
            component = dialogImpl = new HUDMessageImpl();
            Dimension size = dialogImpl.getPreferredSize();
            setBounds(0, 0, size.width, size.height);
            JmeClientMain.getFrame().getCanvas3DPanel().add(component);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setMessage(String message) {
        dialogImpl.setText(message);
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {
        return dialogImpl.getText();
    }

    /**
     * {@inheritDoc}
     */
    public void setRows(int rows) {
        dialogImpl.setRows(rows);
    }

    /**
     * {@inheritDoc}
     */
    public int getRows() {
        return dialogImpl.getRows();
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
