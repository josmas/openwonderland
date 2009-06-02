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

package org.jdesktop.wonderland.modules.contentrepo.client.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 * A generic browser for webdav content repositories. Supports the
 * ContentBrowserSPI interface so that it can be plugged into the browser
 * registry mechanism.
 * 
 * @author jkaplan
 */
public class ContentBrowserJDialog extends javax.swing.JDialog
        implements ContentBrowserSPI {

    private static final Logger logger =
            Logger.getLogger(ContentBrowserJDialog.class.getName());

    private ServerSessionManager session = null;
    private ContentRepository repo = null;
    private ContentCollection directory = null;

    /** Creates new form BrowserFrame */
    public ContentBrowserJDialog(ServerSessionManager session) {
        this.session = session;
        initComponents();

        fileList.setCellRenderer(new ContentRenderer());

        // Listen for when a new directory/file is selected in the list of
        // files in a content repository. Update the state of the buttons
        fileList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                changeListSelection();
            }
        });

        // Listen for when there is a double-click on the file list to change
        // to that directory.
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    changeDirectory();
                }
            }
        });

        // Listen for when a new category is selected in the category list.
        // Update the state of the GUI
        categoryList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                changeCategorySelection();
            }
        });

        // When the Cancel button is pressed, fire off events to the listeners
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
                for (ContentBrowserListener l : listeners) {
                    l.cancelAction();
                }
                listeners.clear();
            }
        });

        // When the Ok button is pressed, fire off events to the listeners,
        // passing them the URI of the selected item
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
                for (ContentBrowserListener l : listeners) {
                    // We need to get the URI of the selection. Start from
                    // the currently selected item, if there is one, otherwise
                    // use the selected directory we are in
                    ContentNode node = getListSelection();
                    String assetPath = (node != null) ? node.getPath() : directory.getPath();

                    // The value returned from getPath() starts with a beginning
                    // slash, so strip it if so
                    if (assetPath.startsWith("/") == true) {
                        assetPath = assetPath.substring(1);
                    }
                    l.okAction("wlcontent://" + assetPath);
                }
                listeners.clear();
            }
        });
    }

    /**
     * @inheritDoc()
     */
    @Override
    public void setVisible(boolean visible) {
        // Set the dialog visible, but also set the current directory to the
        // user root
        super.setVisible(visible);
        if (visible == true) {
            // Set the user root when the content browser is made visible.
            ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
            repo = registry.getRepository(session);
            try {
                setCollection(repo.getUserRoot());
            } catch (ContentRepositoryException cce) {
                logger.log(Level.WARNING, "Error getting user root", cce);
            }

            // Update the state of the GUI to represent the initially selected
            // directory
            categoryList.setSelectedValue("Users", true);
            changeListSelection();
        }
    }

    /**
     * @inheritDoc()
     */
    public void setActionName(BrowserAction action, String name) {
        switch (action) {
            case OK_ACTION:
                okButton.setText(name);
                break;

            case CANCEL_ACTION:
                cancelButton.setText(name);
                break;
        }
    }

    private Set<ContentBrowserListener> listeners =
            Collections.synchronizedSet(new HashSet());

    /**
     * @inheritDoc()
     */
    public void addContentBrowserListener(ContentBrowserListener listener) {
        listeners.add(listener);
    }

    /**
     * @inheritDoc()
     */
    public void removeContentBrowserListener(ContentBrowserListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the currently selected item in the list of files as a ContentNode
     * object, or null if nothing is selected.
     */
    private ContentNode getListSelection() {
        ContentNode selected = null;

        Object selectedObj = fileList.getSelectedValue();
        if (selectedObj instanceof ParentHolder) {
            selected = ((ParentHolder) selectedObj).getParent();
        } else if (selectedObj != null) {
            selected = (ContentNode) selectedObj;
        }

        return selected;
    }

    /**
     * Handles when a new category selection is made in the category list,
     * update the state of the GUI
     */
    private void changeCategorySelection() {
        String category = (String)categoryList.getSelectedValue();
        if (category.equals("System") == true) {
            try {
                setCollection(repo.getSystemRoot());
            } catch (ContentRepositoryException cce) {
                logger.log(Level.WARNING, "Error getting user root", cce);
            }
        }
        else if (category.equals("Users") == true) {
            try {
                setCollection(repo.getUserRoot());
            } catch (ContentRepositoryException cce) {
                logger.log(Level.WARNING, "Error getting user root", cce);
            }
        }

        changeListSelection();
    }

    /**
     * Handles when a new selection is made in the file list, update the state
     * of buttons and labels.
     */
    private void changeListSelection() {
        ContentNode selected = getListSelection();
        
        boolean enableDownload = false;
        boolean enableDelete = false;

        if (selected == null) {
            typeLabel.setText("");
            sizeLabel.setText("");
            modifiedLabel.setText("");
            urlLabel.setText("");
        } else if (selected instanceof ContentCollection) {
            typeLabel.setText("Directory");
            sizeLabel.setText("");
            modifiedLabel.setText("");
            urlLabel.setText("");

            enableDelete = true;
        } else if (selected instanceof ContentResource) {
            ContentResource r = (ContentResource) selected;

            typeLabel.setText("File");
            sizeLabel.setText(String.valueOf(r.getSize()));
            
            DateFormat df = DateFormat.getDateInstance();
            modifiedLabel.setText(df.format(r.getLastModified()));

            try {
                urlLabel.setText(r.getURL().toExternalForm());
            } catch (ContentRepositoryException cre) {
                logger.log(Level.WARNING, "Unable to get URL for " + r, cre);
                urlLabel.setText("Error: " + cre.getMessage());
            }

            enableDownload = true;
            enableDelete = true;
        }

        downloadButton.setEnabled(enableDownload);
        deleteCollectionButton.setEnabled(enableDelete);
    }

    /**
     * When a new directory is selected, change to that directory in the GUI
     */
    private void changeDirectory() {
        ContentNode selected = getListSelection();
        if (selected instanceof ContentCollection) {
            setCollection((ContentCollection) selected);
        }
    }

    /**
     * Sets the current directory (represented by a ContentCollection object)
     * to display in the GUI.
     */
    private void setCollection(ContentCollection collection) {
        directory = collection;
        dirNameLabel.setText(collection.getPath());
        fileList.setModel(new ContentListModel(collection));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        topButtonPanel = new javax.swing.JPanel();
        newCollectionButton = new javax.swing.JButton();
        deleteCollectionButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        directoryNamePanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        dirNameLabel = new javax.swing.JLabel();
        bottomPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        listSplitPane = new javax.swing.JSplitPane();
        categoryScrollPane = new javax.swing.JScrollPane();
        categoryList = new javax.swing.JList();
        subSplitPane = new javax.swing.JSplitPane();
        fileScrollPane = new javax.swing.JScrollPane();
        fileList = new javax.swing.JList();
        previewInfoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        infoPanel = new javax.swing.JPanel();
        modifiedLabel = new javax.swing.JLabel();
        sizeLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setTitle("Content Repository Browser");

        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        newCollectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/contentrepo/client/ui/resources/ContentBrowserNewDirectory32x32.png"))); // NOI18N
        newCollectionButton.setToolTipText("New Directory");
        newCollectionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        newCollectionButton.setMaximumSize(new java.awt.Dimension(32, 32));
        newCollectionButton.setMinimumSize(new java.awt.Dimension(32, 32));
        newCollectionButton.setPreferredSize(new java.awt.Dimension(32, 32));
        newCollectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newCollectionButtonActionPerformed(evt);
            }
        });
        topButtonPanel.add(newCollectionButton);

        deleteCollectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/contentrepo/client/ui/resources/ContentBrowserDeleteFile32x32.png"))); // NOI18N
        deleteCollectionButton.setToolTipText("Delete");
        deleteCollectionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteCollectionButton.setMaximumSize(new java.awt.Dimension(32, 32));
        deleteCollectionButton.setMinimumSize(new java.awt.Dimension(32, 32));
        deleteCollectionButton.setPreferredSize(new java.awt.Dimension(32, 32));
        deleteCollectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCollectionButtonActionPerformed(evt);
            }
        });
        topButtonPanel.add(deleteCollectionButton);

        uploadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/contentrepo/client/ui/resources/ContentBrowserUploadFile32x32.png"))); // NOI18N
        uploadButton.setToolTipText("Upload File");
        uploadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        uploadButton.setMaximumSize(new java.awt.Dimension(32, 32));
        uploadButton.setMinimumSize(new java.awt.Dimension(32, 32));
        uploadButton.setPreferredSize(new java.awt.Dimension(32, 32));
        uploadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadButtonActionPerformed(evt);
            }
        });
        topButtonPanel.add(uploadButton);

        downloadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/contentrepo/client/ui/resources/ContentBrowserDownloadFile32x32.png"))); // NOI18N
        downloadButton.setToolTipText("Download File");
        downloadButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        downloadButton.setMaximumSize(new java.awt.Dimension(32, 32));
        downloadButton.setMinimumSize(new java.awt.Dimension(32, 32));
        downloadButton.setPreferredSize(new java.awt.Dimension(32, 32));
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });
        topButtonPanel.add(downloadButton);

        topPanel.add(topButtonPanel);

        jLabel6.setText("Directory:");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        directoryNamePanel.add(jLabel6);

        dirNameLabel.setText("<directory>");
        dirNameLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        directoryNamePanel.add(dirNameLabel);

        topPanel.add(directoryNamePanel);

        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        bottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 3, 5));

        okButton.setText("OK");
        bottomPanel.add(okButton);

        cancelButton.setText("Cancel");
        bottomPanel.add(cancelButton);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.X_AXIS));

        listSplitPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        listSplitPane.setDividerLocation(200);
        listSplitPane.setDividerSize(7);

        categoryScrollPane.setBackground(new java.awt.Color(204, 204, 204));
        categoryScrollPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        categoryScrollPane.setMaximumSize(new java.awt.Dimension(114, 32767));
        categoryScrollPane.setMinimumSize(new java.awt.Dimension(114, 23));

        categoryList.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        categoryList.setFont(new java.awt.Font("Lucida Grande", 1, 12));
        categoryList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "System", "Users" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        categoryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categoryList.setFixedCellWidth(100);
        categoryList.setMinimumSize(new java.awt.Dimension(100, 44));
        categoryScrollPane.setViewportView(categoryList);

        listSplitPane.setLeftComponent(categoryScrollPane);

        subSplitPane.setBorder(null);
        subSplitPane.setDividerLocation(300);
        subSplitPane.setDividerSize(7);
        subSplitPane.setResizeWeight(1.0);
        subSplitPane.setMaximumSize(new java.awt.Dimension(105, 2147483647));

        fileScrollPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        fileList.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        fileList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fileScrollPane.setViewportView(fileList);

        subSplitPane.setLeftComponent(fileScrollPane);

        previewInfoPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        previewInfoPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setText("Preview:");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setAlignmentX(0.5F);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        previewInfoPanel.add(jLabel1, gridBagConstraints);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout previewPanelLayout = new org.jdesktop.layout.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 270, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 212, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        previewInfoPanel.add(previewPanel, gridBagConstraints);

        modifiedLabel.setText("modified");

        sizeLabel.setText("size");

        typeLabel.setText("type");

        urlLabel.setText("url");

        jLabel2.setText("Type:");

        jLabel3.setText("Size:");

        jLabel4.setText("Modified:");

        jLabel5.setText("URL:");

        org.jdesktop.layout.GroupLayout infoPanelLayout = new org.jdesktop.layout.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infoPanelLayout.createSequentialGroup()
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(jLabel4)
                    .add(jLabel5))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(urlLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sizeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .add(typeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, modifiedLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                .addContainerGap())
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(infoPanelLayout.createSequentialGroup()
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(typeLabel)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sizeLabel)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(modifiedLabel)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(infoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(jLabel5))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        previewInfoPanel.add(infoPanel, gridBagConstraints);

        subSplitPane.setRightComponent(previewInfoPanel);

        listSplitPane.setRightComponent(subSplitPane);

        centerPanel.add(listSplitPane);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        ContentResource selected = (ContentResource) getListSelection();

        // Display a file choose and select a directory in which to save the
        // content
        JFileChooser chooser = new JFileChooser("Choose a directory");
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directories";
            }
        });
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);

        // Download the actual file
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File out = new File(chooser.getSelectedFile(), selected.getName());
            try {
                selected.get(out);
            } catch (ContentRepositoryException cre) {
                logger.log(Level.WARNING, "Unable to download " + out, cre);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Unable to write " + out, ioe);
            }
        }
    }//GEN-LAST:event_downloadButtonActionPerformed

    private void deleteCollectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCollectionButtonActionPerformed

        // Fetch the currently selected item in the file list and remove it
        ContentNode selected = getListSelection();
        try {
            directory.removeChild(selected.getName());
        } catch (ContentRepositoryException cre) {
            logger.log(Level.WARNING, "Error removing " + selected.getPath(),
                       cre);
        }

        setCollection(directory);
    }//GEN-LAST:event_deleteCollectionButtonActionPerformed

    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        // Show a file chooser that queries for the file to upload.
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Fetch the file that was selected, make sure it exists, create an
        // entry in the content repository and upload the file.
        String path = chooser.getSelectedFile().getPath();
        File file = new File(path);
        if (file.exists() == true) {
            try {
                ContentResource r = (ContentResource)
                        directory.createChild(file.getName(), ContentNode.Type.RESOURCE);
                r.put(file);

                setCollection(directory);
            } catch (ContentRepositoryException cre) {
                logger.log(Level.WARNING, "Unable to upload " + file, cre);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Unable to read " + file, ioe);
            }
        }
    }//GEN-LAST:event_uploadButtonActionPerformed

    private void newCollectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newCollectionButtonActionPerformed

        // Display a dialog that queries for the next directory name.
       String s = (String)JOptionPane.showInputDialog(this,
               "Please enter the name of the directory:",
               "Create New Directory",
               JOptionPane.QUESTION_MESSAGE);

        // XXX Probably should check if it already exists.
        if (s == null) {
            return;
        }

        // Go ahead and create the new directory in the content repository
        String name = s.trim();
        try {
            directory.createChild(name, ContentNode.Type.COLLECTION);
            setCollection(directory);
        } catch (ContentRepositoryException ex) {
            logger.log(Level.WARNING, "Unable to create directory", ex);
        }
    }//GEN-LAST:event_newCollectionButtonActionPerformed

    /**
     * Renders the directories and files in the list. Inserts a ".." for the
     * parent directory
     */
    class ContentRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object obj,
                int index, boolean selected, boolean hasFocus)
        {
            String desc;
            if (obj instanceof ParentHolder) {
                desc = "..";
            } else if (obj instanceof ContentNode) {
                desc = ((ContentNode) obj).getName();
            } else {
                desc = obj.toString();
            }

            return super.getListCellRendererComponent(list, desc, index,
                                                      selected, hasFocus);
        }

    }

    /**
     * List model for list of files and directories
     */
    class ContentListModel extends AbstractListModel {
        private ContentCollection dir;
        private ContentCollection parent;
        private List<ContentNode> children;
        private boolean hasParent;
        private int size = 0;

        public ContentListModel(ContentCollection dir) {
            this.dir = dir;

            hasParent = (dir.getParent() != null);
            try {
                size = dir.getChildren().size();
                size = (hasParent == true) ? size + 1 : size;
            } catch (ContentRepositoryException cce) {
                logger.log(Level.WARNING, "Error getting size of " +
                           dir.getName(), cce);
                size = 0;
            }
            parent = dir.getParent();

            try {
                children = dir.getChildren();
            } catch (ContentRepositoryException cce) {
                logger.log(Level.WARNING, "Error reading child from " +
                           dir.getName(), cce);
                children = null;
            }
        }
        
        public int getSize() {
            return size;
        }

        public Object getElementAt(int index) {
            // See if there is a ".." parent node. Adjust the index if so, or
            // return the parent depending upon the index.
            if (index == 0 && hasParent) {
                return new ParentHolder(parent);
            } else if (hasParent) {
                index -= 1;
            }

            // If there are no children, then just return null. Otherwise,
            // return the child.
            if (children == null) {
                return null;
            }
            return children.get(index);
        }
    }

    /**
     * Holds a reference to the parent directory
     */
    class ParentHolder {
        private ContentCollection parent;

        public ParentHolder(ContentCollection parent) {
            this.parent = parent;
        }

        public ContentCollection getParent() {
            return parent;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JList categoryList;
    private javax.swing.JScrollPane categoryScrollPane;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton deleteCollectionButton;
    private javax.swing.JLabel dirNameLabel;
    private javax.swing.JPanel directoryNamePanel;
    private javax.swing.JButton downloadButton;
    private javax.swing.JList fileList;
    private javax.swing.JScrollPane fileScrollPane;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JSplitPane listSplitPane;
    private javax.swing.JLabel modifiedLabel;
    private javax.swing.JButton newCollectionButton;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel previewInfoPanel;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSplitPane subSplitPane;
    private javax.swing.JPanel topButtonPanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton uploadButton;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables
}
