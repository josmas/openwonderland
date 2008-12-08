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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
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
                String[] animations = avatar.getAnimations();

                JMenu animationMenu = new JMenu("Avatar Actions");

                ButtonGroup buttonGroup = new ButtonGroup();

                for(int i=0; i<animations.length; i++) {
                    JRadioButtonMenuItem m = new JRadioButtonMenuItem(animations[i]);
                    buttonGroup.add(m);
                    animationMenu.add(m);
                    m.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            avatar.setAnimation(((JMenuItem)e.getSource()).getText());
                        }
                    });
                }

                JMenuItem startMI = new JMenuItem("Start Animation");
                startMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        avatar.triggerActionStart();
                    }
                });

                JMenuItem stopMI = new JMenuItem("Stop Animation");
                stopMI.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        avatar.triggerActionStop();
                    }
                });

                animationMenu.add(startMI);
                animationMenu.add(stopMI);

                JmeClientMain.getFrame().addToEditMenu(animationMenu);
            }
        });
    }

}
