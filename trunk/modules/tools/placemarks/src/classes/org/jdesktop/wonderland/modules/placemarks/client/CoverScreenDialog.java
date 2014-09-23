/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.placemarks.client;

import com.jme.renderer.ColorRGBA;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JTabbedPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import static org.jdesktop.wonderland.modules.placemarks.client.AddEditPlacemarkJDialog.RET_CANCEL;
import static org.jdesktop.wonderland.modules.placemarks.client.AddEditPlacemarkJDialog.RET_OK;
import org.jdesktop.wonderland.modules.placemarks.common.GoToCoverScreenInfo;
import org.jdesktop.wonderland.modules.placemarks.common.LoginCoverScreenInfo;

/**
 *
 * @author Abhishek Upadhyay
 */
public class CoverScreenDialog extends javax.swing.JDialog {

    private int returnStatus = RET_CANCEL;
    
    private CoverScreenPropertyPanel loginPropertyPanel = null;
    private CoverScreenPropertyPanel goToPropertyPanel = null;
    
    private ColorRGBA origLoginBackgroundColor = ColorRGBA.black;
    private ColorRGBA origLoginTextColor = ColorRGBA.white;
    private String origLoginImageURL = "";
    private String origLoginMessage = "Teleporting. Please Wait...";
    
    private ColorRGBA origGoToBackgroundColor = ColorRGBA.black;
    private ColorRGBA origGoToTextColor = ColorRGBA.white;
    private String origGoToImageURL = "";
    private String origGoToMessage = "Teleporting. Please Wait...";
    private static ContentCollection grpusrRepo = null;
    
    LoginCoverScreenInfo loginInfo = null;
    GoToCoverScreenInfo goToInfo = null;
    
    /**
     * Creates new form CoverScreenDialog
     */
    public CoverScreenDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        loginPropertyPanel = new CoverScreenPropertyPanel();
        goToPropertyPanel = new CoverScreenPropertyPanel();
        
        propertyTab.addTab("<html><body><b>After Login</b></body></html>", loginPropertyPanel);
        propertyTab.addTab("<html><body><b>After Go-To-User</b></body></html>", goToPropertyPanel);
        
        loginPropertyPanel.getTitleCoverScreen().setVisible(false);
        goToPropertyPanel.getTitleCoverScreen().setVisible(false);
        loginPropertyPanel.getTitleCoverScreen().setOpaque(false);
        goToPropertyPanel.getTitleCoverScreen().setOpaque(false);

        saveCloseBut.setEnabled(false);
        applyBut.setEnabled(false);
        
        loginPropertyPanel.getBackColorPanel().addPropertyChangeListener(new CoverScreenPropertyChangeListener());
        loginPropertyPanel.getTextColorPanel().addPropertyChangeListener(new CoverScreenPropertyChangeListener());
        goToPropertyPanel.getBackColorPanel().addPropertyChangeListener(new CoverScreenPropertyChangeListener());
        goToPropertyPanel.getTextColorPanel().addPropertyChangeListener(new CoverScreenPropertyChangeListener());
        loginPropertyPanel.getMessageTf().getDocument().addDocumentListener(new CoverScreenPropertyDocumentListener());
        loginPropertyPanel.getImageTf().getDocument().addDocumentListener(new CoverScreenPropertyDocumentListener());
        goToPropertyPanel.getMessageTf().getDocument().addDocumentListener(new CoverScreenPropertyDocumentListener());
        goToPropertyPanel.getImageTf().getDocument().addDocumentListener(new CoverScreenPropertyDocumentListener());
    }
    private static ContentCollection getSystemContentRepository(ServerSessionManager serverSessionManager)
            throws ContentRepositoryException {

        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository cr = registry.getRepository(serverSessionManager);
        return cr.getSystemRoot();
    }
    private static ContentCollection getGroupUsersRepo() {
        try {
            if(grpusrRepo == null) {
                ContentCollection collection = getSystemContentRepository(LoginManager.getPrimary());
                ContentCollection grps = (ContentCollection) collection.getParent().getChild("groups");
                if(grps==null) {
                    grps = (ContentCollection) collection.getParent().createChild("groups", ContentNode.Type.COLLECTION);
                }
                ContentCollection grpusrs = (ContentCollection) grps.getChild("users");
                if(grpusrs == null) {
                    grpusrs = (ContentCollection) grps.createChild("users", ContentNode.Type.COLLECTION);
                }
                grpusrRepo = grpusrs;
                return grpusrs;
            } else {
                return grpusrRepo;
            }
        } catch (ContentRepositoryException ex) {
            Logger.getLogger(CoverScreenDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private void populateOrigValues() {
        
        if(loginInfo==null)
            loginInfo = PlacemarkUtils.getLoginCoverScreenInfo(getGroupUsersRepo());
        if(goToInfo==null)
            goToInfo = PlacemarkUtils.getGoToCoverScreenInfo(getGroupUsersRepo());
        
        if(loginInfo==null) {
            loginInfo = new LoginCoverScreenInfo();
        }
        if(goToInfo==null) {
            goToInfo = new GoToCoverScreenInfo();
        }
        if(loginInfo==null) {
            loginInfo = new LoginCoverScreenInfo();
        }
        if(goToInfo==null) {
            goToInfo = new GoToCoverScreenInfo();
        }
        
        loginPropertyPanel.setBackColor(loginInfo.getBackgroundColor());
        loginPropertyPanel.setTextColor(loginInfo.getTextColor());
        loginPropertyPanel.setImageURL(loginInfo.getImageURL());
        loginPropertyPanel.setMessage(loginInfo.getMessage());
        
        goToPropertyPanel.setBackColor(goToInfo.getBackgroundColor());
        goToPropertyPanel.setTextColor(goToInfo.getTextColor());
        goToPropertyPanel.setImageURL(goToInfo.getImageURL());
        goToPropertyPanel.setMessage(goToInfo.getMessage());
        
        origLoginBackgroundColor = loginInfo.getBackgroundColor();
        origLoginImageURL = loginInfo.getImageURL();
        origLoginMessage = loginInfo.getMessage();
        origLoginMessage = loginInfo.getMessage();
        
        origGoToBackgroundColor = goToInfo.getBackgroundColor();
        origGoToImageURL = goToInfo.getImageURL();
        origGoToMessage = goToInfo.getMessage();
        origGoToMessage = goToInfo.getMessage();
        
    }
    
    private class CoverScreenPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            checkDirty();
        }
        
    }
    private class CoverScreenPropertyDocumentListener implements DocumentListener {

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
    
    private boolean checkDirty() {
        boolean isdirty = false;
        if(propertyTab.getSelectedIndex()==0) {
            if(!origLoginBackgroundColor.equals(loginPropertyPanel.getBackColor())) {
                isdirty = true;
            }
            if(!origLoginTextColor.equals(loginPropertyPanel.getTextColor())) {
                isdirty = true;
            }
            if(!origLoginMessage.equals(loginPropertyPanel.getMessage())) {
                isdirty = true;
            }
            if(!origLoginImageURL.equals(loginPropertyPanel.getImageURL())) {
                isdirty = true;
            }
        } else {
            if(!origGoToBackgroundColor.equals(goToPropertyPanel.getBackColor())) {
                isdirty = true;
            }
            if(!origGoToTextColor.equals(goToPropertyPanel.getTextColor())) {
                isdirty = true;
            }
            if(!origGoToMessage.equals(goToPropertyPanel.getMessage())) {
                isdirty = true;
            }
            if(!origGoToImageURL.equals(goToPropertyPanel.getImageURL())) {
                isdirty = true;
            }
        }
        
        if(isdirty) {
            saveCloseBut.setEnabled(true);
            applyBut.setEnabled(true);
        } else {
            saveCloseBut.setEnabled(false);
            applyBut.setEnabled(false);
        }
        
        return isdirty;
    }
    
    public CoverScreenPropertyPanel getGoToPropertyPanel() {
        return goToPropertyPanel;
    }

    public CoverScreenPropertyPanel getLoginPropertyPanel() {
        return loginPropertyPanel;
    }

    public JTabbedPane getPropertyTab() {
        return propertyTab;
    }
    
    public CoverScreenPropertyPanel getActiveTabPanel () {
        CoverScreenPropertyPanel csp = (CoverScreenPropertyPanel) propertyTab.getSelectedComponent();
        return csp;
    }
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
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

    private void save() {
        if(propertyTab.getSelectedIndex()==0) {
            CoverScreenPropertyPanel login = (CoverScreenPropertyPanel) getActiveTabPanel();
            LoginCoverScreenInfo info = new LoginCoverScreenInfo();
            info.setBackgroundColor(login.getBackColor());
            File image = login.getImage();
            String uri=login.getImageURL();
            if(image!=null) {
                 uri = uploadImage(image);
            }
            loginPropertyPanel.setImageURL(uri);
            info.setImageURL(uri);
            info.setMessage(login.getMessage());
            info.setTextColor(login.getTextColor());
            
            PlacemarkUtils.setLoginCoverScreenInfo(info);
        } else {
            GoToCoverScreenInfo info = new GoToCoverScreenInfo();
            CoverScreenPropertyPanel goTo = (CoverScreenPropertyPanel) getActiveTabPanel();
            info.setBackgroundColor(goTo.getBackColor());
            String uri=goTo.getImageURL();
            File image = goTo.getImage();
            if(image!=null) {
                 uri = uploadImage(image);
            }
            goToPropertyPanel.setImageURL(uri);
            info.setImageURL(uri);
            info.setMessage(goTo.getMessage());
            info.setTextColor(goTo.getTextColor());
            
            PlacemarkUtils.setGoToCoverScreenInfo(info,LoginManager.getPrimary());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        saveCloseBut = new javax.swing.JButton();
        applyBut = new javax.swing.JButton();
        propertyTab = new javax.swing.JTabbedPane();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        saveCloseBut.setText("Save & Close");
        saveCloseBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveCloseButActionPerformed(evt);
            }
        });

        applyBut.setText("Apply");
        applyBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButActionPerformed(evt);
            }
        });

        propertyTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                propertyTabStateChanged(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("<html><body><i><b>NOTE : </b>Cover screens are "
            + "displayed while objects are loading. Use this dialog to "
            + "edit Login and Go-To-User cover screens. Edit portal and "
            + "placemark cover screens using the portal and placemark "
            + "property sheets in the Object Editor window.</i></body></html>");
        jLabel2.setBorder(javax.swing.BorderFactory.createMatteBorder(2, 0, 0, 0, new java.awt.Color(102, 102, 102)));
        jLabel2.setPreferredSize(new java.awt.Dimension(390, 86));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(applyBut)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveCloseBut)))
                        .addGap(14, 14, 14))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(propertyTab)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(propertyTab, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveCloseBut)
                    .addComponent(jButton1)
                    .addComponent(applyBut))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        doClose(RET_CANCEL);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void saveCloseButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCloseButActionPerformed
        // TODO add your handling code here:
        save();
        loginInfo = null;
        goToInfo = null;
        populateOrigValues();
        checkDirty();
        doClose(RET_OK);
    }//GEN-LAST:event_saveCloseButActionPerformed

    private void propertyTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_propertyTabStateChanged
        // TODO add your handling code here:
        populateOrigValues();
        checkDirty();
    }//GEN-LAST:event_propertyTabStateChanged

    private void applyButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButActionPerformed
        // TODO add your handling code here:
        save();
        loginInfo = null;
        goToInfo = null;
        populateOrigValues();
        checkDirty();
    }//GEN-LAST:event_applyButActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoverScreenDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CoverScreenDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CoverScreenDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CoverScreenDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                CoverScreenDialog dialog = new CoverScreenDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBut;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTabbedPane propertyTab;
    private javax.swing.JButton saveCloseBut;
    // End of variables declaration//GEN-END:variables
}
