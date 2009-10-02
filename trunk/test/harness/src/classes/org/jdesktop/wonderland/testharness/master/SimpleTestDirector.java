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
package org.jdesktop.wonderland.testharness.master;

import com.jme.math.Vector3f;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.testharness.common.Client3DRequest;
import org.jdesktop.wonderland.testharness.common.LoginRequest;
import org.jdesktop.wonderland.testharness.common.LogoutRequest;
import org.jdesktop.wonderland.testharness.manager.common.CommsHandler;
import org.jdesktop.wonderland.testharness.manager.common.ManagerMessage;
import org.jdesktop.wonderland.testharness.manager.common.SimpleTestDirectorMessage;
import org.jdesktop.wonderland.testharness.master.SlaveConnection.SlaveConnectionListener;

/**
 *
 * @author paulby
 */
public class SimpleTestDirector implements TestDirector {

    private ArrayList<SlaveInfo> slaves = new ArrayList();
    private ArrayList<User> users = new ArrayList();
    private ArrayList<UserGroup> userGroups = new ArrayList();
    
    private Logger logger = Logger.getLogger(SimpleTestDirector.class.getName());
    
    private static int USERS_PER_GROUP = 3;
    private static int GROUP_SPACING = 8;

    private int targetUsers = 1;
    private int slaveCount = 0; // Slaves currently in use by this director

    private SlaveAllocator allocator = new RoundRobinSlaveAllocator();

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
                    case USER_COUNT :
                        targetUsers = message.getUserCount();
                        adjustUsers();
                        sendUIUpdate();
                        break;
                    case CHANGE_ALLOCATOR:
                        changeAllocator(message.getAllocatorName(),
                                        message.getProperties());
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

    private void changeAllocator(String allocatorName,
                                 Properties props)
    {
        SlaveAllocator out = null;

        if (allocatorName.equalsIgnoreCase("RoundRobin")) {
            out = new RoundRobinSlaveAllocator();
        } else if (allocatorName.equalsIgnoreCase("Fixed")) {
            out = new FixedMaximumSlaveAllocator();
        } else {
            Logger.getLogger(SimpleTestDirector.class.getName()).warning("Unknown allocator " +
                                                                         allocatorName);
            return;
        }

        out.configure(props);
        allocator = out;
    }

    private void adjustUsers() {
        // remove users if necessary
        while (targetUsers > users.size() && addUser()) {
            // do nothing -- removeUser() did the actual work
            try { Thread.sleep(500); } catch (InterruptedException ie) {}
        }

        // add users if necessary
        while (targetUsers < users.size() && removeUser()) {
            // do nothing -- addUser() did the actual work
            try { Thread.sleep(500); } catch (InterruptedException ie) {}
        }
    }

    private boolean addUser() {
        UserGroup group = findGroup();
        SlaveInfo slave = allocator.findSlave();
        if (slave == null) {
            return false;
        }

        group.add(createUser(slave));
        return true;
    }

    private boolean removeUser() {
        int removeIdx = users.size() - 1;
        User user = users.remove(removeIdx);
        if (user != null) {
            user.disconnect();

            UserGroup group = findGroup(user);
            if (group != null) {
                group.remove(user);
            }

            SlaveInfo slave = findSlave(user);
            if (slave != null) {
                slave.remove(user);
            }
        }

        return true;
    }

    /**
     * Find the first group with space for a new user, creating a group
     * if necessary
     * @return the first group with space, or a new group if no groups
     * have space
     */
    private UserGroup findGroup() {
        for (UserGroup group : userGroups) {
            if (group.getUserCount() < USERS_PER_GROUP) {
                return group;
            }
        }

        UserGroup lastGroup = userGroups.get(userGroups.size() - 1);
        UserGroup out = new UserGroup(lastGroup.getCenter().add(new Vector3f(GROUP_SPACING, 0, 0)));
        userGroups.add(out);

        return out;
    }

    /**
     * Find the group containing the given user, or return null if no
     * group contains the current user.
     * @param user the user to look for
     * @return the group containing the given user
     */
    private UserGroup findGroup(User user) {
        for (UserGroup group : userGroups) {
            if (group.contains(user)) {
                return group;
            }
        }

        return null;
    }

    /**
     * Find the slave containing the given user, or return null if no
     * slave contains the current user.
     * @param user the user to look for
     * @return the slave containing the given user
     */
    private SlaveInfo findSlave(User user) {
        for (SlaveInfo slave : slaves) {
            if (slave.contains(user)) {
                return slave;
            }
        }
        
        return null;
    }

    public boolean slaveJoined(SlaveConnection slaveConnection) {
        SlaveInfo slaveInfo = new SlaveInfo(slaveConnection);
        slaves.add(slaveInfo);
        slaveCount++;

        adjustUsers();
        sendUIUpdate();

        return true; // We used the slave so return true
    }
    
    private User createUser(SlaveInfo slaveInfo) {
        User user = new User(UsernameManager.getUniqueUsername(), slaveInfo);
        users.add(user);
        String serverURL = MasterMain.getMaster().getSgsServerName();
        
        Properties props = new Properties();
        props.setProperty("serverURL", serverURL);
//        LoginRequest lr = new LoginRequest("client3D.Client3DSim", props,
//                                           user.getUsername());
        LoginRequest lr = new LoginRequest("webstart.WebstartClientSim", props,
                                           user.getUsername());
        slaveInfo.getConnection().send(lr);

        return user;
    }
    
    class UserGroup extends ArrayList<User> {
        private Vector3f center;
        private Vector3f[] walkPattern;
        
        public UserGroup(Vector3f center) {
            this.center = center;
            walkPattern = new Vector3f[] {
                new Vector3f(0,0,0).add(center),
                new Vector3f(4,0,0).add(center),
                new Vector3f(2,0,4).add(center)
            };
       }

        @Override
        public boolean add(User user) {
            if (super.add(user)) {
                user.doWalk(walkPattern);
                return true;
            }

            return false;
        }
        
        public int getUserCount() {
            return size();
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
            slaveInfo.add(this);
        }

        public String getUsername() {
            return username;
        }
        
        public void doWalk(Vector3f[] locations) {
            slaveInfo.getConnection().send(Client3DRequest.newWalkLoopRequest(username, locations, 0.25f, -1));            
        }

        public void disconnect() {
            slaveInfo.getConnection().send(new LogoutRequest(username));
        }
    }

    class SlaveInfo extends ArrayList<User> implements SlaveConnectionListener {
        private SlaveConnection slaveConnection;
        
        public SlaveInfo(SlaveConnection slaveConnection) {
            this.slaveConnection = slaveConnection;

            slaveConnection.addListener(this);
        }
        
        public SlaveConnection getConnection() {
            return slaveConnection;
        }

        public int getUserCount() {
            return size();
        }

        public void disconnected(SlaveConnection connection) {
            slaves.remove(this);
            slaveCount--;

            for (User user : this) {
                users.remove(user);
                UserGroup g = findGroup(user);
                if (g != null) {
                    g.remove(user);
                }
            }

            sendUIUpdate();
        }
    }

    interface SlaveAllocator {
        /** configure with the given properties */
        public void configure(Properties props);

        /** find the next available slave */
        public SlaveInfo findSlave();
    }

    class RoundRobinSlaveAllocator implements SlaveAllocator {
        public void configure(Properties props) {
            return;
        }

        public SlaveInfo findSlave() {
            if (slaves.isEmpty()) {
                return null;
            }

            // first, go through and find the maximum number of
            // users on any one slave
            int max = 0;
            for (SlaveInfo slave : slaves) {
                if (slave.size() > max) {
                    max = slave.size();
                }
            }

            // now find the the first slave with fewer than max
            // clients
            SlaveInfo out = null;
            for (SlaveInfo slave : slaves) {
                if (slave.size() < max) {
                    out = slave;
                    break;
                }
            }

            // no luck -- just pick the first
            if (out == null) {
                out = slaves.get(0);
            }
            
            // all set
            return out;
        }
    }

    class FixedMaximumSlaveAllocator implements SlaveAllocator {
        private int max = 10;

        public void configure(Properties props) {
            if (props.containsKey("max")) {
                max = Integer.parseInt(props.getProperty("max"));
            }
        }

        public SlaveInfo findSlave() {
            for (SlaveInfo slave : slaves) {
                if (slave.size() < max) {
                    return slave;
                }
            }

            return null;
        }
    }
}
