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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.jdesktop.j3d.utils.scenegraph.ImageComponent2DURL;
import org.jdesktop.wonderland.common.AssetType;

/**
 *
 * @author paulby
 */
public class AssetImageComponent2DURL extends Asset<ImageComponent2DURL> {

    private static Logger logger = Logger.getLogger(AssetImageComponent2DURL.class.getName());

    AssetImageComponent2DURL(Repository repository, String filename) {
        super(repository, filename);
        type = AssetType.IMAGE;
    }
    
    @Override
    void postProcess() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Load the image from the URL in a format that we can accelerate
     * 
     * Return null if the load failed
     */
    private BufferedImage loadImage(URL url, String format) throws IOException {
        BufferedImage image;
        int width;
        int height;

        ImageInputStream imageIn=null;
        ImageTypeSpecifier imageType;

        TrackingInputStream.ProgressListener downloadTracker = null;

        URLConnection connection = url.openConnection();
        TrackingInputStream track = new TrackingInputStream(connection.getInputStream());

//        System.out.println("loadImage "+url.toExternalForm());

        if (url.getProtocol().equalsIgnoreCase("file")) {
            File f;
            try {
                f = new File(url.toURI());
                imageIn = new FileImageInputStream(f);
                // Dont track images from local files
            } catch (URISyntaxException ex) {
                logger.warning("Corrupt image file "+url.toExternalForm());
                return null;
            }
        } else {
            imageIn = new MemoryCacheImageInputStream(track);
        }

        ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName(format).next();
        reader.setInput(imageIn);
        try {
            imageType = reader.getRawImageType(0);
            width = reader.getWidth(0);
            height = reader.getHeight(0);
        } catch(IndexOutOfBoundsException ioob) {
            logger.warning("Corrupt image file "+url.toExternalForm());
            // The image file is corrupt
            return null;
        }
        if (imageType.getColorModel().getTransparency()==ColorModel.OPAQUE) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        }
        ImageReadParam param = reader.getDefaultReadParam();
        param.setDestination(image);
        image = reader.read(0,param);

        reader.dispose();
        track.close();

        return image;
    }

    @Override
    public ImageComponent2DURL getAsset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    boolean loadLocal() {
        try {
            loadImage(getLocalCacheFile().toURI().toURL(), "png");
        } catch (IOException ex) {
            Logger.getLogger(AssetImageComponent2DURL.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
        
        return true;
    }

    @Override
    void unloaded() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
    