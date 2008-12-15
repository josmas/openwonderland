/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.contentrepo.client.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;

/**
 *
 * @author jkaplan
 */
public class BrowserPlugin implements ClientPlugin {
    private BrowserFrame frame;

    public void initialize(final ServerSessionManager loginInfo) {
        Action launchAction = new AbstractAction("Content Browser...") {
            public synchronized void actionPerformed(ActionEvent e) {
                if (frame == null) {
                    ContentRepository repo =
                            ContentRepositoryRegistry.getInstance().getRepository(loginInfo);
                    frame = new BrowserFrame(repo);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                    }
                });
            }
        };

        JmeClientMain.getFrame().addToToolMenu(new JMenuItem(launchAction));
    }

}
