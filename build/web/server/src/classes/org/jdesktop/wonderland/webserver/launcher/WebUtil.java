/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.webserver.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;

/**
 *
 * @author jkaplan
 */
public class WebUtil {
    private static final Logger logger = 
            Logger.getLogger(WebUtil.class.getName());
    
    // the base directory
    private static File baseDir;
    
    /**
     * Create a temporary directory in a directory that will get
     * cleaned up when we exit
     * @return
     */
    public static File createTempDir(String prefix, String suffix) {
        File out;
        
        do {
            String fileName = prefix + ((int) (Math.random() * 65536)) + suffix; 
            out = new File(getTempBaseDir(), fileName);
        } while (out.exists());
        
        out.mkdir();
        return out;
    }
    
    /**
     * Get the base directory, creating it if necessary
     */
    public synchronized static File getTempBaseDir() {
        if (baseDir == null) {
            try {
                // create a new temp directory
                baseDir = File.createTempFile("wonderlandweb", ".tmp");
                baseDir.delete();
                baseDir.mkdirs();
                
                // remove it on exit.  Only do this if we create a directory
                // in /tmp!!
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        logger.info("Cleaning up Wonderland temp directory.");
                        deleteDir(baseDir);
                    }
                });
            } catch (IOException ioe) {
                logger.warning("Unable to create base temporary directory.");
                baseDir = new File(".");
            }
        }
        return baseDir;
    }
    
    /**
     * Extract the given file available as a resource in the current
     * classloader.
     * @param srcUrl the URL of the source resource in the current
     * class' class loader
     * @param destDir the destination directory to extract to
     * @return the extracted file
     * @throws IOException if there is an error extracting the file
     */
    public static File extract(String srcURL, File destDir) throws IOException {
        // get input from .jar file
        InputStream is = WebServerLauncher.class.getResourceAsStream(srcURL);
        BufferedInputStream in = new BufferedInputStream(is);
    
        // write to a file with the same name as the source
        String fileName = srcURL.substring(srcURL.lastIndexOf("/"));
        File out = new File(destDir, fileName);
            
        FileOutputStream os = new FileOutputStream(out);
        BufferedOutputStream bos = new BufferedOutputStream(os);
       
        byte[] buffer = new byte[1024 * 64];
        int read;
        
        while ((read = in.read(buffer)) > 0) {
            bos.write(buffer, 0, read);
        }
        
        bos.close();
        return out;
    }
    
    /**
     * Extract the given jar file available as a resource in the current
     * classloader.  This method extracts the contents of the jar file
     * to a directory, and returns that directory.
     * @param srcUrl the URL of the source resource in the current class' class
     * loader
     * @param destDir the destination directory to extract to.  Note the
     * contents of the jar will be put in a subdirectory of this directory.
     * @return the directory where the contents of the jar have been 
     * extracted
     * @throws IOException if there is an error extracting the file
     */
    
    public static File extractJar(String srcURL, File destDir) 
            throws IOException 
    {
        // get input from .war file
        InputStream is = WebServerLauncher.class.getResourceAsStream(srcURL);
        JarInputStream jis = new JarInputStream(is);

        // create the output directory
        String fileName = srcURL.substring(srcURL.lastIndexOf("/"));
        File outputDir = new File(destDir, fileName);
        outputDir.mkdir();
        
        // write the contents of the jar there
        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null) {
            if (je.isDirectory()) {
                // ignore directories
                continue;
            }
            
            File out = new File(outputDir, je.getName());
            File dir = out.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            
            FileOutputStream os = new FileOutputStream(out);
            BufferedOutputStream bos = new BufferedOutputStream(os);
       
            byte[] buffer = new byte[1024 * 64];
            int read;
        
            while ((read = jis.read(buffer)) > 0) {
                bos.write(buffer, 0, read);
            }
        
            bos.close();
        }
        
        return outputDir;
    }
    
    /**
     * Delete a directory.  Dangerous!!
     */
    private static void deleteDir(File base) {
        if (base.isDirectory()) {
            for (File f : base.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    deleteDir(f);
                }
            }
        }
        
        base.delete();
    }
}
