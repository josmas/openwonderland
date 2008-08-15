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

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.export.binary.BinaryExporter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;

/**
 * Compile a loaded model into a .j3s.gz file.  Re-write textures as needed
 * @author paulby
 */
public class ModelCompiler {
    private static final Logger logger =
            Logger.getLogger(ModelCompiler.class.getName());
    
    private CompilerMessageDisplay display;
    
    /**
     * Create a new model compiler
     * @parama display the display to show messages to the user
     */
    public ModelCompiler(CompilerMessageDisplay display) {
        this.display = display;
    }
     
    /**
     * Compile the model, ready for deployment.
     * 
     * @param importConfig
     * @param baseURL
     * @param compiledModelDir
     */
    public boolean compileModel(ImportedModel importConfig, String baseURL, String compiledModelDir) {

        OutputStream out = null;
        try {
            System.err.println("compileModel not implemented");
            out = new BufferedOutputStream(new FileOutputStream(compiledModelDir + File.separatorChar + importConfig.getWonderlandName() + ".wlm"));
            BinaryExporter.getInstance().save(importConfig.getRootBG(), out);

//        ArrayList<UpdatedImageComponent> updatedICs = updateImageComponentURLData(importConfig.getModelBG(), baseURL,importConfig.getTexturePrefix());
//
//        File file = new File(compiledModelDir+File.separatorChar+"models"+File.separatorChar+importConfig.getWonderlandName()+".j3s");
//
//        if (!checkDirectory(file, true)) {
//            getDisplay().displayError("Unable to write to directory for file "+file.getAbsolutePath());
//            return false;
//        }
//
//        File fileGZ = new File(compiledModelDir+File.separatorChar+"models"+File.separatorChar+importConfig.getWonderlandName()+".j3s.gz");
//        if (file.exists() || fileGZ.exists()) {
//            if (!getDisplay().requestConfirmation("File exists, replace ?\n\n"+file.getAbsolutePath())) {
//                return false;
//            }                    
//        }
//
//        try {
//            GZIPOutputStream gOut = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(fileGZ)));
//            SceneGraphStreamWriter writer = new SceneGraphStreamWriter(gOut);
//            try {
//                writer.writeBranchGraph(importConfig.getModelBG(), new HashMap());
//            } catch (DanglingReferenceException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            } catch (NamedObjectException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//            writer.close();
//            
//        } catch(Exception e) {
//            getDisplay().displayError("Error writing model: " + e.getMessage());
//            logger.log(Level.SEVERE, null, e);
//            return false;
//        } finally {
//            // Restore the changes we made to the ImageComponent URL's
//            restoreImageComponentURLData(updatedICs);
//        }
//
//
//        // Copy the textures to the deployment directory
//        Collection<ImageComponent2DURL> icSet = getImageComponents(importConfig.getModelBG());
//        String origDir = importConfig.getOrigModel();
//        origDir = origDir.substring(0, origDir.lastIndexOf(File.separatorChar)+1);
//        String texturePrefix = importConfig.getTexturePrefix();
//        String texturePath = File.separator+"textures"+File.separator; 
//        for(ImageComponent2DURL ic : icSet) {
//            try {
//                //System.out.println("cp "+origDir + ic.getImageName()+"  to  "+
//                //         compiledModelDir +File.separatorChar+ texturePath + texturePrefix + File.separatorChar + ic.getImageName());
//                copyFile(origDir + ic.getImageName(), 
//                         compiledModelDir +File.separatorChar +texturePath + texturePrefix + File.separatorChar + ic.getImageName());
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, "Failed to copy file", ex);
//            }
//        }       
//
//        // Extra data required to create wfs file
//        System.out.println("WFS Data---------");
//        System.out.println("Bounds "+importConfig.getModelBG().getBounds());
//        System.out.println("Translation "+importConfig.getTranslation());
//        System.out.println("Orientation "+importConfig.getOrientation());
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ModelCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ModelCompiler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(ModelCompiler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false; // Save failed
    }
        
     /**
     * Get the message display
     * @return the message display
     */
    protected CompilerMessageDisplay getDisplay() {
        return display;
    }
    
    private void copyFile(String srcFilename, String destFilename) throws IOException {
        File src = new File(srcFilename);
        File dest = new File(destFilename);
        
        if (!checkDirectory(dest, true)) {
            getDisplay().displayError("Failed to create directory for file" + dest.getAbsolutePath());
            throw new IOException("Unable to write to directory for file " + dest.getAbsolutePath());
        }
        
        if (dest.exists()) {
            // todo check we can overwrite file
            System.out.println("File exists, overwriting");
        }
        
        byte[] buf = new byte[1024*1024];
        InputStream in = new BufferedInputStream(new FileInputStream(src));
        OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        
        int size;
        while ((size = in.read(buf))!=-1) {
            out.write(buf, 0, size);
        }
        out.close();
        in.close();
    }
    
    /**
     * Check the directory which will contain file exists, 
     * if create is true then attempt to create it
     * 
     * @param create
     * @return
     */
    private boolean checkDirectory(File file, boolean create) {
        String filename = file.getAbsolutePath();
        String dirName = filename.substring(0, filename.lastIndexOf(File.separatorChar));
        File dir = new File(dirName);
        
        if (dir.canWrite())
            return true;
        
        if (create && dir.mkdirs()) {
            return true;
        }
        
        return false;
    }
    
//    private ArrayList<UpdatedImageComponent> updateImageComponentURLData(BranchGroup bg, final String baseURL, final String namePrefix) {
//        final ArrayList<UpdatedImageComponent> ret = new ArrayList();
//        final HashSet<ImageComponent2DURL> updatedSet = new HashSet();
//        
//        try {
//            final URL url = new URL(baseURL);
//                TreeScan.findNode( bg, Shape3D.class, new ProcessNodeInterface() {
//                    public boolean processNode( javax.media.j3d.Node node ) {
//                        try {
//                            System.out.println("Shape "+node.getName());
//                            Appearance app = ((Shape3D)node).getAppearance();
//                            if (app==null) return false;
//                            Texture tex = app.getTexture();
//                            if (tex==null) return false;
//                            ImageComponent[] ics = tex.getImages();
//                            for(ImageComponent ic : ics) {
//                                if (ic instanceof ImageComponent2DURL && !updatedSet.contains(ic)) {
//                                    ImageComponent2DURL icURL = (ImageComponent2DURL)ic;
//                                    icURL.setBaseURL(url);
//                                    String filename= icURL.getImageName();
//                                    ret.add(new UpdatedImageComponent(icURL, filename));
//                                    if (filename.lastIndexOf('/')!=-1)
//                                        filename.substring(filename.lastIndexOf('/')+1);
//                                    if (!filename.startsWith(namePrefix+"/"))
//                                        icURL.setImageName("textures/"+namePrefix+"/"+filename);
//                                    updatedSet.add(icURL);
//                                    System.err.println("Processing IC "+icURL.getBaseURL().toExternalForm()+"  "+icURL.getImageName());
//                                    if (icURL.getImageName()==null || icURL.getBaseURL()==null)
//                                        System.err.println("WARNING! null in ImageComponent "+icURL.getBaseURL() +"  "+icURL.getImageName());
//                                } else
//                                    System.out.println("WARNING : non URL ImageComponent2D "+ic.getName());
//                            }
//                        } catch(Exception e) {
//                            e.printStackTrace();
//                        }
//                        return false;
//                    }
//                }, false, true);
//        } catch(MalformedURLException mue) {
//            getDisplay().displayError("Malformed URL "+mue.getMessage());
//        }
//
//        return ret;
//    }  
//    
//    private void restoreImageComponentURLData(ArrayList<UpdatedImageComponent> restoreList) {
//        for(UpdatedImageComponent updated : restoreList) {
//            updated.ic.setImageName(updated.filename);
//        }
//    }
    
     /**
     * Traverse the graph and return the Collection of Textures
     * Duplicate references to the same Texture are not included
     * in the result.
     * 
     * @param bg
     * @return
     */
    public static Collection<Texture> getImageComponents(Spatial bg) {
        final Set<Texture> ret = new HashSet();
        TreeScan.findNode(bg, Spatial.class, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                TextureState ts = (TextureState) node.getRenderState(RenderState.RS_TEXTURE);
                
                if (ts==null) {
                    // No texture
                    return true;
                }
                
                for(int unit=0; unit<ts.getNumberOfSetTextures(); unit++) {
                    Texture t = ts.getTexture(unit);
                    if (t!=null)
                        ret.add(t);
                }
                
                return true;
            }
            
        }, false, true);
        
        return ret;
    }
//    
//    class UpdatedImageComponent {
//        ImageComponent2DURL ic;
//        String filename;
//        
//        public UpdatedImageComponent(ImageComponent2DURL ic, String filename) {
//            this.ic = ic;
//            this.filename = filename;
//        }
//    }
    
    /**
     * Display messages to the user in the process of compilation
     */
    public interface CompilerMessageDisplay {
        /**
         * Display a message to the user
         * @param message the message to display
         */
        public void displayMessage(String message);
        
        /**
         * Display an error message to the user
         * @param message the message to display
         */
        public void displayError(String message);
        
        /**
         * Request configuration from the user
         * @param message the message to display
         * @return true if the user selects "yes" or false if they select "no"
         */
        public boolean requestConfirmation(String message);
    } 
}
