/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.defaultenvironment.client;

import com.jme.light.LightNode;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.modules.defaultenvironment.common.DefaultEnvironmentCellServerState;

/**
 * Property sheet
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author JagWire
 */
@PropertiesFactory(DefaultEnvironmentCellServerState.class)
public class DefaultEnvironmentProperties
        extends JPanel implements PropertiesFactorySPI
{
    private static final Logger LOGGER =
            Logger.getLogger(DefaultEnvironmentProperties.class.getName());

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/defaultenvironment/client/Bundle");
    
    CellPropertiesEditor editor = null;

    public DefaultEnvironmentProperties() {
        initComponents();
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Default_Environment");
    }

    /**
     * @inheritDoc()
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @inheritDoc()
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @inheritDoc()
     */
    public void open() {
        // Fetch the current state from the cell's server state and update
        // the GUI.
        
        
        DefaultEnvironmentCell cell = (DefaultEnvironmentCell)editor.getCell();
        Map<String, LightNode> lights = cell.getLightMap();
        
        //remove any existing panels.
        tabbedPane.removeAll();
        
        for(Map.Entry<String, LightNode> e: lights.entrySet()) {
            tabbedPane.add(e.getKey(), new DirectionalLightPropertiesPanel(e.getValue(), cell, e.getKey(), this.editor));
        
        }
        
        tabbedPane.add("Skybox", new SkyboxProperties(cell, editor));
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        LOGGER.fine("Running on thread: "+Thread.currentThread().getName());
        DirectionalLightPropertiesPanel p1 = getLightPanelFromString("LIGHT-1");
        DirectionalLightPropertiesPanel p2 = getLightPanelFromString("LIGHT-2");
        DirectionalLightPropertiesPanel p3 = getLightPanelFromString("LIGHT-3");
        SkyboxProperties skybox = getSkyboxProperties();
        
        p1.close();
        p2.close();
        p3.close();
        skybox.close();
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        DirectionalLightPropertiesPanel p1 = getLightPanelFromString("LIGHT-1");
        DirectionalLightPropertiesPanel p2 = getLightPanelFromString("LIGHT-2");
        DirectionalLightPropertiesPanel p3 = getLightPanelFromString("LIGHT-3");
        SkyboxProperties skybox = getSkyboxProperties();
        
        p1.applyIfNeeded();
        p2.applyIfNeeded();
        p3.applyIfNeeded();
        skybox.applyIfNeeded();
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        DirectionalLightPropertiesPanel p1 = getLightPanelFromString("LIGHT-1");
        DirectionalLightPropertiesPanel p2 = getLightPanelFromString("LIGHT-2");
        DirectionalLightPropertiesPanel p3 = getLightPanelFromString("LIGHT-3");
        SkyboxProperties skybox = getSkyboxProperties();
        
        p1.restore();
        p2.restore();
        p3.restore();
        skybox.restore();
    }
    
    private DirectionalLightPropertiesPanel getLightPanelFromString(String key) {
        int index = tabbedPane.indexOfTab(key);
        LOGGER.fine("Got index: "+index+" for light: "+key);
        if(tabbedPane.getComponentAt(index) == null) {
            LOGGER.warning("NULL component at index: "+index);
            return null;
        }
        return (DirectionalLightPropertiesPanel)tabbedPane.getComponentAt(index);
    }
    
    private SkyboxProperties getSkyboxProperties() {
        int index = tabbedPane.indexOfTab("Skybox");
        if(tabbedPane.getComponentAt(index) == null) {
            LOGGER.warning("NULL component at index: "+index);
            return null;
        }
        return (SkyboxProperties)tabbedPane.getComponentAt(index);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/defaultenvironment/client/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("DefaultEnvironmentProperties.jLabel1.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

}
