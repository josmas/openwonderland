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
package org.jdesktop.wonderland.modules.contentrepo.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.content.ContentBrowserManager;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.BrowserAction;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.ContentBrowserListener;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;

/**
 *
 * @author jkaplan
 */
@Plugin
public class BrowserPlugin implements ClientPlugin {
    private static Logger logger = Logger.getLogger(BrowserPlugin.class.getName());
    private BrowserFrame frame;
    private WeakReference<ContentBrowserJDialog> browserDialogRef = null;

    public void initialize(final ServerSessionManager loginInfo) {

        Action launchAction = new AbstractAction("Content Browser") {
            public synchronized void actionPerformed(ActionEvent e) {
                if (frame == null) {
                    ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
                    ContentRepository repo = registry.getRepository(loginInfo);
                    frame = new BrowserFrame(repo);
                }

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        frame.setVisible(true);
                    }
                });
            }
        };
        JmeClientMain.getFrame().addToToolsMenu(new JMenuItem(launchAction), 5);

        // Add the Palette menu and the Cell submenu and dialog that lets users
        // create new cells.
        JMenuItem browserItem = new JMenuItem("Content Browser (New)");
        browserItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ContentBrowserJDialog contentBrowserFrame;
                if (browserDialogRef == null || browserDialogRef.get() == null) {
                    contentBrowserFrame = new ContentBrowserJDialog(loginInfo);
                    contentBrowserFrame.setModal(false);
                    contentBrowserFrame.setActionName(BrowserAction.OK_ACTION, "Create");
                    contentBrowserFrame.setActionName(BrowserAction.CANCEL_ACTION, "Cancel");

                    contentBrowserFrame.addContentBrowserListener(new ContentBrowserListener() {
                        public void okAction(String uri) {
                            // Figure out what the file extension is from the uri, looking for
                            // the final '.'.
                            int index = uri.lastIndexOf(".");
                            if (index == -1) {
                                logger.warning("Could not find extension for " + uri);
                                return;
                            }
                            String extension = uri.substring(index + 1);

                            // Next look for a cell type that handles content with this file
                            // extension and create a new cell with it.
                            CellRegistry registry = CellRegistry.getCellRegistry();
                            Set<CellFactorySPI> factories = registry.getCellFactoriesByExtension(extension);
                            if (factories == null) {
                                logger.warning("Could not find cell factory for " + extension);
                            }
                            CellFactorySPI factory = factories.iterator().next();

                            // Create the cell, inject the content uri
                            Properties props = new Properties();
                            props.put("content-uri", uri);
                            CellServerState state = factory.getDefaultCellServerState(props);

                            // Create the new cell at a distance away from the avatar
                            try {
                                CellUtils.createCell(state, 5.0f);
                            } catch (CellCreationException excp) {
                                logger.log(Level.WARNING, "Unable to create cell for uri " + uri, excp);
                            }
                        }

                        public void cancelAction() {
                            // Do nothing
                        }
                    });
                    browserDialogRef = new WeakReference(contentBrowserFrame);
                }
                else {
                    contentBrowserFrame = browserDialogRef.get();
                }

                if (contentBrowserFrame.isVisible() == false) {
                    contentBrowserFrame.setVisible(true);
                }
            }
        });
        JmeClientMain.getFrame().addToToolsMenu(browserItem, 6);

        // Register the content browser frame with the registry of such panels
        ContentBrowserManager manager = ContentBrowserManager.getContentBrowserManager();
        manager.setDefaultContentBrowser(new ContentBrowserJDialog(loginInfo));
    }

}
