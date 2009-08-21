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
package org.jdesktop.wonderland.modules.artimport.client.jme;

import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.jme.artimport.ImportSettings;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.image.Texture;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.TextureState;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.artimport.ImportedModel;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author  paulby
 */
public class ModelImporterFrame extends javax.swing.JFrame {
    
    private File lastModelDir;
//    private GeometryStatsDialog geometryStatsDialog = null;
    private TransformChangeListener userMotionListener = null;
    private ChangeListener translationChangeListener = null;
    private ChangeListener rotationChangeListener = null;
    private ChangeListener scaleChangeListener = null;
    
    private Vector3f currentTranslation = new Vector3f();
    private Vector3f currentRotationValues = new Vector3f();
    private Vector3f currentScale = new Vector3f();
    private Matrix3f currentRotation = new Matrix3f();

    private ImportSessionFrame sessionFrame;
    
    private ImportSettings importSettings=null;
    private ImportedModel importedModel = null;
    private TransformProcessorComponent transformProcessor;

    /** Creates new form ModelImporterFrame */
    public ModelImporterFrame(ImportSessionFrame session, File lastModelDir) {
        this.lastModelDir = lastModelDir;
        sessionFrame = session;
        initComponents();

        Float value = new Float(0); 
        Float min = new Float(Float.NEGATIVE_INFINITY);
        Float max = new Float(Float.POSITIVE_INFINITY); 
        Float step = new Float(0.1); 
        SpinnerNumberModel translationX = new SpinnerNumberModel(value, min, max, step); 
        SpinnerNumberModel translationY = new SpinnerNumberModel(value, min, max, step); 
        SpinnerNumberModel translationZ = new SpinnerNumberModel(value, min, max, step); 
        translationXTF.setModel(translationX);
        translationYTF.setModel(translationY);
        translationZTF.setModel(translationZ);

        value = new Float(1);
        SpinnerNumberModel scaleX = new SpinnerNumberModel(value, min, max, step);
        scaleTF.setModel(scaleX);
                
        value = new Float(0);
        min = new Float(-360);
        max = new Float(360);
        step = new Float(1);
        SpinnerNumberModel rotationX = new SpinnerNumberModel(value, min, max, step); 
        SpinnerNumberModel rotationY = new SpinnerNumberModel(value, min, max, step); 
        SpinnerNumberModel rotationZ = new SpinnerNumberModel(value, min, max, step); 
        rotationXTF.setModel(rotationX);
        rotationYTF.setModel(rotationY);
        rotationZTF.setModel(rotationZ);
        currentRotation.loadIdentity();


        // TODO add Float editors to the spinners
                
        userMotionListener = new TransformChangeListener() {
            private Vector3f look = new Vector3f();
            private Vector3f pos = new Vector3f();
            public void transformChanged(Cell cell, ChangeSource source) {
                CellTransform t = cell.getWorldTransform();
                t.getLookAt(pos, look);

                look.mult(3);
                pos.addLocal(look);

                currentTranslation.set(pos);
                ((SpinnerNumberModel)translationXTF.getModel()).setValue(new Float(pos.x));
                ((SpinnerNumberModel)translationYTF.getModel()).setValue(new Float(pos.y));
                ((SpinnerNumberModel)translationZTF.getModel()).setValue(new Float(pos.z));
                if (transformProcessor!=null)
                    transformProcessor.setTransform(currentRotation, currentTranslation);
            }

        };
                
        translationChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float x = (Float)((SpinnerNumberModel)translationXTF.getModel()).getValue();
                float y = (Float)((SpinnerNumberModel)translationYTF.getModel()).getValue();
                float z = (Float)((SpinnerNumberModel)translationZTF.getModel()).getValue();
                
                if (x!=currentTranslation.x ||
                    y!=currentTranslation.y ||
                    z!=currentTranslation.z) {
                    currentTranslation.set(x,y,z);
                    importedModel.setTranslation(currentTranslation);
                    if (transformProcessor!=null)
                        transformProcessor.setTransform(currentRotation, currentTranslation);
                }
            }

        };
        
        rotationChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float x = (Float)((SpinnerNumberModel)rotationXTF.getModel()).getValue();
                float y = (Float)((SpinnerNumberModel)rotationYTF.getModel()).getValue();
                float z = (Float)((SpinnerNumberModel)rotationZTF.getModel()).getValue();
                
                if (x!=currentRotationValues.x ||
                    y!=currentRotationValues.y ||
                    z!=currentRotationValues.z) {
                    currentRotationValues.set(x,y,z);
                    importedModel.setOrientation(currentRotationValues);
                    calcCurrentRotationMatrix();
                    if (transformProcessor!=null)
                        transformProcessor.setTransform(currentRotation, currentTranslation);
                }
            }

        };

        ((SpinnerNumberModel)rotationXTF.getModel()).addChangeListener(rotationChangeListener);
        ((SpinnerNumberModel)rotationYTF.getModel()).addChangeListener(rotationChangeListener);
        ((SpinnerNumberModel)rotationZTF.getModel()).addChangeListener(rotationChangeListener);            
                
        scaleChangeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                float x = (Float)((SpinnerNumberModel)scaleTF.getModel()).getValue();
//                float y = (Float)((SpinnerNumberModel)translationYTF.getModel()).getValue();
//                float z = (Float)((SpinnerNumberModel)translationZTF.getModel()).getValue();
                
                if (x!=currentScale.x ) {
                    currentScale.set(x,x,x);
                    importedModel.setScale(currentScale);
                    if (transformProcessor!=null)
                        transformProcessor.setTransform(currentRotation, currentTranslation, currentScale);
                }
            }

        };

        ((SpinnerNumberModel)scaleTF.getModel()).addChangeListener(scaleChangeListener);
        
        // Disable move with avatar
        avatarMoveCB.setSelected(false);
        enableSpinners(true);

    }

    /**
     * Set the spinners to the rotation, translation and scale local coords of this node
     * @param node
     */
    private void setSpinners(Node modelBG, Node rootBG) {
        Vector3f translation = rootBG.getLocalTranslation();
        Quaternion quat = modelBG.getLocalRotation();
        float[] angles = quat.toAngles(new float[3]);
        Vector3f scale = modelBG.getLocalScale();

        translationXTF.setValue(translation.x);
        translationYTF.setValue(translation.y);
        translationZTF.setValue(translation.z);

        rotationXTF.setValue((float)Math.toDegrees(angles[0]));
        rotationYTF.setValue((float)Math.toDegrees(angles[1]));
        rotationZTF.setValue((float)Math.toDegrees(angles[2]));

        scaleTF.setValue(scale.x);

        importedModel.setTranslation(translation);
        importedModel.setOrientation(new Vector3f(
                (float)Math.toDegrees(angles[0]),
                (float)Math.toDegrees(angles[1]),
                (float)Math.toDegrees(angles[2])));
        importedModel.setScale(new Vector3f(scale.x, scale.x, scale.x));
    }

    private void calcCurrentRotationMatrix() {
        currentRotation = sessionFrame.calcRotationMatrix(
                (float)Math.toRadians(currentRotationValues.x),
                (float)Math.toRadians(currentRotationValues.y),
                (float)Math.toRadians(currentRotationValues.z));
    }
        
    void chooseFile() {
        texturePrefixTF.setText("");
        modelNameTF.setText("");
        modelX3dTF.setText("");
        importedModel = null;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(LoaderManager.getLoaderManager().getLoaderExtensions());
                chooser.setFileFilter(filter);
                if (lastModelDir!=null)
                    chooser.setCurrentDirectory(lastModelDir);
                int returnVal = chooser.showOpenDialog(ModelImporterFrame.this);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        importModel(chooser.getSelectedFile(), false);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ModelImporterFrame.class.getName()).log(Level.SEVERE, null, ex);
                    } catch(IOException ioe) {
                        Logger.getLogger(ModelImporterFrame.class.getName()).log(Level.SEVERE, null, ioe);                        
                    }
                   setVisible(true);
                   lastModelDir = chooser.getSelectedFile().getParentFile();
                } 
            }
            
        });
    }

    /**
     * Edit a model that has already been imported
     * @param model
     */
    void editModel(ImportedModel model) {
//        texturePrefixTF.setText(model.getTexturePrefix());
        modelX3dTF.setText(model.getOriginalURL().toExternalForm());
        modelNameTF.setText(model.getWonderlandName());
        currentTranslation.set(model.getTranslation());
        currentRotationValues.set(model.getOrientation());
        calcCurrentRotationMatrix();
        ((SpinnerNumberModel)rotationXTF.getModel()).setValue(model.getOrientation().x);
        ((SpinnerNumberModel)rotationYTF.getModel()).setValue(model.getOrientation().y);
        ((SpinnerNumberModel)rotationZTF.getModel()).setValue(model.getOrientation().z);
        ((SpinnerNumberModel)translationXTF.getModel()).setValue(model.getTranslation().x);
        ((SpinnerNumberModel)translationYTF.getModel()).setValue(model.getTranslation().y);
        ((SpinnerNumberModel)translationZTF.getModel()).setValue(model.getTranslation().x);
        ((SpinnerNumberModel)scaleTF.getModel()).setValue(model.getScale().x);
        
        avatarMoveCB.setSelected(false);
        populateTextureList(model.getRootBG());
        
        processBounds(model.getModelBG());
    }
    
    /**
     * Import a model from a file
     * 
     * @param origFile
     */
    void importModel(final File origFile, boolean attachToAvatar) throws IOException {
        avatarMoveCB.setSelected(attachToAvatar);
        
        modelX3dTF.setText(origFile.getAbsolutePath());
        importSettings = new ImportSettings(origFile.toURI().toURL());

        sessionFrame.asyncLoadModel(importSettings, new ImportSessionFrame.LoadCompleteListener() {

            public void loadComplete(ImportedModel importedModel) {
                ModelImporterFrame.this.importedModel = importedModel;
                Entity entity = importedModel.getEntity();
                transformProcessor = (TransformProcessorComponent) entity.getComponent(TransformProcessorComponent.class);
                setSpinners(importedModel.getModelBG(), importedModel.getRootBG());

                entity.addComponent(LoadCompleteProcessor.class, new LoadCompleteProcessor(importedModel));
                
                String dir = origFile.getAbsolutePath();
                dir = dir.substring(0,dir.lastIndexOf(File.separatorChar));
                dir = dir.substring(dir.lastIndexOf(File.separatorChar)+1);
                texturePrefixTF.setText(dir);

                String filename = origFile.getAbsolutePath();
                filename = filename.substring(filename.lastIndexOf(File.separatorChar)+1);
                filename = filename.substring(0, filename.lastIndexOf('.'));
                modelNameTF.setText(filename);

                if (avatarMoveCB.isSelected()) {
                    ViewManager.getViewManager().getPrimaryViewCell().addTransformChangeListener(userMotionListener);
                }
            }
        });
        
    }

    private void populateTextureList(Node bg) {
        final DefaultTableModel model = (DefaultTableModel)textureTable.getModel();
        while(model.getRowCount()!=0)
            model.removeRow(0);
        
        final String texturePath = texturePrefixTF.getText();
        final HashSet<String> textureSet = new HashSet();
        
        TreeScan.findNode(bg, Geometry.class, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                TextureState ts = (TextureState)node.getRenderState(TextureState.RS_TEXTURE);
                if (ts==null)
                    return true;

                Texture t = ts.getTexture();
                if (t!=null) {
                    String tFile = t.getImageLocation();
                    if (textureSet.add(tFile))
                        model.addRow(new Object[] {new String(tFile),
                                                   "not implemented",
                                                   "not implemented" });
                }
                return true;
            }
        }, false, true);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        basicPanel = new javax.swing.JPanel();
        avatarMoveCB = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        translationXTF = new javax.swing.JSpinner();
        translationYTF = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        translationZTF = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        rotationXTF = new javax.swing.JSpinner();
        rotationYTF = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        rotationZTF = new javax.swing.JSpinner();
        modelNameTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        modelX3dTF = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        okB1 = new javax.swing.JButton();
        cancelB1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        scaleTF = new javax.swing.JSpinner();
        advancedPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textureTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        texturePrefixTF = new javax.swing.JTextField();
        okB = new javax.swing.JButton();
        cancelB = new javax.swing.JButton();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        boundsCenterYTF = new javax.swing.JTextField();
        boundsCenterXTF = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        boundsCenterZTF = new javax.swing.JTextField();
        boundsSizeXTF = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        geometryStatsB = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        boundsSizeYTF = new javax.swing.JTextField();
        boundsSizeZTF = new javax.swing.JTextField();

        setTitle("Model Import");

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(102, 167));

        basicPanel.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                basicPanelInputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        avatarMoveCB.setSelected(true);
        avatarMoveCB.setText("Move with Avatar");
        avatarMoveCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avatarMoveCBActionPerformed(evt);
            }
        });

        jLabel7.setText("Location");

        jLabel6.setText("X :");

        translationXTF.setEnabled(false);

        translationYTF.setEnabled(false);

        jLabel8.setText("Y :");

        jLabel9.setText("Z :");

        translationZTF.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(translationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel8)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(translationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel4Layout.createSequentialGroup()
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(translationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel7)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(translationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel6))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(translationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(translationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel10.setText("Rotation");

        jLabel11.setText("X :");

        jLabel13.setText("Y :");

        jLabel12.setText("Z :");

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel10)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jLabel13)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rotationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jLabel11)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rotationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jPanel5Layout.createSequentialGroup()
                                .add(jLabel12)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rotationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(jLabel10)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(rotationXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rotationYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel13))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rotationZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel12))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        modelNameTF.setText("jTextField1");
        modelNameTF.setToolTipText("The name of the model used in wonderland");

        jLabel5.setText("Wonderland Model Name :");

        modelX3dTF.setEditable(false);
        modelX3dTF.setText("jTextField1");
        modelX3dTF.setToolTipText("The model file being imported");

        jLabel1.setText("Original Model File :");

        okB1.setText("OK");
        okB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okB1ActionPerformed(evt);
            }
        });

        cancelB1.setText("Cancel");
        cancelB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelB1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Scale");

        org.jdesktop.layout.GroupLayout basicPanelLayout = new org.jdesktop.layout.GroupLayout(basicPanel);
        basicPanel.setLayout(basicPanelLayout);
        basicPanelLayout.setHorizontalGroup(
            basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicPanelLayout.createSequentialGroup()
                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(basicPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(avatarMoveCB)
                            .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(basicPanelLayout.createSequentialGroup()
                                    .add(jLabel1)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(modelX3dTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 305, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, basicPanelLayout.createSequentialGroup()
                                    .add(jLabel5)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(modelNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(basicPanelLayout.createSequentialGroup()
                                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(basicPanelLayout.createSequentialGroup()
                                        .add(jLabel3)
                                        .add(18, 18, 18)
                                        .add(scaleTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(38, 38, 38)
                                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(basicPanelLayout.createSequentialGroup()
                        .add(216, 216, 216)
                        .add(okB1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelB1)))
                .addContainerGap(135, Short.MAX_VALUE))
        );
        basicPanelLayout.setVerticalGroup(
            basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(basicPanelLayout.createSequentialGroup()
                .add(14, 14, 14)
                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(modelX3dTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(modelNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(avatarMoveCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(basicPanelLayout.createSequentialGroup()
                        .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(scaleTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(84, 84, 84)
                .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okB1)
                    .add(cancelB1))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Basic", basicPanel);

        advancedPanel.setMinimumSize(new java.awt.Dimension(615, 647));

        textureTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Original Filename", "Wonderland Path", "Wonderland Filename"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(textureTable);

        jLabel2.setText("Wonderland Texture Dir :");

        texturePrefixTF.setEditable(false);
        texturePrefixTF.setText("jTextField1");
        texturePrefixTF.setToolTipText("The directory in which the model textures will be placed");

        okB.setText("OK");
        okB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okBActionPerformed(evt);
            }
        });

        cancelB.setText("Cancel");
        cancelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBActionPerformed(evt);
            }
        });

        jLabel25.setText("Bounds Center :");

        jLabel27.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel27.setText("X");

        boundsCenterYTF.setColumns(12);
        boundsCenterYTF.setEditable(false);
        boundsCenterYTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsCenterYTF.setText("jTextField1");

        boundsCenterXTF.setColumns(12);
        boundsCenterXTF.setEditable(false);
        boundsCenterXTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsCenterXTF.setText("jTextField1");

        jLabel28.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel28.setText("Y");

        jLabel29.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel29.setText("Z");

        boundsCenterZTF.setColumns(12);
        boundsCenterZTF.setEditable(false);
        boundsCenterZTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsCenterZTF.setText("jTextField1");

        boundsSizeXTF.setColumns(12);
        boundsSizeXTF.setEditable(false);
        boundsSizeXTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsSizeXTF.setText("jTextField1");

        jLabel26.setText("Bounds Size :");

        geometryStatsB.setText("Geometry Stats...");
        geometryStatsB.setToolTipText("Show the geometry statistics");
        geometryStatsB.setEnabled(false);
        geometryStatsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                geometryStatsBActionPerformed(evt);
            }
        });

        jLabel30.setText("Texture Details");

        boundsSizeYTF.setColumns(12);
        boundsSizeYTF.setEditable(false);
        boundsSizeYTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsSizeYTF.setText("jTextField1");

        boundsSizeZTF.setColumns(12);
        boundsSizeZTF.setEditable(false);
        boundsSizeZTF.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        boundsSizeZTF.setText("jTextField1");

        org.jdesktop.layout.GroupLayout advancedPanelLayout = new org.jdesktop.layout.GroupLayout(advancedPanel);
        advancedPanel.setLayout(advancedPanelLayout);
        advancedPanelLayout.setHorizontalGroup(
            advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedPanelLayout.createSequentialGroup()
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(advancedPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(texturePrefixTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, advancedPanelLayout.createSequentialGroup()
                        .add(44, 44, 44)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE))
                    .add(advancedPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(advancedPanelLayout.createSequentialGroup()
                                .add(jLabel25)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(advancedPanelLayout.createSequentialGroup()
                                        .add(jLabel27)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(boundsCenterYTF))
                                    .add(advancedPanelLayout.createSequentialGroup()
                                        .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(jLabel29)
                                            .add(jLabel28))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(boundsCenterXTF)
                                            .add(boundsCenterZTF))))
                                .add(18, 18, 18)
                                .add(jLabel26)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(boundsSizeZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(boundsSizeYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(boundsSizeXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(jLabel30))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 130, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, advancedPanelLayout.createSequentialGroup()
                        .add(123, 123, 123)
                        .add(okB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 174, Short.MAX_VALUE)
                        .add(geometryStatsB)))
                .addContainerGap())
        );
        advancedPanelLayout.setVerticalGroup(
            advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(advancedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(texturePrefixTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel30)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel27)
                    .add(jLabel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(boundsCenterYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel26)
                    .add(boundsSizeXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(boundsCenterXTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel28)
                    .add(boundsSizeYTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(boundsCenterZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel29)
                    .add(boundsSizeZTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(89, 89, 89)
                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(okB)
                    .add(cancelB)
                    .add(geometryStatsB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Advanced", advancedPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 460, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okBActionPerformed
        if (avatarMoveCB.isSelected()) {            
            ViewManager.getViewManager().getPrimaryViewCell().removeTransformChangeListener(userMotionListener);
        }
        setVisible(false);
        Vector3f translation=new Vector3f((Float)translationXTF.getValue(), 
                (Float)translationYTF.getValue(), 
                (Float)translationZTF.getValue());
        Vector3f orientation=new Vector3f((Float)rotationXTF.getValue(), 
                (Float)rotationYTF.getValue(), 
                (Float)rotationZTF.getValue());
        
        importedModel.setWonderlandName(modelNameTF.getText());
//        importedModel.setTexturePrefix(texturePrefixTF.getText());
        
        sessionFrame.loadCompleted(importedModel);
}//GEN-LAST:event_okBActionPerformed

    
    private void cancelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBActionPerformed
        this.setVisible(false);
        if (userMotionListener!=null) {
            ViewManager.getViewManager().getPrimaryViewCell().removeTransformChangeListener(userMotionListener);
        }
        sessionFrame.loadCancelled(importedModel);
    }//GEN-LAST:event_cancelBActionPerformed

    private void okB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okB1ActionPerformed
        okBActionPerformed(evt);
}//GEN-LAST:event_okB1ActionPerformed

    private void cancelB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelB1ActionPerformed
        cancelBActionPerformed(evt);
    }//GEN-LAST:event_cancelB1ActionPerformed

    private void geometryStatsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_geometryStatsBActionPerformed
//        if (geometryStatsDialog==null) 
//            geometryStatsDialog = new GeometryStatsDialog(this);
//        geometryStatsDialog.calcGeometryStats(modelBG);
//        geometryStatsDialog.setVisible(true);
        System.err.println("geometryStats not implemented");
}//GEN-LAST:event_geometryStatsBActionPerformed

    private void avatarMoveCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avatarMoveCBActionPerformed
        if (userMotionListener==null)
            return;
        
        if (avatarMoveCB.isSelected()) {
            enableSpinners(false);
            ViewManager.getViewManager().getPrimaryViewCell().addTransformChangeListener(userMotionListener);
        } else {
            enableSpinners(true);
            ViewManager.getViewManager().getPrimaryViewCell().removeTransformChangeListener(userMotionListener);
        }

    }//GEN-LAST:event_avatarMoveCBActionPerformed

    private void basicPanelInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_basicPanelInputMethodTextChanged
        // TODO add your handling code here:
        System.err.println(evt);
    }//GEN-LAST:event_basicPanelInputMethodTextChanged
    
    

    private void enableSpinners(boolean enabled) {
        translationXTF.setEnabled(enabled);
        translationYTF.setEnabled(enabled);
        translationZTF.setEnabled(enabled);
        rotationXTF.setEnabled(enabled);
        rotationYTF.setEnabled(enabled);
        rotationZTF.setEnabled(enabled);
        
        if (enabled) {
            ((SpinnerNumberModel)translationXTF.getModel()).addChangeListener(translationChangeListener);
            ((SpinnerNumberModel)translationYTF.getModel()).addChangeListener(translationChangeListener);
            ((SpinnerNumberModel)translationZTF.getModel()).addChangeListener(translationChangeListener);
            ((SpinnerNumberModel)rotationXTF.getModel()).addChangeListener(rotationChangeListener);
            ((SpinnerNumberModel)rotationYTF.getModel()).addChangeListener(rotationChangeListener);
            ((SpinnerNumberModel)rotationZTF.getModel()).addChangeListener(rotationChangeListener);
        } else {
            ((SpinnerNumberModel)translationXTF.getModel()).removeChangeListener(translationChangeListener);
            ((SpinnerNumberModel)translationYTF.getModel()).removeChangeListener(translationChangeListener);
            ((SpinnerNumberModel)translationZTF.getModel()).removeChangeListener(translationChangeListener);
            ((SpinnerNumberModel)rotationXTF.getModel()).removeChangeListener(rotationChangeListener);
            ((SpinnerNumberModel)rotationYTF.getModel()).removeChangeListener(rotationChangeListener);
            ((SpinnerNumberModel)rotationZTF.getModel()).removeChangeListener(rotationChangeListener);           
        }
    }
    
    /**
     * Process the bounds of the graph, updating the UI.
     */
    private void processBounds( Node bg ) {
//        System.err.println("Model Node "+bg);

        if (bg==null) {
            return;
        }

        BoundingVolume bounds = bg.getWorldBound();

        if (bounds==null) {
            bounds = calcBounds(bg);
        }

        // Remove the rotation from the bounds because it will be reapplied by the cell
//        Quaternion rot = bg.getWorldRotation();
//        rot.inverseLocal();
//        bounds = bounds.transform(rot, new Vector3f(), new Vector3f(1,1,1), bounds);
//
//        System.err.println("ROTATED "+bounds);
//        System.err.println(rot.toAngleAxis(null));

        if (bounds instanceof BoundingSphere) {
            BoundingSphere sphere = (BoundingSphere)bounds;
            Vector3f center = new Vector3f();
            sphere.getCenter(center);
            boundsCenterXTF.setText(Double.toString(center.x));
            boundsCenterYTF.setText(Double.toString(center.y));
            boundsCenterZTF.setText(Double.toString(center.z));
            boundsSizeXTF.setText(Double.toString(sphere.getRadius()));
            boundsSizeYTF.setText("N/A Sphere");
            boundsSizeZTF.setText("N/A Sphere");
       } else if (bounds instanceof BoundingBox) {
            BoundingBox box = (BoundingBox)bounds;
            Vector3f center = new Vector3f();
            box.getCenter();
            boundsCenterXTF.setText(Double.toString(center.x));
            boundsCenterYTF.setText(Double.toString(center.y));
            boundsCenterZTF.setText(Double.toString(center.z));
            
            boundsSizeXTF.setText(Float.toString(box.xExtent));
            boundsSizeYTF.setText(Float.toString(box.yExtent));
            boundsSizeZTF.setText(Float.toString(box.zExtent));
        }
    }

    /**
     * Traverse the graph, combining all the world bounds into bv
     * @param n
     * @param bv
     */
    BoundingVolume calcBounds(Spatial n) {
        BoundingVolume bounds=null;

        if (n instanceof Geometry) {
            bounds = new BoundingBox();
            bounds.computeFromPoints(((Geometry)n).getVertexBuffer());

            bounds.transform(n.getLocalRotation(), n.getLocalTranslation(), n.getLocalScale());
        } 

        if (n instanceof Node && ((Node)n).getQuantity()>0) {
            for(Spatial child : ((Node)n).getChildren()) {
                BoundingVolume childB = calcBounds(child);
                if (bounds==null)
                    bounds = childB;
                else
                    bounds.mergeLocal(childB);
            }
        }

        if (bounds!=null)
            bounds.transform(n.getLocalRotation(), n.getLocalTranslation(), n.getLocalScale(), bounds);
//        Vector3f axis = new Vector3f();
//        float angle = n.getLocalRotation().toAngleAxis(axis);
//        System.err.println("Applying transform "+n.getLocalTranslation()+"  "+angle+"  "+axis);
//        System.err.println("BOunds "+bounds);
        
        return bounds;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JCheckBox avatarMoveCB;
    private javax.swing.JPanel basicPanel;
    private javax.swing.JTextField boundsCenterXTF;
    private javax.swing.JTextField boundsCenterYTF;
    private javax.swing.JTextField boundsCenterZTF;
    private javax.swing.JTextField boundsSizeXTF;
    private javax.swing.JTextField boundsSizeYTF;
    private javax.swing.JTextField boundsSizeZTF;
    private javax.swing.JButton cancelB;
    private javax.swing.JButton cancelB1;
    private javax.swing.JButton geometryStatsB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField modelNameTF;
    private javax.swing.JTextField modelX3dTF;
    private javax.swing.JButton okB;
    private javax.swing.JButton okB1;
    private javax.swing.JSpinner rotationXTF;
    private javax.swing.JSpinner rotationYTF;
    private javax.swing.JSpinner rotationZTF;
    private javax.swing.JSpinner scaleTF;
    private javax.swing.JTextField texturePrefixTF;
    private javax.swing.JTable textureTable;
    private javax.swing.JSpinner translationXTF;
    private javax.swing.JSpinner translationYTF;
    private javax.swing.JSpinner translationZTF;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Case independent filename extension filter
     */
    class FileNameExtensionFilter extends FileFilter {
        private HashSet<String> extensions;
        private String description;
        
        public FileNameExtensionFilter(String ext) {
            extensions = new HashSet();
            extensions.add(ext);
            description = new String(ext);
        }
        
        public FileNameExtensionFilter(String[] ext) {
            extensions = new HashSet();
            StringBuffer desc = new StringBuffer();
            for(String e : ext) {
                extensions.add(e);
                desc.append(e+", ");
            }
            description = desc.toString();
        }
        
        public boolean accept(File pathname) {
            if (pathname.isDirectory())
                return true;
            String e = pathname.getName();
            e = e.substring(e.lastIndexOf('.')+1);
            if (extensions.contains(e.toLowerCase()))
                return true;
            
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }
        
    }
    
    class LoadCompleteProcessor extends ProcessorComponent {

        private ImportedModel importedModel;

        public LoadCompleteProcessor(ImportedModel importedModel) {
            this.importedModel = importedModel;

        }

        @Override
        public void compute(ProcessorArmingCollection arg0) {
            processBounds(importedModel.getModelBG());

            populateTextureList(importedModel.getModelBG());

            importedModel.getEntity().removeComponent(LoadCompleteProcessor.class);
            setArmingCondition(null);
        }

        @Override
        public void commit(ProcessorArmingCollection arg0) {
        }

        @Override
        public void initialize() {
            setArmingCondition(new NewFrameCondition(this));
        }

    }

}
