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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;

/**
 *
 * @author paulby
 */
public class AvatarPlugin implements ClientPlugin {

    private WeakReference<AvatarTestPanel> testPanelRef = null;

    public void initialize(ServerSessionManager loginManager) {
        ClientContextJME.getAvatarRenderManager().registerRenderer(AvatarImiJME.class);

        ClientContextJME.getViewManager().addViewManagerListener(new ViewManagerListener() {
            public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
                if (oldViewCell!=null) {
                    Logger.getAnonymousLogger().severe("TODO Handle primary View Change");
                    return;
                }

                CellRenderer rend = newViewCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                if (!(rend instanceof AvatarImiJME))
                    return;

                final AvatarImiJME avatar = (AvatarImiJME)rend;

                if (testPanelRef==null || testPanelRef.get()==null) {
                    // Do nothing
                } else {
                    testPanelRef.get().setAvatarCharactar(avatar.getAvatarCharacter());
                }
                

                JMenuItem avatarControlFrameMI = new JMenuItem("Avatar Controls");
                avatarControlFrameMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (testPanelRef==null || testPanelRef.get()==null) {
                            AvatarTestPanel test = new AvatarTestPanel();
                            JFrame f = new JFrame("Test Controls");
                            f.getContentPane().add(test);
                            f.pack();
                            f.setVisible(true);
                            f.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                            test.setAvatarCharactar(avatar.getAvatarCharacter());
                            testPanelRef = new WeakReference(test);
                        } else {
                            SwingUtilities.getRoot(testPanelRef.get().getParent()).setVisible(true);
                        }
                    }
                });

                JmeClientMain.getFrame().addToEditMenu(avatarControlFrameMI);
            }
        });
    }

}
