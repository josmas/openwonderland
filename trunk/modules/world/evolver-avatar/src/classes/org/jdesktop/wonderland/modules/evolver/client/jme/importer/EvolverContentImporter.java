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
package org.jdesktop.wonderland.modules.evolver.client.jme.importer;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.content.spi.ContentImporterSPI;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.avatarbase.client.registry.AvatarRegistry;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.evolver.client.evolver.EvolverAvatar;
import org.jdesktop.wonderland.modules.evolver.client.evolver.EvolverAvatarConfigManager;
import org.jdesktop.wonderland.modules.evolver.client.evolver.EvolverAvatarInfo;

/**
 * A content importer handler for Evolver Avatar (.eva) files
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class EvolverContentImporter implements ContentImporterSPI {

    private static Logger logger = Logger.getLogger(EvolverContentImporter.class.getName());
    private ServerSessionManager loginInfo = null;

    /** Constructor, takes the login information */
    public EvolverContentImporter(ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
    }

    /**
     * @inheritDoc()
     */
    public String[] getExtensions() {
        return new String[] { "eva" };
    }

    /**
     * @inheritDoc()
     */
    public String importFile(File file, String extension) {
        JFrame frame = JmeClientMain.getFrame().getFrame();
        String fname = file.getName();

        logger.warning("Importing Evolver avatar file " + fname);

        // We first need to check whether the file is a .zip file. If not, then
        // it is not properly formatted. Display a dialog indicating such.
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        } catch (java.lang.Exception excp) {
            logger.log(Level.WARNING, "Dropped file is not a .zip", excp);
            JOptionPane.showMessageDialog(frame,
                    "The file " + fname + " is not a valid Evolver Avatar file",
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Next find the essential avatar information (e.g. name, gender) by
        // parsing the evolver.xml file to get an EvolverAvatarInfo class
        EvolverAvatarInfo avatarInfo = getAvatarInfo(zipFile);
        if (avatarInfo == null) {
            logger.warning("Dropped file does not contain evolver.xml");
            JOptionPane.showMessageDialog(frame,
                    "The file " + fname + " is not a valid Evolver Avatar file",
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Fetch the avatar name. If there is no name, then return an error
        String avatarName = avatarInfo.getAvatarName();
        if (avatarName == null || avatarName.equals("") == true) {
            logger.warning("No avatar name found in evolver.xml");
             JOptionPane.showMessageDialog(frame,
                    "The file " + fname + " is not a valid Evolver Avatar file",
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        logger.warning("Found avatar with named " + avatarName);

        // Otherwise, check to see if the name already exists in the user's
        // content repository. If so, then ask whether the user wishes to
        // overwrite the existing avatar.
        if (isAvatarExists(avatarName) == true) {
            int result = JOptionPane.showConfirmDialog(frame,
                    "The avatar " + avatarName + " already exists in the " +
                    "content repository. Do you wish to replace it and " +
                    "continue?", "Replace avatar?",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return null;
            }
        }

        logger.warning("Adding avatar named " + avatarName + " into the system.");

        // Display a dialog showing a wait message while we import. We need
        // to do this in the SwingWorker thread so it gets drawn
        JOptionPane waitMsg = new JOptionPane("Please wait while the avatar " +
                "named " + avatarName + " is being uploaded");
        final JDialog dialog = waitMsg.createDialog(frame, "Uploading Avatar");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
        
        // Go ahead and create the avatar in the system. This will take care
        // of uploading all of the files and properly register the avatar in
        // the system.
        try {
            EvolverAvatarConfigManager m =
                    EvolverAvatarConfigManager.getEvolverAvatarConfigManager();
            EvolverAvatar avatar = m.createAvatar(zipFile, avatarInfo);

            logger.warning("Setting avatar named " + avatarName + " in-use.");

            // Finally, tell the avatar system to use this avatar as the default.
            AvatarRegistry registry = AvatarRegistry.getAvatarRegistry();
            registry.setAvatarInUse(avatar, false);
        } finally {
            // Close down the dialog indicating success
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.setVisible(false);
                }
            });
        }
        
        // We do not wish to create a Cell in this case, so always return null,
        // even upon success.
        return null;
    }

    /**
     * Searches the given zip file for a .dae and returns its name. If one
     * does not exist, then return null
     */
    private String getAvatarName(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements() == true) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".dae") == true) {
                return name.substring(0, name.length() - 4);
            }
        }
        return null;
    }

    /**
     * Searches the given zip file for a file named evolver.xml and returns an
     * object which represents its information. If no file exists, returns
     * null
     */
    private EvolverAvatarInfo getAvatarInfo(ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements() == true) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.equals("evolver.xml") == true) {
                try {
                    InputStream is = zipFile.getInputStream(entry);
                    return EvolverAvatarInfo.decode(is);
                } catch (java.lang.Exception excp) {
                    logger.log(Level.WARNING, "Unable to find evolver.xml", excp);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the given avatar name already exists within the user's
     * content repository
     */
    private boolean isAvatarExists(String avatarName) {
        ContentCollection userRoot = getUserRoot();
        String path = "/evolver/" + avatarName;
        try {
            boolean exists = (userRoot.getChild(path) != null);
            return exists;
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Error while try to find " + path +
                    " in content repository", excp);
            return false;
        }
    }

    /**
     * Returns the content repository root for the current user, or null upon
     * error.
     */
    private ContentCollection getUserRoot() {
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(loginInfo);
        try {
            return repo.getUserRoot();
        } catch (ContentRepositoryException excp) {
            logger.log(Level.WARNING, "Unable to find repository root", excp);
            return null;
        }
    }
}
