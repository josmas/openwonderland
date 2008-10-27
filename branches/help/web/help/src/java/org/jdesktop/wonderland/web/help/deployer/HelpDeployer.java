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

package org.jdesktop.wonderland.web.help.deployer;

import org.jdesktop.wonderland.common.help.HelpInfo;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuEntry;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuFolder;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuItem;
import org.jdesktop.wonderland.common.help.HelpInfo.HelpMenuSeparator;

/**
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HelpDeployer {

    public static HelpInfo getHelpInfo() {
        HelpInfo info = new HelpInfo();
        info.setHelpEntries(new HelpMenuEntry[] {
            new HelpMenuItem("Moving About", "wlh://fubar", "http://www.fubar.com"),
            new HelpMenuItem("Audio", "fubar", "fubar"),
            new HelpMenuSeparator(),
            new HelpMenuFolder("My item", new HelpMenuEntry[] {
                new HelpMenuItem("New Imte", "fubar1", "fubar1")
            })
        });
        return info;
    }
}
