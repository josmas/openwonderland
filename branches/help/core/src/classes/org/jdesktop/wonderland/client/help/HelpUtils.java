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

import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import org.jdesktop.wonderland.common.help.HelpInfo;

/**
 * A collection of static utility routines to help with the help system on the
 * client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpUtils {

    /* The base URL of the web server */
    private static final String BASE_URL = "http://localhost:8080/wonderland-web-help/help/";
    
    /* The error logger for this class */
    private static Logger logger = Logger.getLogger(HelpUtils.class.getName());
    
    /**
     * Asks the web server for the help menu items.
     */
    public static HelpInfo fetchHelpInfo() {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL(BASE_URL + "info/get");
            return HelpInfo.decode(new InputStreamReader(url.openStream()));
        } catch (java.lang.Exception excp) {
            /* Log an error and return null */
            logger.log(Level.WARNING, "[HELP] FETCH HELP INFO Failed", excp);
            return null;
        }
    }
    
    /**
     * Returns a menu representing the help system
     */
    public static JMenu getHelpMenu() {
        /* Fetch the menu structure, if it does not exist, return an empty menu */
        HelpInfo helpInfo = HelpUtils.fetchHelpInfo();
        JMenu helpMenu = new JMenu("Help");
        if (helpInfo == null) {
            return helpMenu;
        }
        
        /* Iterate through all of the entries and create the help menu structure */
        HelpInfo.HelpMenuEntry entries[] = helpInfo.getHelpEntries();
        for (HelpInfo.HelpMenuEntry entry : entries) {
            // TODO
        }
        return helpMenu;
    }
}
