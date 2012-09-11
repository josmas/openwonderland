/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.userlist.client;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClientPlugin;
import org.jdesktop.wonderland.modules.audiomanager.client.PresenceControls;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.ChangeUsernameAliasMessage;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * This class will act as the Model in the MVP architecture.
 * 
 * @author JagWire
 */
public enum WonderlandUserList implements PresenceManagerListener {
    INSTANCE;

    private WonderlandSession session;    
    private PresenceManager manager;
    private Cell cell;
    private PresenceInfo localPresenceInfo;
    private ModelChangedListener listener;
    private Set<PresenceInfo> usersInRange = null;
    private Set<PresenceInfo> usersNotInRange = null;
    
    
    private ConcurrentHashMap<String, String> usernameMap;
    private int lastPositionOfInRangeList = 0;
    private VolumeConverter converter;
    
    private static final Logger LOGGER = Logger.getLogger(WonderlandUserList.class.getName());
    
    public void initialize() { 
                           
        LOGGER.warning("inside initialize!");
        this.session = LoginManager.getPrimary().getPrimarySession();
        this.manager = PresenceManagerFactory.getPresenceManager(session);
        this.cell = ClientContextJME.getViewManager().getPrimaryViewCell();
        this.localPresenceInfo = manager.getPresenceInfo(cell.getCellID());
        
        usersInRange = Collections.synchronizedSet(new LinkedHashSet<PresenceInfo>());
        usersNotInRange =Collections.synchronizedSet(new LinkedHashSet<PresenceInfo>());
        usernameMap = new ConcurrentHashMap<String, String>();
//        usersInRange.add(localPresenceInfo);
        
        manager.addPresenceManagerListener(this);
        usersInRange.add(localPresenceInfo);
        
    }
    
    public String getMyDisplayName() {
        return localPresenceInfo.getUsernameAlias();
    }
    
    public Set<PresenceInfo> getUsersInRange() {
        return usersInRange;
    }
    
    public Set<PresenceInfo> getUsersNotInRange() {
        return usersNotInRange;
    }
    
    public Cell getMyCell() {
        CellID cellID = localPresenceInfo.getCellID();
        
        return ClientContext.getCellCache(session).getCell(cellID);
    }
    
    public Cell getCellFromPresenceInfo(PresenceInfo info) {
        CellID cellID = info.getCellID();
        return ClientContext.getCellCache(session).getCell(cellID);
    }
    
    public void setVolumeConverter(int volumeSliderMaximum) {
        this.converter = new VolumeConverter(volumeSliderMaximum);
    }
    
    public VolumeConverter getVolumeConverter() {
        return converter;
    }
    
    public void setWonderlandSession(WonderlandSession session) {
        manager = PresenceManagerFactory.getPresenceManager(session);
        manager.addPresenceManagerListener(this);       
    }
    
    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public void cleanup() {
        if(manager != null) {
            manager.removePresenceManagerListener(this);            
        }
        manager = null;
    }
    
    public void addModelChangedListener(ModelChangedListener listener) {
        this.listener = listener;
    }
    
    public void incrementLastPositionOfInRangeList() {
        lastPositionOfInRangeList += 1;
    }
    
    public void decrementLastPositionOfInRangeList() {
        lastPositionOfInRangeList -= 1;
    }
    
    public int getLastPositionOfInRangeList() {
        return lastPositionOfInRangeList;
    }
    
    public synchronized void presenceInfoChanged(PresenceInfo pi, ChangeType ct) {
        
        
        switch(ct) {
            case UPDATED:
//                logger.warning("INFO CHANGED: UPDATED: "+pi.getUsernameAlias());
                listener.infoUpdated(pi);
                break;
            case USER_ADDED:
                LOGGER.fine("INFO CHANGED: ADDED: "+pi.getUsernameAlias());
                usersNotInRange.add(pi);
                
                if(listener != null) {
                    listener.userMovedOutOfRange(pi);
                }
                break;
            case USER_IN_RANGE:
                LOGGER.fine("INFO CHANGED: MOVED_IN_RANGE: "+pi.getUsernameAlias());
                
                //remove user from "out of range" list, if it exists in that set
                usersNotInRange.remove(pi);
                
                //add user to "in range" list.
                usersInRange.add(pi);
                
                //notify listener
                listener.userMovedInRange(pi);
                break;
            case USER_OUT_OF_RANGE:
                LOGGER.fine("INFO CHANGED: MOVED_OUT_OF_RANGE: "+pi.getUsernameAlias());
                
                if(usersInRange.contains(pi)) {
                    usersInRange.remove(pi);
                }
                
                //remove user from "in range" list, if it exists.
                usersInRange.remove(pi);                
                
                //add user to "out of range" list.
                usersNotInRange.add(pi);
                
                //notify listener.
                listener.userMovedOutOfRange(pi);
                break;
            case USER_REMOVED:
                LOGGER.fine("INFO CHANGED: REMOVED: "+pi.getUsernameAlias());
                
                listener.userMovedOutOfRange(pi);
                
                //remove user from both lists if they exist in those lists
                usersInRange.remove(pi);
                usersNotInRange.remove(pi);
                
                break;
        };
    
    }
    
    public boolean isMe(PresenceInfo info) {
        if(info == null) {
            return false;
        }
        
        if( cell != null && manager != null) {
            return info.equals(manager.getPresenceInfo(cell.getCellID()));
        }
        
        return false;
    }
    
    public boolean isInRange(PresenceInfo info) {
        return isMe(info) || (usersInRange.contains(info));
    }
    
    public boolean isNewUser(String username) {
        if(!usernameMap.containsKey(username))
            return true;
        
        return false;
    }
    
    public PresenceInfo[] getAllUsers() {
        if(manager != null) {
            return manager.getAllUsers();
        } else {
            
        }
        
        return null;
    }
    
    public void addUserToMap(String username, String displayName) {
        if(usernameMap == null)
            return;
        
        usernameMap.put(username, displayName);
    }

    public String getDisplayNameForUser(String username) {
        if(!usernameMap.containsKey(username)) {
            return null;
        }
        
        return usernameMap.get(username);
    }
    
    public void replace(String username, String displayName) {
        usernameMap.replace(username, displayName);
    }
    
    public Iterator<String> getKeySetIterator()  {
        return usernameMap.keySet().iterator();
    }
    
    public void usernameAliasChanged(PresenceInfo info) {
        listener.aliasChanged(info);
    }
    
    public WonderlandIdentity getIDForAlias(String username) {
        PresenceInfo pi = manager.getAliasPresenceInfo(username);
        return pi.getUserID();
    }
    
    public PresenceControls getPresenceControls() {
        return new PresenceControls(AudioManagerClientPlugin.getClient(),
                                    session, manager, localPresenceInfo);
    }
    
    public void sendChangeUsernameAliasMessage(PresenceInfo info) {
        session.send(AudioManagerClientPlugin.getClient(),
                    new ChangeUsernameAliasMessage(info.getCellID(),
                    info));

    }
    
    public PresenceInfo getLocalPresenceInfo() {
        return localPresenceInfo;
    }
    
    public PresenceInfo getAliasInfo(String s) {
        s = NameTagNode.getUsername(s);
        return manager.getAliasPresenceInfo(s);
    }
    
    public BigInteger getSessionIDFromName(String name) {
        
        name = NameTagNode.getUsername(name);
        PresenceInfo info = manager.getUserPresenceInfo(name);
        
        
        return info.getClientID();
    }
    
    public void requestChangeUsernameAlias(String alias) {
        manager.requestChangeUsernameAlias(alias);
    }
    
    public WonderlandSession getSession() {
        return session;
    }
    
    public CellTransform getCellTransformForCellID(CellID cellID) {
        return manager.getCellTransform(cellID);
    }
    
    public static interface ModelChangedListener {
        public void userMovedInRange(PresenceInfo info);
        
        public void userMovedOutOfRange(PresenceInfo info);
        
        public void aliasChanged(PresenceInfo info);

        public void infoUpdated(PresenceInfo pi);
    }
}
