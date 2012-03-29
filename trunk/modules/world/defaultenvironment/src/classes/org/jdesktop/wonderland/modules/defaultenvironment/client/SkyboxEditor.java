
/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */

package org.jdesktop.wonderland.modules.defaultenvironment.client;

import com.jme.image.Texture;
import com.jme.scene.Skybox;
import com.jme.util.TextureManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedSkybox;

/**
 *
 * @author JagWire
 */
public class SkyboxEditor extends javax.swing.JPanel {
    private static final Logger LOGGER = Logger.getLogger(SkyboxEditor.class.getName());

    /** Creates new form SkyboxProperties */
    
    private String originalName = "No name";
    private String originalDescription = "Vanilla Skybox";
    private String originalNorth = "wla://defaultenvironment/skybox1/1.jpg";
    private String originalSouth = "wla://defaultenvironment/skybox1/3.jpg";
    private String originalEast = "wla://defaultenvironment/skybox1/2.jpg";
    private String originalWest = "wla://defaultenvironment/skybox1/4.jpg";
    private String originalUp = "wla://defaultenvironment/skybox1/6.jpg";
    private String originalDown = "wla://defaultenvironment/skybox1/5.jpg";
    
    private String name = "";
    private String description = "";
    private String northURI = "";
    private String southURI = "";
    private String eastURI = "";
    private String westURI = "";
    private String upURI = "";
    private String downURI = "";
    
    private SharedSkybox skybox = null;
    private SkyboxProperties skyboxProperties = null;
    
    private JFileChooser fileChooser = new JFileChooser();
//    private SkyboxChooser chooser;
    private JDialog dialog;
    
    public SkyboxEditor(SkyboxProperties skyboxProperties, SharedSkybox skybox,  JDialog dialog) {
        initComponents();
        initializeTextFieldListeners();
        this.dialog = dialog;
        this.skybox = skybox;
        this.skyboxProperties = skyboxProperties;
        
        originalName = skybox.getId();
        originalDescription = skybox.getDescription();
        originalNorth = skybox.getNorth();
        originalSouth = skybox.getSouth();
        originalEast = skybox.getEast();
        originalWest = skybox.getWest();
        originalUp = skybox.getUp();
        originalDown = skybox.getDown();                  
        
        //initialize active variables.
        name = skybox.getId();
        description = skybox.getDescription();
        northURI = skybox.getNorth();
        southURI = skybox.getSouth();
        eastURI = skybox.getEast();
        westURI = skybox.getWest();
        upURI = skybox.getUp();
        downURI = skybox.getDown();
        
        //set fields so we can see the active URIs in the textboxes.
        nameField.setText(name);
        descriptionField.setText(description);
        northField.setText(northURI);
        southField.setText(southURI);
        eastField.setText(eastURI);
        westField.setText(westURI);
        upField.setText(upURI);
        downField.setText(downURI);
    }
    
    /**
     * Registers listeners to fire when a user types in the respective text
     * boxes.
     * 
     */
    private void initializeTextFieldListeners() {
        
        nameField.getDocument().addDocumentListener(new DocumentListener() { 
            public void insertUpdate(DocumentEvent de) {
                nameFieldChanged(de);
            }
            
            public void removeUpdate(DocumentEvent de) {
                nameFieldChanged(de);
            }
            
            public void changedUpdate(DocumentEvent de) {
                nameFieldChanged(de);
            }
            
        });
        
        descriptionField.getDocument().addDocumentListener(new DocumentListener() { 
        
            public void insertUpdate(DocumentEvent de) {
                descriptionFieldChanged(de);                
            }
            
            public void removeUpdate(DocumentEvent de) {
                descriptionFieldChanged(de);
            }
            
            public void changedUpdate(DocumentEvent de) {
                descriptionFieldChanged(de);
            }
        });
               
        northField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                northFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                northFieldChanged(de);
            }

            public void changedUpdate(DocumentEvent de) {
                northFieldChanged(de);
            }
        
        });
        
        southField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                southFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                southFieldChanged(de);
            }

            public void changedUpdate(DocumentEvent de) {
                southFieldChanged(de);
            }
        
        });
            
        eastField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                eastFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                eastFieldChanged(de);
            }   

            public void changedUpdate(DocumentEvent de) {
                eastFieldChanged(de);
            }
        
        });
        
        westField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                westFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                westFieldChanged(de);
            }

            public void changedUpdate(DocumentEvent de) {
                westFieldChanged(de);
            }
        
        });
        
        upField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                upFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                upFieldChanged(de);
            }

            public void changedUpdate(DocumentEvent de) {
                upFieldChanged(de);
            }
        
        });
        
        downField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent de) {
                downFieldChanged(de);
            }

            public void removeUpdate(DocumentEvent de) {
                downFieldChanged(de);
            }

            public void changedUpdate(DocumentEvent de) {
                downFieldChanged(de);
            }
        
        });
        
    }
    
    public void update() {
        if (isActiveSkybox()) {
            final Skybox skybox = buildSkybox();
            final DefaultEnvironmentRenderer renderer =
                            skyboxProperties.getDefaultEnvironmentRenderer();

            SceneWorker.addWorker(new WorkCommit() {

                public void commit() {
                    renderer.updateSkybox(skybox);
                }
            });
        }
    }

    private Skybox buildSkybox() {
        Skybox skybox = new Skybox("skybox", 1000, 1000, 1000);
        try {
            
            LOGGER.fine("Acquiring URLs");
            final URL northURL = AssetUtils.getAssetURL(northURI);
            final URL southURL = AssetUtils.getAssetURL(southURI);
            final URL eastURL = AssetUtils.getAssetURL(eastURI);
            final URL westURL = AssetUtils.getAssetURL(westURI);
            final URL downURL = AssetUtils.getAssetURL(downURI);
            final URL upURL = AssetUtils.getAssetURL(upURI);
            
            LOGGER.fine("URLs acquired. Building textures.");
            Texture north = TextureManager.loadTexture(northURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture south = TextureManager.loadTexture(southURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture east = TextureManager.loadTexture(eastURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture west = TextureManager.loadTexture(westURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture up = TextureManager.loadTexture(upURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            Texture down = TextureManager.loadTexture(downURL, Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);                        
         
            LOGGER.fine("Textures built. Setting fields.");
            skybox.setTexture(Skybox.Face.North, north);
            skybox.setTexture(Skybox.Face.West, west);
            skybox.setTexture(Skybox.Face.South, south);
            skybox.setTexture(Skybox.Face.East, east);
            skybox.setTexture(Skybox.Face.Up, up);
            skybox.setTexture(Skybox.Face.Down, down);

            LOGGER.fine("Fields set. Skybox finished.");

        } catch (MalformedURLException ex) {
            Logger.getLogger(SkyboxEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return skybox;
    }
    
    private void setDirty(boolean dirty) {
        if(dirty) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }
    
    private boolean isActiveSkybox() {
        
        String active = skyboxProperties.getActiveSkyboxName();
        if(active.equals(originalName)) 
            return true;
        
        return false;
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        northField = new javax.swing.JTextField();
        northButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        southField = new javax.swing.JTextField();
        southButton = new javax.swing.JButton();
        eastField = new javax.swing.JTextField();
        eastButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        westField = new javax.swing.JTextField();
        upField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        downField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        westButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        descriptionField = new javax.swing.JTextField();

        northField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                northFieldActionPerformed(evt);
            }
        });

        northButton.setText("Browse");
        northButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                northButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("North/Right/1:");
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 16));
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 16));

        jLabel2.setText("South/Left/3:");

        southField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                southFieldActionPerformed(evt);
            }
        });

        southButton.setText("Browse");
        southButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                southButtonActionPerformed(evt);
            }
        });

        eastField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eastFieldActionPerformed(evt);
            }
        });

        eastButton.setText("Browse");
        eastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eastButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("East/Back/2:");

        jLabel4.setText("Up/Top/6:");

        westField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                westFieldActionPerformed(evt);
            }
        });

        upField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("West/Front/4:");

        downField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Down/Bottom/5:");

        westButton.setText("Browse");
        westButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                westButtonActionPerformed(evt);
            }
        });

        upButton.setText("Browse");
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        downButton.setText("Browse");
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Add Skybox Name:");

        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        jLabel8.setText("Add Short Description:");

        descriptionField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptionFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeButton)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(eastField, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(eastButton))
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(southField, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(southButton))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(westField, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(westButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(downField, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(downButton))
                            .addComponent(jLabel6)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(upField, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(upButton))
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(nameField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(northField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                                    .addComponent(descriptionField, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(northButton))
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addContainerGap(20, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(northField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(northButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eastField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eastButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(southField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(southButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(westField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(westButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(downField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(upField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(upButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(okButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    //<editor-fold defaultstate="collapsed" desc="event handlers">
    
    private void northFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_northFieldActionPerformed
        northURI = northField.getText();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_northFieldActionPerformed

    private void southButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_southButtonActionPerformed
        // TODO add your handling code here:
        pickFile(southField);
        southURI = southField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
        
    }//GEN-LAST:event_southButtonActionPerformed

    private void eastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eastButtonActionPerformed
        // TODO add your handling code here:
        pickFile(eastField);
        eastURI = eastField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_eastButtonActionPerformed

    private void northButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_northButtonActionPerformed
        // TODO add your handling code here:
        pickFile(northField);
        northURI = northField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_northButtonActionPerformed

    private void westButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_westButtonActionPerformed
        // TODO add your handling code here:
        pickFile(westField);
        westURI = westField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_westButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        // TODO add your handling code here:
        pickFile(upField);
        upURI = upField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        // TODO add your handling code here:
        pickFile(downField);
        downURI = downField.getText();
        update();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_downButtonActionPerformed

    private void southFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_southFieldActionPerformed
        southURI = southField.getText();
        if(isDirty()) {           
            setDirty(true);           
        }
    }//GEN-LAST:event_southFieldActionPerformed

    private void eastFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eastFieldActionPerformed
        eastURI = eastField.getText();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_eastFieldActionPerformed

    private void westFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_westFieldActionPerformed
        westURI = westField.getText();
        if(isDirty()) {
            setDirty(true);

        }
    }//GEN-LAST:event_westFieldActionPerformed

    private void upFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upFieldActionPerformed
        upURI = upField.getText();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_upFieldActionPerformed

    private void downFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downFieldActionPerformed
        downURI = downField.getText();
        if(isDirty()) {
            setDirty(true);
        }
        
    }//GEN-LAST:event_downFieldActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // TODO add your handling code here:

        if (isDirty()) {

            if (skyboxProperties.alreadyContainsSkybox(name)) {
                skyboxProperties.updateSkybox(name,
                        description,
                        northURI,
                        southURI,
                        eastURI,
                        westURI,
                        upURI,
                        downURI);
            } else {
                skyboxProperties.addSkyboxToList(new SharedSkybox(name,
                        description,
                        northURI,
                        southURI,
                        eastURI,
                        westURI,
                        upURI,
                        downURI));
            }
            skyboxProperties.setDirtyFromSkyboxDialog();
        }

        dialog.setVisible(false);
    }//GEN-LAST:event_okButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // TODO add your handling code here:
        
        //do nothing to SkyboxListProperties.java
        
        dialog.setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // TODO add your handling code here:
        name = nameField.getText();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_nameFieldActionPerformed

    private void descriptionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionFieldActionPerformed
        // TODO add your handling code here:
        description = descriptionField.getText();
        if(isDirty()) {
            setDirty(true);
        }
    }//GEN-LAST:event_descriptionFieldActionPerformed

    private void nameFieldChanged(DocumentEvent e) {
        name = nameField.getText();
        if(isDirty()) {
            setDirty(true);
        } else {
            setDirty(false);
        }
    }
    
    private void descriptionFieldChanged(DocumentEvent e) {
        description = descriptionField.getText();
        if(isDirty()) {
            setDirty(true);
        } else {
            setDirty(false);
        }
    }
    
    private void northFieldChanged(DocumentEvent e) {
       northURI = northField.getText();
        if(isDirty()) {
            setDirty(true);
        } else {
//            editor.setPanelDirty(DefaultEnvironmentProperties.class, false);
            setDirty(false);
        }       
    }
    
    private void southFieldChanged(DocumentEvent e) {
        southURI = southField.getText();
        if(isDirty()) {
            setDirty(true);
           
        } else {
            setDirty(false);
        }        
    }
    
    private void eastFieldChanged(DocumentEvent e) {
        eastURI = eastField.getText();
        if(isDirty()) {
            setDirty(true);
           
        } else {
            setDirty(false);
        }        
    }
    
    private void westFieldChanged(DocumentEvent e) {
        westURI = westField.getText();
        if(isDirty()) {
            setDirty(true);
           
        } else {
            setDirty(false);
        }          
    }
    
    private void upFieldChanged(DocumentEvent e) {
        upURI = upField.getText();
        if(isDirty()) {
            setDirty(true);
           
        } else {
            setDirty(false);
        }        
    }
    
    private void downFieldChanged(DocumentEvent e) {
        downURI = downField.getText();
        if(isDirty()) {
            setDirty(true);
           
        } else {
            setDirty(false);
        }  
    }
    
    
    //</editor-fold>
    
    private void pickFile(JTextField textField) {
        
        File file = null;
        int returned = fileChooser.showDialog(this, "Choose image");
        if(returned == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            fileChooser.setCurrentDirectory(new File(file.getParent()));
             
            String url = uploadFile(file);
            textField.setText(url);
        }
        else {
            LOGGER.fine("File choosing canceled.");
        }

    }
    
    private String uploadFile(File file) {
        String fileName = file.getName();
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(LoginManager.getPrimary());
        ContentCollection userRoot;

        // First try to find the resource, if it exists, then simply upload the
        // new bits. Otherwise create the resource and upload the new bits
        try {
            userRoot = repo.getUserRoot();
            ContentNode node = (ContentNode)userRoot.getChild(fileName);
            if (node == null) {
                node = (ContentNode)userRoot.createChild(fileName, Type.RESOURCE);
            }
            ((ContentResource)node).put(file);
        } catch (ContentRepositoryException excp) {
            LOGGER.log(Level.WARNING, "Error uploading", excp);
        } catch(IOException ie) {
            LOGGER.log(Level.WARNING, "Error uploading", ie);
        }

        // If we have reached here, then we have successfully uploaded the bits
        // so we return a valid URI to the content.
        return "wlcontent://users/" + LoginManager.getPrimary().getUsername() + "/" + fileName;
    
    }
      
    public void close() {
        
    }
    
    public void restore() {
//            SharedMapCli map = cell.getSkyboxMap();
            
            //XXX commented out strictly for compilation
//            map.put("textures", SharedSkybox.valueOf(originalNorth,
//                                                     originalSouth,
//                                                     originalEast,
//                                                     originalWest,
//                                                     originalUp,
//                                                     originalDown));
            
            northField.setText(originalNorth);
            southField.setText(originalSouth);
            eastField.setText(originalEast);
            westField.setText(originalWest);
            upField.setText(originalUp);
            downField.setText(originalDown);
            
//            map.put("north", SharedString.valueOf(originalNorth));
//            map.put("south", SharedString.valueOf(originalSouth));
//            map.put("east", SharedString.valueOf(originalEast));
//            map.put("west", SharedString.valueOf(originalWest));
//            map.put("up", SharedString.valueOf(originalUp));
//            map.put("down", SharedString.valueOf(originalDown));
    }
    
    public void apply() {
        if(isDirty())  {
            skyboxProperties.addSkyboxToList(new SharedSkybox(name,
                                                              description,
                                                              northURI,
                                                              southURI,
                                                              eastURI,
                                                              westURI,
                                                              upURI,
                                                              downURI));
                                                            
//            SharedMapCli map = cell.getSkyboxMap();
  
            //XXX Commented out strictly for compilation
//            map.put("textures", SharedSkybox.valueOf(northURI,
//                                                     southURI,
//                                                     eastURI,
//                                                     westURI,
//                                                     upURI,
//                                                     downURI));
            
            
            
//            map.put("north", SharedString.valueOf(northURI));
//            map.put("south", SharedString.valueOf(southURI));
//            map.put("east", SharedString.valueOf(eastURI));
//            map.put("west", SharedString.valueOf(westURI));
//            map.put("up", SharedString.valueOf(upURI));
//            map.put("down", SharedString.valueOf(downURI));                        
        }
    }    
    
    public boolean isDirty() {
        return !originalName.equals(name) //if name doesn't equal name OR
                || !originalDescription.equals(description) //if description doesn't equal description
                || !originalNorth.equals(northURI) //if north doesn't equal north' OR
                || !originalSouth.equals(southURI)// south doesn't equal south' OR
                || !originalEast.equals(eastURI)// east doesn't equal east' OR
                || !originalWest.equals(westURI)// west doesn't equal west' OR
                || !originalUp.equals(upURI)// up doesn't equal up' OR
                || !originalDown.equals(downURI);// down doesn't equal down', return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JButton downButton;
    private javax.swing.JTextField downField;
    private javax.swing.JButton eastButton;
    private javax.swing.JTextField eastField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton northButton;
    private javax.swing.JTextField northField;
    private javax.swing.JButton okButton;
    private javax.swing.JButton southButton;
    private javax.swing.JTextField southField;
    private javax.swing.JButton upButton;
    private javax.swing.JTextField upField;
    private javax.swing.JButton westButton;
    private javax.swing.JTextField westField;
    // End of variables declaration//GEN-END:variables
}
