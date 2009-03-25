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
package org.jdesktop.wonderland.modules.avatarbase.client;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 * Manages the various avatar configurations a user may have.
 *
 * The local avatar (ie on local disk) are the ones presented to the user
 * for selection.
 *
 * Each avatar file is versioned in the filename using the _[number] postfix
 *
 * When the user connects to a server the system will ensure that the latest
 * versions of avatars are uploaded to the server. It will also download any
 * new files from the server.
 *
 * @author paulby
 */
public class AvatarConfigManager {

    private ContentCollection avatarsDir;

    private static final String extension=".xml";

    private HashMap<String, AvatarConfigFile> localAvatars = new HashMap();

    private ArrayList<AvatarManagerListener> listeners = new ArrayList();

    private static AvatarConfigManager avatarConfigManager=null;

    AvatarConfigManager() {
        ContentCollection localContent = ContentRepositoryRegistry.getInstance().getLocalRepository();
        try {
            ContentCollection avatarDir = (ContentCollection) localContent.getChild("avatars");
            if (avatarDir==null) {
                avatarDir = (ContentCollection) localContent.createChild("avatars", Type.COLLECTION);
            }

            List<ContentNode> avatarList = avatarDir.getChildren();
            for(ContentNode a : avatarList) {
                if (a instanceof ContentResource) {
                    AvatarConfigFile acf = new AvatarConfigFile((ContentResource) a);
                    localAvatars.put(acf.avatarName, acf);
                }
            }
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static AvatarConfigManager getAvatarConigManager() {
        if (avatarConfigManager==null)
            avatarConfigManager = new AvatarConfigManager();
        return avatarConfigManager;
    }

    public void addAvatarManagerListener(AvatarManagerListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    private void notifyListeners(boolean added, String name) {
        for(AvatarManagerListener l : listeners) {
            if (added)
                l.avatarAdded(name);
            else
                l.avatarRemoved(name);
        }
    }

    private int getAvatarVersion(String filename) {
        int underscore = filename.lastIndexOf('_');
        int ext = filename.lastIndexOf('.');

        String verStr = filename.substring(underscore+1, ext);

        try {
            return Integer.parseInt(verStr);
        } catch(NumberFormatException e) {
            return -1;
        }
    }

    public void addServer(final ServerSessionManager session) {
        Thread t = new Thread() {
            public void run() {
                ContentRepository repository = ContentRepositoryRegistry.getInstance().getRepository(session);

                try {
                    ContentCollection userDir = repository.getUserRoot(true);
                    avatarsDir = (ContentCollection) userDir.getChild("avatars");
                    if (avatarsDir==null) {
                        avatarsDir = (ContentCollection) userDir.createChild("avatars", Type.COLLECTION);
                    }

                    List<ContentNode> avatarList = avatarsDir.getChildren();
                    for(ContentNode a : avatarList) {
                        System.err.println("Found avatar "+a.getName());
                    }

        //            ContentResource file = (ContentResource) avatarsDir.createChild("test"+System.currentTimeMillis(), Type.RESOURCE);
        //            file.put(new byte[4]);
                } catch (ContentRepositoryException ex) {
                    Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        t.start();
    }

    public Iterable<String> getAvatars() {
        synchronized(localAvatars) {
            LinkedList<String> ret = new LinkedList();
            for(AvatarConfigFile f : localAvatars.values())
                ret.add(f.avatarName);
            return ret;
        }
    }

    public void saveFile(String repositoryFilename, File f) throws ContentRepositoryException, IOException {
        ContentResource file = (ContentResource) avatarsDir.createChild(repositoryFilename, Type.RESOURCE);
        file.put(f);
    }

    public boolean exists(String filename) {
        try {
            return avatarsDir.getChild(filename) != null;
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Return the directory which contains the avatar co
     * @return
     */
    public static File getAvatarConfigDir() {
        File userDir = ClientContext.getUserDirectory();
        File avatarDir = new File(userDir, "avatars");
        if (!avatarDir.exists())
            avatarDir.mkdir();
        return avatarDir;
    }

    /**
     * Returns the default avatar config file. The file may
     * or may not exist.
     * @return
     */
    public static File getDefaultAvatarConfigFile() {
        return new File(getAvatarConfigDir(), "avatar_config.xml");
    }

    class AvatarConfigFile {
        ContentResource resource;
        int version;
        String avatarName;      // Stripped filename

        public AvatarConfigFile(ContentResource resource) {
            this.resource = resource;
            version = getAvatarVersion(resource.getName());
            avatarName = resource.getName().substring(0, resource.getName().lastIndexOf('_'));
        }
    }

    public interface AvatarManagerListener {
        public void avatarAdded(String name);

        public void avatarRemoved(String name);
    }
}
