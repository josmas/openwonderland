/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.portal.client;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.annotation.PropertiesFactory;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.content.ContentBrowserManager;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI;
import org.jdesktop.wonderland.client.content.spi.ContentBrowserSPI.ContentBrowserListener;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;
import org.jdesktop.wonderland.client.utils.AudioResource;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistry;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistry.PlacemarkType;
import org.jdesktop.wonderland.modules.placemarks.api.client.PlacemarkRegistryFactory;
import org.jdesktop.wonderland.modules.placemarks.api.common.Placemark;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState.AudioSourceType;
import org.jdesktop.wonderland.modules.portal.common.VolumeConverter;

/**
 * A property sheet for the Portal component, allowing users to enter the
 * destination URL, location, and look direction.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
@PropertiesFactory(PortalComponentServerState.class)
public class PortalComponentProperties extends JPanel
        implements PropertiesFactorySPI {

    // The I18N resource bundle
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/portal/client/resources/Bundle");
    
    private static final Logger LOGGER =
	Logger.getLogger(PortalComponentProperties.class.getName());

    // The main editor object for the Cell Editor
    private CellPropertiesEditor editor = null;

    private SpinnerNumberModel xSpinnerModel;
    private SpinnerNumberModel ySpinnerModel;
    private SpinnerNumberModel zSpinnerModel;

    private SpinnerNumberModel lookDirectionSpinnerModel;

    // The original values for all of the fields. We use the convention that
    // if empty, an empty string ("") is used, rather than null.
    private String origServerURL = "";

    private float origX;
    private float origY;
    private float origZ;
    private float origLookDirection;

    private AudioSourceType origAudioSourceType = AudioSourceType.FILE;
    private String origAudioSource = "";
    private boolean origUploadFile;
    private float origVolume = 1F;

    private AudioSourceType audioSourceType = AudioSourceType.FILE;

    private String lastFileAudioSource;
    private String lastContentRepositoryAudioSource;
    private String lastURLAudioSource;

    private VolumeConverter volumeConverter;

    private AudioCacheHandler audioCacheHandler;

    private static String defaultAudioSource;

    /** Creates new form PortalComponentProperties */
    public PortalComponentProperties() {
	this(false);
    }

    public PortalComponentProperties(boolean cacheResources) {
        // Initialize the GUI
        initComponents();

	audioCacheHandler = new AudioCacheHandler();

	try {
	    audioCacheHandler.initialize();

	    if (cacheResources) {
		cacheResource("resources/whatever.au");
		cacheResource("resources/teleport1.au");
		cacheResource("resources/Transporter_Passby.au");
		cacheResource("resources/teleport.au");
		    cacheResource("resources/weapAppear.au");
		defaultAudioSource = cacheResource("resources/disappear.au");

		System.out.println("defaultAudio SOurce " + defaultAudioSource);
	    }
	} catch (AudioCacheHandlerException e) {
	    errorMessage("Cache Resources", e.getMessage());
	}

	audioSourceTextField.setText(defaultAudioSource);

	Float value = new Float(0);
	Float min = null; //new Float(-Float.MIN_VALUE);
	Float max = null; //new Float(Float.MAX_VALUE);
	Float step = new Float(.1);

	xSpinnerModel = new SpinnerNumberModel(value, min, max, step);
	xSpinner.setModel(xSpinnerModel);

	value = new Float(0);
	//min = new Float(-Float.MIN_VALUE);
	//max = new Float(Float.MAX_VALUE);
	step = new Float(.1);

	ySpinnerModel = new SpinnerNumberModel(value, min, max, step);
	ySpinner.setModel(ySpinnerModel);

	value = new Float(0);
	//min = new Float(-Float.MIN_VALUE);
	//max = new Float(Float.MAX_VALUE);
	step = new Float(.1);

	zSpinnerModel = new SpinnerNumberModel(value, min, max, step);
	zSpinner.setModel(zSpinnerModel);

	value = new Float(0);
	min = new Float(0);
	max = new Float(360);
	step = new Float(1);
	
	lookDirectionSpinnerModel = new SpinnerNumberModel(value, min, max, step);
	lookDirectionSpinner.setModel(lookDirectionSpinnerModel);

        // Listen for changes to the text fields
        TextFieldListener listener = new TextFieldListener();
        urlTF.getDocument().addDocumentListener(listener);

	audioSourceTextField.getDocument().addDocumentListener(listener);

        volumeConverter = new VolumeConverter(volumeSlider.getMinimum(),
            volumeSlider.getMaximum());

	
        // set renderer for placemarks
        placemarkCB.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
            {
                Placemark placemark = (Placemark) value;

                String name = placemark == null ? "" : placemark.getName();

                return super.getListCellRendererComponent(list, name,
                        index, isSelected, cellHasFocus);
            }
        });
    }

    public static String getDefaultAudioSource() {
	return defaultAudioSource;
    }

    private String cacheResource(String resource) throws AudioCacheHandlerException {
	return audioCacheHandler.cacheURL(PortalCellFactory.class.getResource(resource));
    }

    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Portal");
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
        // Fetch the current state from the Cell. If none exist, then just
        // return.
        CellServerState cellServerState = editor.getCellServerState();
        PortalComponentServerState state = (PortalComponentServerState)
                cellServerState.getComponentServerState(
                PortalComponentServerState.class);
        if (state == null) {
            return;
        }

        // Otherwise, update the values of the text fields and store away the
        // original values. We use the convention that an empty entry is
        // represented by an empty string ("") rather than null.

        // Fetch the destination URL from the server state. If the original
        // state is null, then convert it into an empty string and update the
        // text field.
        origServerURL = state.getServerURL();
        if (origServerURL == null) {
            origServerURL = "";
        }

	if (origServerURL.length() == 0) {
	    origServerURL = LoginManager.getPrimary().getServerURL();
	}

        urlTF.setText(origServerURL);

        // Fetch the destination location from the server state. If the value
        // is null, then set the original values and text fields to empty
        // strings.
        Vector3f origin = state.getLocation();
        if (origin != null) {
            origX = origin.x;
            origY = origin.y;
            origZ = origin.z;
        } else {
            origX = 0;
            origY = 0;
            origZ = 0;
        }

	xSpinnerModel.setValue(origX);
	ySpinnerModel.setValue(origY);
	zSpinnerModel.setValue(origZ);

	System.out.println("origX " + origX + " " + (Float) xSpinnerModel.getValue());

        // Fetc the destination look direction from the server state. If the
        // value is null, then set the original value and text field to an
        // empty string.
        Quaternion lookAt = state.getLook();
        if (lookAt != null) {
            float lookDirection = (float) Math.toDegrees(lookAt.toAngleAxis(new Vector3f()));
            origLookDirection = lookDirection;
        } else {
            origLookDirection = 0;
        }
        lookDirectionSpinnerModel.setValue(origLookDirection);

	origUploadFile = state.getUploadFile();

	origVolume = state.getVolume();

	volumeSlider.setValue(volumeConverter.getVolume(state.getVolume()));

	if (state.getAudioSourceType() != null) {
	    origAudioSourceType = state.getAudioSourceType();
	} else {
	    origAudioSourceType = AudioSourceType.FILE;
	}

	origAudioSource = state.getAudioSource();

	if (origAudioSource == null) {
	    origAudioSource = "";
	}

	audioSourceTextField.setText(origAudioSource);

	switch (origAudioSourceType) {
	case FILE:
	    lastFileAudioSource = origAudioSource;
	    fileRadioButton.setSelected(true);
	    browseButton.setEnabled(true);
	    uploadFileCheckBox.setEnabled(true);
	    break;
	
	case CONTENT_REPOSITORY:
	    lastContentRepositoryAudioSource = origAudioSource;
	    contentRepositoryRadioButton.setSelected(true);
	    uploadFileCheckBox.setEnabled(false);
	    browseButton.setEnabled(true);
	    break;

	case URL:
	    lastURLAudioSource = origAudioSource;
	    URLRadioButton.setSelected(true);
	    uploadFileCheckBox.setEnabled(false);
	    browseButton.setEnabled(false);
	    break;
	}

	enablePreviewButton();
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // Do nothing
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Figure out whether there already exists a server state for the
        // component. If not, then create one.
        CellServerState cellServerState = editor.getCellServerState();
        PortalComponentServerState state = 
                (PortalComponentServerState) cellServerState.getComponentServerState(
                PortalComponentServerState.class);
        if (state == null) {
            //state = new PortalComponentServerState();
	    return;
        }

        // Set the values in the server state from the text fields. If the text
        // fields are empty, they will return an empty string (""), this is
        // converted to null to set in the server state.

        // Fetch the destination URL from the text field, and convert an empty
        // string into a null.
        String serverURL = urlTF.getText().trim();
        if (serverURL.length() == 0) {
            serverURL = null;
        }
        state.setServerURL(serverURL);

        // Set the location on the server state
        state.setLocation(new Vector3f((Float) xSpinnerModel.getValue(),
	    (Float) ySpinnerModel.getValue(), (Float) zSpinnerModel.getValue()));

        // Set the destination look direction from the text field. If the text
        // field is empty, then set the server state as a zero rotation.
        Quaternion look = new Quaternion();
        float lookDirection = (Float) lookDirectionSpinnerModel.getValue();
        Vector3f axis = new Vector3f(0.0f, 1.0f, 0.0f);
        float angle = (float) Math.toRadians(lookDirection);
	look.fromAngleAxis((float) angle, axis);
        state.setLook(look);

	state.setAudioSourceType(audioSourceType);

	String audioSource = audioSourceTextField.getText();

	state.setAudioSource(audioSource);

	state.setUploadFile(uploadFileCheckBox.isSelected());

	state.setVolume(volumeConverter.getVolume(volumeSlider.getValue()));

	URL url;

        String cacheFilePath;

	switch (audioSourceType) {
	case FILE:
	    lastFileAudioSource = audioSource;

	    try {
	        cacheFilePath = audioCacheHandler.cacheFile(audioSource);
	    } catch (AudioCacheHandlerException e) {
		break;
	    }

	    if (uploadFileCheckBox.isSelected()) {
	        try {
                    audioCacheHandler.uploadFileAudioSource(audioSource);
	        } catch (AudioCacheHandlerException e) {
	        }
	    }

	    state.setCachedAudioSource(cacheFilePath);
            lastContentRepositoryAudioSource = audioSource;
            break;

	case CONTENT_REPOSITORY:
	    try {
		cacheFilePath = 
		    audioCacheHandler.cacheContent(urlTF.getText().trim(), audioSource);
	    } catch (AudioCacheHandlerException e) {
		break;
	    }

	    state.setCachedAudioSource(cacheFilePath);
	    lastContentRepositoryAudioSource = audioSource;
            break;

        case URL:
	    try {
                cacheFilePath = audioCacheHandler.cacheURL(new URL(audioSource));
	    } catch (Exception e) {
		errorMessage("Cache URL", "Unable to cache URL: " + e.getMessage());
		break;
	    }

	    state.setCachedAudioSource(cacheFilePath);
            lastURLAudioSource = audioSource;
            break;
        }

        editor.addToUpdateList(state);
    }

    private void error(String title, String msg) throws IOException {
	errorMessage(title, msg);
	throw new IOException(msg);
    }

    private void errorMessage(final String title, final String msg) {
	final javax.swing.JPanel panel = this;

	java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
		System.out.println(msg);
		javax.swing.JOptionPane.showMessageDialog(
            	    panel, msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
	    }
	});
    }

    private boolean inRestore;

    /**
     * @inheritDoc()
     */
    public void restore() {
	inRestore = true;
        // Restore from the originally stored values.
        urlTF.setText(origServerURL);
	xSpinnerModel.setValue(origX);
	ySpinnerModel.setValue(origY);
	zSpinnerModel.setValue(origZ);
	lookDirectionSpinnerModel.setValue(origLookDirection);

	switch (origAudioSourceType) {
	case FILE:
	    fileRadioButton.setSelected(true);
	    browseButton.setEnabled(true);
	    break;
	
	case CONTENT_REPOSITORY:
	    contentRepositoryRadioButton.setSelected(true);
	    browseButton.setEnabled(true);
	    break;

	case URL:
	    URLRadioButton.setSelected(true);
	    browseButton.setEnabled(false);
	    break;
	}

	audioSourceTextField.setText(origAudioSource);
	enablePreviewButton();
	uploadFileCheckBox.setSelected(origUploadFile);
	volumeSlider.setValue(volumeConverter.getVolume(origVolume));
	inRestore = false;
    }

    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class TextFieldListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            setPanelDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            setPanelDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            setPanelDirty();
        }

    }

    private void setPanelDirty() {
	if (editor != null) {
            editor.setPanelDirty(PortalComponentProperties.class, isDirty());
	}
    }

    private boolean isDirty() {
	if (inRestore) {
	    return false;
	}

	if (urlTF.getText().length() == 0) {
	    return false;
	}

	if (volumeSlider.getValue() == 0) {
	    return false;
	}

	boolean clean = urlTF.getText().equals(origServerURL);
	    clean &= ((Float) xSpinnerModel.getValue() == origX);
	    clean &= ((Float) ySpinnerModel.getValue() == origY);
	    clean &= ((Float) zSpinnerModel.getValue() == origZ);
	    clean &= ((Float) lookDirectionSpinnerModel.getValue() == origLookDirection);
            clean &= audioSourceType.equals(origAudioSourceType);
            clean &= audioSourceTextField.getText().equals(origAudioSource);
	    clean &= uploadFileCheckBox.isSelected() == origUploadFile;
            clean &= (volumeConverter.getVolume(volumeSlider.getValue()) == origVolume);
	
	//System.out.println("url " + urlTF.getText() + " o " + origServerURL);
	//System.out.println("locX " + ((Float) xSpinnerModel.getValue()) + " o " + origX);
	//System.out.println("locY " + ((Float) ySpinnerModel.getValue()) + " o " + origY);
	//System.out.println("locZ " + ((Float) zSpinnerModel.getValue()) + " o " + origZ);
	//System.out.println("angle " + ((Float) lookDirectionSpinnerModel.getValue()) + " o " 
	//    + origLookDirection);
	//System.out.println("type " + audioSourceType + " o " + origAudioSourceType);
	//System.out.println("source " + audioSourceTextField.getText() + " o " + origAudioSource);
	//System.out.println("upload " + uploadFileCheckBox.isSelected() + " o " + origUploadFile);
	//System.out.println("v " + volumeConverter.getVolume(volumeSlider.getValue()) 
	//    + " o " + origVolume);
	return !clean;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placemarkDialog = new javax.swing.JDialog();
        jLabel6 = new javax.swing.JLabel();
        placemarkCB = new javax.swing.JComboBox();
        placemarkCancelB = new javax.swing.JButton();
        placemarkSetB = new javax.swing.JButton();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        urlTF = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        fromPlacemarkB = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        fileRadioButton = new javax.swing.JRadioButton();
        contentRepositoryRadioButton = new javax.swing.JRadioButton();
        URLRadioButton = new javax.swing.JRadioButton();
        audioSourceTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        volumeSlider = new javax.swing.JSlider();
        jLabel8 = new javax.swing.JLabel();
        previewButton = new javax.swing.JButton();
        xSpinner = new javax.swing.JSpinner();
        ySpinner = new javax.swing.JSpinner();
        zSpinner = new javax.swing.JSpinner();
        lookDirectionSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        uploadFileCheckBox = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/portal/client/resources/Bundle"); // NOI18N
        jLabel6.setText(bundle.getString("PortalComponentProperties.jLabel6.text")); // NOI18N

        placemarkCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        placemarkCancelB.setText(bundle.getString("PortalComponentProperties.placemarkCancelB.text")); // NOI18N
        placemarkCancelB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                placemarkCancelBActionPerformed(evt);
            }
        });

        placemarkSetB.setText(bundle.getString("PortalComponentProperties.placemarkSetB.text")); // NOI18N
        placemarkSetB.setSelected(true);
        placemarkSetB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                placemarkSetBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout placemarkDialogLayout = new org.jdesktop.layout.GroupLayout(placemarkDialog.getContentPane());
        placemarkDialog.getContentPane().setLayout(placemarkDialogLayout);
        placemarkDialogLayout.setHorizontalGroup(
            placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(placemarkDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(placemarkDialogLayout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(placemarkCB, 0, 243, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, placemarkDialogLayout.createSequentialGroup()
                        .add(placemarkSetB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(placemarkCancelB))))
        );
        placemarkDialogLayout.setVerticalGroup(
            placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(placemarkDialogLayout.createSequentialGroup()
                .addContainerGap()
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(placemarkCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(placemarkDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(placemarkCancelB)
                    .add(placemarkSetB))
                .addContainerGap())
        );

        jLabel1.setText(bundle.getString("PortalComponentProperties.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("PortalComponentProperties.jLabel2.text")); // NOI18N

        jLabel4.setText(bundle.getString("PortalComponentProperties.jLabel4.text")); // NOI18N

        jLabel5.setText(bundle.getString("PortalComponentProperties.jLabel5.text")); // NOI18N

        jLabel10.setText(bundle.getString("PortalComponentProperties.jLabel10.text")); // NOI18N

        fromPlacemarkB.setText(bundle.getString("PortalComponentProperties.fromPlacemarkB.text")); // NOI18N
        fromPlacemarkB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fromPlacemarkBActionPerformed(evt);
            }
        });

        jLabel7.setText(bundle.getString("PortalComponentProperties.jLabel7.text")); // NOI18N

        buttonGroup1.add(fileRadioButton);
        fileRadioButton.setSelected(true);
        fileRadioButton.setText(bundle.getString("PortalComponentProperties.fileRadioButton.text")); // NOI18N
        fileRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(contentRepositoryRadioButton);
        contentRepositoryRadioButton.setText(bundle.getString("PortalComponentProperties.contentRepositoryRadioButton.text")); // NOI18N
        contentRepositoryRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contentRepositoryRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(URLRadioButton);
        URLRadioButton.setText(bundle.getString("PortalComponentProperties.URLRadioButton.text")); // NOI18N
        URLRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                URLRadioButtonActionPerformed(evt);
            }
        });

        audioSourceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                audioSourceTextFieldKeyReleased(evt);
            }
        });

        browseButton.setText(bundle.getString("PortalComponentProperties.browseButton.text")); // NOI18N
        browseButton.setEnabled(false);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                volumeSliderStateChanged(evt);
            }
        });

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText(bundle.getString("PortalComponentProperties.jLabel8.text")); // NOI18N

        previewButton.setText(bundle.getString("PortalComponentProperties.previewButton.text")); // NOI18N
        previewButton.setEnabled(false);
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        xSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xSpinnerStateChanged(evt);
            }
        });

        ySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ySpinnerStateChanged(evt);
            }
        });

        zSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                zSpinnerStateChanged(evt);
            }
        });

        lookDirectionSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lookDirectionSpinnerStateChanged(evt);
            }
        });

        jLabel9.setText(bundle.getString("PortalComponentProperties.jLabel9.text")); // NOI18N

        uploadFileCheckBox.setText(bundle.getString("PortalComponentProperties.uploadFileCheckBox.text")); // NOI18N
        uploadFileCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadFileCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(browseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                            .add(jLabel2)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jLabel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(jLabel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .add(jLabel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, uploadFileCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(xSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(zSpinner, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fileRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 167, Short.MAX_VALUE)
                                .add(contentRepositoryRadioButton))
                            .add(layout.createSequentialGroup()
                                .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(31, 31, 31)
                                .add(URLRadioButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(previewButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .add(lookDirectionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 374, Short.MAX_VALUE)
                        .add(fromPlacemarkB))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, audioSourceTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                    .add(urlTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE))
                .add(122, 122, 122))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(urlTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(29, 29, 29)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(xSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4)
                    .add(jLabel5)
                    .add(ySpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2)
                    .add(zSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel9))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(95, 95, 95)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fileRadioButton)
                            .add(jLabel7)))
                    .add(layout.createSequentialGroup()
                        .add(30, 30, 30)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(fromPlacemarkB)
                            .add(lookDirectionSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel10))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(contentRepositoryRadioButton)
                            .add(URLRadioButton))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(audioSourceTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(browseButton))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(uploadFileCheckBox)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(25, 25, 25)
                                .add(previewButton))
                            .add(layout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(volumeSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(layout.createSequentialGroup()
                        .add(71, 71, 71)
                        .add(jLabel8)))
                .addContainerGap(79, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void placemarkSetBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_placemarkSetBActionPerformed
        placemarkDialog.setVisible(false);

        // get the currently selected placemark
        Placemark pm = (Placemark) placemarkCB.getSelectedItem();

        if (pm == null) {
            LOGGER.warning("null placemark selected!");
            return;
        }

        // set values
        urlTF.setText(pm.getUrl());
	xSpinnerModel.setValue(pm.getX());
	ySpinnerModel.setValue(pm.getY());
	zSpinnerModel.setValue(pm.getZ());
        // convert angle properly
	lookDirectionSpinnerModel.setValue((float) Math.toDegrees(pm.getAngle()));
    }//GEN-LAST:event_placemarkSetBActionPerformed

    private void placemarkCancelBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_placemarkCancelBActionPerformed
        placemarkDialog.setVisible(false);
    }//GEN-LAST:event_placemarkCancelBActionPerformed

    private void fromPlacemarkBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fromPlacemarkBActionPerformed
        // display the list of placemarks
        PlacemarkRegistry reg = PlacemarkRegistryFactory.getInstance();
        Vector<Placemark> allPlacemarks = new Vector<Placemark>();
        allPlacemarks.addAll(reg.getAllPlacemarks(PlacemarkType.SYSTEM));
        allPlacemarks.addAll(reg.getAllPlacemarks(PlacemarkType.USER));
        placemarkCB.setModel(new DefaultComboBoxModel(allPlacemarks));
        placemarkDialog.setVisible(true);
    }//GEN-LAST:event_fromPlacemarkBActionPerformed

    private void URLRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_URLRadioButtonActionPerformed
	if (URLRadioButton.isSelected() == false) {
	    return;
	}

	if (audioSourceType.equals(AudioSourceType.URL) == false) {
	    if (lastURLAudioSource != null) {
		audioSourceTextField.setText(lastURLAudioSource);
	    } else {
		audioSourceTextField.setText("");
	    }
	}

        audioSourceType = AudioSourceType.URL;
	URLRadioButton.setSelected(true);
        uploadFileCheckBox.setEnabled(false);
	browseButton.setEnabled(false);
	enablePreviewButton();

	setPanelDirty();
    }//GEN-LAST:event_URLRadioButtonActionPerformed

    private void fileRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileRadioButtonActionPerformed
	if (fileRadioButton.isSelected() == false) {
	    return;
	}

	if (audioSourceType.equals(AudioSourceType.FILE) == false) {
	    if (lastFileAudioSource != null) {
		audioSourceTextField.setText(lastFileAudioSource);
	    } else {
		audioSourceTextField.setText("");
	    }
	}

        audioSourceType = AudioSourceType.FILE;
	fileRadioButton.setSelected(true);
        uploadFileCheckBox.setEnabled(true);
	browseButton.setEnabled(true);
	enablePreviewButton();

	setPanelDirty();
    }//GEN-LAST:event_fileRadioButtonActionPerformed

    private void contentRepositoryRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contentRepositoryRadioButtonActionPerformed
	if (contentRepositoryRadioButton.isSelected() == false) {
	    return;
	}

	if (audioSourceType.equals(AudioSourceType.CONTENT_REPOSITORY) == false) {
	    if (lastContentRepositoryAudioSource != null) {
		audioSourceTextField.setText(lastContentRepositoryAudioSource);
	    } else {
		audioSourceTextField.setText("");
	    }
	}

        audioSourceType = AudioSourceType.CONTENT_REPOSITORY;
	contentRepositoryRadioButton.setSelected(true);
        uploadFileCheckBox.setEnabled(false);
	browseButton.setEnabled(true);
	enablePreviewButton();

	setPanelDirty();
    }//GEN-LAST:event_contentRepositoryRadioButtonActionPerformed

    private void audioSourceTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_audioSourceTextFieldKeyReleased
	enablePreviewButton();
	browseButton.setEnabled(audioSourceTextField.getText().length() > 0);
	setPanelDirty();
    }//GEN-LAST:event_audioSourceTextFieldKeyReleased

    private void enablePreviewButton() {
	if (audioSourceTextField.getText().length() == 0) {
	    previewButton.setEnabled(false);
	    return;
	}

	if (audioSourceType.equals(AudioSourceType.URL)) {
	    previewButton.setEnabled(false);
	    return;
	}
	    
	if (audioSourceType.equals(AudioSourceType.CONTENT_REPOSITORY)) {
	    previewButton.setEnabled(true);
	    return;
	}
	    
	File file = new File(audioSourceTextField.getText());

	if (file.isFile() == true) {
	    previewButton.setEnabled(true);
	} else {
	    previewButton.setEnabled(false);
	}
    }

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        if (audioSourceType.equals(AudioSourceType.FILE)) {
            JFileChooser chooser = new JFileChooser(audioCacheHandler.getAudioCacheDir());

            int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                audioSourceTextField.setText(chooser.getSelectedFile().getAbsolutePath());
		enablePreviewButton();
            }
        } else if (audioSourceType.equals(AudioSourceType.CONTENT_REPOSITORY)) {
	    // display a GUI to browser the content repository. Wait until OK has been
            // selected and fill in the text field with the URI
	    // Fetch the browser for the webdav protocol and display it.
            // Add a listener for the result and update the value of the
            // text field for the URI
            ContentBrowserManager manager = ContentBrowserManager.getContentBrowserManager();
	    final ContentBrowserSPI browser = manager.getDefaultContentBrowser();
	    browser.addContentBrowserListener(new ContentBrowserListener() {

                public void okAction(String uri) {
                    audioSourceTextField.setText(uri);
		    enablePreviewButton();
                    browser.removeContentBrowserListener(this);
                }

                public void cancelAction() {
                    browser.removeContentBrowserListener(this);
                }
            });
            browser.setVisible(true);
	}
	setPanelDirty();
    }//GEN-LAST:event_browseButtonActionPerformed

    private void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_volumeSliderStateChanged
	setPanelDirty();
    }//GEN-LAST:event_volumeSliderStateChanged

    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
	try {
	    preview();
	} catch (Exception e) {
	}
    }//GEN-LAST:event_previewButtonActionPerformed

    private void preview() throws IOException {
	String cacheFilePath = null;

	String audioSource = audioSourceTextField.getText().trim();

	try {
	    switch (audioSourceType) {
	    case FILE:
	        cacheFilePath = audioCacheHandler.cacheFile(audioSource);
	        break;
	
	    case CONTENT_REPOSITORY:
	        cacheFilePath = audioCacheHandler.cacheContent(urlTF.getText().trim(), 
		    audioSource);
	        break;

	    case URL:
	        try {
	            cacheFilePath = audioCacheHandler.cacheURL(new URL(audioSource));
	        } catch (MalformedURLException e) {
		    throw new IOException("Bad URL: " + e.getMessage());
	        }
	        break;
	    }
	} catch (AudioCacheHandlerException e) {
	    throw new IOException(e.getMessage());
	}

	try {
            SoftphoneControlImpl.getInstance().sendCommandToSoftphone("playFile=" 
		+ cacheFilePath + "=" + volumeConverter.getVolume(volumeSlider.getValue()));
	} catch (IOException e) {
	    errorMessage("Preview Error", e.getMessage());
	}
    }

    private void xSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xSpinnerStateChanged
	System.out.println("x spinner change " + (Float) xSpinnerModel.getValue());
	setPanelDirty();
    }//GEN-LAST:event_xSpinnerStateChanged

    private void ySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ySpinnerStateChanged
	setPanelDirty();
    }//GEN-LAST:event_ySpinnerStateChanged

    private void zSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_zSpinnerStateChanged
	setPanelDirty();
    }//GEN-LAST:event_zSpinnerStateChanged

    private void lookDirectionSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lookDirectionSpinnerStateChanged
	setPanelDirty();
    }//GEN-LAST:event_lookDirectionSpinnerStateChanged

    private void uploadFileCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadFileCheckBoxActionPerformed
        setPanelDirty();
    }//GEN-LAST:event_uploadFileCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton URLRadioButton;
    private javax.swing.JTextField audioSourceTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton contentRepositoryRadioButton;
    private javax.swing.JRadioButton fileRadioButton;
    private javax.swing.JButton fromPlacemarkB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner lookDirectionSpinner;
    private javax.swing.JComboBox placemarkCB;
    private javax.swing.JButton placemarkCancelB;
    private javax.swing.JDialog placemarkDialog;
    private javax.swing.JButton placemarkSetB;
    private javax.swing.JButton previewButton;
    private javax.swing.JCheckBox uploadFileCheckBox;
    private javax.swing.JTextField urlTF;
    private javax.swing.JSlider volumeSlider;
    private javax.swing.JSpinner xSpinner;
    private javax.swing.JSpinner ySpinner;
    private javax.swing.JSpinner zSpinner;
    // End of variables declaration//GEN-END:variables
}
