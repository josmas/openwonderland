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
package org.jdesktop.wonderland.modules.sample.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.content.ContentBrowserManager;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.ContentBrowserListener;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.utils.ContentRepositoryUtils;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.sample.common.SampleCellServerState;

/**
 * A property sheet for the sample cell type
 *
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(SampleCellServerState.class)
public class SampleCellProperties
        extends JPanel implements PropertiesFactorySPI {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/sample/client/resources/Bundle");
    private static final Logger LOGGER =
            Logger.getLogger(SampleCellProperties.class.getName());
    CellPropertiesEditor editor = null;
    private String originalShapeType = null;

    /** Creates new form SampleCellProperties */
    public SampleCellProperties() {
        initComponents();

        ComboBoxModel shapeTypeComboBoxModel = new DefaultComboBoxModel(
                new String[]{
                    BUNDLE.getString("BOX"),
                    BUNDLE.getString("SPHERE")});
        shapeTypeComboBox.setModel(shapeTypeComboBoxModel);

        // Listen for when the Browse... button is selected and display a
        // GUI to browser the content repository. Wait until OK has been
        // selected and fill in the text field with the URI
        browseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Fetch the browser for the webdav protocol and display it.
                // Add a listener for the result and update the value of the
                // text field for the URI
                ContentBrowserManager manager =
                        ContentBrowserManager.getContentBrowserManager();
                final ContentBrowserSPI browser =
                        manager.getDefaultContentBrowser();
                browser.addContentBrowserListener(new ContentBrowserListener() {

                    public void okAction(String uri) {
                        uriTextField.setText(uri);
                        browser.removeContentBrowserListener(this);
                    }

                    public void cancelAction() {
                        browser.removeContentBrowserListener(this);
                    }
                });
                browser.setVisible(true);
            }
        });

        // Listen for when the Edit button is selected and display a simple
        // text editor with the URI in the text field
        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String uri = uriTextField.getText();
                SimpleTextEditor editor = new SimpleTextEditor(uri);
                editor.setVisible(true);
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Sample_Cell");
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
        CellServerState state = editor.getCellServerState();
        if (state != null) {
            originalShapeType = ((SampleCellServerState) state).getShapeType();
            shapeTypeComboBox.setSelectedItem(originalShapeType);
        }
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing for now.
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Take the value from the shape type and populate the server state
        // with it.
        String newShapeType = (String) shapeTypeComboBox.getSelectedItem();
        CellServerState state = editor.getCellServerState();
        ((SampleCellServerState) state).setShapeType(newShapeType);
        editor.addToUpdateList(state);
    }

    /**
     * @inheritDoc()
     */
    public void restore() {
        shapeTypeComboBox.setSelectedItem(originalShapeType);
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
        shapeTypeComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        uriTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/sample/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("SampleCellProperties.jLabel1.text")); // NOI18N

        shapeTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shapeTypeActionPerformed(evt);
            }
        });
        shapeTypeComboBox.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                shapeTypePropertyChange(evt);
            }
        });

        jLabel2.setText(bundle.getString("SampleCellProperties.jLabel2.text")); // NOI18N

        browseButton.setText(bundle.getString("SampleCellProperties.browseButton.text")); // NOI18N

        editButton.setText(bundle.getString("SampleCellProperties.editButton.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(shapeTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(uriTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 308, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(editButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(shapeTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(uriTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(editButton)
                .addContainerGap(65, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void shapeTypePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_shapeTypePropertyChange
        // early return needed because of late model setting
        if (editor == null) {
            return;
        }

        // If the shape type has changed since the initial value, then
        // set the dirty bit to try
        String newShapeType = (String) shapeTypeComboBox.getSelectedItem();
        if ((originalShapeType != null) &&
                (originalShapeType.equals(newShapeType) == false)) {
            editor.setPanelDirty(SampleCellProperties.class, true);
        } else {
            editor.setPanelDirty(SampleCellProperties.class, false);
        }
    }//GEN-LAST:event_shapeTypePropertyChange

    private void shapeTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shapeTypeActionPerformed
        // TODO add your handling code here:
        // If the shape type has changed since the initial value, then
        // set the dirty bit to try
        String newShapeType = (String) shapeTypeComboBox.getSelectedItem();
        if ((originalShapeType != null) &&
                (originalShapeType.equals(newShapeType) == false)) {
            editor.setPanelDirty(SampleCellProperties.class, true);
        } else {
            editor.setPanelDirty(SampleCellProperties.class, false);
        }
    }//GEN-LAST:event_shapeTypeActionPerformed

    /**
     * A simple text frame with a "Save" button
     */
    private class SimpleTextEditor extends JFrame {

        private String uri = null;
        private JTextArea textArea = null;

        public SimpleTextEditor(final String uri) {
            this.uri = uri;

            // Set up the simple editor GUI components
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());
            textArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(textArea);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            JButton saveButton = new JButton(BUNDLE.getString("Save"));
            getContentPane().add(saveButton, BorderLayout.SOUTH);

            // Download the URI and text in the text area
            try {
                // Download the URI and text in the text area
                URL url = AssetUtils.getAssetURL(uri);
                textArea.setText(getURLAsString(url));
            } catch (MalformedURLException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            // Listen for the "Save" button
            saveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ContentNode contentNode =
                            ContentRepositoryUtils.findContentNode(null, uri);
                    ContentResource res = (ContentResource) contentNode;
                    String text = textArea.getText();
                    try {
                        res.put(text.getBytes());
                    } catch (ContentRepositoryException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            });

            pack();
            setSize(300, 300);
        }

        private String getURLAsString(URL url) throws IOException {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = r.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox shapeTypeComboBox;
    private javax.swing.JTextField uriTextField;
    // End of variables declaration//GEN-END:variables

    public <T extends CellServerState> void updateGUI(T cellServerState) {
        SampleCellServerState state = (SampleCellServerState) cellServerState;
        originalShapeType = state.getShapeType();
        shapeTypeComboBox.setSelectedItem(originalShapeType);
    }

    public <T extends CellServerState> void getCellServerState(T state) {
        ((SampleCellServerState) state).setShapeType(
                (String) shapeTypeComboBox.getSelectedItem());
    }
}
