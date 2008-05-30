/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * The Network Manager creates and controls all network connections.  This
 * includes establishing the connections as well as associating the connections
 * with various Entities and other systems.
 * 
 * The network manager is a pluggable system.  The system will load a specified
 * network manager. during initialization.
 * 
 * @author Doug Twilleager
 */
public abstract class NetworkManager {
    /**
     * This method connects the application to a server.
     * TODO: This is a placeholder
     */
    public abstract void connect();
    
    /**
     * This method processes login information for the session.
     * TODO: This is a placeholder
     */
    public abstract void login();
    
    /**
     * This method returns a communication channel for an Entity
     * TODO: This is a placeholder
     */
    public abstract Object getChannel();
    
    /**
     * This method releases a communication channel for an Entity
     * TODO: This is a placeholder
     */
    public abstract void releaseChannel(Object channel);
}
