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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.modules.defaultenvironment.common.SharedSkybox;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 *
 * @author JagWire
 */
public class SkyboxProperties extends javax.swing.JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(SkyboxProperties.class.getName());
    
    private DefaultEnvironmentCell cell;
    private CellPropertiesEditor editor;
    private boolean isDirty = false;
    private DefaultListModel model;
    private String originalActiveName;
    private String currentActiveName;
    private Map<String, SharedData> originalSkyboxMap;
    private Map<String, SharedData> currentSkyboxMap;
    
    //separate window stuph.
    private SkyboxEditor editorPanel;
    private JDialog editorDialog;
    
    
    
    public SkyboxProperties(DefaultEnvironmentCell cell, CellPropertiesEditor editor) {
        initComponents();
        
        this.cell = cell;
        this.editor = editor;
        
        model = new DefaultListModel();
        editorDialog = new JDialog();
        editorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
//        editorPanel = new SkyboxEditor();
        
        originalActiveName = cell.getActiveSkyboxName();
        currentActiveName = cell.getActiveSkyboxName();        
        
        originalSkyboxMap = new HashMap<String, SharedData>();
        currentSkyboxMap = new HashMap<String, SharedData>();
        
        originalSkyboxMap.putAll(cell.getSkyboxMap());
        currentSkyboxMap.putAll(cell.getSkyboxMap());
        
        skyboxList.setModel(model);
                        
        populateSkyboxList(cell.getSkyboxMap());                        
        
        setInitialActiveSkyboxOnList();
        
        skyboxList.addMouseListener(new MouseAdapter() { 
            
            @Override
            public void mouseClicked(MouseEvent me) {
                if(me.getClickCount() == 2) {
                  currentActiveName = getSelectedSkyboxName();
                  //check to see if panel is dirty now
                  if(!currentActiveName.equals(originalActiveName)) {
                      isDirty = true;
                      getEditor().setPanelDirty(DefaultEnvironmentProperties.class, true);
                  }
                }
            }
        });
        
    }
    
    private CellPropertiesEditor getEditor() {
        return this.editor;
    }
    
    private void setInitialActiveSkyboxOnList() {
        skyboxList.setSelectedValue(cell.getActiveSkyboxName(), true);
    }
    
    private void populateSkyboxList(Map<String, SharedData> skyboxes) {
        for(String key: skyboxes.keySet()) {
            model.addElement(key);
        }
    }
    
    public void close() {
                        
    }
    
    
    public void applyIfNeeded() {
        if(isDirty) {
            LOGGER.fine("APPLYING SKYBOX!");
            apply();
        }
    }
    
    
    protected void apply() {
        //Is there any map in current, that is not in original?
        //If so, remove them from shared map.
        
        for(String key: originalSkyboxMap.keySet()) {
            if(!currentSkyboxMap.containsKey(key)) {
                cell.getSkyboxMap().remove(key);
            }
        }
       
        cell.updateActiveSkyboxName(currentActiveName);
//        SharedData active = currentSkyboxMap.get(currentActiveName);
//        currentSkyboxMap.put("active", active);
        
        cell.getSkyboxMap().putAll(currentSkyboxMap);                                        
    }
    
    public void restore() {
        populateSkyboxList(originalSkyboxMap);
        skyboxList.setSelectedValue(originalActiveName, true);
    }
    
    /** 
     * Should be called from SkyboxDialog
     */
    public void addSkyboxToList(SharedSkybox skyboxToBeAdded) {
        currentSkyboxMap.put(skyboxToBeAdded.getId(), skyboxToBeAdded);
        
        
        updateList();
    }
    
    private void removeSkyboxFromList() {
        currentSkyboxMap.remove(getSelectedSkyboxName());
        model.removeElement(getSelectedSkyboxName());
        
    }    
    
    private String getSelectedSkyboxName() {
        return (String)skyboxList.getSelectedValue();
    }
    
    private void updateList() {
        model.removeAllElements();
        populateSkyboxList(currentSkyboxMap);
    }

    private void showSkyboxEditorDialog(boolean useExistingSkybox) {
        SharedSkybox skyboxPassedToEditorPanel = null;
        String name = getSelectedSkyboxName();
        
        if(useExistingSkybox && currentSkyboxMap.containsKey(name)) {
            skyboxPassedToEditorPanel = (SharedSkybox)currentSkyboxMap.get(name);            
        } else {
            skyboxPassedToEditorPanel = SharedSkybox.valueOf("none",
                                                             "none",
                                                             "none",
                                                             "none",
                                                             "none",
                                                             "none",
                                                             "none",
                                                             "none");
        }
        
        
        
        editorDialog.setTitle("Skybox Editor - "+name);
        editorDialog.setContentPane(new SkyboxEditor(this,
                                                     skyboxPassedToEditorPanel,
                                                     editorDialog));
        editorDialog.pack();
        editorDialog.setVisible(true);
    }
    
    /**
     * Only called when we absolutely need to make the panel dirty
     */
    public void setDirtyFromSkyboxDialog() {
        isDirty = true;
        editor.setPanelDirty(DefaultEnvironmentProperties.class, true);        
    }
    
    public DefaultEnvironmentRenderer getDefaultEnvironmentRenderer() {
        return (DefaultEnvironmentRenderer)cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
    }
    
    public String getActiveSkyboxName() {
        return currentActiveName;
    }
    
    public boolean alreadyContainsSkybox(String name) {
        if(currentSkyboxMap.containsKey(name)) {
            return true;
        }
        
        return false;
    }
    
    public void updateSkybox(String name,
                             String description,
                             String northURI,
                             String soutURI,
                             String eastURI,
                             String westURI,
                             String upURI,
                             String downURI) {
        if(currentSkyboxMap.containsKey(name)) {
            SharedSkybox tmp = (SharedSkybox)currentSkyboxMap.get(name);
            tmp.setDescription(description);
            tmp.setNorth(northURI);
            tmp.setSouth(soutURI);
            tmp.setEast(eastURI);
            tmp.setWest(westURI);
            tmp.setUp(upURI);
            tmp.setDown(downURI);
        } else {
            LOGGER.warning("That skybox, "+name+", doesn't exist in current map");
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

        jScrollPane1 = new javax.swing.JScrollPane();
        skyboxList = new javax.swing.JList();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setViewportView(skyboxList);

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("-");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        editButton.setText("Edit");
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Double-click an item to activate skybox:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(editButton)))
                        .addGap(0, 97, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(editButton))
                .addContainerGap(102, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Show SkyboxEditor dialog
        showSkyboxEditorDialog(false);
        
        //The dialog will use public addSkyboxToList() to affect the panel
        //and maps.
                     
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // TODO add your handling code here:
        removeSkyboxFromList();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // TODO add your handling code here:
        showSkyboxEditorDialog(true);
    }//GEN-LAST:event_editButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JList skyboxList;
    // End of variables declaration//GEN-END:variables
}
