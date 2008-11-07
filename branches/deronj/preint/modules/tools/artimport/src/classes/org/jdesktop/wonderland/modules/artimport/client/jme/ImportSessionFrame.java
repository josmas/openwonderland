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
package org.jdesktop.wonderland.modules.artimport.client.jme;

import org.jdesktop.wonderland.client.jme.artimport.ModelUploader;
import org.jdesktop.wonderland.client.jme.artimport.ImportedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.light.PointLight;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.resource.ResourceLocator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.artimport.ModelCompiler;
import org.jdesktop.wonderland.client.jme.artimport.ModelCompiler.CompilerMessageDisplay;
import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.common.config.WonderlandConfigUtil;

/**
 * Frame that provides the controls for the user to position and orient
 * a model instance in the world. Also allows configuration of other instance
 * data such as world name and texture directory.
 * 
 * @author  paulby
 */
public class ImportSessionFrame extends javax.swing.JFrame
        implements CompilerMessageDisplay 
{    
    private static final Logger logger =
            Logger.getLogger(ImportSessionFrame.class.getName());
    
    private ArrayList<ImportedModel> imports = new ArrayList();
    
    private DefaultTableModel tableModel = null;
    private File compiledDir = null; // Directory in which to store the compiled model
    private File lastModelDir = null; // Directory we last loaded a model from
    private ModelImporterFrame importFrame = null;
    
    private int editingRow = -1;
    
    private SceneGraphViewFrame sgViewFrame;
   
    /** Creates new form ImportSessionFrame */
    public ImportSessionFrame() {
        initComponents();
        
        sgViewFrame = new SceneGraphViewFrame();
        sgViewFrame.setVisible(true);
        
        tableModel = (DefaultTableModel) importTable.getModel();
        importTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        importTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                
                int row = importTable.getSelectedRow();
                boolean validSelection = (row>=0);
                editB.setEnabled(validSelection);
                removeB.setEnabled(validSelection);
            }
 }
        );
        
        importTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent tme) {
                boolean models = importTable.getModel().getRowCount() > 0;
                localB.setEnabled(models);
//                serverB.setEnabled(models && 
//                        ModelUploader.uploadAvailable(targetModuleTF.getText()));
            }
        });
        
        compiledModelsTF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent arg0) {
                checkLocalSaveButtons();
            }

            public void removeUpdate(DocumentEvent arg0) {
                checkLocalSaveButtons();
            }

            public void changedUpdate(DocumentEvent arg0) {
                checkLocalSaveButtons();
            }
        });
        
        // Load the config file which contains the directory from which we last
        // loaded a model.
        try {
            String tmp = WonderlandConfigUtil.getUserConfigDir() + File.separator + "last_model_dir";
            if (new File(tmp).exists()) {            
                DataInputStream in = new DataInputStream(WonderlandConfigUtil.getInputStream(tmp));
                String str;
                if (in.readBoolean()) {
                    str = in.readUTF();
                    lastModelDir = new File(str);
                } else
                    lastModelDir = null;
                
                if (in.readBoolean()) {
                    str = in.readUTF();
                    compiledDir = new File(str);
                    compiledModelsTF.setText(str);
                } else
                    compiledDir=null;
                in.close();
            }
        } catch (Exception ex) {
            lastModelDir = null;
            Logger.getLogger(ModelImporterFrame.class.getName()).log(Level.INFO, null, ex);
        }
        
        if (compiledDir==null) {
            try {
                File artDirCheck = new File("../lg3d-wonderland-art");
                if (artDirCheck.exists()) {
                    compiledModelsTF.setText(artDirCheck.getCanonicalPath());
                    compiledDir = artDirCheck;
                } 
            } catch(IOException ex) {

            }
        }
        
        targetModuleTF.setText("../modules/samples/arttest");
        
        importFrame = new ModelImporterFrame(this, lastModelDir);
 
    }
    
    /**
     * Write the defaults for this UI
     */
    void writeDefaultsConfig() {
        try {
            DataOutputStream out = new DataOutputStream(WonderlandConfigUtil.getOutputStream(WonderlandConfigUtil.getUserConfigDir() + File.separator + "last_model_dir"));
            out.writeBoolean(lastModelDir!=null);
            if (lastModelDir!=null)
                out.writeUTF(lastModelDir.getAbsolutePath());
            out.writeBoolean(compiledDir!=null);
            if (compiledDir!=null)
                out.writeUTF(compiledDir.getAbsolutePath());
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ModelImporterFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        tablePopupMenu = new javax.swing.JPopupMenu();
        editPMI = new javax.swing.JMenuItem();
        removePMI = new javax.swing.JMenuItem();
        localSaveDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        compiledModelsTF = new javax.swing.JTextField();
        chooseLocalDirB = new javax.swing.JButton();
        localSaveCancelB = new javax.swing.JButton();
        localSaveOKB = new javax.swing.JButton();
        invalidDirLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        importTable = new javax.swing.JTable();
        importModelB = new javax.swing.JButton();
        serverB = new javax.swing.JButton();
        editB = new javax.swing.JButton();
        removeB = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        targetModuleTF = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        localB = new javax.swing.JButton();
        doneButton = new javax.swing.JButton();
        moduleChooseB = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        loadImportGroupMI = new javax.swing.JMenuItem();
        saveImportGroupMI = new javax.swing.JMenuItem();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        editPMI.setText("Edit");
        tablePopupMenu.add(editPMI);

        removePMI.setText("Remove");
        tablePopupMenu.add(removePMI);

        jLabel2.setText("Save to local directory");

        compiledModelsTF.setToolTipText("Select a directory to save the local files in");
        compiledModelsTF.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                compiledModelsTFFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                compiledModelsTFFocusLost(evt);
            }
        });

        chooseLocalDirB.setText("Choose...");

        localSaveCancelB.setText("Cancel");
        localSaveCancelB.setToolTipText("Cancel save to local directory");
        localSaveCancelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localSaveCancelBActionPerformed(evt);
            }
        });

        localSaveOKB.setText("OK");
        localSaveOKB.setEnabled(false);
        localSaveOKB.setSelected(true);
        localSaveOKB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localSaveOKBActionPerformed(evt);
            }
        });

        invalidDirLabel.setForeground(new java.awt.Color(255, 0, 0));
        invalidDirLabel.setText("Invalid directory");

        org.jdesktop.layout.GroupLayout localSaveDialogLayout = new org.jdesktop.layout.GroupLayout(localSaveDialog.getContentPane());
        localSaveDialog.getContentPane().setLayout(localSaveDialogLayout);
        localSaveDialogLayout.setHorizontalGroup(
            localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(localSaveDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(localSaveDialogLayout.createSequentialGroup()
                        .add(localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(invalidDirLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, compiledModelsTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                            .add(localSaveDialogLayout.createSequentialGroup()
                                .add(localSaveCancelB)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(localSaveOKB)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(chooseLocalDirB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        localSaveDialogLayout.setVerticalGroup(
            localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(localSaveDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chooseLocalDirB)
                    .add(compiledModelsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(invalidDirLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(localSaveDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(localSaveOKB)
                    .add(localSaveCancelB))
                .addContainerGap())
        );

        setTitle("Import Manager");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        importTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Wonderland Name", "Original Model Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(importTable);

        importModelB.setText("Load Model...");
        importModelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importModelBActionPerformed(evt);
            }
        });

        serverB.setText("Deploy to server");
        serverB.setToolTipText("Upload models to server");
        serverB.setEnabled(false);
        serverB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverBActionPerformed(evt);
            }
        });

        editB.setText("Edit");
        editB.setEnabled(false);
        editB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBActionPerformed(evt);
            }
        });

        removeB.setText("Remove");
        removeB.setToolTipText("Remove the model from the import");
        removeB.setEnabled(false);
        removeB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBActionPerformed(evt);
            }
        });

        jLabel1.setText("Model List");

        targetModuleTF.setText("jTextField1");
        targetModuleTF.setToolTipText("The module into which this model will be added");

        jLabel3.setText("Target Module :");

        localB.setText("Save to local system...");
        localB.setToolTipText("Save the loaded models to your local system");
        localB.setEnabled(false);
        localB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localBActionPerformed(evt);
            }
        });

        doneButton.setText("Done");
        doneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneButtonActionPerformed(evt);
            }
        });

        moduleChooseB.setText("Choose");
        moduleChooseB.setToolTipText("Choose the target module");
        moduleChooseB.setEnabled(false);
        moduleChooseB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moduleChooseBActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        loadImportGroupMI.setText("Load Import Group");
        loadImportGroupMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadImportGroupMIActionPerformed(evt);
            }
        });
        jMenu1.add(loadImportGroupMI);

        saveImportGroupMI.setText("Save Import Group");
        saveImportGroupMI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveImportGroupMIActionPerformed(evt);
            }
        });
        jMenu1.add(saveImportGroupMI);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(localB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(serverB))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(targetModuleTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(doneButton)
                    .add(editB)
                    .add(importModelB)
                    .add(removeB)
                    .add(moduleChooseB))
                .add(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(importModelB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeB))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(targetModuleTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(moduleChooseB))
                .add(76, 76, 76)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverB)
                    .add(localB)
                    .add(doneButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void importModelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importModelBActionPerformed
        editingRow = -1;            // Not editing
        importFrame.chooseFile();  // choosefile will set the frame to visible
        importFrame.setVisible(true);
    }//GEN-LAST:event_importModelBActionPerformed

    private void editBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBActionPerformed
        editingRow = importTable.getSelectedRow();
        importFrame.editModel(imports.get(editingRow));
        importFrame.setVisible(true);
    }//GEN-LAST:event_editBActionPerformed

    private void removeBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBActionPerformed
        int row = importTable.getSelectedRow();
        if (row==-1) {
            Logger.getAnonymousLogger().warning("Remove with invalid row");
            return;
        }
        
        ImportedModel ic = imports.remove(row);
        ic.getRootBG().getParent().detachChild(ic.getRootBG());
        tableModel.removeRow(row);
    }//GEN-LAST:event_removeBActionPerformed

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden

    }//GEN-LAST:event_formComponentHidden

    private void serverBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverBActionPerformed
        // upload to server
        String baseURL = targetModuleTF.getText();
        ModelUploader uploader = new ModelUploader(this);
        
        try {
            URL uploadURL = uploader.getUploadURL(baseURL);
            
            for(ImportedModel config : imports) {
                uploader.uploadModel(config, uploadURL, baseURL);
            }
            
            // reload the world
//            ServerManagerMessage msg = ServerManagerMessage.createWFSReloadMessage();
//            ChannelController.getController().sendMessage(msg);
            System.err.println("Server reload not implemented");
            
            // empty the list of models
            imports.clear();
            tableModel.setRowCount(0);
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error uploading models", ioe);
        }
}//GEN-LAST:event_serverBActionPerformed

    private void saveImportGroupMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveImportGroupMIActionPerformed
        File sessionFile = new File(WonderlandConfigUtil.getUserConfigDir() + File.separator + "import_session");
        saveImportSession(sessionFile);
}//GEN-LAST:event_saveImportGroupMIActionPerformed

    private void loadImportGroupMIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadImportGroupMIActionPerformed
        File sessionFile = new File(WonderlandConfigUtil.getUserConfigDir() + File.separator + "import_session");
        for (ImportedModel m : imports) {
            m.getRootBG().getParent().detachChild(m.getRootBG());
        }
        
        imports.clear();
        tableModel.setRowCount(0);
        loadImportSession(sessionFile);
        for(ImportedModel m : imports) {
            addToTable(m);
            try {
                loadModel(m);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, "Unable to load model "+m.getOrigModel(), ex);
            } catch(IOException ioe) {
                Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, "Unable to load model "+m.getOrigModel(), ioe);                
            }
        }
}//GEN-LAST:event_loadImportGroupMIActionPerformed

private void localBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localBActionPerformed
        localSaveDialog.pack();
        localSaveDialog.setVisible(true);
        checkLocalSaveButtons();
}//GEN-LAST:event_localBActionPerformed

private void localSaveCancelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localSaveCancelBActionPerformed
        localSaveDialog.setVisible(false);
}//GEN-LAST:event_localSaveCancelBActionPerformed

private void localSaveOKBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localSaveOKBActionPerformed
        String compiledDirName = compiledModelsTF.getText();
        String baseURL = targetModuleTF.getText();

        ModelCompiler compiler = new ModelCompiler(this);
        
        for(ImportedModel config : imports) {
            compiler.compileModel(config, baseURL, compiledDirName);
        }
        
        localSaveDialog.setVisible(false);
}//GEN-LAST:event_localSaveOKBActionPerformed

private void compiledModelsTFFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_compiledModelsTFFocusLost
        checkLocalSaveButtons();
}//GEN-LAST:event_compiledModelsTFFocusLost

private void compiledModelsTFFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_compiledModelsTFFocusGained
        checkLocalSaveButtons();
}//GEN-LAST:event_compiledModelsTFFocusGained

private void doneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneButtonActionPerformed
        setVisible(false);
}//GEN-LAST:event_doneButtonActionPerformed

private void moduleChooseBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moduleChooseBActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_moduleChooseBActionPerformed
    
    private void checkLocalSaveButtons() {
        boolean okEnabled = false;
        
        String dirName = compiledModelsTF.getText();
        if (dirName != null && dirName.length() > 0) {
            File dir = new File(dirName);
            okEnabled = dir.exists() && dir.isDirectory();
        }
        
        localSaveOKB.setEnabled(okEnabled);
        invalidDirLabel.setVisible(!okEnabled);
    }

    /**
     * Load model from file
     * 
     * @param origFile
     */
    Entity loadModel(ImportedModel model) throws IOException {
        Vector3f rot = model.getOrientation();

        Node rootBG = new Node();
        rootBG.setLocalRotation(calcRotationMatrix(rot.x, rot.y, rot.z));
        rootBG.setLocalTranslation(model.getTranslation());
        
        File dir = new File(model.getOrigModel()).getParentFile();
        
        lastModelDir = dir;
        
        Node modelBG=null;
        
        
//        if (model.getOrigModel().endsWith("dae")) {
//            InputStream in = new FileInputStream(model.getOrigModel());
//            ColladaImporter.load(in, model.getOrigModel());
//            modelBG = ColladaImporter.getModel();
//
//            ColladaImporter.cleanUp();
//            in.close();
//        } else if (model.getOrigModel().endsWith("kmz")) {
//            modelBG = load(new File(model.getOrigModel()));
//        } else if (model.getOrigModel().endsWith("wlm")) {
//            InputStream in = new FileInputStream(model.getOrigModel());
//            Savable s = BinaryImporter.getInstance().load(in);
//            System.out.println("LOADED "+s);
//            modelBG = (Node) s;
//            in.close();
//        } else {
//            logger.severe("Unrecognised file extension "+model.getOrigModel());
//            return null;
//        }
        
        File modelFile = new File(model.getOrigModel());
        ModelLoader modelLoader = LoaderManager.getLoaderManager().getLoader(modelFile);
        
        
        if (modelLoader==null) {
            JOptionPane.showMessageDialog(null, "No Loader for "+org.jdesktop.wonderland.common.FileUtils.getFileExtension(modelFile.getName()));
            return null;
        }
        
        modelBG = modelLoader.importModel(modelFile);
        
        rootBG.attachChild(modelBG);
        
        model.setModelBG(modelBG);
        model.setRootBG(rootBG);
        rootBG.setModelBound(new BoundingBox());
        rootBG.updateModelBound();
        
        WorldManager wm = ClientContextJME.getWorldManager();
        
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = (LightState) wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        lightState.setEnabled(true);
        lightState.attach(light);

        MaterialState matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
//        matState.setDiffuse(color);
        rootBG.setRenderState(matState);
        rootBG.setRenderState(buf);
        rootBG.setRenderState(lightState);
        rootBG.setLocalTranslation(0f,0f,0f);

                        
        Entity entity = new Entity(model.getOrigModel());
        RenderComponent scene = wm.getRenderManager().createRenderComponent(rootBG);
//        scene.getSceneRoot().attachChild(rootBG);
        entity.addComponent(RenderComponent.class,scene);
        
        model.setEntity(entity);
        
        entity.addComponent(ProcessorComponent.class, new TransformProcessorComponent(wm, rootBG));
        
        wm.addEntity(entity);
        sgViewFrame.addEntity(entity);
        
        findTextures(modelBG);
        
        File targetArtDir = new File(targetModuleTF.getText());
        targetArtDir.mkdir();
        
        modelLoader.deployToModule(targetArtDir);

        return entity;
    }
      
     // This gimble locks, but good enough for now...
    public static Matrix3f calcRotationMatrix(float x, float y, float z) {
        Matrix3f m3f = new Matrix3f();
        m3f.loadIdentity();
        m3f.fromAngleAxis(x, new Vector3f(1f, 0f, 0f));
        Matrix3f rotY = new Matrix3f();
        rotY.loadIdentity();
        rotY.fromAngleAxis(y, new Vector3f(0f, 1f, 0f));
        Matrix3f rotZ = new Matrix3f();
        rotZ.loadIdentity();
        rotZ.fromAngleAxis(z, new Vector3f(0f, 0f, 1f));

        m3f.multLocal(rotY);
        m3f.multLocal(rotZ);
        
        return m3f;
    }
    
    /**
     * Notification from ImporterFrame that a model has been loaded. 
     * This method is called at the end of both load and edit 
     * 
     * @param origModel original model absolute filename
     * @param wonderlandName name of model in wonderland
     * @param translation model translation
     * @param orientation model orientation
     */
    void loadCompleted(ImportedModel imp) {
        
        if (editingRow>=0) {
            setRow(editingRow, imp);
        } else {
            imports.add(imp);
            addToTable(imp);
        }
        
        writeDefaultsConfig();
    }
    
    void findTextures(Node root) {
        TreeScan.findNode(root, new ProcessNodeInterface() {

            public boolean processNode(Spatial node) {
                TextureState ts = (TextureState) node.getRenderState(TextureState.RS_TEXTURE);
                if (ts!=null) {
                    Texture t = ts.getTexture();
                    if (t!=null)
                        System.out.println("Texture "+t.getImageLocation());
                }
                return true;
            }
            
        });
    }
    
    /**
     * Called when the user cancels the load
     */
    void loadCancelled(ImportedModel model) {
        if (editingRow>=0) {
            // Restore Position of model
            ImportedModel imp = imports.get(editingRow);
            Node tg = imp.getRootBG();
            Vector3f rot = imp.getOrientation();
            tg.setLocalRotation(calcRotationMatrix(rot.x, rot.y, rot.z));
            tg.setLocalTranslation(imp.getTranslation());
        } else {
//            JmeClientMain.getWorldManager().removeEntity(model.getEntity());
            System.err.println("ImportSessionFrame.loadCancelled - TODO needs WorldManager.removeEntity");
        }
    }
    
    void addToTable(ImportedModel config) {
        tableModel.addRow(new Object[] { config.getWonderlandName(),config.getOrigModel() });
    }
    
    void setRow(int row, ImportedModel config) {
        tableModel.setValueAt(config.getWonderlandName(), row, 0);        
        tableModel.setValueAt(config.getOrigModel(), row, 1);        
    }
    
    /**
     * Save the current import session to the specified file
     * @param file
     */
    private void saveImportSession(File file) {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeObject(imports);
        } catch (IOException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch(IOException ex) {
            }
        }
    }
    
    private void loadImportSession(File file) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            imports = (ArrayList<ImportedModel>) in.readObject();
        } catch (IOException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch(IOException ex) {
            }
        }
        
    }
    
    public void displayMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void displayError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error compiling model",
                                      JOptionPane.ERROR_MESSAGE);
    }

    public boolean requestConfirmation(String message) {
        int answer = JOptionPane.showConfirmDialog(this, message, 
                "Select an option", JOptionPane.YES_NO_OPTION);
        return (answer == JOptionPane.YES_OPTION);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton chooseLocalDirB;
    private javax.swing.JTextField compiledModelsTF;
    private javax.swing.JButton doneButton;
    private javax.swing.JButton editB;
    private javax.swing.JMenuItem editPMI;
    private javax.swing.JButton importModelB;
    private javax.swing.JTable importTable;
    private javax.swing.JLabel invalidDirLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList jList1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem loadImportGroupMI;
    private javax.swing.JButton localB;
    private javax.swing.JButton localSaveCancelB;
    private javax.swing.JDialog localSaveDialog;
    private javax.swing.JButton localSaveOKB;
    private javax.swing.JButton moduleChooseB;
    private javax.swing.JButton removeB;
    private javax.swing.JMenuItem removePMI;
    private javax.swing.JMenuItem saveImportGroupMI;
    private javax.swing.JButton serverB;
    private javax.swing.JPopupMenu tablePopupMenu;
    private javax.swing.JTextField targetModuleTF;
    // End of variables declaration//GEN-END:variables
   
    /**
     * Filter that only accepts directories
     */
    class DirExtensionFilter extends FileFilter {
        
        public DirExtensionFilter() {
         }
        
        public boolean accept(File pathname) {
            if (pathname.isDirectory())
                return true;
            
            return false;
        }

        @Override
        public String getDescription() {
            return "Directory";
        }
        
    }
    
    class ImporterResourceLocator implements ResourceLocator {

        private String baseURL;
        
        public ImporterResourceLocator(URI baseURI) {
            try {
                this.baseURL = baseURI.toURL().toExternalForm();
            } catch (MalformedURLException ex) {
                Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public URL locateResource(String resource) {
            System.out.println("*************** Looking for texture "+resource);
            try {
                URL ret = new URL(baseURL + "/" + removePath(resource));
                System.out.println("Resource URL "+ret.toExternalForm());
                
                return ret;
            } catch (MalformedURLException ex) {
                Logger.getLogger(ImportSessionFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
     
        private String removePath(String filename) {
            int i = filename.lastIndexOf(File.separatorChar);
            if (i<0)
                return filename;
            return filename.substring(i+1);
        }
    }
}
