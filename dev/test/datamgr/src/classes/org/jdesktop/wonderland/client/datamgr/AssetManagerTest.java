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
package org.jdesktop.wonderland.client.datamgr;

import java.net.MalformedURLException;
import java.net.URL;
import org.jdesktop.wonderland.common.AssetType;
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
    
    public AssetManagerTest() {
    }
    
    @Before
    public void setUp() {
        System.setProperty("wonderland.dir",".wonderland-junit");
        assetManager = AssetManager.getAssetManager();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void checksumConversionTest(){
        byte[] t1 = new byte[]{0, 1, 10, 13, 14, 15};

        String resStr = AssetDB.toHexString(t1);
        byte[] resByteArray = AssetDB.fromHexString(resStr);
        boolean fail = false;
        for (int i = 0; i < t1.length; i++) {
            if (t1[i] != resByteArray[i]) {
                fail = true;
            }
        }
        assertFalse(fail);        
    }
    
    @Test
    public void downloadBadURL() {
        try {
            Repository r = new Repository(new URL("http://error.error.com/"));
            Asset asset = assetManager.getAsset(AssetType.FILE, r, "foo", null);
            assertFalse(assetManager.waitForAsset(asset));
            assertNotNull(asset.getFailureInfo());
        } catch (MalformedURLException ex) {
            fail("Bad URL in test!");
        }
    }
    
    @Test
    public void downloadFile() {
        try {
            Repository r = new Repository(new URL(WONDERLAND_WORLD));
            Asset asset = assetManager.getAsset(AssetType.FILE, r, "checksums.txt", null);
            assertTrue(assetManager.waitForAsset(asset));
            assertNull(asset.getFailureInfo());
            assertNotNull(asset.getLocalCacheFile());
            
            assetManager.deleteAsset(asset);
        } catch (MalformedURLException ex) {
            fail("Bad URL in test!");
        }
    }
    
}
