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
package org.jdesktop.wonderland.testharness.master;

import com.jme.math.Vector3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.Client3DRequest;
import org.jdesktop.wonderland.testharness.common.LoginRequest;
import org.jdesktop.wonderland.testharness.manager.common.CommsHandler;
import org.jdesktop.wonderland.testharness.manager.common.ManagerMessage;
import org.jdesktop.wonderland.testharness.manager.common.SimpleTestDirectorMessage;

/**
 *
 * @author paulby
 */
public class SimpleTestDirector implements TestDirector {

    private ArrayList<SlaveInfo> slaves = new ArrayList();
    private ArrayList<User> users = new ArrayList();
    private ArrayList<UserGroup> userGroups = new ArrayList();
    
    private Logger logger = Logger.getLogger(SimpleTestDirector.class.getName());
    
    private static int USERS_PER_SLAVE = 10;
    private static int USERS_PER_GROUP = 3;
    private static int GROUP_SPACING = 8;
    private int maxUsers = 1;
    
    private int slaveCount = 0; // Slaves currently in use by this director
    private int currentGroupIndex = 0;
    
    private CommsHandler commsHandler;
    
    public SimpleTestDirector(CommsHandler commsHandler) {
        this.commsHandler = commsHandler;
        userGroups.add(new UserGroup(new Vector3f(0,0,0)));
        
        commsHandler.addMessageListener(SimpleTestDirectorMessage.class, new CommsHandler.MessageListener() {

            public void messageReceived(ManagerMessage msg) {
                assert(msg instanceof SimpleTestDirectorMessage);
                SimpleTestDirectorMessage message = (SimpleTestDirectorMessage) msg;
                
                System.err.println("TestDirector received "+message.getMessageType());
                
                switch(message.getMessageType()) {
                    case REQUEST_STATUS :
                        sendUIUpdate();
                        break;
                    case ADD_USER :
                        maxUsers++;
                        boolean foundSpace = false;
                        for(SlaveInfo slave : slaves) {
                            if (slave.getUserCount()<USERS_PER_SLAVE) {
                                foundSpace = true;
                                addUser(slave);
                                break;
                            }
                        }
                        
                        if (!foundSpace)
                            MasterMain.getMaster().requestSlave(SimpleTestDirector.this);
                        break;
                    default :
                        System.err.println("Unexepected message type "+message.getMessageType());
                }

            }
        });
    }
    
    private void sendUIUpdate() {
        try {
            commsHandler.send(SimpleTestDirectorMessage.newUIUpdate(users.size()));
        } catch (IOException ex) {
            Logger.getLogger(SimpleTestDirector.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }
    
    public boolean slaveJoined(SlaveConnection slaveConnection) {
        System.out.println("SLAVE JOINED "+users.size());
        if (users.size()>=maxUsers)
            return false;
        
        SlaveInfo slaveInfo = new SlaveInfo(slaveConnection);
        slaves.add(slaveInfo);
        
        addUser(slaveInfo);
        
        slaveCount++;
        return true; // We used the slave so return true
    }
    
    private void addUser(SlaveInfo slaveInfo) {
        for(int i=0; i<USERS_PER_SLAVE && users.size()<maxUsers; i++) {
            try {
                Thread.sleep(500);              // Pause before logging in next user
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleTestDirector.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            UserGroup userGroup = userGroups.get(currentGroupIndex);
            if (userGroup.getUserCount()>=USERS_PER_GROUP) {
                 currentGroupIndex++;
                 userGroup = new UserGroup(userGroup.getCenter().add(new Vector3f(GROUP_SPACING, 0, 0)));
                 userGroups.add(userGroup);
            }
            userGroup.addUser(createUser(slaveInfo));
            
        }
        
        sendUIUpdate();
        
    }
    
    private User createUser(SlaveInfo slaveInfo) {
        User user = new User(UsernameManager.getUniqueUsername(), slaveInfo);
        users.add(user);
        String serverName = MasterMain.getMaster().getSgsServerName();
        int serverPort = MasterMain.getMaster().getSgsPort();
        
        slaveInfo.getConnection().send(new LoginRequest(serverName, 
                                                  serverPort, 
                                                  user.getUsername(), 
                                                  new char[] {}, 
                                                  0f, 0f, 0f));

        return user;
    }
    
    class UserGroup {
        private Vector3f center;
        private Vector3f[] walkPattern;
        private ArrayList<User> groupUsers = new ArrayList();
        
        public UserGroup(Vector3f center) {
            this.center = center;
            walkPattern = new Vector3f[] {
                new Vector3f(0,0,0).add(center),
                new Vector3f(4,0,0).add(center),
                new Vector3f(2,0,4).add(center)
            };
       }
        
        public void addUser(User user) {
            groupUsers.add(user);
            user.doWalk(walkPattern);
        }
        
        public int getUserCount() {
            return groupUsers.size();
        }
        
        public Vector3f getCenter() {
            return center;
        }
    }
    
    class User {
        private String username;
        private SlaveInfo slaveInfo;
        
        public User(String username, SlaveInfo slaveInfo) {
            this.username = username;
            this.slaveInfo = slaveInfo;
            slaveInfo.addUser(this);
        }

        public String getUsername() {
            return username;
        }
        
        public void doWalk(Vector3f[] locations) {
            slaveInfo.getConnection().send(Client3DRequest.newWalkLoopRequest(username, locations, 0.25f, -1));            
        }
    }

    class SlaveInfo {
        private SlaveConnection slaveConnection;
        private int userCount = 0;
        
        public SlaveInfo(SlaveConnection slaveConnection) {
            this.slaveConnection = slaveConnection;
        }
        
        public SlaveConnection getConnection() {
            return slaveConnection;
        }
        
        public void addUser(User user) {
            userCount++;
        }
        
        public int getUserCount() {
            return userCount;
        }
    }

}
