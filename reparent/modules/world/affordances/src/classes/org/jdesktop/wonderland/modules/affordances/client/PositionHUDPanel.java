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
package org.jdesktop.wonderland.modules.affordances.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ComponentChangeListener;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.input.InputManager;
import org.jdesktop.wonderland.client.scenemanager.SceneManager;
import org.jdesktop.wonderland.client.scenemanager.event.SelectionEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A panel to display affordance items on the HUD.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class PositionHUDPanel extends javax.swing.JPanel {

    private static Logger logger = Logger.getLogger(PositionHUDPanel.class.getName());

    /* The currently selected Cell and its movable component */
    private Cell selectedCell = null;
    private MovableComponent movableComponent = null;

    /* The frame that holds this panel */
    private JFrame frame = null;

    /* Various listener on the Cell and Swing JSpinners */
    private ComponentChangeListener componentListener = null;
    private TransformChangeListener transformListener = null;
    private ChangeListener translationListener = null;
    private ChangeListener rotationListener = null;
    private ChangeListener scaleListener = null;

    /* Models for the Swing JSpinners */
    private SpinnerNumberModel xTranslationModel = null;
    private SpinnerNumberModel yTranslationModel = null;
    private SpinnerNumberModel zTranslationModel = null;
    private SpinnerNumberModel xScaleModel = null;
    private SpinnerNumberModel yScaleModel = null;
    private SpinnerNumberModel zScaleModel = null;
    private SpinnerNumberModel xRotationModel = null;
    private SpinnerNumberModel yRotationModel = null;
    private SpinnerNumberModel zRotationModel = null;

    /*
     * This boolean indicates whether the values of the spinners are being
     * set programmatically, e.g. when a transform changed event has been
     * received from the Cell. In such a case, we do not want to generate a
     * new message to the movable component
     */
    private boolean setLocal = false;

    /** Creates new form AffordanceHUDPanel */
    public PositionHUDPanel(JFrame frame) {
        this.frame = frame;
        initComponents();
        
        // Listen for selections to update the HUD panel
        InputManager.inputManager().addGlobalEventListener(new SelectionListener());

        // Set the maximum and minimum values for each
        Float value = new Float(0);
        Float min = new Float(Float.NEGATIVE_INFINITY);
        Float max = new Float(Float.POSITIVE_INFINITY);
        Float step = new Float(0.1);
        xTranslationModel = new SpinnerNumberModel(value, min, max, step);
        yTranslationModel = new SpinnerNumberModel(value, min, max, step);
        zTranslationModel = new SpinnerNumberModel(value, min, max, step);
        translationXTF.setModel(xTranslationModel);
        translationYTF.setModel(yTranslationModel);
        translationZTF.setModel(zTranslationModel);

        value = new Float(1);
        min = new Float(0);
        xScaleModel = new SpinnerNumberModel(value, min, max, step);
        yScaleModel = new SpinnerNumberModel(value, min, max, step);
        zScaleModel = new SpinnerNumberModel(value, min, max, step);
        scaleXTF.setModel(xScaleModel);

        value = new Float(0);
        min = new Float(-360);
        max = new Float(360);
        step = new Float(1);
        xRotationModel = new SpinnerNumberModel(value, min, max, step);
        yRotationModel = new SpinnerNumberModel(value, min, max, step);
        zRotationModel = new SpinnerNumberModel(value, min, max, step);
        rotationXTF.setModel(xRotationModel);
        rotationYTF.setModel(yRotationModel);
        rotationZTF.setModel(zRotationModel);

        // Listen for changes, if there is a movable component added or removed
        // update the state of the fields
        componentListener = new ComponentChangeListener() {
            public void componentChanged(Cell cell, ChangeType type, CellComponent component) {
                if (type == ChangeType.ADDED && component instanceof MovableComponent) {
                    movableComponent = (MovableComponent)component;
                    setGUIEnabled(true);
                }
            }
        };

        // Listen for changes to the cell transform that may be done by other
        // parts of this client or other clients.
        transformListener = new TransformChangeListener() {
            public void transformChanged(Cell cell, ChangeSource source) {
//                CellTransform cellTransform = cell.getLocalTransform();
//                Quaternion rotation = cellTransform.getRotation(null);
//                float[] angles = rotation.toAngles(new float[3]);
//                System.out.println("CELL TRANSFORM CHANGED NEW ROTATION VALUES " +
//                        angles[0] + " " + angles[1] + " " + angles[2]);

                updateGUI();
            }
        };

        // Listen for changes to the translation values and update the cell as
        // a result. Only update the result if it doesn't happen because the
        // value in the spinner is changed programmatically. The value of
        // 'setLocal' is set always in the AWT Event Thread, the same thread
        // as this listener.
        translationListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (setLocal == false) {
                    updateTranslation();
                }
            }
        };
        xTranslationModel.addChangeListener(translationListener);
        yTranslationModel.addChangeListener(translationListener);
        zTranslationModel.addChangeListener(translationListener);

        // Listen for changes to the rotation values and update the cell as a
        // result. See the comments above for 'translationListener' too.
        rotationListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
//                float x = (Float) xRotationModel.getValue();
//                float y = (Float) yRotationModel.getValue();
//                float z = (Float) zRotationModel.getValue();
//                System.out.println("STATE CHANGED in JSPINNER VALUES setLocal=" +
//                        setLocal + " " + x + " " + y + " " + z + " SOURCE " +
//                        e.getSource());

                if (setLocal == false) {
                    updateRotation();
                }
            }
        };
        xRotationModel.addChangeListener(rotationListener);
        yRotationModel.addChangeListener(rotationListener);
        zRotationModel.addChangeListener(rotationListener);

        // Listen for changes to the scale values and update the cell as a
        // result. See the comments above for 'translationListener' too.
        scaleListener = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (setLocal == false) {
                    updateScale();
                }
            }
        };
        xScaleModel.addChangeListener(scaleListener);
        yScaleModel.addChangeListener(scaleListener);
        zScaleModel.addChangeListener(scaleListener);

        // Turn off the GUI initially, until we have a selected cell
        clearGUI();
        setGUIEnabled(false);
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
        translationXTF = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        translationYTF = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        translationZTF = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        rotationXTF = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        rotationYTF = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        rotationZTF = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        scaleXTF = new javax.swing.JSpinner();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("Position: (");
        add(jLabel1);

        translationXTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        translationXTF.setMinimumSize(new java.awt.Dimension(60, 30));
        translationXTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(translationXTF);

        jLabel2.setText(",");
        add(jLabel2);

        translationYTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        translationYTF.setMinimumSize(new java.awt.Dimension(60, 30));
        translationYTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(translationYTF);

        jLabel3.setText(",");
        add(jLabel3);

        translationZTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        translationZTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(translationZTF);

        jLabel4.setText(")");
        add(jLabel4);

        jLabel5.setText("Rotation: (");
        jLabel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        add(jLabel5);

        rotationXTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        rotationXTF.setMinimumSize(new java.awt.Dimension(60, 30));
        rotationXTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(rotationXTF);

        jLabel6.setText(",");
        add(jLabel6);

        rotationYTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        rotationYTF.setMinimumSize(new java.awt.Dimension(60, 30));
        rotationYTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(rotationYTF);

        jLabel7.setText(",");
        add(jLabel7);

        rotationZTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        rotationZTF.setMinimumSize(new java.awt.Dimension(60, 30));
        rotationZTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(rotationZTF);

        jLabel8.setText(")");
        add(jLabel8);

        jLabel9.setText("Scaling: (");
        jLabel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        add(jLabel9);

        scaleXTF.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        scaleXTF.setMinimumSize(new java.awt.Dimension(60, 30));
        scaleXTF.setPreferredSize(new java.awt.Dimension(60, 30));
        add(scaleXTF);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Resets the values in the GUI back to 0
     */
    private void clearGUI() {
        translationXTF.setValue(0.0f);
        translationYTF.setValue(0.0f);
        translationZTF.setValue(0.0f);

        rotationXTF.setValue(0.0f);
        rotationYTF.setValue(0.0f);
        rotationZTF.setValue(0.0f);

        scaleXTF.setValue(1.0f);
    }

    /**
     * Updates the translation of the cell with the given values of the GUI.
     */
    private void updateTranslation() {
        float x = (Float) xTranslationModel.getValue();
        float y = (Float) yTranslationModel.getValue();
        float z = (Float) zTranslationModel.getValue();

        Vector3f translation = new Vector3f(x, y, z);
        if (movableComponent != null) {
            CellTransform cellTransform = selectedCell.getLocalTransform();
            cellTransform.setTranslation(translation);
            movableComponent.localMoveRequest(cellTransform);
        }
    }

    /**
     * Updates the rotation of the cell with the given values of the GUI.
     */
    private void updateRotation() {
        // Fetch the x, y, z rotation values from the GUI in degrees
        float x = (Float) xRotationModel.getValue();
        float y = (Float) yRotationModel.getValue();
        float z = (Float) zRotationModel.getValue();

//        System.out.println("NEW ROTATION SET IN GUI " + x + " " + y + " " + z);

        // Convert to radians
        x = (float)Math.toRadians(x);
        y = (float)Math.toRadians(y);
        z = (float)Math.toRadians(z);

        Quaternion newRotation = new Quaternion(new float[] { x, y, z });
        if (movableComponent != null) {
//            System.out.println("SENDING ROTATION VALUES TO SERVER");
            CellTransform cellTransform = selectedCell.getLocalTransform();
            cellTransform.setRotation(newRotation);
            movableComponent.localMoveRequest(cellTransform);
        }
    }

    /**
     * Updates the scale of the cell with the given values of the GUI.
     */
    private void updateScale() {
        float x = (Float) xScaleModel.getValue();

        if (movableComponent != null) {
            CellTransform cellTransform = selectedCell.getLocalTransform();
            cellTransform.setScaling(x);
            movableComponent.localMoveRequest(cellTransform);
        }
    }

    /**
     * Updates the GUI items in this panel for the currently selected cell. If
     * there is nothing selected, do nothing. This method does not assume it
     * is being called within the AWT Event Thread, so it does all operations
     * that affect the GUI using SwingUtilities.
     */
    public void updateGUI() {
        // Fetch the currently selected Cell. If none, then do nothing
        setSelectedCell(getSelectedCell());
        if (selectedCell == null) {
            clearGUI();
            return;
        }

        // Fetch the current transform from the movable component
        CellTransform cellTransform = selectedCell.getLocalTransform();
        final Vector3f translation = cellTransform.getTranslation(null);
        Quaternion rotation = cellTransform.getRotation(null);
        final Vector3f scale = cellTransform.getScaling(null);
        final float[] angles = rotation.toAngles(new float[3]);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Says that we are changing the values of the spinners
                // programmatically. This prevents the values from being sent
                // back to the Cell via the movable component.
                setLocalChanges(true);

                try {
                    // Do all actions to update the GUI in the AWT Event Thread.
                    // Update the translation spinners
                    translationXTF.setValue(translation.x);
                    translationYTF.setValue(translation.y);
                    translationZTF.setValue(translation.z);

                    // Update the rotation spinners only if they have changed
//        System.out.println("UPDATING GUI WITH ROTATION " +
//                Math.toDegrees(angles[0]) + " " + Math.toDegrees(angles[1]) +
//                " " + Math.toDegrees(angles[2]));

                    rotationXTF.setValue((float) Math.toDegrees(angles[0]));
                    rotationYTF.setValue((float) Math.toDegrees(angles[1]));
                    rotationZTF.setValue((float) Math.toDegrees(angles[2]));

                    // Update the scale spinners only if they have changes
                    scaleXTF.setValue((float) scale.x);
                } finally {
                    setLocalChanges(false);
                }
            }
        });
    }

    /**
     * Sets whether the GUI components are active (enabled).
     *
     * @param enabled True to make the GUI components enabled, false to not
     */
    private void setGUIEnabled(boolean enabled) {
        translationXTF.setEnabled(enabled);
        translationYTF.setEnabled(enabled);
        translationZTF.setEnabled(enabled);
        rotationXTF.setEnabled(enabled);
        rotationYTF.setEnabled(enabled);
        rotationZTF.setEnabled(enabled);
        scaleXTF.setEnabled(enabled);
    }

    /**
     * Sets whether the changes being made to the JSpinners are doing so
     * programmatically, rather than via a movable event. This is used to
     * make sure that requests to the movable component are not made at the
     * wrong time.
     *
     * @param isLocal True to indicate the JSpinner values are being set
     * programmatically.
     */
    private void setLocalChanges(boolean isLocal) {
        setLocal = isLocal;
    }

    /**
     * Handles when a new Cell is selected in the world. This removes the
     * listeners from the old Cell, adds the listeners to the new Cell and
     * updates the GUI.
     */
    private void setSelectedCell(Cell cell) {
        // First remove the listeners from the old Cell if such a Cell exists.
        if (selectedCell != null) {
            selectedCell.removeComponentChangeListener(componentListener);
            selectedCell.removeTransformChangeListener(transformListener);
        }
        selectedCell = cell;

        // If the newly selected Cell is null, we turn off the GUI and return
        if (cell == null) {
            frame.setTitle("Edit Cell: <none selected>");
            setGUIEnabled(false);
            return;
        }

        // Listen for changes in the transform of the Cell and update the
        // values of the spinners when that happens.
        cell.addTransformChangeListener(transformListener);

        // Listen for when the movable component is added, in case it has not
        // already been added. Set the movable component member variable.
        cell.addComponentChangeListener(componentListener);

        // Set the title of the frame properly to the selected Cell
        frame.setTitle("Edit Cell: " + cell.getName());

        // Fetch the movable component. For now, if it does not exist, then
        // turn off everything
        movableComponent = cell.getComponent(MovableComponent.class);
        if (movableComponent == null) {
            setGUIEnabled(false);
            addMovableComponent(cell);
        }
        else {
            setGUIEnabled(true);
        }
    }
    
    /**
     * Returns the currently selected cell, null if no cell is currently
     * selected.
     */
    private Cell getSelectedCell() {
        SceneManager manager = SceneManager.getSceneManager();
        List<Entity> entityList = manager.getSelectedEntities();
        if (entityList != null && entityList.size() > 0) {
            return SceneManager.getCellForEntity(entityList.get(0));
        }
        return null;
    }

    /**
     * Adds the movable component to the Cell
     */
    private void addMovableComponent(Cell cell) {
        String className = "org.jdesktop.wonderland.server.cell.MovableComponentMO";
        CellID cellID = cell.getCellID();
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newAddMessage(cellID, className);
        ResponseMessage response = cell.sendCellMessageAndWait(cscm);
        if (response instanceof ErrorMessage) {
            logger.log(Level.WARNING, "Unable to add movable component " +
                    "for Cell " + cell.getName() + " with ID " +
                    cellID, ((ErrorMessage) response).getErrorCause());
        }
    }
    /**
     * Inner class that listens for changes to the selection and upates the
     * state of the dialog appropriately
     */
    class SelectionListener extends EventClassListener {

        public SelectionListener() {
            setSwingSafe(true);
        }
        
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { SelectionEvent.class };
        }

        @Override
        public void commitEvent(Event event) {
            // Update the GUI based upon the newly selected Cell.
            updateGUI();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner rotationXTF;
    private javax.swing.JSpinner rotationYTF;
    private javax.swing.JSpinner rotationZTF;
    private javax.swing.JSpinner scaleXTF;
    private javax.swing.JSpinner translationXTF;
    private javax.swing.JSpinner translationYTF;
    private javax.swing.JSpinner translationZTF;
    // End of variables declaration//GEN-END:variables
}
