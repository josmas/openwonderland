/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

/**
 * The Content manager takes care of preparing content for association with an
 * Entity.  The content descriptors can come from a local file or be streamed 
 * from the network or other media.  The actual data is retrieved from a 
 * pluggable AssetManager.  By default, the default asset manager is used, which
 * simply loads the assets as if they were files.
 * 
 * @author Doug Twilleager
 */
public class ContentManager implements ContentListener {
    /**
     * The AssetManager currently being used.
     */
    AssetManager assetManager = new DefaultAssetManager();
    
    /**
     * The current Network Manager.  This is used for listening to streams
     * for content.
     */
    NetworkManager networkManager = null;
    
    /**
     * This method parses a given file and returns a list of ContentDescriptors.
     * These Descriptors can then be instantiated as Entities.
     * 
     * @param filemane The file to parse
     */
    public ContentDescriptor[] loadFile(String filename) {
        return (null);
    }

    /**
     * This method establishes a listener which listens for Content updates
     * through a stream.
     * 
     * @param stream The stream to listen on
     */
    public void listenForContent(Object stream) {
        
    }
    
    /**
     * This method sets the AssetManager
     */
    public void setAssetManager(AssetManager am) {
        assetManager = am;
    } 
    
    /**
     * This method sets the network manager
     */
    public void setNetworkManager(NetworkManager nm) {
        networkManager = nm;
    }
}
