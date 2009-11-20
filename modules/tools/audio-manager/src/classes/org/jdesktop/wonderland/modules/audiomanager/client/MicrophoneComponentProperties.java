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

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.text.MessageFormat;
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
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.ActiveArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.FullVolumeArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.MicrophoneBoundsType;

/**
 *
 * @author jp
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(MicrophoneComponentServerState.class)
public class MicrophoneComponentProperties extends javax.swing.JPanel 
	implements PropertiesFactorySPI {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle");

    private CellPropertiesEditor editor = null;

    private String originalName = null;
    private float originalVolume = 1;
    private FullVolumeArea originalFullVolumeArea = new FullVolumeArea();
    private boolean originalShowBounds = false;
    private ActiveArea originalActiveArea = new ActiveArea();
    private boolean originalShowActiveArea = false;

    private SpinnerNumberModel volumeModel = null;
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

        volumeModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(10), new Float(.05));
        micVolumeSpinner.setModel(volumeModel);

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
        return BUNDLE.getString("Microphone");
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

	originalVolume = (float) state.getVolume();

	originalFullVolumeArea = state.getFullVolumeArea();

	originalShowBounds = state.getShowBounds();

	originalActiveArea = state.getActiveArea();

	originalShowActiveArea = state.getShowActiveArea();

	if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    fullVolumeAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

            BoundingVolume bounds = editor.getCell().getLocalBounds();

            if (bounds instanceof BoundingSphere) {
                float radius = ((BoundingSphere) bounds).getRadius();
                String text = BUNDLE.getString("Sphere_With_Radius");
                text = MessageFormat.format(text, (Math.round(radius * 10) / 10f));
                cellBoundsLabel.setText(text);
            } else {
                Vector3f extent = new Vector3f();
                extent = ((BoundingBox) bounds).getExtent(extent);

                float x = Math.round(extent.getX() * 10) / 10f;
                float y = Math.round(extent.getY() * 10) / 10f;
                float z = Math.round(extent.getZ() * 10) / 10f;

                String text = BUNDLE.getString("BOX");
                text = MessageFormat.format(text, x, y, z);
                cellBoundsLabel.setText(text);
            }
	} else if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    fullVolumeAreaBoundsType = MicrophoneBoundsType.SPHERE;
	} else {
	    fullVolumeAreaBoundsType = MicrophoneBoundsType.BOX;
        }

	activeAreaBoundsType = originalActiveArea.activeAreaBoundsType;

	restore();
    }

    /**
     * @{inheritDoc}
     */
    public void close() {
	hideBounds();
    }

    private void hideBounds() {
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

	System.out.println("Name text field " + nameTextField.getText());

        state.setName(nameTextField.getText());

	state.setVolume((Float) micVolumeSpinner.getValue());

        if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    state.setFullVolumeArea(new FullVolumeArea());
        } else if (fullVolumeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    state.setFullVolumeArea(new FullVolumeArea( (Float) fullVolumeRadiusModel.getValue()));
        } else {
	    state.setFullVolumeArea(new FullVolumeArea(
	        new Vector3f((Float) xExtentSpinner.getValue(), (Float) yExtentSpinner.getValue(),
	        (Float) zExtentSpinner.getValue())));
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

	hideBounds();

        editor.addToUpdateList(state);
    }

    /**
     * @{inheritDoc}
     */
    public void restore() {
        // Reset the original values to the GUI

	nameTextField.setText(originalName);

	micVolumeSpinner.setValue(originalVolume);

	fullVolumeRadiusSpinner.setEnabled(false);
	xExtentSpinner.setEnabled(false);
	yExtentSpinner.setEnabled(false);
	zExtentSpinner.setEnabled(false);

	if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    useCellBoundsRadioButton.setSelected(true);
            useCellBoundsRadioButton.setSelected(true);
        } else if (originalFullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
            specifyRadiusRadioButton.setSelected(true);
	    fullVolumeRadiusSpinner.setEnabled(true);
	    fullVolumeRadiusSpinner.setValue(originalFullVolumeArea.bounds.getX());
        } else {
	    specifyBoxRadioButton.setSelected(true);
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

	Vector3f origin = originalActiveArea.activeAreaOrigin;

	activeAreaXOriginSpinner.setValue(origin.getX());
	activeAreaYOriginSpinner.setValue(origin.getY());
	activeAreaZOriginSpinner.setValue(origin.getZ());

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

	if (originalVolume != (Float) micVolumeSpinner.getValue()) {
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
        jSpinner1 = new javax.swing.JSpinner();
        cellBoundsLabel = new javax.swing.JLabel();
        showBoundsCheckBox = new javax.swing.JCheckBox();
        activeAreaUseCellBoundsRadioButton = new javax.swing.JRadioButton();
        activeAreaZOriginSpinner = new javax.swing.JSpinner();
        activeAreaYOriginSpinner = new javax.swing.JSpinner();
        xExtentSpinner = new javax.swing.JSpinner();
        yExtentSpinner = new javax.swing.JSpinner();
        fullVolumeRadiusSpinner = new javax.swing.JSpinner();
        nameTextField = new javax.swing.JTextField();
        zExtentSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        activeAreaSpecifyRadiusRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        showActiveAreaCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        activeAreaZExtentSpinner = new javax.swing.JSpinner();
        activeAreaFullVolumeRadiusSpinner = new javax.swing.JSpinner();
        activeAreaSpecifyBoxRadioButton = new javax.swing.JRadioButton();
        activeAreaYExtentSpinner = new javax.swing.JSpinner();
        activeAreaXExtentSpinner = new javax.swing.JSpinner();
        specifyRadiusRadioButton = new javax.swing.JRadioButton();
        specifyBoxRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        activeAreaXOriginSpinner = new javax.swing.JSpinner();
        useCellBoundsRadioButton = new javax.swing.JRadioButton();
        micVolumeSpinner = new javax.swing.JSpinner();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        showBoundsCheckBox.setText(bundle.getString("MicrophoneComponentProperties.showBoundsCheckBox.text")); // NOI18N
        showBoundsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBoundsCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup2.add(activeAreaUseCellBoundsRadioButton);
        activeAreaUseCellBoundsRadioButton.setText(bundle.getString("MicrophoneComponentProperties.activeAreaUseCellBoundsRadioButton.text")); // NOI18N
        activeAreaUseCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaUseCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        xExtentSpinner.setEnabled(false);

        yExtentSpinner.setEnabled(false);

        fullVolumeRadiusSpinner.setEnabled(false);

        zExtentSpinner.setEnabled(false);

        jLabel1.setText(bundle.getString("MicrophoneComponentProperties.jLabel1.text")); // NOI18N

        buttonGroup2.add(activeAreaSpecifyRadiusRadioButton);
        activeAreaSpecifyRadiusRadioButton.setSelected(true);
        activeAreaSpecifyRadiusRadioButton.setText(bundle.getString("MicrophoneComponentProperties.activeAreaSpecifyRadiusRadioButton.text")); // NOI18N
        activeAreaSpecifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaSpecifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle.getString("MicrophoneComponentProperties.jLabel3.text")); // NOI18N

        jLabel2.setText(bundle.getString("MicrophoneComponentProperties.jLabel2.text")); // NOI18N

        showActiveAreaCheckBox.setText(bundle.getString("MicrophoneComponentProperties.showActiveAreaCheckBox.text")); // NOI18N
        showActiveAreaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showActiveAreaCheckBoxActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("MicrophoneComponentProperties.jLabel4.text")); // NOI18N

        activeAreaZExtentSpinner.setEnabled(false);

        buttonGroup2.add(activeAreaSpecifyBoxRadioButton);
        activeAreaSpecifyBoxRadioButton.setText(bundle.getString("MicrophoneComponentProperties.activeAreaSpecifyBoxRadioButton.text")); // NOI18N
        activeAreaSpecifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeAreaSpecifyBoxRadioButtonActionPerformed(evt);
            }
        });

        activeAreaYExtentSpinner.setEnabled(false);

        activeAreaXExtentSpinner.setEnabled(false);

        buttonGroup1.add(specifyRadiusRadioButton);
        specifyRadiusRadioButton.setText(bundle.getString("MicrophoneComponentProperties.specifyRadiusRadioButton.text")); // NOI18N
        specifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(specifyBoxRadioButton);
        specifyBoxRadioButton.setText(bundle.getString("MicrophoneComponentProperties.specifyBoxRadioButton.text")); // NOI18N
        specifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                specifyBoxRadioButtonActionPerformed(evt);
            }
        });

        jLabel5.setText(bundle.getString("MicrophoneComponentProperties.jLabel5.text")); // NOI18N

        buttonGroup1.add(useCellBoundsRadioButton);
        useCellBoundsRadioButton.setSelected(true);
        useCellBoundsRadioButton.setText(bundle.getString("MicrophoneComponentProperties.useCellBoundsRadioButton.text")); // NOI18N
        useCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        micVolumeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                micVolumeSpinnerStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(124, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(showBoundsCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(useCellBoundsRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(specifyRadiusRadioButton)
                                    .add(specifyBoxRadioButton))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, activeAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                                            .add(xExtentSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(activeAreaYOriginSpinner)
                                            .add(yExtentSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(zExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(activeAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(micVolumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(activeAreaUseCellBoundsRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(activeAreaSpecifyBoxRadioButton)
                                    .add(layout.createSequentialGroup()
                                        .add(activeAreaSpecifyRadiusRadioButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, activeAreaXExtentSpinner)
                                            .add(org.jdesktop.layout.GroupLayout.LEADING, activeAreaFullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(activeAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(activeAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(showActiveAreaCheckBox))
                        .add(51, 51, 51))))
        );

        layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3, jLabel4}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {activeAreaFullVolumeRadiusSpinner, activeAreaXExtentSpinner, activeAreaXOriginSpinner, activeAreaYExtentSpinner, activeAreaYOriginSpinner, activeAreaZExtentSpinner, activeAreaZOriginSpinner, fullVolumeRadiusSpinner, xExtentSpinner, yExtentSpinner, zExtentSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(micVolumeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(useCellBoundsRadioButton)
                            .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(specifyRadiusRadioButton)
                            .add(fullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(specifyBoxRadioButton)
                            .add(xExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(yExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(zExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showBoundsCheckBox)))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabel5)
                    .add(activeAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(activeAreaUseCellBoundsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(activeAreaSpecifyRadiusRadioButton)
                    .add(activeAreaFullVolumeRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(activeAreaSpecifyBoxRadioButton)
                    .add(activeAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(activeAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showActiveAreaCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void activeAreaSpecifyBoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeAreaSpecifyBoxRadioButtonActionPerformed
	if (activeAreaSpecifyBoxRadioButton.isSelected() == false) {
	    return;
	}

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
	if (activeAreaSpecifyRadiusRadioButton.isSelected() == false) {
	    return;
	}

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
	if (activeAreaUseCellBoundsRadioButton.isSelected() == false) {
	    return;
	}

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

    private void useCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useCellBoundsRadioButtonActionPerformed
	if (useCellBoundsRadioButton.isSelected() == false) {
	    return;
	}

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
	if (specifyRadiusRadioButton.isSelected() == false) {
	    return;
	}

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
	if (specifyBoxRadioButton.isSelected() == false) {
	    return;
	}

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

    private void micVolumeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_micVolumeSpinnerStateChanged
	if (editor == null) {
	    return;
	}

        editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());
    }//GEN-LAST:event_micVolumeSpinnerStateChanged

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
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner micVolumeSpinner;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox showActiveAreaCheckBox;
    private javax.swing.JCheckBox showBoundsCheckBox;
    private javax.swing.JRadioButton specifyBoxRadioButton;
    private javax.swing.JRadioButton specifyRadiusRadioButton;
    private javax.swing.JRadioButton useCellBoundsRadioButton;
    private javax.swing.JSpinner xExtentSpinner;
    private javax.swing.JSpinner yExtentSpinner;
    private javax.swing.JSpinner zExtentSpinner;
    // End of variables declaration//GEN-END:variables

}
