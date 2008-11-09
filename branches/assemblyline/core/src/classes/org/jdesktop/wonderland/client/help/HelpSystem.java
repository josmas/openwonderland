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

package org.jdesktop.wonderland.client.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.common.help.HelpInfo;

/**
 * The system of menus and menu items for the help in a WL client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpSystem {
    
    /* The root help menu */
    private JMenu helpMenu = null;
    
    /** Constructor */
    public HelpSystem() {
        this.reload();
    }
    
    /**
     * Returns the root JMenu for the help system.
     * 
     * @return The base JMenu help menu
     */
    public JMenu getHelpJMenu() {
        return this.helpMenu;
    }
    
    /**
     * Builds the menu system from scratch
     */
    private void reload() {      
        /* Fetch the menu structure, if it does not exist, return an empty menu */
        HelpInfo helpInfo = HelpUtils.fetchHelpInfo();
        if (helpInfo == null) {
            this.helpMenu = new JMenu("Help");
            return;
        }
        
        /* Generate the help menu structure */
        HelpInfo.HelpMenuEntry entries[] = helpInfo.getHelpEntries();
        this.helpMenu = this.buildJMenu("Help", entries);
    }
    
    /**
     * Builds and returns a JMenu representing a folder (and any sub-items)
     */
    private JMenu buildJMenu(String name, HelpInfo.HelpMenuEntry[] entries) {
        JMenu menu = new JMenu(name);
        if (entries == null) {
            return menu;
        }
        
        /* Loop through all of the entries and recursively create submenus */
        for (HelpInfo.HelpMenuEntry entry : entries) {
            if (entry instanceof HelpInfo.HelpMenuFolder) {
                HelpInfo.HelpMenuFolder folder = (HelpInfo.HelpMenuFolder)entry;
                    menu.add(this.buildJMenu(folder.name, folder.entries));
            }
            else if (entry instanceof HelpInfo.HelpMenuItem) {
                HelpInfo.HelpMenuItem item = (HelpInfo.HelpMenuItem)entry;
                final String uri = item.helpURI;
                JMenuItem menuItem = new JMenuItem(item.name);
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            System.out.println("URL: " + uri);
                            WebBrowserLauncher.openURL(uri);
                        } catch (Exception excp) {
                            excp.printStackTrace();
                        }
                    }
                });
                menu.add(menuItem);
            }
            else if (entry instanceof HelpInfo.HelpMenuSeparator) {
                menu.addSeparator();
            }
        }
        return menu;
    }
}

