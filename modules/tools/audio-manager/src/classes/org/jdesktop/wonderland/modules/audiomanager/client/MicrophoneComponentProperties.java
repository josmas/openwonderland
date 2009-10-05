/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MicrophoneComponentProperties.java
 *
 * Created on Sep 15, 2009, 12:14:47 PM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.MicrophoneBoundsType;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.FullVolumeArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.ActiveArea;
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.bounding.OrientedBoundingBox;
import com.jme.math.Vector3f;

/**
 *
 * @author jp
 */
@PropertiesFactory(MicrophoneComponentServerState.class)
public class MicrophoneComponentProperties extends javax.swing.JPanel 
	implements PropertiesFactorySPI {

    private CellPropertiesEditor editor = null;

    private String originalName = null;
    private int originalVolume = 1;
    private FullVolumeArea originalFullVolumeArea = new FullVolumeArea();
    private boolean originalShowBounds = false;
    private ActiveArea originalActiveArea = new ActiveArea();
    private boolean originalShowActiveArea = false;

    private SpinnerNumberModel fullVolumeRadiusModel = null;
    private SpinnerNumberModel xExtentModel = null;
    private SpinnerNumberModel yExtentModel = null;
    private SpinnerNumberModel zExtentModel = null;

    private SpinnerNumberModel activeAreaFullVolumeRadiusModel = null;
    private SpinnerNumberModel activeAreaXExtentModel = null;
    private SpinnerNumberModel activeAreaYExtentModel = null;
    private SpinnerNumberModel activeAreaZExtentModel = null;
    private SpinnerNumberModel activeAreaXOriginModel = null;
    private SpinnerNumberModel activeAreaYOriginModel = null;
    private SpinnerNumberModel activeAreaZOriginModel = null;

    private MicrophoneBoundsType fullVolumeAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;
    private MicrophoneBoundsType activeAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

    private BoundsViewerEntity boundsViewerEntity;
    private BoundsViewerEntity activeAreaViewerEntity;

    /** Creates new form MicrophoneComponentProperties */
    public MicrophoneComponentProperties() {
        initComponents();

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(
                new NameTextFieldListener());

        // Set the maximum and minimum values for the volume radius spinner
        fullVolumeRadiusModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        fullVolumeRadiusSpinner.setModel(fullVolumeRadiusModel);

        xExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        xExtentSpinner.setModel(xExtentModel);

        yExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        yExtentSpinner.setModel(yExtentModel);

        zExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        zExtentSpinner.setModel(zExtentModel);

        fullVolumeRadiusModel.addChangeListener(new RadiusChangeListener());
        xExtentModel.addChangeListener(new XExtentChangeListener());
        yExtentModel.addChangeListener(new YExtentChangeListener());
        zExtentModel.addChangeListener(new ZExtentChangeListener());

        // Set the maximum and minimum values for the volume radius spinner
        activeAreaFullVolumeRadiusModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        activeAreaFullVolumeRadiusSpinner.setModel(activeAreaFullVolumeRadiusModel);

        activeAreaXExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        activeAreaXExtentSpinner.setModel(activeAreaXExtentModel);

        activeAreaYExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        activeAreaYExtentSpinner.setModel(activeAreaYExtentModel);

        activeAreaZExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        activeAreaZExtentSpinner.setModel(activeAreaZExtentModel);

        activeAreaXOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        activeAreaXOriginSpinner.setModel(activeAreaXOriginModel);

        activeAreaYOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        activeAreaYOriginSpinner.setModel(activeAreaYOriginModel);

        activeAreaZOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        activeAreaZOriginSpinner.setModel(activeAreaZOriginModel);

        activeAreaFullVolumeRadiusModel.addChangeListener(new ActiveAreaRadiusChangeListener());
        activeAreaXExtentModel.addChangeListener(new ActiveAreaXExtentChangeListener());
        activeAreaYExtentModel.addChangeListener(new ActiveAreaYExtentChangeListener());
        activeAreaZExtentModel.addChangeListener(new ActiveAreaZExtentChangeListener());
        activeAreaXOriginModel.addChangeListener(new ActiveAreaXOriginChangeListener());
        activeAreaYOriginModel.addChangeListener(new ActiveAreaYOriginChangeListener());
        activeAreaZOriginModel.addChangeListener(new ActiveAreaZOriginChangeListener());
    }

    /**
     * @{inheritDoc}
     */
    public String getDisplayName() {
        //return BUNDLE.getString("Microphone");
        return "Microphone";
    }

    /**
     * @{inheritDoc}
     */
    public JPanel getPropertiesJPanel() {
        return this;
    }

    /**
     * @{inheritDoc}
     */
    public void setCellPropertiesEditor(CellPropertiesEditor editor) {
        this.editor = editor;
    }

    /**
     * @{inheritDoc}
     */
    public void open() {
        CellServerState cellServerState = editor.getCellServerState();
        MicrophoneComponentServerState state =
                (MicrophoneComponentServerState) cellServerState.getComponentServerState(
                MicrophoneComponentServerState.class);

        if (state == null) {
            return;
        }

	originalName = state.getName();

	originalVolume = VolumeUtil.getClientVolume(state.getVolume());

	originalFullVolumeArea = state.getFullVolumeArea();

	originalActiveArea = state.getActiveArea();

        BoundingVolume bounds = editor.getCell().getLocalBounds();

        if (bounds instanceof BoundingSphere) {
            float radius = ((BoundingSphere) bounds).getRadius();

            cellBoundsLabel.setText("Sphere with radius " + (Math.round(radius * 10) / 10f));
        } else if (bounds instanceof BoundingBox) {
            Vector3f extent = new Vector3f();
            extent = ((BoundingBox) bounds).getExtent(extent);
            showBoxBounds("Box", extent);
        } else if (bounds instanceof OrientedBoundingBox) {
            Vector3f extent = ((OrientedBoundingBox) bounds).getExtent();
            showBoxBounds("OrientedBox", extent);
        } else {
            cellBoundsLabel.setText(bounds.toString());
        }

	restore();
    }

    private void showBoxBounds(String s, Vector3f extent) {
        float x = Math.round(extent.getX() * 10) / 10f;
        float y = Math.round(extent.getY() * 10) / 10f;
        float z = Math.round(extent.getZ() * 10) / 10f;

        cellBoundsLabel.setText(s + " (" + x + ", " + y + ", " + z + ")");
    }

    /**
     * @{inheritDoc}
     */
    public void close() {
        if (boundsViewerEntity != null) {
            boundsViewerEntity.dispose();
            boundsViewerEntity = null;

	    showBoundsCheckBox.setSelected(false);
        }

        if (activeAreaViewerEntity != null) {
            activeAreaViewerEntity.dispose();
            activeAreaViewerEntity = null;
	    showActiveAreaCheckBox.setSelected(false);
        }
    }

    /**
     * @{inheritDoc}
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component. If it does not exist, then return, but we could always
        // create a new one really.
        CellServerState cellServerState = editor.getCellServerState();
        MicrophoneComponentServerState state = (MicrophoneComponentServerState) 
	    cellServerState.getComponentServerState(MicrophoneComponentServerState.class);

        if (state == null) {
            return;
        }

        state.setName(nameTextField.getText());

	state.setVolume(VolumeUtil.getServerVolume(volumeSlider.getValue()));

        if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    state.setFullVolumeArea(new FullVolumeArea());
        } else if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    state.setFullVolumeArea(new FullVolumeArea( (Float) fullVolumeRadiusModel.getValue()));
        } else {
	    state.setFullVolumeArea(new FullVolumeArea(
	        new Vector3f((Float) xExtentSpinner.getValue(), (Float) yExtentSpinner.getValue(),
	        (Float) zExtentSpinner.getValue())));

	    System.out.println("fva " + state.getFullVolumeArea());
        }

	Vector3f origin = new Vector3f((Float) activeAreaXOriginSpinner.getValue(),
	    (Float) activeAreaYOriginSpinner.getValue(),
	    (Float) activeAreaZOriginSpinner.getValue());

        if (activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    state.setActiveArea(new ActiveArea(origin));
        } else if (activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    state.setActiveArea(new ActiveArea(origin,
		(Float) activeAreaFullVolumeRadiusModel.getValue()));
        } else {
	    state.setActiveArea(new ActiveArea(origin,
		new Vector3f((Float) activeAreaXExtentSpinner.getValue(), 
		(Float) activeAreaYExtentSpinner.getValue(),
	        (Float) activeAreaZExtentSpinner.getValue())));
        }

        editor.addToUpdateList(state);
    }

    /**
     * @{inheritDoc}
     */
    public void restore() {
        // Reset the original values to the GUI
	nameTextField.setText(originalName);

	volumeSlider.setValue(originalVolume);

	fullVolumeRadiusSpinner.setEnabled(false);
	xExtentSpinner.setEnabled(false);
	yExtentSpinner.setEnabled(false);
	zExtentSpinner.setEnabled(false);

	if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
            useCellBoundsRadioButton.setSelected(true);
        } else if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
            specifyRadiusRadioButton.setSelected(true);
	    fullVolumeRadiusSpinner.setEnabled(true);
	    fullVolumeRadiusSpinner.setValue(originalFullVolumeArea.bounds.getX());
        } else {
	    fullVolumeRadiusSpinner.setEnabled(false);
	    xExtentSpinner.setValue(originalFullVolumeArea.bounds.getX());
	    yExtentSpinner.setValue(originalFullVolumeArea.bounds.getY());
	    zExtentSpinner.setValue(originalFullVolumeArea.bounds.getZ());
	    xExtentSpinner.setEnabled(true);
	    yExtentSpinner.setEnabled(true);
	    zExtentSpinner.setEnabled(true);
        }

	activeAreaXOriginSpinner.setEnabled(false);
	activeAreaYOriginSpinner.setEnabled(false);
	activeAreaZOriginSpinner.setEnabled(false);
	activeAreaFullVolumeRadiusSpinner.setEnabled(false);
	activeAreaXExtentSpinner.setEnabled(false);
	activeAreaYExtentSpinner.setEnabled(false);
	activeAreaZExtentSpinner.setEnabled(false);

	if (originalActiveArea.activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
            activeAreaUseCellBoundsRadioButton.setSelected(true);
        } else if (originalActiveArea.activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
            activeAreaSpecifyRadiusRadioButton.setSelected(true);
	    activeAreaFullVolumeRadiusSpinner.setValue(originalActiveArea.activeAreaBounds.getX());
	    activeAreaFullVolumeRadiusSpinner.setEnabled(true);
	    activeAreaXOriginSpinner.setEnabled(true);
	    activeAreaYOriginSpinner.setEnabled(true);
	    activeAreaZOriginSpinner.setEnabled(true);
        } else {
            activeAreaSpecifyBoxRadioButton.setSelected(true);
	    activeAreaXExtentSpinner.setValue(originalActiveArea.activeAreaBounds.getX());
	    activeAreaYExtentSpinner.setValue(originalActiveArea.activeAreaBounds.getY());
	    activeAreaZExtentSpinner.setValue(originalActiveArea.activeAreaBounds.getZ());
	    activeAreaXExtentSpinner.setEnabled(true);
	    activeAreaYExtentSpinner.setEnabled(true);
	    activeAreaZExtentSpinner.setEnabled(true);
	    activeAreaXOriginSpinner.setEnabled(true);
	    activeAreaYOriginSpinner.setEnabled(true);
	    activeAreaZOriginSpinner.setEnabled(true);
        }

        showBounds();
	showActiveArea();
    }

    private void showBounds() {
	if (boundsViewerEntity != null) {
            boundsViewerEntity.dispose();
	    boundsViewerEntity = null;
	}

        if (showBoundsCheckBox.isSelected() == false) {
	    return;
	}

	boundsViewerEntity = new BoundsViewerEntity(editor.getCell());

	if (useCellBoundsRadioButton.isSelected()) {
	    boundsViewerEntity.showBounds(editor.getCell().getLocalBounds());
	} else if (specifyRadiusRadioButton.isSelected()) {
	    boundsViewerEntity.showBounds(
		new BoundingSphere((Float) fullVolumeRadiusModel.getValue(),
		new Vector3f()));
	} else {
	    boundsViewerEntity.showBounds(new BoundingBox(new Vector3f(),
		(Float) xExtentModel.getValue(), (Float) yExtentModel.getValue(), 
		(Float) zExtentModel.getValue()));
	}
    }

    private void showActiveArea() {
	if (activeAreaViewerEntity != null) {
            activeAreaViewerEntity.dispose();
	    activeAreaViewerEntity = null;
	}

        if (showActiveAreaCheckBox.isSelected() == false) {
	    return;
	}

	activeAreaViewerEntity = new BoundsViewerEntity(editor.getCell());

	Vector3f origin = new Vector3f((Float) activeAreaXOriginSpinner.getValue(),
	    (Float) activeAreaYOriginSpinner.getValue(),
	    (Float) activeAreaZOriginSpinner.getValue());

	if (activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    activeAreaViewerEntity.showBounds(editor.getCell().getLocalBounds());
	} else if (activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    activeAreaViewerEntity.showBounds(new BoundingSphere(
		(Float) activeAreaFullVolumeRadiusModel.getValue(), origin));
	} else {
	    activeAreaViewerEntity.showBounds(new BoundingBox(origin,
		(Float) activeAreaXExtentModel.getValue(),
		(Float) activeAreaYExtentModel.getValue(), 
		(Float) activeAreaZExtentModel.getValue()));
	}
    }

    private boolean isDirty() {
	if (nameTextField.getText().equals(originalName) == false) {
	    return true;
	}

	if (originalVolume != volumeSlider.getValue()) {
	    return true;
	}

	if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS) == false) {
	        return true;
	    }
	} else if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE) == false) {
		return true;
	    }

	    Float radius = (Float) fullVolumeRadiusModel.getValue();

	    if (radius != originalFullVolumeArea.bounds.getX()) {
	        return true;
	    }
	} else {
       	    if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.BOX) == false) {
		return true;
	    }

	    Float xExtent = (Float) xExtentModel.getValue();

            if (xExtent != originalFullVolumeArea.bounds.getX()) {
	        return true;
	    }

	    Float yExtent = (Float) yExtentModel.getValue();

	    if (yExtent != originalFullVolumeArea.bounds.getY()) {
	        return true;
	    }

	    Float zExtent = (Float) zExtentModel.getValue();

	    if (zExtent != originalFullVolumeArea.bounds.getZ()) {
	        return true;
	    }
	}

	if (activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    if (originalActiveArea.activeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS) == false) {
	        return true;
	    }
	} else if (activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
            if (originalActiveArea.activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE) == false) {
		return true;
	    }

	    Float radius = (Float) activeAreaFullVolumeRadiusModel.getValue();

	    if (radius != originalActiveArea.activeAreaBounds.getX()) {
	        return true;
	    }
	} else {
	    if (originalActiveArea.activeAreaBoundsType.equals(MicrophoneBoundsType.BOX) == false) {
		return true;
	    }

	    Float xExtent = (Float) activeAreaXExtentModel.getValue();

	    if (xExtent != originalActiveArea.activeAreaBounds.getX()) {
	        return true;
	    }

	    Float yExtent = (Float) activeAreaYExtentModel.getValue();

            if (yExtent != originalActiveArea.activeAreaBounds.getY()) {
	        return true;
	    }

	    Float zExtent = (Float) activeAreaZExtentModel.getValue();

            if (zExtent != originalActiveArea.activeAreaBounds.getZ()) {
	        return true;
	    }
	}

	Float xOrigin = (Float) activeAreaXOriginModel.getValue();

	if (xOrigin != originalActiveArea.activeAreaOrigin.getX()) {
	    return true;
	}

	Float yOrigin = (Float) activeAreaYOriginModel.getValue();

	if (yOrigin != originalActiveArea.activeAreaOrigin.getY()) {
	    return true;
	}

	Float zOrigin = (Float) activeAreaZOriginModel.getValue();

	if (zOrigin != originalActiveArea.activeAreaOrigin.getZ()) {
	    return true;
	}

	if (originalShowBounds != showBoundsCheckBox.isSelected()) {
	    return true;
	}

	if (originalShowActiveArea != showActiveAreaCheckBox.isSelected()) {
	    return true;
	}

	return false;
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class NameTextFieldListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }

        private void checkDirty() {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class RadiusChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

                showBounds();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class XExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

                showBounds();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class YExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

                showBounds();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ZExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

                showBounds();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaRadiusChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaXExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaYExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaZExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaXOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaYOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ActiveAreaZOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showActiveArea();
            }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();
        xExtentSpinner = new javax.swing.JSpinner();
        yExtentSpinner = new javax.swing.JSpinner();
        zExtentSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        volumeSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        activeAreaSpecifyRadiusRadioButton = new javax.swing.JRadioButton();
        activeAreaSpecifyBoxRadioButton = new javax.swing.JRadioButton();
        activeAreaFullVolumeRadiusSpinner = new javax.swing.JSpinner();
        activeAreaXExtentSpinner = new javax.swing.JSpinner();
        activeAreaYExtentSpinner = new javax.swing.JSpinner();
        activeAreaZExtentSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        useCellBoundsRadioButton = new javax.swing.JRadioButton();
        specifyRadiusRadioButton = new javax.swing.JRadioButton();
        specifyBoxRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        activeAreaXOriginSpinner = new javax.swing.JSpinner();
        activeAreaYOriginSpinner = new javax.swing.JSpinner();
        activeAreaZOriginSpinner = new javax.swing.JSpinner();
        activeAreaUseCellBoundsRadioButton = new javax.swing.JRadioButton();
        showBoundsCheckBox = new javax.swing.JCheckBox();
        cellBoundsLabel = new javax.swing.JLabel();
        showActiveAreaCheckBox = new javax.swing.JCheckBox();

        fullVolumeRadiusSpinner.setEnabled(false);

        xExtentSpinner.setEnabled(false);

        yExtentSpinner.setEnabled(false);

        zExtentSpinner.setEnabled(false);

        jLabel1.setText("Name:");

        jLabel2.setText("Volume:");

        volumeSlider.setMajorTickSpacing(1);
        volumeSlider.setMaximum(10);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setValue(5);
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        jLabel3.setText("Active Area:");

        buttonGroup2.add(activeAreaSpecifyRadiusRadioButton);
        activeAreaSpecifyRadiusRadioButton.setSelected(true);
        activeAreaSpecifyRadiusRadioButton.setText("Specify Radius:");
        activeAreaSpecifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaSpecifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(activeAreaSpecifyBoxRadioButton);
        activeAreaSpecifyBoxRadioButton.setText("Specify Box:");
        activeAreaSpecifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaSpecifyBoxRadioButtonActionPerformed(evt);
            }
        });

        activeAreaXExtentSpinner.setEnabled(false);

        activeAreaYExtentSpinner.setEnabled(false);

        activeAreaZExtentSpinner.setEnabled(false);

        jLabel4.setText("Bounds:");

        buttonGroup1.add(useCellBoundsRadioButton);
        useCellBoundsRadioButton.setSelected(true);
        useCellBoundsRadioButton.setText("Use Cell Bounds:");
        useCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(specifyRadiusRadioButton);
        specifyRadiusRadioButton.setText("Specify Radius:");
        specifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(specifyBoxRadioButton);
        specifyBoxRadioButton.setText("Specify Box:");
        specifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specifyBoxRadioButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Origin:");

        buttonGroup2.add(activeAreaUseCellBoundsRadioButton);
        activeAreaUseCellBoundsRadioButton.setText("Use Cell Bounds");
        activeAreaUseCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaUseCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        showBoundsCheckBox.setText("Show Bounds");
        showBoundsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBoundsCheckBoxActionPerformed(evt);
            }
        });

        showActiveAreaCheckBox.setText("Show Active Area");
        showActiveAreaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showActiveAreaCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(33, 33, 33)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel4)
                                .add(jLabel2))))
                    .add(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel3)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(useCellBoundsRadioButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(specifyBoxRadioButton)
                            .add(specifyRadiusRadioButton)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 62, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(24, 24, 24)
                                .add(activeAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(showBoundsCheckBox)
                            .add(activeAreaUseCellBoundsRadioButton)
                            .add(activeAreaSpecifyBoxRadioButton)
                            .add(showActiveAreaCheckBox)
                            .add(activeAreaSpecifyRadiusRadioButton))
                        .add(12, 12, 12)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(activeAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(xExtentSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                        .add(activeAreaYOriginSpinner)
                                        .add(activeAreaFullVolumeRadiusSpinner)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(activeAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 51, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(activeAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(activeAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jPanel1Layout.createSequentialGroup()
                                        .add(yExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(zExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(9, 9, 9))
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(25, 25, 25)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(useCellBoundsRadioButton)
                    .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(specifyRadiusRadioButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(yExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(zExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(specifyBoxRadioButton)
                            .add(xExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(3, 3, 3)
                        .add(showBoundsCheckBox))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(113, 113, 113)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(jLabel5)
                            .add(activeAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(activeAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(activeAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(16, 16, 16)
                .add(activeAreaUseCellBoundsRadioButton)
                .add(18, 18, 18)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(activeAreaFullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaSpecifyRadiusRadioButton))
                .add(17, 17, 17)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(activeAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaSpecifyBoxRadioButton)
                    .add(activeAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(showActiveAreaCheckBox))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 422, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void activeAreaSpecifyBoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeAreaSpecifyBoxRadioButtonActionPerformed
	activeAreaBoundsType = MicrophoneBoundsType.BOX;

	activeAreaXOriginSpinner.setEnabled(true);
	activeAreaYOriginSpinner.setEnabled(true);
	activeAreaZOriginSpinner.setEnabled(true);

	activeAreaFullVolumeRadiusSpinner.setEnabled(false);

	activeAreaXExtentSpinner.setEnabled(true);
	activeAreaYExtentSpinner.setEnabled(true);
	activeAreaZExtentSpinner.setEnabled(true);
	
	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showActiveArea();
	}
}//GEN-LAST:event_activeAreaSpecifyBoxRadioButtonActionPerformed

    private void activeAreaSpecifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeAreaSpecifyRadiusRadioButtonActionPerformed
	activeAreaBoundsType = MicrophoneBoundsType.SPHERE;

	activeAreaXOriginSpinner.setEnabled(true);
	activeAreaYOriginSpinner.setEnabled(true);
	activeAreaZOriginSpinner.setEnabled(true);

	activeAreaFullVolumeRadiusSpinner.setEnabled(true);

	activeAreaXExtentSpinner.setEnabled(false);
	activeAreaYExtentSpinner.setEnabled(false);
	activeAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showActiveArea();
	}
}//GEN-LAST:event_activeAreaSpecifyRadiusRadioButtonActionPerformed

    private void activeAreaUseCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeAreaUseCellBoundsRadioButtonActionPerformed
	activeAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

	activeAreaXOriginSpinner.setEnabled(false);
	activeAreaYOriginSpinner.setEnabled(false);
	activeAreaZOriginSpinner.setEnabled(false);

	activeAreaFullVolumeRadiusSpinner.setEnabled(false);

	activeAreaXExtentSpinner.setEnabled(false);
	activeAreaYExtentSpinner.setEnabled(false);
	activeAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showActiveArea();
	}
}//GEN-LAST:event_activeAreaUseCellBoundsRadioButtonActionPerformed

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());
	}
}//GEN-LAST:event_volumeSliderStateChanged

    private void useCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCellBoundsRadioButtonActionPerformed
        fullVolumeAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

	fullVolumeRadiusSpinner.setEnabled(false);
	xExtentSpinner.setEnabled(false);
	yExtentSpinner.setEnabled(false);
	zExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_useCellBoundsRadioButtonActionPerformed

    private void specifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specifyRadiusRadioButtonActionPerformed
        fullVolumeAreaBoundsType = MicrophoneBoundsType.SPHERE;

	fullVolumeRadiusSpinner.setEnabled(true);
	xExtentSpinner.setEnabled(false);
	yExtentSpinner.setEnabled(false);
	zExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_specifyRadiusRadioButtonActionPerformed

    private void specifyBoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_specifyBoxRadioButtonActionPerformed
        fullVolumeAreaBoundsType = MicrophoneBoundsType.BOX;

	fullVolumeRadiusSpinner.setEnabled(false);
	xExtentSpinner.setEnabled(true);
	yExtentSpinner.setEnabled(true);
	zExtentSpinner.setEnabled(true);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_specifyBoxRadioButtonActionPerformed

    private void showBoundsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBoundsCheckBoxActionPerformed
	if (editor == null) {
	    return;
	}

        editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

        showBounds();
    }//GEN-LAST:event_showBoundsCheckBoxActionPerformed

    private void showActiveAreaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showActiveAreaCheckBoxActionPerformed
	if (editor == null) {
	    return;
	}

        editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

        showActiveArea();
    }//GEN-LAST:event_showActiveAreaCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner activeAreaFullVolumeRadiusSpinner;
    private javax.swing.JRadioButton activeAreaSpecifyBoxRadioButton;
    private javax.swing.JRadioButton activeAreaSpecifyRadiusRadioButton;
    private javax.swing.JRadioButton activeAreaUseCellBoundsRadioButton;
    private javax.swing.JSpinner activeAreaXExtentSpinner;
    private javax.swing.JSpinner activeAreaXOriginSpinner;
    private javax.swing.JSpinner activeAreaYExtentSpinner;
    private javax.swing.JSpinner activeAreaYOriginSpinner;
    private javax.swing.JSpinner activeAreaZExtentSpinner;
    private javax.swing.JSpinner activeAreaZOriginSpinner;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel cellBoundsLabel;
    private javax.swing.JSpinner fullVolumeRadiusSpinner;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox showActiveAreaCheckBox;
    private javax.swing.JCheckBox showBoundsCheckBox;
    private javax.swing.JRadioButton specifyBoxRadioButton;
    private javax.swing.JRadioButton specifyRadiusRadioButton;
    private javax.swing.JRadioButton useCellBoundsRadioButton;
    private javax.swing.JSlider volumeSlider;
    private javax.swing.JSpinner xExtentSpinner;
    private javax.swing.JSpinner yExtentSpinner;
    private javax.swing.JSpinner zExtentSpinner;
    // End of variables declaration//GEN-END:variables

}
