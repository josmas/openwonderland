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
package org.jdesktop.wonderland.client.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.PrimaryServerListener;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.help.HelpInfo;

/**
 * The system of menus and menu items for the help in a WL client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpSystem implements PrimaryServerListener {

    private static Logger logger = Logger.getLogger(HelpSystem.class.getName());
    private JMenu helpMenu = null;
    
    /** Constructor */
    public HelpSystem() {
        // Create the root menu. This will be available immediately so the
        // main frame can add it right away. It won't get populated until the
        // session becomes active.
        helpMenu = new JMenu("Help");

        // Listen for connections to the primary server. Whenever there is a
        // new primary server, we want to refresh the Help menu
        LoginManager.addPrimaryServerListener(this);
    }
    
    /**
     * Returns the root JMenu for the help system.
     * 
     * @return The base JMenu help menu
     */
    public JMenu getHelpJMenu() {
        return helpMenu;
    }

    /**
     * {@inheritDoc}
     */
    public void primaryServer(ServerSessionManager server) {
        // If the primary server is null, then simply ignore. Perhaps we should
        // remove the menu items whenver we get a 'null' primary server?
        if (server != null) {
            reload(server);
        }
    }

    /**
     * Builds the menu system from scratch.
     */
    private synchronized void reload(ServerSessionManager manager) {

        // Fetch the menu structure, if it does not exist, return and leave
        // the menu empty. We need to clear out the menu structure in the AWT
        // Event Thread.
        HelpInfo helpInfo = HelpUtils.fetchHelpInfo(manager);
        if (helpInfo == null) {
            logger.warning("Unable to fetch Help Info from server");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    helpMenu.removeAll();
                }
            });
            return;
        }
        
        // Generate the new menu structure. We need to update the menu structure
        // in the AWT Event Thread.
        final HelpInfo.HelpMenuEntry entries[] = helpInfo.getHelpEntries();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                helpMenu.removeAll();
                buildJMenu(helpMenu, entries);
            }
        });
    }
    
    /**
     * Builds the chilren beneath a JMenu representing a folder (and any
     * sub-items) recursively.
     */
    private void buildJMenu(JMenu menu, HelpInfo.HelpMenuEntry[] entries) {
        // If there are no entries to add, then simply return.
        if (entries == null) {
            return;
        }
        
        // Loop through all of the entries and add them to the given menu.
        // Recursively add children to sub-menus too.
        for (HelpInfo.HelpMenuEntry entry : entries) {
            // If the entry is a "folder" (i.e. a sub-menu), create a new JMenu
            // to put its children in, add the JMenu to the current menu and
            // recursively add its children.
            if (entry instanceof HelpInfo.HelpMenuFolder) {
                HelpInfo.HelpMenuFolder folder = (HelpInfo.HelpMenuFolder)entry;
                JMenu subMenu = new JMenu(folder.name);
                menu.add(subMenu);
                buildJMenu(subMenu, folder.entries);
            }
            else if (entry instanceof HelpInfo.HelpMenuItem) {
                // If we are just adding a item, then create a new JMenuItem.
                // We need to create a listener to act when the menu item is
                // selected.
                HelpInfo.HelpMenuItem item = (HelpInfo.HelpMenuItem)entry;
                final String uri = item.helpURI;
                JMenuItem menuItem = new JMenuItem(item.name);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            WebBrowserLauncher.openURL(uri);
                        } catch (Exception excp) {
                            logger.log(Level.WARNING, "Unable to launch URL " +
                                    uri, excp);
                        }
                    }
                });
                menu.add(menuItem);
            }
            else if (entry instanceof HelpInfo.HelpMenuSeparator) {
                // If we are adding a separator, then do so.
                menu.addSeparator();
            }
        }
    }
}

