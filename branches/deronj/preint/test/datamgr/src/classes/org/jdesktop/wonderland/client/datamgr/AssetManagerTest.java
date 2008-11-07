/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.datamgr;

import org.jdesktop.wonderland.client.assetmgr.Asset;
import org.jdesktop.wonderland.client.assetmgr.AssetManager;
import org.jdesktop.wonderland.common.AssetType;
import org.jdesktop.wonderland.common.AssetURI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the asset manager
 * 
 * @author paulby
 */
public class AssetManagerTest {

    private AssetManager assetManager;
    
    private static final String WONDERLAND_WORLD="http://192.18.37.42/compiled_models";
    
    /* URLs to download */
    private static final String urls[] = {
        "wla://mpk20/mpk20.jme.gz",
        "wla://mpk20/mpk20.jme.gz",
        "wla://mpk20/mpk20.jme.gz",
        "wla://mpk20/mpk20.jme.gz",
        "wla://mpk20/mpk20.jme.gz",
    };
    
    public AssetManagerTest() {
    }
    
    @Before
    public void setUp() {
        //System.setProperty("wonderland.dir",".wonderland-junit");
        assetManager = AssetManager.getAssetManager();
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void testCachePath() {
        try {
            AssetURI assetURI1 = new AssetURI("http://www.foo.net/models/mymodel.gz");
            System.out.println("For: " + assetURI1.toString() + ", cache file=" + assetURI1.getRelativeCachePath());
            
            AssetURI assetURI2 = new AssetURI("models/mymodel.gz");
            System.out.println("For: " + assetURI2.toString() + ", cache file=" + assetURI2.getRelativeCachePath());
            
            AssetURI assetURI3 = new AssetURI("wlm://mpk20/models/mymodel.gz");
            System.out.println("For: " + assetURI3.toString() + ", cache file=" + assetURI3.getRelativeCachePath());           
        } catch (java.lang.Exception excp) {
        }
    }
    
    //@Test
//    public void checksumConversionTest(){
//        byte[] t1 = new byte[]{0, 1, 10, 13, 14, 15};
//
//        String resStr = Checksum.toHexString(t1);
//        byte[] resByteArray = AssetDB.fromHexString(resStr);
//        boolean fail = false;
//        for (int i = 0; i < t1.length; i++) {
//            if (t1[i] != resByteArray[i]) {
//                fail = true;
//            }
//        }
//        assertFalse(fail);        
//    }
    
    //@Test
    /*
    public void downloadBadURL() {
        try {
            Repository r = new Repository(new URL("http://error.error.com/"));
            Asset asset = assetManager.getAsset(AssetType.FILE, r, "foo", null);
            assertFalse(assetManager.waitForAsset(asset));
            assertNotNull(asset.getFailureInfo());
        } catch (MalformedURLException ex) {
            fail("Bad URL in test!");
        }
    }*/
    
    @Test
    public void downloadFile() throws java.net.URISyntaxException {
        final Thread threads[] = new Thread[urls.length];
        for (int i = 0; i < urls.length; i++) {
            final int j = i;
            threads[i] = new Thread() {
                public void run() {
                    try {
                        AssetURI assetURI = new AssetURI(urls[j]);
                        Asset asset = assetManager.getAsset(assetURI, AssetType.FILE);
                        assertTrue(assetManager.waitForAsset(asset));
                        assertNull(asset.getFailureInfo());
                        assertNotNull(asset.getLocalCacheFile());
                        System.out.println("Done with: " + assetURI.toString());
                    } catch (java.net.URISyntaxException excp) {
                        System.out.println(excp.toString());
                    }
                }
            };
            threads[i].start();
        }
        
        for (int i = 0; i < urls.length; i++) {
            try {
                threads[i].join();
            } catch (java.lang.InterruptedException excp) {
                System.out.println(excp.toString());
            }
        }
    }
    
}