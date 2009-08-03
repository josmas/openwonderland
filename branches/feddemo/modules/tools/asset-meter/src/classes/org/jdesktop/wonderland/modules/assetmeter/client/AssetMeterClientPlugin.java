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
package org.jdesktop.wonderland.modules.assetmeter.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin to register the asset meter on the Tools menu.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class AssetMeterClientPlugin implements ClientPlugin {

    private WeakReference<AssetMeterJFrame> assetMeterJFrameRef = null;

    public void initialize(ServerSessionManager loginInfo) {
        // First create the asset meter frame and keep a weak reference to it
        // so that it gets garbage collected
        JFrame assetMeterJFrame = new AssetMeterJFrame();
        assetMeterJFrame.setSize(350, 200);
        assetMeterJFrameRef = new WeakReference(assetMeterJFrame);

        // Add the Asset Meter as a checkbox menu item to the Tools menu as a
        // Checkbox menu item. If it is selected, then show it or hide it. Keep
        // the frame in a weak reference.
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Asset Meter", true);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame assetMeterJFrame = assetMeterJFrameRef.get();
                assetMeterJFrame.setVisible(item.isSelected());
            }
        });

        // Listener for when the Asset Meter frame is closed by itself and
        // uncheck the checkbox item in the menu
        assetMeterJFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                item.setSelected(false);
            }
        });

        // Add the item to the tools menu and make the Asset Meter visible
        // by default initially.
        JmeClientMain.getFrame().addToToolMenu(item);
        assetMeterJFrame.setVisible(true);
    }
}