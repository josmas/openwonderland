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

import com.jme.math.Vector3f;
import imi.character.CharacterSteeringHelm;
import imi.character.statemachine.GameContext;
import imi.character.steering.GoTo;
import imi.loaders.repository.Repository;
import imi.utils.instruments.DefaultInstrumentation;
import imi.utils.instruments.Instrumentation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager.ViewManagerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.modules.avatarbase.client.AvatarConfigManager;

/**
 *
 * @author paulby
 */
public class AvatarPlugin implements ClientPlugin {

    private WeakReference<AvatarTestPanel> testPanelRef = null;
    private Instrumentation instrumentation;

    public void initialize(ServerSessionManager loginManager) {
        // Set the base URL
        String serverHostAndPort = loginManager.getServerNameAndPort();
        String baseURL = "wla://avatarbaseart@" + serverHostAndPort + "/";

        WorldManager worldManager = ClientContextJME.getWorldManager();
        worldManager.addUserData(Repository.class, new Repository(worldManager, baseURL, ClientContext.getUserDirectory("AvatarCache")));

        ClientContextJME.getAvatarRenderManager().registerRenderer(AvatarImiJME.class);

        // Workaround to guarntee that the webdav module has been initialized
        loginManager.addLifecycleListener(new SessionLifecycleListener() {

            public void sessionCreated(WonderlandSession session) {
            }

            public void primarySession(WonderlandSession session) {
                // We need a primary session, so wait for it
                AvatarConfigManager.getAvatarConigManager().addServer(session.getSessionManager());
            }
        });


        ClientContextJME.getViewManager().addViewManagerListener(new ViewManagerListener() {

            public void primaryViewCellChanged(ViewCell oldViewCell, ViewCell newViewCell) {
                if (oldViewCell != null) {
                    Logger.getAnonymousLogger().severe("TODO Handle primary View Change");
                    return;
                }

                CellRenderer rend = newViewCell.getCellRenderer(Cell.RendererType.RENDERER_JME);
                if (!(rend instanceof AvatarImiJME)) {
                    return;
                }

                final AvatarImiJME avatar = (AvatarImiJME) rend;

                if (testPanelRef == null || testPanelRef.get() == null) {
                    // Do nothing
                } else {
                    testPanelRef.get().setAvatarCharactar(avatar.getAvatarCharacter());
                }

                JMenuItem avatarControlsMI = new JMenuItem("Avatar Controls");
                avatarControlsMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (testPanelRef == null || testPanelRef.get() == null) {
                            AvatarTestPanel test = new AvatarTestPanel();
                            JFrame f = new JFrame("Avatar Controls");
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
                JmeClientMain.getFrame().addToWindowMenu(avatarControlsMI, 0);

                JMenuItem avatarMI = new JMenuItem("Avatar...");
                avatarMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        ViewCell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
                        if (cell instanceof AvatarCell) {
                            AvatarImiJME rend = (AvatarImiJME) ((AvatarCell) cell).getCellRenderer(ClientContext.getRendererType());
                            AvatarConfigFrame f = new AvatarConfigFrame(rend);
                            f.setTitle("Avatar");
                            f.setVisible(true);
                        }
                    }
                });
                JmeClientMain.getFrame().addToEditMenu(avatarMI, 0);

                JMenuItem avatarSettingsMI = new JMenuItem("Avatar Settings...");
                avatarSettingsMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        AvatarInstrumentation in = new AvatarInstrumentation(instrumentation);
                        in.setVisible(true);
                    }
                });
                JmeClientMain.getFrame().addToEditMenu(avatarSettingsMI, 1);

                JMenuItem startingLocationMI = new JMenuItem("Starting Location");
                startingLocationMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        GameContext context = avatar.getAvatarCharacter().getContext();
                        CharacterSteeringHelm helm = avatar.getAvatarCharacter().getContext().getSteering();
                        helm.addTaskToTop(new GoTo(new Vector3f(0, 0, 0), context));
                        helm.setEnable(true);
                    }
                });
                JmeClientMain.getFrame().addToPlacemarksMenu(startingLocationMI, 0);

                AvatarConfigManager.getAvatarConigManager().setViewCell(newViewCell);
            }
        });

        instrumentation = new DefaultInstrumentation(ClientContextJME.getWorldManager());
    }
}
