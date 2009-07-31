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
package org.jdesktop.wonderland.modules.palette.client;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry;
import org.jdesktop.wonderland.client.cell.registry.CellRegistry.CellRegistryListener;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.modules.palette.client.dnd.PaletteDragGestureListener;

/**
 * A palette of cells to create in the world by drag and drop, as a HUD panel.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class HUDCellPalette extends javax.swing.JPanel {

    private Map<String, CellFactorySPI> cellFactoryMap = new HashMap();
    private Image noPreviewAvailableImage = null;
    private CellRegistryListener cellListener = null;

    private int index = 0;
    private JPanel palettePanel;
    private static final int SIZE = 48;
    private static final int SPACING = 10;
    private static final int NUMBER_VISIBLE = 5;
    private int width = 0;

    /** Creates new form VisualPanelFrame */
    public HUDCellPalette() {
        initComponents();

        // Create the icon for the "No Preview Available" image
        URL url = CellPalette.class.getResource("resources/nopreview.png");
        noPreviewAvailableImage = Toolkit.getDefaultToolkit().createImage(url);

        // Create the scroll pane and viewport, and add the icons
        JViewport viewport = new JViewport();
        FlowLayout layout = new FlowLayout();
        palettePanel = new JPanel();
        palettePanel.setLayout(layout);
        layout.setHgap(0);
        layout.setVgap(0);

        viewport.setView(palettePanel);
        width = getWidth(NUMBER_VISIBLE, SIZE, SPACING);
        viewport.setPreferredSize(new Dimension(width, SIZE));
        mainPanel.add(viewport);

        // Create a listener for changes to the list of registered Cell
        // factories, to be used in setVisible(). When the list changes we
        // simply do a fresh update of all values.
        cellListener = new CellRegistryListener() {
            public void cellRegistryChanged() {
                // Since this is not happening (necessarily) in the AWT Event
                // Thread, we should put it in one
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updatePanelIcons();
                    }
                });
            }
        };
        CellRegistry.getCellRegistry().addCellRegistryListener(cellListener);
        updatePanelIcons();
    }

    /**
     * Updates the list of values displayed from the CellRegistry.
     *
     * NOTE: This method assumes it is being called in the AWT Event Thread.
     */
    public void updatePanelIcons() {
        // We synchronized around the cellFactoryMap so that this action does not
        // interfere with any changes in the map.
        synchronized (cellFactoryMap) {
            // First remove all of the entries in the map and the panel
            cellFactoryMap.clear();
            palettePanel.removeAll();

            // Fetch the registry of cells and for each, get the palette info and
            // populate the list.
            CellRegistry registry = CellRegistry.getCellRegistry();
            Set<CellFactorySPI> cellFactories = registry.getAllCellFactories();

            for (CellFactorySPI cellFactory : cellFactories) {
                try {
                    // We only add the entry if it has a non-null display name.
                    // Fetch the preview image (use the default if none exists
                    // and add to the panel
                    String name = cellFactory.getDisplayName();
                    Image preview = cellFactory.getPreviewImage();
                    if (name != null) {
                        cellFactoryMap.put(name, cellFactory);
                        JPanel label = createJLabel(preview, name, SIZE);
                        palettePanel.add(label);
                    }
                } catch (java.lang.Exception excp) {
                    // Just ignore, but log a message
                    Logger logger = Logger.getLogger(CellPalette.class.getName());
                    logger.log(Level.WARNING, "No Display Name for Cell Factory " +
                            cellFactory, excp);
                }
            }

            // Tell the panel to invalide itself and re-do its layout
            palettePanel.invalidate();
            palettePanel.repaint();
            index = 0;
            int offset = getOffset(index, SIZE, SPACING);
            palettePanel.scrollRectToVisible(new Rectangle(offset, 0, width, SIZE));
        }
    }

    /**
     * Computes the total width of the viewport given the number of items visible,
     * their size, and the spacing between each
     */
    private int getWidth(int numberItems, int size, int spacing) {
        // The first part of the width is the number of items times each of
        // their sizes
        int guiWidth = numberItems * size;

        // But we need to account for the interior spacing of each, to the right
        // of every item but the last. For for n items, there are n-1 spacings
        guiWidth += (spacing * (numberItems - 1));
        return guiWidth;
    }

    /**
     * Computes the offset (width-wise) of the nth item
     */
    private int getOffset(int n, int size, int spacing) {
        // The first part of the offset is the number of items times each of
        // their sizes
        int offset = n * size;

        // But we need to account for the interior spacing of each, to the right
        // of every item.
        offset += (spacing * n);
        return offset;
    }

    /**
     * Creates a new label given the Image, the cell name, and the size to make
     * it.
     */
    private JPanel createJLabel(Image image, String displayName, int size) {
        // If the preview image is null, then use the default one
        if (image == null) {
            image = noPreviewAvailableImage;
        }

        // First resize the image. We use a trick to fetch the BufferedImage
        // from the given Image, by creating the ImageIcon and calling the
        // getImage() method. Then resize into a Buffered Image.
        Image srcImage = new ImageIcon(image).getImage();
        BufferedImage resizedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImage, 0, 0, size, size, null);
        g2.dispose();

        // Create the label with the preview image
        JLabel label = new JLabel(new ImageIcon(resizedImage));
        label.setPreferredSize(new Dimension(size, size));
        label.setMaximumSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setToolTipText(displayName);

        // Put the label in a panel with a border
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, SPACING));
        panel.add(label);

        // Set up the drag and drop support for the image
        DragSource ds = DragSource.getDefaultDragSource();
        PaletteDragGestureListener listener = new PaletteDragGestureListener();
        listener.previewImage = resizedImage;
        listener.cellFactory = cellFactoryMap.get(displayName);
        ds.createDefaultDragGestureRecognizer(label,
                DnDConstants.ACTION_COPY_OR_MOVE, listener);

        return panel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton2 = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/palette/client/resources/left_arrow.png"))); // NOI18N
        jButton2.setBorderPainted(false);
        jButton2.setMargin(new java.awt.Insets(0, 5, 0, 5));
        jButton2.setMaximumSize(new java.awt.Dimension(16, 32));
        jButton2.setPreferredSize(new java.awt.Dimension(16, 32));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftMouseButtonActionPerformed(evt);
            }
        });
        add(jButton2);

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new java.awt.GridLayout(1, 0));
        add(mainPanel);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/palette/client/resources/right_arrow.png"))); // NOI18N
        jButton3.setBorderPainted(false);
        jButton3.setIconTextGap(0);
        jButton3.setMargin(new java.awt.Insets(0, 5, 0, 5));
        jButton3.setMaximumSize(new java.awt.Dimension(16, 32));
        jButton3.setMinimumSize(new java.awt.Dimension(16, 32));
        jButton3.setPreferredSize(new java.awt.Dimension(16, 32));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightMouseButtonActionPerformed(evt);
            }
        });
        add(jButton3);
    }// </editor-fold>//GEN-END:initComponents

    private void leftMouseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftMouseButtonActionPerformed
        if (index <= 0) {
            return;
        }
        index--;
        int offset = getOffset(index, SIZE, SPACING);
        palettePanel.scrollRectToVisible(new Rectangle(offset, 0, width, SIZE));
}//GEN-LAST:event_leftMouseButtonActionPerformed

    private void rightMouseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightMouseButtonActionPerformed
        if ((index + NUMBER_VISIBLE) >= cellFactoryMap.size()) {
            return;
        }
        index++;
        int offset = getOffset(index, SIZE, SPACING);
        palettePanel.scrollRectToVisible(new Rectangle(offset, 0, width, SIZE));
}//GEN-LAST:event_rightMouseButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

}
