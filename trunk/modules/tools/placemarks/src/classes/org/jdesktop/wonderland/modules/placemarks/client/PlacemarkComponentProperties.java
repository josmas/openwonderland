/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.placemarks.client;

import com.jme.renderer.ColorRGBA;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.placemarks.common.PlacemarkComponentServerState;

/**
 * Edit the placemark component
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 * @author Abhishek Upadhyay
 */
@PropertiesFactory(PlacemarkComponentServerState.class)
public class PlacemarkComponentProperties extends JPanel
        implements PropertiesFactorySPI {
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/placemarks/client/resources/Bundle");

    private CellPropertiesEditor editor;
    private String origName;
    private String origRotation;
    private CoverScreenPropertyPanel cspp;
    private String origMessage;
    private String origImageURL;
    private ColorRGBA origBackColor;
    private ColorRGBA origTextColor;

    /** Creates new form PortalComponentProperties */
    public PlacemarkComponentProperties() {
        // Initialize the GUI
        initComponents();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
        // Listen for changes to the text fields
        TextFieldListener listener = new TextFieldListener();
        nameTF.getDocument().addDocumentListener(listener);
        rotationTF.getDocument().addDocumentListener(listener);
        cspp = new CoverScreenPropertyPanel();
        cspp.getMessageTf().getDocument().addDocumentListener(listener);
        cspp.getImageTf().getDocument().addDocumentListener(listener);
        cspp.getBackColorPanel().addPropertyChangeListener(new PanelPropChangeListener());
        cspp.getTextColorPanel().addPropertyChangeListener(new PanelPropChangeListener());
        this.add(cspp);
        this.repaint();
        this.validate();
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Placemark");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        CellServerState cellServerState = editor.getCellServerState();
        PlacemarkComponentServerState state =
                (PlacemarkComponentServerState) cellServerState.getComponentServerState(
                PlacemarkComponentServerState.class);
        if (state != null) {
            origName = state.getPlacemarkName();
            origRotation = state.getPlacemarkRotation();
            origMessage = state.getMessage();
            origImageURL = state.getImageURL();
            origBackColor = state.getBackgroundColor();
            origTextColor = state.getTextColor();
            
            if(origImageURL==null) {
                origImageURL="";
            }
            if(origMessage==null) {
                origMessage="";
            }
        }

        if (origName == null) {
            origName = "";
        }
        nameTF.setText(origName);
        if (origRotation == null) {
            origRotation = "0";
        }
        rotationTF.setText(origRotation);
        cspp.setMessage(origMessage);
        cspp.setImageURL(origImageURL);
        cspp.setBackColor(origBackColor);
        cspp.setTextColor(origTextColor);
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component.
        CellServerState cellServerState = editor.getCellServerState();
        PlacemarkComponentServerState state =
                (PlacemarkComponentServerState) cellServerState.getComponentServerState(
                PlacemarkComponentServerState.class);
        if (state == null) {
            state = new PlacemarkComponentServerState();
        }

        String name = nameTF.getText().trim();
        String rotation = rotationTF.getText().trim();

        if (name.length() == 0) {
            name = null;
        }

        if (rotation.length() == 0) {
            rotation = null;
        }

        File image = cspp.getImage();
        String uri=cspp.getImageURL();
        if(image!=null) {
             uri = uploadImage(image);
        }
        
        state.setPlacemarkName(name);
        state.setPlacemarkRotation(rotation);

        state.setMessage(cspp.getMessage());
        state.setImageURL(uri);
        state.setBackgroundColor(cspp.getBackColor());
        state.setTextColor(cspp.getTextColor());
        editor.addToUpdateList(state);
    }

    private String uploadImage(File image) {
        
        Image scaledBimg = null;
        File temp=null; 
        try {
            temp = File.createTempFile("Temp_Image", image.getName().split("\\.")[1]);
        } catch (IOException ex) {
            Logger.getLogger(PlacemarkComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedImage bimg = ImageIO.read(image);
            
            if(bimg.getWidth()>800 || bimg.getHeight()>600) {
                if(bimg.getWidth()>800 && bimg.getHeight()>600) {
                    if(bimg.getWidth()-800>bimg.getHeight()-600) {
                        float nw = 800;
                        float nh = (800*bimg.getHeight())/bimg.getWidth();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                        //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                    } else {
                        float nh = 600;
                        float nw = (600*bimg.getWidth())/bimg.getHeight();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                        //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                    }
                } else if(bimg.getWidth()>800) {
                    float nw = 800;
                    float nh = (800*bimg.getHeight())/bimg.getWidth();
                    scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                } else if(bimg.getHeight()>600) {
                    float nh = 600;
                    float nw = (600*bimg.getWidth())/bimg.getHeight();
                    scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                }
            } else {
                scaledBimg = bimg;
            }
            BufferedImage new_bimg = new BufferedImage(scaledBimg.getWidth(null),
                    scaledBimg.getHeight(null), BufferedImage.SCALE_SMOOTH);
            new_bimg.getGraphics().drawImage(scaledBimg, 0, 0, null);
            ImageIO.write(new_bimg, image.getName().split("\\.")[1], temp);
        } catch (IOException ex) {
            Logger.getLogger(PlacemarkComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String uri = "";
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(LoginManager.getPrimary());
        try {
            ContentCollection c = repo.getUserRoot();
            try {
                /*
                 * Remove file if it exists.
                 */
                ContentResource r = (ContentResource) c.removeChild(image.getName());
            } catch (Exception e) {
            }
            
            ContentResource r = (ContentResource) c.createChild(
                image.getName(), ContentNode.Type.RESOURCE);
            try {
                
                r.put(image);
                
                uri = "wlcontent:/"+r.getPath();
            } catch (IOException ex) {
                Logger.getLogger(PlacemarkComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(PlacemarkComponentProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uri;
        
    }
    
    /**
     * @inheritDoc()
     */
    public void restore() {
        // Restore from the originally stored values.
        nameTF.setText(origName);
        rotationTF.setText(origRotation);
        cspp.setMessage(origMessage);
        cspp.setImageURL(origImageURL);
        cspp.setBackColor(origBackColor);
        cspp.setTextColor(origTextColor);
    }

    private void checkDirty() {
        if (editor == null) {
            return;
        }

        boolean clean = nameTF.getText().equals(origName);
        clean = clean & rotationTF.getText().equals(origRotation);//////////////////////////////???????????????????????????
        clean = clean & cspp.getMessage().equals(origMessage);
        clean = clean & cspp.getImageURL().equals(origImageURL);
        if(cspp.getBackColor()!=null)
            clean = clean & cspp.getBackColor().equals(origBackColor);
        if(cspp.getTextColor()!=null)
            clean = clean & cspp.getTextColor().equals(origTextColor);
        editor.setPanelDirty(PlacemarkComponentProperties.class, !clean);
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class TextFieldListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }
    }
    
    class PanelPropChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            checkDirty();
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        nameTF = new javax.swing.JTextField();
        rotationTF = new javax.swing.JTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/placemarks/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("PlacemarkComponentProperties.jLabel1.text_1")); // NOI18N

        jLabel2.setText(bundle.getString("PlacemarkComponentProperties.jLabel2.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rotationTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 295, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(rotationTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        remove(jPanel1);
        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField nameTF;
    private javax.swing.JTextField rotationTF;
    // End of variables declaration//GEN-END:variables
}
