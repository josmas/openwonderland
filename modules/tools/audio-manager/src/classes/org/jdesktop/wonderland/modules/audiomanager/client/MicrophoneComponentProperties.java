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
import org.jdesktop.wonderland.modules.audiomanager.common.VolumeConverter;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.TalkArea;
import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.ListenArea;
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
    private int originalVolume = 1;
    private ListenArea originalListenArea = new ListenArea();
    private boolean originalShowBounds = false;
    private TalkArea originalTalkArea = new TalkArea();
    private boolean originalShowTalkArea = false;

    private SpinnerNumberModel listenRadiusModel = null;
    private SpinnerNumberModel listenAreaXExtentModel = null;
    private SpinnerNumberModel listenAreaYExtentModel = null;
    private SpinnerNumberModel listenAreaZExtentModel = null;
    private SpinnerNumberModel listenAreaXOriginModel = null;
    private SpinnerNumberModel listenAreaYOriginModel = null;
    private SpinnerNumberModel listenAreaZOriginModel = null;

    private SpinnerNumberModel talkAreaRadiusModel = null;
    private SpinnerNumberModel talkAreaXExtentModel = null;
    private SpinnerNumberModel talkAreaYExtentModel = null;
    private SpinnerNumberModel talkAreaZExtentModel = null;
    private SpinnerNumberModel talkAreaXOriginModel = null;
    private SpinnerNumberModel talkAreaYOriginModel = null;
    private SpinnerNumberModel talkAreaZOriginModel = null;

    private MicrophoneBoundsType listenAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;
    private MicrophoneBoundsType talkAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

    private BoundsViewerEntity boundsViewerEntity;
    private BoundsViewerEntity talkAreaViewerEntity;

    private VolumeConverter volumeConverter;

    /** Creates new form MicrophoneComponentProperties */
    public MicrophoneComponentProperties() {
        initComponents();

	volumeConverter = new VolumeConverter(volumeSlider.getMaximum());

        // Listen for changes to the text field and spinner
        nameTextField.getDocument().addDocumentListener(
                new NameTextFieldListener());

        // Set the maximum and minimum values for the volume radius spinner
        listenRadiusModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        listenAreaRadiusSpinner.setModel(listenRadiusModel);

        listenAreaXExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        listenAreaXExtentSpinner.setModel(listenAreaXExtentModel);

        listenAreaYExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        listenAreaYExtentSpinner.setModel(listenAreaYExtentModel);

        listenAreaZExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        listenAreaZExtentSpinner.setModel(listenAreaZExtentModel);

        listenRadiusModel.addChangeListener(new RadiusChangeListener());
        listenAreaXExtentModel.addChangeListener(new ListenAreaXExtentChangeListener());
        listenAreaYExtentModel.addChangeListener(new ListenAreaYExtentChangeListener());
        listenAreaZExtentModel.addChangeListener(new ListenAreaZExtentChangeListener());

        listenAreaXOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        listenAreaXOriginSpinner.setModel(listenAreaXOriginModel);

        listenAreaYOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        listenAreaYOriginSpinner.setModel(listenAreaYOriginModel);

        listenAreaZOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        listenAreaZOriginSpinner.setModel(listenAreaZOriginModel);

        listenAreaXOriginModel.addChangeListener(new ListenAreaXOriginChangeListener());
        listenAreaYOriginModel.addChangeListener(new ListenAreaYOriginChangeListener());
        listenAreaZOriginModel.addChangeListener(new ListenAreaZOriginChangeListener());

        // Set the maximum and minimum values for the volume radius spinner
        talkAreaRadiusModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        talkAreaRadiusSpinner.setModel(talkAreaRadiusModel);

        talkAreaXExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        talkAreaXExtentSpinner.setModel(talkAreaXExtentModel);

        talkAreaYExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        talkAreaYExtentSpinner.setModel(talkAreaYExtentModel);

        talkAreaZExtentModel = new SpinnerNumberModel(new Float(1), new Float(0),
            new Float(100), new Float(.1));
        talkAreaZExtentSpinner.setModel(talkAreaZExtentModel);

        talkAreaXOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        talkAreaXOriginSpinner.setModel(talkAreaXOriginModel);

        talkAreaYOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        talkAreaYOriginSpinner.setModel(talkAreaYOriginModel);

        talkAreaZOriginModel = new SpinnerNumberModel(new Float(0), new Float(-100),
            new Float(100), new Float(.1));
        talkAreaZOriginSpinner.setModel(talkAreaZOriginModel);

        talkAreaRadiusModel.addChangeListener(new TalkAreaRadiusChangeListener());
        talkAreaXExtentModel.addChangeListener(new TalkAreaXExtentChangeListener());
        talkAreaYExtentModel.addChangeListener(new TalkAreaYExtentChangeListener());
        talkAreaZExtentModel.addChangeListener(new TalkAreaZExtentChangeListener());
        talkAreaXOriginModel.addChangeListener(new TalkAreaXOriginChangeListener());
        talkAreaYOriginModel.addChangeListener(new TalkAreaYOriginChangeListener());
        talkAreaZOriginModel.addChangeListener(new TalkAreaZOriginChangeListener());
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

	originalVolume = volumeConverter.getVolume((float) state.getVolume());

	originalListenArea = state.getListenArea();

	originalShowBounds = state.getShowBounds();

	originalTalkArea = state.getTalkArea();

	originalShowTalkArea = state.getShowTalkArea();

	if (originalListenArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    listenAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

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
	} else if (originalListenArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    listenAreaBoundsType = MicrophoneBoundsType.SPHERE;
	} else {
	    listenAreaBoundsType = MicrophoneBoundsType.BOX;
        }

	talkAreaBoundsType = originalTalkArea.talkAreaBoundsType;

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

	    showListenAreaCheckBox.setSelected(false);
        }

        if (talkAreaViewerEntity != null) {
            talkAreaViewerEntity.dispose();
            talkAreaViewerEntity = null;
	    showTalkAreaCheckBox.setSelected(false);
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

	state.setVolume(volumeConverter.getVolume(volumeSlider.getValue()));

	Vector3f listenAreaOrigin = new Vector3f((Float) listenAreaXOriginSpinner.getValue(),
	    (Float) listenAreaYOriginSpinner.getValue(),
	    (Float) listenAreaZOriginSpinner.getValue());

        if (listenAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    state.setListenArea(new ListenArea(listenAreaOrigin));
        } else if (listenAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    state.setListenArea(new ListenArea(listenAreaOrigin, (Float) listenRadiusModel.getValue()));
        } else {
	    state.setListenArea(new ListenArea(listenAreaOrigin,
	        new Vector3f((Float) listenAreaXExtentSpinner.getValue(), (Float) listenAreaYExtentSpinner.getValue(),
	        (Float) listenAreaZExtentSpinner.getValue())));
        }

	Vector3f talkAreaOrigin = new Vector3f((Float) talkAreaXOriginSpinner.getValue(),
	    (Float) talkAreaYOriginSpinner.getValue(),
	    (Float) talkAreaZOriginSpinner.getValue());

        if (talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    state.setTalkArea(new TalkArea(talkAreaOrigin));
        } else if (talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    state.setTalkArea(new TalkArea(talkAreaOrigin,
		(Float) talkAreaRadiusModel.getValue()));
        } else {
	    state.setTalkArea(new TalkArea(talkAreaOrigin,
		new Vector3f((Float) talkAreaXExtentSpinner.getValue(),
		(Float) talkAreaYExtentSpinner.getValue(),
	        (Float) talkAreaZExtentSpinner.getValue())));
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

	volumeSlider.setValue(originalVolume);

	listenAreaRadiusSpinner.setEnabled(false);
	listenAreaXExtentSpinner.setEnabled(false);
	listenAreaYExtentSpinner.setEnabled(false);
	listenAreaZExtentSpinner.setEnabled(false);
	listenAreaXOriginSpinner.setEnabled(false);
	listenAreaYOriginSpinner.setEnabled(false);
	listenAreaZOriginSpinner.setEnabled(false);

	Vector3f listenOrigin = originalTalkArea.talkAreaOrigin;

	listenAreaXOriginSpinner.setValue(listenOrigin.getX());
	listenAreaYOriginSpinner.setValue(listenOrigin.getY());
	listenAreaZOriginSpinner.setValue(listenOrigin.getZ());

	if (originalListenArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    listenAreaUseCellBoundsRadioButton.setSelected(true);
            listenAreaUseCellBoundsRadioButton.setSelected(true);
        } else if (originalListenArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
            listenAreaSpecifyRadiusRadioButton.setSelected(true);
	    listenAreaRadiusSpinner.setEnabled(true);
	    listenAreaRadiusSpinner.setValue(originalListenArea.bounds.getX());
        } else {
	    listenAreaSpecifyBoxRadioButton.setSelected(true);
	    listenAreaRadiusSpinner.setEnabled(false);
	    listenAreaXExtentSpinner.setValue(originalListenArea.bounds.getX());
	    listenAreaYExtentSpinner.setValue(originalListenArea.bounds.getY());
	    listenAreaZExtentSpinner.setValue(originalListenArea.bounds.getZ());
	    listenAreaXOriginSpinner.setValue(listenOrigin.getX());
	    listenAreaYOriginSpinner.setValue(listenOrigin.getY());
	    listenAreaZOriginSpinner.setValue(listenOrigin.getZ());
	    listenAreaXExtentSpinner.setEnabled(true);
	    listenAreaYExtentSpinner.setEnabled(true);
	    listenAreaZExtentSpinner.setEnabled(true);
	    listenAreaXOriginSpinner.setEnabled(true);
	    listenAreaYOriginSpinner.setEnabled(true);
	    listenAreaZOriginSpinner.setEnabled(true);
        }

	talkAreaXOriginSpinner.setEnabled(false);
	talkAreaYOriginSpinner.setEnabled(false);
	talkAreaZOriginSpinner.setEnabled(false);
	talkAreaRadiusSpinner.setEnabled(false);
	talkAreaXExtentSpinner.setEnabled(false);
	talkAreaYExtentSpinner.setEnabled(false);
	talkAreaZExtentSpinner.setEnabled(false);

	Vector3f origin = originalTalkArea.talkAreaOrigin;

	talkAreaXOriginSpinner.setValue(origin.getX());
	talkAreaYOriginSpinner.setValue(origin.getY());
	talkAreaZOriginSpinner.setValue(origin.getZ());

	if (originalTalkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
            talkAreaUseCellBoundsRadioButton.setSelected(true);
        } else if (originalTalkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
            talkAreaSpecifyRadiusRadioButton.setSelected(true);
	    talkAreaRadiusSpinner.setValue(originalTalkArea.talkAreaBounds.getX());
	    talkAreaRadiusSpinner.setEnabled(true);
	    talkAreaXOriginSpinner.setEnabled(true);
	    talkAreaYOriginSpinner.setEnabled(true);
	    talkAreaZOriginSpinner.setEnabled(true);
        } else {
            talkAreaSpecifyBoxRadioButton.setSelected(true);
	    talkAreaXExtentSpinner.setValue(originalTalkArea.talkAreaBounds.getX());
	    talkAreaYExtentSpinner.setValue(originalTalkArea.talkAreaBounds.getY());
	    talkAreaZExtentSpinner.setValue(originalTalkArea.talkAreaBounds.getZ());
	    talkAreaXOriginSpinner.setValue(origin.getX());
	    talkAreaYOriginSpinner.setValue(origin.getY());
	    talkAreaZOriginSpinner.setValue(origin.getZ());
	    talkAreaXExtentSpinner.setEnabled(true);
	    talkAreaYExtentSpinner.setEnabled(true);
	    talkAreaZExtentSpinner.setEnabled(true);
	    talkAreaXOriginSpinner.setEnabled(true);
	    talkAreaYOriginSpinner.setEnabled(true);
	    talkAreaZOriginSpinner.setEnabled(true);
        }

        showBounds();
	showTalkArea();
    }

    private void showBounds() {
	if (boundsViewerEntity != null) {
            boundsViewerEntity.dispose();
	    boundsViewerEntity = null;
	}

        if (showListenAreaCheckBox.isSelected() == false) {
	    return;
	}

	boundsViewerEntity = new BoundsViewerEntity(editor.getCell());

	Vector3f origin = new Vector3f((Float) listenAreaXOriginSpinner.getValue(),
	    (Float) listenAreaYOriginSpinner.getValue(),
	    (Float) listenAreaZOriginSpinner.getValue());

	if (listenAreaUseCellBoundsRadioButton.isSelected()) {
	    boundsViewerEntity.showBounds(editor.getCell().getLocalBounds());
	} else if (listenAreaSpecifyRadiusRadioButton.isSelected()) {
	    boundsViewerEntity.showBounds(
		new BoundingSphere((Float) listenRadiusModel.getValue(), origin));
		
	} else {
	    boundsViewerEntity.showBounds(new BoundingBox(origin,
		(Float) listenAreaXExtentModel.getValue(), (Float) listenAreaYExtentModel.getValue(), 
		(Float) listenAreaZExtentModel.getValue()));
	}
    }

    private void showTalkArea() {
	if (talkAreaViewerEntity != null) {
            talkAreaViewerEntity.dispose();
	    talkAreaViewerEntity = null;
	}

        if (showTalkAreaCheckBox.isSelected() == false) {
	    return;
	}

	talkAreaViewerEntity = new BoundsViewerEntity(editor.getCell());

	Vector3f origin = new Vector3f((Float) talkAreaXOriginSpinner.getValue(),
	    (Float) talkAreaYOriginSpinner.getValue(),
	    (Float) talkAreaZOriginSpinner.getValue());

	if (talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    talkAreaViewerEntity.showBounds(editor.getCell().getLocalBounds());
	} else if (talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    talkAreaViewerEntity.showBounds(new BoundingSphere(
		(Float) talkAreaRadiusModel.getValue(), origin));
	} else {
	    talkAreaViewerEntity.showBounds(new BoundingBox(origin,
		(Float) talkAreaXExtentModel.getValue(),
		(Float) talkAreaYExtentModel.getValue(), 
		(Float) talkAreaZExtentModel.getValue()));
	}
    }

    private boolean isDirty() {
	if (nameTextField.getText().equals(originalName) == false) {
	    return true;
	}

	if (originalVolume != volumeSlider.getValue()) {
	    return true;
	}

	if (listenAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    if (originalListenArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS) == false) {
	        return true;
	    }
	} else if (listenAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
	    if (originalListenArea.boundsType.equals(MicrophoneBoundsType.SPHERE) == false) {
		return true;
	    }

	    Float radius = (Float) listenRadiusModel.getValue();

	    if (radius != originalListenArea.bounds.getX()) {
	        return true;
	    }
	} else {
       	    if (originalListenArea.boundsType.equals(MicrophoneBoundsType.BOX) == false) {
		return true;
	    }

	    Float listenAreaXExtent = (Float) listenAreaXExtentModel.getValue();

            if (listenAreaXExtent != originalListenArea.bounds.getX()) {
	        return true;
	    }

	    Float listenAreaYExtent = (Float) listenAreaYExtentModel.getValue();

	    if (listenAreaYExtent != originalListenArea.bounds.getY()) {
	        return true;
	    }

	    Float listenAreaZExtent = (Float) listenAreaZExtentModel.getValue();

	    if (listenAreaZExtent != originalListenArea.bounds.getZ()) {
	        return true;
	    }
	}

	if (talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
	    if (originalTalkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.CELL_BOUNDS) == false) {
	        return true;
	    }
	} else if (talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
            if (originalTalkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.SPHERE) == false) {
		return true;
	    }

	    Float radius = (Float) talkAreaRadiusModel.getValue();

	    if (radius != originalTalkArea.talkAreaBounds.getX()) {
	        return true;
	    }
	} else {
	    if (originalTalkArea.talkAreaBoundsType.equals(MicrophoneBoundsType.BOX) == false) {
		return true;
	    }

	    Float xExtent = (Float) talkAreaXExtentModel.getValue();

	    if (xExtent != originalTalkArea.talkAreaBounds.getX()) {
	        return true;
	    }

	    Float yExtent = (Float) talkAreaYExtentModel.getValue();

            if (yExtent != originalTalkArea.talkAreaBounds.getY()) {
	        return true;
	    }

	    Float zExtent = (Float) talkAreaZExtentModel.getValue();

            if (zExtent != originalTalkArea.talkAreaBounds.getZ()) {
	        return true;
	    }
	}

	Float listenXOrigin = (Float) listenAreaXOriginModel.getValue();

	if (listenXOrigin != originalListenArea.listenAreaOrigin.getX()) {
	    return true;
	}

	Float listenYOrigin = (Float) listenAreaYOriginModel.getValue();

	if (listenYOrigin != originalListenArea.listenAreaOrigin.getY()) {
	    return true;
	}

	Float listenZOrigin = (Float) listenAreaZOriginModel.getValue();

	if (listenZOrigin != originalListenArea.listenAreaOrigin.getZ()) {
	    return true;
	}

	Float xOrigin = (Float) talkAreaXOriginModel.getValue();

	if (xOrigin != originalTalkArea.talkAreaOrigin.getX()) {
	    return true;
	}

	Float yOrigin = (Float) talkAreaYOriginModel.getValue();

	if (yOrigin != originalTalkArea.talkAreaOrigin.getY()) {
	    return true;
	}

	Float zOrigin = (Float) talkAreaZOriginModel.getValue();

	if (zOrigin != originalTalkArea.talkAreaOrigin.getZ()) {
	    return true;
	}

	if (originalShowBounds != showListenAreaCheckBox.isSelected()) {
	    return true;
	}

	if (originalShowTalkArea != showTalkAreaCheckBox.isSelected()) {
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
    class ListenAreaXExtentChangeListener implements ChangeListener {

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
    class ListenAreaYExtentChangeListener implements ChangeListener {

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
    class ListenAreaZExtentChangeListener implements ChangeListener {

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
    class ListenAreaXOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ListenAreaYOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class ListenAreaZOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }
    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaRadiusChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaXExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaYExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaZExtentChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaXOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaYOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
            }
        }
    }

    /**
     * Inner class to listen for changes to the spinner and fire off dirty
     * or clean indications to the cell properties editor
     */
    class TalkAreaZOriginChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

		showTalkArea();
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
        showListenAreaCheckBox = new javax.swing.JCheckBox();
        talkAreaUseCellBoundsRadioButton = new javax.swing.JRadioButton();
        talkAreaZOriginSpinner = new javax.swing.JSpinner();
        talkAreaYOriginSpinner = new javax.swing.JSpinner();
        listenAreaXExtentSpinner = new javax.swing.JSpinner();
        listenAreaYExtentSpinner = new javax.swing.JSpinner();
        listenAreaRadiusSpinner = new javax.swing.JSpinner();
        nameTextField = new javax.swing.JTextField();
        listenAreaZExtentSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        talkAreaSpecifyRadiusRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        showTalkAreaCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        talkAreaZExtentSpinner = new javax.swing.JSpinner();
        talkAreaRadiusSpinner = new javax.swing.JSpinner();
        talkAreaSpecifyBoxRadioButton = new javax.swing.JRadioButton();
        talkAreaYExtentSpinner = new javax.swing.JSpinner();
        talkAreaXExtentSpinner = new javax.swing.JSpinner();
        listenAreaSpecifyRadiusRadioButton = new javax.swing.JRadioButton();
        listenAreaSpecifyBoxRadioButton = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        talkAreaXOriginSpinner = new javax.swing.JSpinner();
        listenAreaUseCellBoundsRadioButton = new javax.swing.JRadioButton();
        volumeSlider = new javax.swing.JSlider();
        jLabel6 = new javax.swing.JLabel();
        listenAreaXOriginSpinner = new javax.swing.JSpinner();
        listenAreaYOriginSpinner = new javax.swing.JSpinner();
        listenAreaZOriginSpinner = new javax.swing.JSpinner();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/audiomanager/client/resources/Bundle"); // NOI18N
        showListenAreaCheckBox.setText(bundle.getString("MicrophoneComponentProperties.showListenAreaCheckBox.text")); // NOI18N
        showListenAreaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showListenAreaCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup2.add(talkAreaUseCellBoundsRadioButton);
        talkAreaUseCellBoundsRadioButton.setText(bundle.getString("MicrophoneComponentProperties.talkAreaUseCellBoundsRadioButton.text")); // NOI18N
        talkAreaUseCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                talkAreaUseCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        listenAreaXExtentSpinner.setEnabled(false);

        listenAreaYExtentSpinner.setEnabled(false);

        listenAreaRadiusSpinner.setEnabled(false);

        listenAreaZExtentSpinner.setEnabled(false);

        jLabel1.setText(bundle.getString("MicrophoneComponentProperties.jLabel1.text")); // NOI18N

        buttonGroup2.add(talkAreaSpecifyRadiusRadioButton);
        talkAreaSpecifyRadiusRadioButton.setSelected(true);
        talkAreaSpecifyRadiusRadioButton.setText(bundle.getString("MicrophoneComponentProperties.talkAreaSpecifyRadiusRadioButton.text")); // NOI18N
        talkAreaSpecifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                talkAreaSpecifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(bundle.getString("MicrophoneComponentProperties.jLabel3.text")); // NOI18N

        jLabel2.setText(bundle.getString("MicrophoneComponentProperties.jLabel2.text")); // NOI18N

        showTalkAreaCheckBox.setText(bundle.getString("MicrophoneComponentProperties.showTalkAreaCheckBox.text")); // NOI18N
        showTalkAreaCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showTalkAreaCheckBoxActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("MicrophoneComponentProperties.jLabel4.text")); // NOI18N

        talkAreaZExtentSpinner.setEnabled(false);

        buttonGroup2.add(talkAreaSpecifyBoxRadioButton);
        talkAreaSpecifyBoxRadioButton.setText(bundle.getString("MicrophoneComponentProperties.talkAreaSpecifyBoxRadioButton.text")); // NOI18N
        talkAreaSpecifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                talkAreaSpecifyBoxRadioButtonActionPerformed(evt);
            }
        });

        talkAreaYExtentSpinner.setEnabled(false);

        talkAreaXExtentSpinner.setEnabled(false);

        buttonGroup1.add(listenAreaSpecifyRadiusRadioButton);
        listenAreaSpecifyRadiusRadioButton.setText(bundle.getString("MicrophoneComponentProperties.listenAreaSpecifyRadiusRadioButton.text")); // NOI18N
        listenAreaSpecifyRadiusRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenAreaSpecifyRadiusRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(listenAreaSpecifyBoxRadioButton);
        listenAreaSpecifyBoxRadioButton.setText(bundle.getString("MicrophoneComponentProperties.listenAreaSpecifyBoxRadioButton.text")); // NOI18N
        listenAreaSpecifyBoxRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenAreaSpecifyBoxRadioButtonActionPerformed(evt);
            }
        });

        jLabel5.setText(bundle.getString("MicrophoneComponentProperties.jLabel5.text")); // NOI18N

        buttonGroup1.add(listenAreaUseCellBoundsRadioButton);
        listenAreaUseCellBoundsRadioButton.setSelected(true);
        listenAreaUseCellBoundsRadioButton.setText(bundle.getString("MicrophoneComponentProperties.listenAreaUseCellBoundsRadioButton.text")); // NOI18N
        listenAreaUseCellBoundsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenAreaUseCellBoundsRadioButtonActionPerformed(evt);
            }
        });

        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPreferredSize(new java.awt.Dimension(200, 38));
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        jLabel6.setText(bundle.getString("MicrophoneComponentProperties.jLabel6.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(showListenAreaCheckBox)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(listenAreaUseCellBoundsRadioButton)
                            .add(1, 1, 1)
                            .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, nameTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(jLabel6)
                            .add(86, 86, 86)
                            .add(listenAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(listenAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 53, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                            .add(listenAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel5)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, talkAreaUseCellBoundsRadioButton)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, showTalkAreaCheckBox)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(listenAreaSpecifyRadiusRadioButton)
                                        .add(listenAreaSpecifyBoxRadioButton))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(listenAreaRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                                        .add(listenAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))
                                    .add(12, 12, 12)
                                    .add(listenAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(listenAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(talkAreaSpecifyBoxRadioButton)
                                        .add(layout.createSequentialGroup()
                                            .add(talkAreaSpecifyRadiusRadioButton)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, talkAreaXExtentSpinner)
                                                .add(org.jdesktop.layout.GroupLayout.LEADING, talkAreaRadiusSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                                                .add(talkAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE))))
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                            .add(talkAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(talkAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(layout.createSequentialGroup()
                                            .add(talkAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                            .add(talkAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))))
                        .add(60, 60, 60)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {listenAreaXExtentSpinner, talkAreaRadiusSpinner, talkAreaXExtentSpinner, talkAreaXOriginSpinner, talkAreaYExtentSpinner, talkAreaYOriginSpinner, talkAreaZExtentSpinner, talkAreaZOriginSpinner}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(34, 34, 34)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(jLabel6)
                    .add(listenAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(listenAreaUseCellBoundsRadioButton)
                    .add(cellBoundsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenAreaSpecifyRadiusRadioButton)
                    .add(listenAreaRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(listenAreaSpecifyBoxRadioButton)
                    .add(listenAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(listenAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(10, 10, 10)
                .add(showListenAreaCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jLabel3)
                    .add(jLabel5)
                    .add(talkAreaXOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(talkAreaYOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(talkAreaZOriginSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(talkAreaUseCellBoundsRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(talkAreaSpecifyRadiusRadioButton)
                    .add(talkAreaRadiusSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(talkAreaSpecifyBoxRadioButton)
                    .add(talkAreaXExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(talkAreaYExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(talkAreaZExtentSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(showTalkAreaCheckBox)
                .addContainerGap(125, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void talkAreaSpecifyBoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_talkAreaSpecifyBoxRadioButtonActionPerformed
	if (talkAreaSpecifyBoxRadioButton.isSelected() == false) {
	    return;
	}

	talkAreaBoundsType = MicrophoneBoundsType.BOX;

	talkAreaXOriginSpinner.setEnabled(true);
	talkAreaYOriginSpinner.setEnabled(true);
	talkAreaZOriginSpinner.setEnabled(true);

	talkAreaRadiusSpinner.setEnabled(false);

	talkAreaXExtentSpinner.setEnabled(true);
	talkAreaYExtentSpinner.setEnabled(true);
	talkAreaZExtentSpinner.setEnabled(true);
	
	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showTalkArea();
	}
}//GEN-LAST:event_talkAreaSpecifyBoxRadioButtonActionPerformed

    private void talkAreaSpecifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_talkAreaSpecifyRadiusRadioButtonActionPerformed
	if (talkAreaSpecifyRadiusRadioButton.isSelected() == false) {
	    return;
	}

	talkAreaBoundsType = MicrophoneBoundsType.SPHERE;

	talkAreaXOriginSpinner.setEnabled(true);
	talkAreaYOriginSpinner.setEnabled(true);
	talkAreaZOriginSpinner.setEnabled(true);

	talkAreaRadiusSpinner.setEnabled(true);

	talkAreaXExtentSpinner.setEnabled(false);
	talkAreaYExtentSpinner.setEnabled(false);
	talkAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showTalkArea();
	}
}//GEN-LAST:event_talkAreaSpecifyRadiusRadioButtonActionPerformed

    private void talkAreaUseCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_talkAreaUseCellBoundsRadioButtonActionPerformed
	if (talkAreaUseCellBoundsRadioButton.isSelected() == false) {
	    return;
	}

	talkAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

	talkAreaXOriginSpinner.setEnabled(false);
	talkAreaYOriginSpinner.setEnabled(false);
	talkAreaZOriginSpinner.setEnabled(false);

	talkAreaRadiusSpinner.setEnabled(false);

	talkAreaXExtentSpinner.setEnabled(false);
	talkAreaYExtentSpinner.setEnabled(false);
	talkAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showTalkArea();
	}
}//GEN-LAST:event_talkAreaUseCellBoundsRadioButtonActionPerformed

    private void listenAreaUseCellBoundsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenAreaUseCellBoundsRadioButtonActionPerformed
	if (listenAreaUseCellBoundsRadioButton.isSelected() == false) {
	    return;
	}

        listenAreaBoundsType = MicrophoneBoundsType.CELL_BOUNDS;

	listenAreaXOriginSpinner.setEnabled(false);
	listenAreaYOriginSpinner.setEnabled(false);
	listenAreaZOriginSpinner.setEnabled(false);

	listenAreaRadiusSpinner.setEnabled(false);
	listenAreaXExtentSpinner.setEnabled(false);
	listenAreaYExtentSpinner.setEnabled(false);
	listenAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_listenAreaUseCellBoundsRadioButtonActionPerformed

    private void listenAreaSpecifyRadiusRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenAreaSpecifyRadiusRadioButtonActionPerformed
	if (listenAreaSpecifyRadiusRadioButton.isSelected() == false) {
	    return;
	}

        listenAreaBoundsType = MicrophoneBoundsType.SPHERE;

	listenAreaXOriginSpinner.setEnabled(true);
	listenAreaYOriginSpinner.setEnabled(true);
	listenAreaZOriginSpinner.setEnabled(true);

	listenAreaRadiusSpinner.setEnabled(true);
	listenAreaXExtentSpinner.setEnabled(false);
	listenAreaYExtentSpinner.setEnabled(false);
	listenAreaZExtentSpinner.setEnabled(false);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_listenAreaSpecifyRadiusRadioButtonActionPerformed

    private void listenAreaSpecifyBoxRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenAreaSpecifyBoxRadioButtonActionPerformed
	if (listenAreaSpecifyBoxRadioButton.isSelected() == false) {
	    return;
	}

        listenAreaBoundsType = MicrophoneBoundsType.BOX;

	listenAreaXOriginSpinner.setEnabled(true);
	listenAreaYOriginSpinner.setEnabled(true);
	listenAreaZOriginSpinner.setEnabled(true);

	listenAreaRadiusSpinner.setEnabled(false);
	listenAreaXExtentSpinner.setEnabled(true);
	listenAreaYExtentSpinner.setEnabled(true);
	listenAreaZExtentSpinner.setEnabled(true);

	if (editor != null) {
	    editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

	    showBounds();
	}
    }//GEN-LAST:event_listenAreaSpecifyBoxRadioButtonActionPerformed

    private void showListenAreaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showListenAreaCheckBoxActionPerformed
	if (editor == null) {
	    return;
	}

        editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

        showBounds();
    }//GEN-LAST:event_showListenAreaCheckBoxActionPerformed

    private void showTalkAreaCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showTalkAreaCheckBoxActionPerformed
	if (editor == null) {
	    return;
	}

        editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());

        showTalkArea();
    }//GEN-LAST:event_showTalkAreaCheckBoxActionPerformed

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
	if (editor == null) {
	    return;
	}

	editor.setPanelDirty(MicrophoneComponentProperties.class, isDirty());
    }//GEN-LAST:event_volumeSliderStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel cellBoundsLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner listenAreaRadiusSpinner;
    private javax.swing.JRadioButton listenAreaSpecifyBoxRadioButton;
    private javax.swing.JRadioButton listenAreaSpecifyRadiusRadioButton;
    private javax.swing.JRadioButton listenAreaUseCellBoundsRadioButton;
    private javax.swing.JSpinner listenAreaXExtentSpinner;
    private javax.swing.JSpinner listenAreaXOriginSpinner;
    private javax.swing.JSpinner listenAreaYExtentSpinner;
    private javax.swing.JSpinner listenAreaYOriginSpinner;
    private javax.swing.JSpinner listenAreaZExtentSpinner;
    private javax.swing.JSpinner listenAreaZOriginSpinner;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JCheckBox showListenAreaCheckBox;
    private javax.swing.JCheckBox showTalkAreaCheckBox;
    private javax.swing.JSpinner talkAreaRadiusSpinner;
    private javax.swing.JRadioButton talkAreaSpecifyBoxRadioButton;
    private javax.swing.JRadioButton talkAreaSpecifyRadiusRadioButton;
    private javax.swing.JRadioButton talkAreaUseCellBoundsRadioButton;
    private javax.swing.JSpinner talkAreaXExtentSpinner;
    private javax.swing.JSpinner talkAreaXOriginSpinner;
    private javax.swing.JSpinner talkAreaYExtentSpinner;
    private javax.swing.JSpinner talkAreaYOriginSpinner;
    private javax.swing.JSpinner talkAreaZExtentSpinner;
    private javax.swing.JSpinner talkAreaZOriginSpinner;
    private javax.swing.JSlider volumeSlider;
    // End of variables declaration//GEN-END:variables

}
