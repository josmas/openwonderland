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

import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.jme.SceneWorker;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedDirectionLight;

/**
 *
 * @author JagWire
 */
public class DirectionalLightPropertiesPanel extends javax.swing.JPanel {

    private Logger LOGGER = Logger.getLogger(DirectionalLightPropertiesPanel.class.getName());
    /** Creates new form LightPropertiesPanel */
    
    private DefaultEnvironmentCell cell;
    private LightNode lightNode;
    private DirectionalLightViewerEntity lightViewer = null;
    private String name = "LIGHT";
    private CellPropertiesEditor editor = null;
    
    
    
    private ColorRGBA originalAmbient = new ColorRGBA();
    private ColorRGBA originalDiffuse = new ColorRGBA();
    private ColorRGBA originalSpecular = new ColorRGBA();
    private boolean originalCast = false;
    private Vector3f originalPosition = new Vector3f();
    private Vector3f originalDirection = new Vector3f();
    
    
    private ColorRGBA ambient = ColorRGBA.white;
    private ColorRGBA diffuse = ColorRGBA.white;
    private ColorRGBA specular = ColorRGBA.white;
    private boolean castShadows = false;
    private Vector3f position = new Vector3f();
    private Vector3f direction = new Vector3f();
    
    
    //spinner models
    SpinnerNumberModel positionX = new SpinnerNumberModel();
    SpinnerNumberModel positionY = new SpinnerNumberModel();
    SpinnerNumberModel positionZ = new SpinnerNumberModel();
    
    SpinnerNumberModel directionX = new SpinnerNumberModel();
    SpinnerNumberModel directionY = new SpinnerNumberModel();
    SpinnerNumberModel directionZ = new SpinnerNumberModel();
                
    public DirectionalLightPropertiesPanel(LightNode lightNode, DefaultEnvironmentCell cell, String name, CellPropertiesEditor editor) {
        initComponents();
        
        this.lightNode = lightNode;
        this.cell = cell;
        this.name = name;
        this.editor = editor;
        DirectionalLight light = (DirectionalLight)lightNode.getLight();
        initializeButtonIcons(light.getAmbient(),
                              light.getDiffuse(),
                              light.getSpecular());
        
        initializeSpinners(lightNode.getLocalTranslation().x,
                           lightNode.getLocalTranslation().y,
                           lightNode.getLocalTranslation().z,
                           light.getDirection().x,
                           light.getDirection().y,
                           light.getDirection().z);
        castShadowsBox.setSelected(lightNode.getLight().isShadowCaster());
        
        originalAmbient = light.getAmbient();
        originalDiffuse = light.getDiffuse();
        originalSpecular = light.getSpecular();
        originalCast = light.isShadowCaster();
        originalPosition = lightNode.getLocalTranslation();
        originalDirection = light.getDirection();
        
        ambient = originalAmbient;
        diffuse = originalDiffuse;
        specular = originalSpecular;
        castShadows = originalCast;
        position = originalPosition;
        direction = originalDirection;
        

    }
    
//    public LightPropertiesPanel() {
//        initComponents();
//        initializeButtonIcons(ColorRGBA.white, ColorRGBA.white, ColorRGBA.white);
//        initializeSpinners(0, 0, 0,
//                           0, 0, 0); //star with 0 values in spinners
//        castShadowsBox.setSelected(false);
//        
//    }
    
    
    
//    public LightPropertiesPanel(ColorRGBA ambient,
//                                ColorRGBA diffuse,
//                                ColorRGBA specular,
//                                boolean castShadows,
//                                Vector3f position,
//                                Vector3f direction) {
//        initComponents();
//        initializeButtonIcons(ambient, diffuse, specular);
//        initializeSpinners(position.x, position.y, position.z,
//                           direction.x, direction.y, direction.z);
////        Number n = null;
//        castShadowsBox.setSelected(castShadows);
//        
//                originalAmbient = ambient;
//        originalDiffuse = diffuse;
//        originalSpecular = specular;
//        originalCast = castShadows;
//        originalPosition = position;
//        
//        
//    }
    
    private void initializeButtonIcons(ColorRGBA ambient,
                                ColorRGBA diffuse,
                                ColorRGBA specular) {
        ambientButton.setMinimumSize(new Dimension(49, 18));
        ambientButton.setPreferredSize(new Dimension(49, 18));
        ambientButton.setSize(new Dimension(49, 18));
        ambientButton.setIcon(new ColorSwatch(ambient));
        
        diffuseButton.setMinimumSize(new Dimension(49, 18));
        diffuseButton.setPreferredSize(new Dimension(49, 18));
        diffuseButton.setSize(new Dimension(49, 18));
        diffuseButton.setIcon(new ColorSwatch(diffuse));
        
        specularButton.setMinimumSize(new Dimension(49, 18));
        specularButton.setPreferredSize(new Dimension(49, 18));
        specularButton.setSize(new Dimension(49, 18));
        specularButton.setIcon(new ColorSwatch(specular));
        
    }
    
    private void initializeSpinners(float pX, float pY, float pZ,
                                    float dX, float dY, float dZ) {
        positionXSpinner.setModel(positionX);
        positionYSpinner.setModel(positionY);
        positionZSpinner.setModel(positionZ);
        
        directionXSpinner.setModel(directionX);
        directionYSpinner.setModel(directionY);
        directionZSpinner.setModel(directionZ);
   
        positionX.setValue(new Float(pX));
        positionY.setValue(new Float(pY));
        positionZ.setValue(new Float(pZ));
        
        positionX.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                position.setX(positionX.getNumber().floatValue());
                update();
                
            }
        });
        
        positionY.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                position.setY(positionY.getNumber().floatValue());
                update();
               
            }
        });
        
        positionZ.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                position.setZ(positionZ.getNumber().floatValue());
                update();
            }
        });
        
        directionX.setValue(new Float(dX));
        directionY.setValue(new Float(dY));
        directionZ.setValue(new Float(dZ));
        
        directionX.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                direction.setY(directionX.getNumber().floatValue());
                update();
            }
        });
        
        directionY.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                direction.setY(directionY.getNumber().floatValue());
            }
        });
        
        directionZ.addChangeListener(new ChangeListener() { 
            public void stateChanged(ChangeEvent ce) {
                direction.setZ(directionZ.getNumber().floatValue());
            }
        });

    }
    
    
    public ColorRGBA ColorToColorRGBA(Color c) {
        float[] rgb = new float[3];
        rgb = c.getRGBComponents(null);
        LOGGER.info("Converting Color:\n"
                + "\tred: "+c.getRGBColorComponents(null)[0]+"\n"
                + "\tgreen: "+c.getRGBColorComponents(null)[1]+"\n"
                + "\tblue: "+c.getRGBColorComponents(null)[2]);
//                        );
        return new ColorRGBA(rgb[0], rgb[1], rgb[2], 1.0f);
    }
    
    public Color ColorRGBAtoColor(ColorRGBA in) {
        LOGGER.info("Converting from ColorRGBA:\n"
                + "\tred: "+in.r+"\n"
                + "\tgreen: "+in.g+"\n"
                + "\tblue: "+in.b);            
        return new Color(in.r, in.g, in.b, in.a);
    }

    public ColorRGBA getAmbient() {
        return ambient;
    }

    public boolean isCastShadows() {
        return castShadows;
    }

    public ColorRGBA getDiffuse() {
        return diffuse;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getPosition() {
        return position;
    }

    public ColorRGBA getSpecular() {
        return specular;
    }
        
    /**
     * Checks to see if the current values and the original values equate.
     * 
     * @return 
     */
    public boolean isDirty() {
        return !areColorsTheSame(originalAmbient, ambient)
                || !areColorsTheSame(originalDiffuse, diffuse)
                || !areColorsTheSame(originalSpecular, specular)
                || !areVectorsTheSame(originalDirection, direction)
                || !areVectorsTheSame(originalPosition, position)
                || !(originalCast == castShadows);
        
        
    }
    /**
     * Checks to see if the RGBA values of the first are the same as the second.
     * 
     * @param first
     * @param second
     * @return 
     */
    private boolean areColorsTheSame(ColorRGBA first, ColorRGBA second) {
        if(first == null || second == null)
            return false;
        
        //boolean check = true;
        if(first.r != second.r)
            return false;
        
        if(first.g != second.g)
            return false;
        
        if(first.b != second.b)
            return false;
        
        if(first.a != second.a)
            return false;
        
        return true;
    }
    /**
     * Checks to see if XYZ values of the first are the same as the second.
     * @param first
     * @param second
     * @return 
     */
    private boolean areVectorsTheSame(Vector3f first, Vector3f second) {
        if(first == null || second == null)
            return false;
        
        
        if(first.x != second.x)
            return false;
        
        if(first.y != second.y)
            return false;
        
        if(first.z != second.z)
            return false;
        
        return true;
                    
    }
    
    public LightNode reconstructLight() {
        LightNode node = new LightNode();
        DirectionalLight light = new DirectionalLight();
        
        light.setAmbient(ambient);
        light.setDiffuse(diffuse);
        light.setSpecular(specular);
        light.setShadowCaster(castShadows);
        light.setDirection(direction);
        
        node.setLight(light);
        node.setLocalTranslation(position);
        
        return node;
    }
    
    public SharedDirectionLight reconstructState() {
        
        return new SharedDirectionLight(ambient,
                                        diffuse,
                                        specular,
                                        position,
                                        direction,
                                        castShadows);
    }
    
    public void update() {
        
        editor.setPanelDirty(DefaultEnvironmentProperties.class, isDirty());
        
        SceneWorker.addWorker(new WorkCommit() {

            public void commit() {
                DirectionalLight dl = (DirectionalLight)lightNode.getLight();
                
                
                dl.setAmbient(ambient);
                dl.setDiffuse(diffuse);
                dl.setSpecular(specular);
                dl.setShadowCaster(castShadows);
                dl.setDirection(direction);
                lightNode.setLocalTranslation(position);
                showLight();
            }
        });
    }
    
    public void close() {
        if(lightViewer != null) {
            lightViewer.dispose();
            lightViewer = null;
        }
    }
    
    public void restore() {
        //XXX should we assign these directly to the original references
        // or should we clone the original objects instead?
        ambient = originalAmbient;
        diffuse = originalDiffuse;
        specular = originalSpecular;
        position = originalPosition;
        direction = originalDirection;
        castShadows = originalCast;
        
        showLight();
    }
    
    public void applyIfNeeded() {
        if(isDirty()) {
            LOGGER.log(Level.INFO, "APPLYING {0}", name);
            apply();
        }
    }
    
    protected void apply() {
        
        cell.getSharedLightMap().put(name, reconstructState());
        
        hideLight();
    }
    
        /**
     * Highly adapted from MicrophoneComponentProperties.
     * Thanks to author Joe Provino
     */
    private void showLight() {
        if(lightViewer != null) {
            lightViewer.dispose();
            lightViewer = null;
        }

        if(!viewLightBox.isSelected()) {
           return;
        }
        lightViewer = new DirectionalLightViewerEntity(cell);
//        Vector3f origin = new Vector3f();//getCellTranslation();
//        SpinnerNumberModel snm = (SpinnerNumberModel)radiusSpinner.getModel();
        lightViewer.showLight(reconstructLight(), name);
    }
    private void hideLight() {
        if(lightViewer != null) {
            lightViewer.dispose();
            lightViewer = null;

            viewLightBox.setSelected(false);
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

        jSpinner1 = new javax.swing.JSpinner();
        directionLabel = new javax.swing.JLabel();
        directionXLabel = new javax.swing.JLabel();
        directionYLabel = new javax.swing.JLabel();
        directionZLabel = new javax.swing.JLabel();
        positionLabel = new javax.swing.JLabel();
        directionXSpinner = new javax.swing.JSpinner();
        directionYSpinner = new javax.swing.JSpinner();
        directionZSpinner = new javax.swing.JSpinner();
        positionXLabel = new javax.swing.JLabel();
        positionYLabel = new javax.swing.JLabel();
        positionZLabel = new javax.swing.JLabel();
        positionXSpinner = new javax.swing.JSpinner();
        positionYSpinner = new javax.swing.JSpinner();
        positionZSpinner = new javax.swing.JSpinner();
        ambientLabel = new javax.swing.JLabel();
        diffuseLabel = new javax.swing.JLabel();
        specularLabel = new javax.swing.JLabel();
        ambientButton = new javax.swing.JButton();
        diffuseButton = new javax.swing.JButton();
        specularButton = new javax.swing.JButton();
        castShadowsBox = new javax.swing.JCheckBox();
        viewLightBox = new javax.swing.JCheckBox();

        directionLabel.setText("Direction:");

        directionXLabel.setText("X:");

        directionYLabel.setText("Y:");

        directionZLabel.setText("Z:");

        positionLabel.setText("Position:");

        positionXLabel.setText("X:");

        positionYLabel.setText("Y:");

        positionZLabel.setText("Z:");

        ambientLabel.setText("Ambient:");

        diffuseLabel.setText("Diffuse:");

        specularLabel.setText("Specular:");

        ambientButton.setMaximumSize(new java.awt.Dimension(49, 18));
        ambientButton.setMinimumSize(new java.awt.Dimension(49, 18));
        ambientButton.setPreferredSize(new java.awt.Dimension(49, 18));
        ambientButton.setSize(new java.awt.Dimension(49, 18));
        ambientButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ambientButtonActionPerformed(evt);
            }
        });

        diffuseButton.setMaximumSize(new java.awt.Dimension(49, 18));
        diffuseButton.setMinimumSize(new java.awt.Dimension(49, 18));
        diffuseButton.setPreferredSize(new java.awt.Dimension(49, 18));
        diffuseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                diffuseButtonActionPerformed(evt);
            }
        });

        specularButton.setMaximumSize(new java.awt.Dimension(49, 18));
        specularButton.setMinimumSize(new java.awt.Dimension(49, 18));
        specularButton.setPreferredSize(new java.awt.Dimension(49, 18));
        specularButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specularButtonActionPerformed(evt);
            }
        });

        castShadowsBox.setText("Casts Shadows");

        viewLightBox.setText("Show Lights");
        viewLightBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewLightBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ambientLabel)
                    .addComponent(directionXLabel)
                    .addComponent(directionLabel)
                    .addComponent(directionYLabel)
                    .addComponent(directionZLabel)
                    .addComponent(diffuseLabel)
                    .addComponent(specularLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(viewLightBox)
                    .addComponent(castShadowsBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(directionZSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(directionYSpinner, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(directionXSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(positionXLabel)
                            .addComponent(positionLabel)
                            .addComponent(positionYLabel)
                            .addComponent(positionZLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(positionXSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                            .addComponent(positionZSpinner)
                            .addComponent(positionYSpinner)))
                    .addComponent(ambientButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(diffuseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(specularButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(191, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directionLabel)
                    .addComponent(positionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(directionXLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(positionXLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(positionXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(directionXSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(directionYLabel)
                            .addComponent(directionYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(positionYLabel)
                            .addComponent(positionYSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(directionZSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(directionZLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(positionZSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(positionZLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ambientLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ambientButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(diffuseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(specularButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(castShadowsBox))
                    .addComponent(diffuseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(specularLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(viewLightBox)
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ambientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ambientButtonActionPerformed
        // TODO add your handling code here:
        Color c = JColorChooser.showDialog(this, "Choose Ambient Color", Color.white);
        ambientButton.setIcon(new ColorSwatch(c));
        ambient = ColorToColorRGBA(c);
        update();
    }//GEN-LAST:event_ambientButtonActionPerformed

    private void diffuseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_diffuseButtonActionPerformed
        // TODO add your handling code here:
        Color c = JColorChooser.showDialog(this, "Choose Diffuse Color", Color.white);
        diffuseButton.setIcon(new ColorSwatch(c));
        diffuse = ColorToColorRGBA(c);
        update();
    }//GEN-LAST:event_diffuseButtonActionPerformed

    private void specularButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specularButtonActionPerformed
        // TODO add your handling code here:
        Color c = JColorChooser.showDialog(this, "Choose Specular Color", Color.white);
        specularButton.setIcon(new ColorSwatch(c));
        specular = ColorToColorRGBA(c);
        update();
    }//GEN-LAST:event_specularButtonActionPerformed

    private void viewLightBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLightBoxActionPerformed
        // TODO add your handling code here:
        showLight();  
    }//GEN-LAST:event_viewLightBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ambientButton;
    private javax.swing.JLabel ambientLabel;
    private javax.swing.JCheckBox castShadowsBox;
    private javax.swing.JButton diffuseButton;
    private javax.swing.JLabel diffuseLabel;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JLabel directionXLabel;
    private javax.swing.JSpinner directionXSpinner;
    private javax.swing.JLabel directionYLabel;
    private javax.swing.JSpinner directionYSpinner;
    private javax.swing.JLabel directionZLabel;
    private javax.swing.JSpinner directionZSpinner;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JLabel positionXLabel;
    private javax.swing.JSpinner positionXSpinner;
    private javax.swing.JLabel positionYLabel;
    private javax.swing.JSpinner positionYSpinner;
    private javax.swing.JLabel positionZLabel;
    private javax.swing.JSpinner positionZSpinner;
    private javax.swing.JButton specularButton;
    private javax.swing.JLabel specularLabel;
    private javax.swing.JCheckBox viewLightBox;
    // End of variables declaration//GEN-END:variables

    class ColorSwatch implements Icon {

        private ColorRGBA color = new ColorRGBA(ColorRGBA.white);
        private int width = 47;
        private int height = 16;
        
        public ColorSwatch(ColorRGBA color) {
            this.color = color;
        }
        
        public ColorSwatch(Color color) {
            this.color = ColorToColorRGBA(color);
        }
        
        public ColorRGBA getColor() {
            return color;
        }

        public void setColor(ColorRGBA color) {
            this.color = color;
        }        
        
        public void paintIcon(Component cmpnt, Graphics grphcs, int x, int y) {
            Graphics2D g2d = (Graphics2D)grphcs.create();
            
            g2d.setColor(ColorRGBAtoColor(color));
            g2d.fillRect(x +1, y+1, width-2, height-2);
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }
                
    }
}
    
