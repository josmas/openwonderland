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
package org.jdesktop.wonderland.client.jme.artimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Upload a model to a Wonderland art server
 * @author jkaplan
 */
public class ModelUploader extends ModelCompiler {

    private static final Logger logger =
            Logger.getLogger(ModelUploader.class.getName());
    
    public ModelUploader(CompilerMessageDisplay display) {
        super (display);
    }
    
    /**
     * Convert a Wonderland art URL into a model upload URL.  This strips off
     * the ending "/art" and replaces it with "/upload".
     * @param baseURL the art base URL ending in "/art"
     * @return the upload URL
     */
    public static URL getUploadURL(String baseURL) throws MalformedURLException {
        if (baseURL.endsWith("/art")) {
            baseURL = baseURL.substring(0, baseURL.length() - 4);
        }
        
        return new URL(baseURL + "/upload");
    }
    
    /**
     * Check whether uploading is available
     * @param baseURL the base url to check
     * @return true if uploading is available, or false if not
     */
    public static boolean uploadAvailable(String baseURL) {
        if (baseURL.endsWith("/art")) {
            baseURL = baseURL.substring(0, baseURL.length() - 4);
        }
        
        try {
            URL checkURL = new URL(baseURL + "/uploadAvailable.jsp");
            URLConnection uc = checkURL.openConnection();
            if (!(uc instanceof HttpURLConnection)) {
                logger.info("Art URL is not http: " + checkURL);
                return false;
            }
            
            HttpURLConnection conn = (HttpURLConnection) uc;
            conn.connect();
            conn.disconnect();
            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    return true;
                    
                default:
                    logger.info("Http response " + conn.getResponseCode());
                    return false;
            }
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error checking URL", ioe);
            return false;
        }
    }
    
    public void uploadModel(ImportedModel model, URL uploadURL, String baseURL)
            throws IOException
    {
        throw new RuntimeException("Not implemented");
        
        // first make a temporary directory
//        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
//        File genDir = new File(tmpDir, "Art" + System.currentTimeMillis());
//        genDir.mkdir();
//        genDir.deleteOnExit();
//
//        // compile the model into the temporary directory
//        compileModel(model, baseURL, genDir.getCanonicalPath());
//
//        // zip the texture directory
//        File textureDir = new File(genDir, "textures");
//        File textureZip = new File(genDir, "textures.zip");
//        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(textureZip));
//        zipDir(textureDir, textureDir.getPath(), zout);
//        zout.close();
//
//        // create the connection to write data to the form
//        String boundary = MultiPartFormOutputStream.createBoundary();
//        URLConnection uc = MultiPartFormOutputStream.createConnection(uploadURL);
//        if (!(uc instanceof HttpURLConnection)) {
//            getDisplay().displayError("http URL required");
//            return;
//        }
//        HttpURLConnection conn = (HttpURLConnection) uc;
//        conn.setRequestProperty("Accept", "*/*");
//        conn.setRequestProperty("Content-Type",
//                MultiPartFormOutputStream.getContentType(boundary));
//        conn.setRequestProperty("Connection", "Keep-Alive");
//        conn.setRequestProperty("Cache-Control", "no-cache");
//
//        MultiPartFormOutputStream up =
//                new MultiPartFormOutputStream(conn.getOutputStream(), boundary);
//
//        // write name
//        up.writeField("name", model.getWonderlandName());

        // write location
//        up.writeField("xloc", model.getTranslation().x);
//        up.writeField("yloc", model.getTranslation().y);
//        up.writeField("zloc", model.getTranslation().z);
//
//        // write rotation
//        Matrix3f mat = ImportSessionFrame.calcRotationMatrix(
//                (float)Math.toRadians(model.getOrientation().x),
//                (float)Math.toRadians(model.getOrientation().y),
//                (float)Math.toRadians(model.getOrientation().z));
//        AxisAngle4d angle = new AxisAngle4d();
//        angle.set(mat);
//        up.writeField("xrot", angle.x);
//        up.writeField("yrot", angle.y);
//        up.writeField("zrot", angle.z);
//        up.writeField("arot", angle.angle);
//
//        // write bounds
//        double xBounds = 1.0;
//        double yBounds = 1.0;
//        double zBounds = 1.0;
//        Bounds bounds = model.getModelBG().getBounds();
//        if (bounds instanceof BoundingBox) {
//            Point3d lower = new Point3d();
//            Point3d upper = new Point3d();
//            ((BoundingBox) bounds).getLower(lower);
//            ((BoundingBox) bounds).getUpper(upper);
//            upper.sub(lower);
//
//            xBounds = upper.x;
//            yBounds = upper.y;
//            zBounds = upper.z;
//        } else if (bounds instanceof BoundingSphere) {
//            xBounds = ((BoundingSphere) bounds).getRadius();
//            yBounds = ((BoundingSphere) bounds).getRadius();
//            zBounds = ((BoundingSphere) bounds).getRadius();
//        } else {
//            logger.log(Level.WARNING, "Unable to send bounds of type " +
//                    bounds.getClass().getName());
//        }
//        up.writeField("xbounds", xBounds);
//        up.writeField("ybounds", yBounds);
//        up.writeField("zbounds", zBounds);
//
//        // write model file
//        File modelsDir = new File(genDir, "models");
//        File modelFile = new File(modelsDir, model.getWonderlandName() + ".j3s.gz");
//        up.writeFile("model", "application/x-gzip", modelFile);
//
//        // write textures zip
//        up.writeFile("textures", "application/zip", textureZip);
//        up.close();
//
//        // read response
//        int responseCode = conn.getResponseCode();
//        if (responseCode != HttpURLConnection.HTTP_OK) {
//            getDisplay().displayError("Server error (code " + responseCode + ")");
//        
//            // print error in console
//            BufferedReader reader = 
//                    new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuffer resp = new StringBuffer();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                resp.append(line + "\n");
//            }
//            logger.log(Level.WARNING, "Response from server", resp);
//        }
//        
//        // close the connection
//        conn.disconnect();
//
//        // remove the model from the graph
//        model.getRootBG().detach();
//        
//        // delete the temporary directory
//        deleteDir(genDir);
    }

    /**
     * Zip a directory
     * @param dir the directory to zip
     * @param dirStr the directory name string to remove from all paths
     * @param out the zip output stream to write to
     */
    private void zipDir(File dir, String dirStr, ZipOutputStream out) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            // get the name by taking the path to this file and removing
            // the path to the root directory.  This should give the name
            // relative to the root directory
            String name = file.getPath().substring(dirStr.length() + 1);

            if (file.isDirectory()) {
                // write an entry for the directory
                out.putNextEntry(new ZipEntry(name + File.separator));
                out.closeEntry();

                // write the directory
                zipDir(file, dirStr, out);
            } else {
                // write a zip entry for the file
                ZipEntry ze = new ZipEntry(name);
                ze.setSize(file.length());
                ze.setTime(file.lastModified());
                out.putNextEntry(ze);

                byte[] buffer = new byte[64 * 1024];
                FileInputStream in = new FileInputStream(file);
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }

                out.closeEntry();
            }
        }
    }
    
    /**
     * Recursively delete a directory
     * @param file the directory to delete
     */
    private void deleteDir(File file) {
        if (!file.exists() || !file.isDirectory()) {
            return;
        }
        
        for (File child : file.listFiles()) {
            if (child.isDirectory()) {
                deleteDir(child);
            } else {
                child.delete();
            }
        }
        
        file.delete();
    }
}
