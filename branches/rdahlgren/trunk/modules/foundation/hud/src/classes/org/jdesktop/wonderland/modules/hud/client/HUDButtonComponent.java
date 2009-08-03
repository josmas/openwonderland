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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import org.jdesktop.wonderland.client.hud.HUDButton;
import org.jdesktop.wonderland.client.jme.JmeClientMain;

/**
 *
 * @author nsimpson
 */
public class HUDButtonComponent extends HUDComponent2D implements HUDButton {

    private List<ActionListener> listeners;
    private JButton button;

    public HUDButtonComponent() {
        super();
        component = button = initializeButton("");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                notifyListeners(event);
            }
        });
    }

    public HUDButtonComponent(String label) {
        this();
        setLabel(label);
    }

    public void setLabel(String label) {
        if (button != null) {
            button.setText(label);
        }
    }

    public String getLabel() {
        String label = null;
        if (button != null) {
            label = button.getText();
        }
        return label;
    }

    private JButton initializeButton(String label) {
        JButton b = new JButton(label);
        Dimension size = b.getPreferredSize();
        setBounds(0, 0, size.width, size.height);
        JmeClientMain.getFrame().getCanvas3DPanel().add(b);
        return b;
    }

    public void addActionListener(ActionListener listener) {
        if (listeners == null) {
            listeners = Collections.synchronizedList(new ArrayList());
        }
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(ActionEvent event) {
        if (listeners != null) {
            Iterator<ActionListener> iter = listeners.iterator();
            while (iter.hasNext()) {
                ActionListener notifiee = iter.next();
                notifiee.actionPerformed(event);
            }
        }
    }
}
