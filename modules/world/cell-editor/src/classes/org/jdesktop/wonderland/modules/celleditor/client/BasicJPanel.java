/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.modules.celleditor.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
import org.jdesktop.wonderland.client.cell.properties.spi.PropertiesFactorySPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.InteractionComponentServerState;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.celleditor.common.NewInteractionComponentServerState;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;

/**
 * A property sheet to edit the basic attributes of a cell
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 * @author Abhishek Upadhyay
 */
public class BasicJPanel extends JPanel implements PropertiesFactorySPI {
    private static final Logger LOGGER =
            Logger.getLogger(BasicJPanel.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/celleditor/client/resources/Bundle");

    private CellPropertiesEditor editor = null;
    private String originalCellName = null;
    private boolean origCollidable;
    private boolean origSelectable;
    private boolean origHighlightEnable = false;
    private Color origHighlightColor = Color.yellow;
    private boolean origCursorEnable = false;
    private boolean origStadardCursor = true;
    private String origCursorFilePath = "";
    private int origIndex = -1;
    private File cursorFile = null;
    private Vector<List> cursors;
    private List firstCursor = null;

    
    /** Creates new form BasicJPanel */
    public BasicJPanel() {
        
        initComponents();
        populateStandardCursor();
        // Listen for changes in the entry for the text field
        cellNameTextField.getDocument().addDocumentListener(new BasicPanelDocumentListener());
        txtUploadCursor.getDocument().addDocumentListener(new BasicPanelDocumentListener());
       
    }
    
    /**
     * @inheritDoc()
     */
    public String getDisplayName() {
        return BUNDLE.getString("Basic");
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
        // Fetch the name and CellID from the Cell and Cell server state and
        // update the GUI
        Cell cell = editor.getCell();
        CellServerState cellServerState = editor.getCellServerState();

        if (cellServerState != null) {
            originalCellName = cellServerState.getName();
            cellNameTextField.setText(originalCellName);
            cellIDLabel.setText(cell.getCellID().toString());
            cellClassLabel.setText(cell.getClass().getName());

            InteractionComponentServerState icss = (InteractionComponentServerState)
                    cellServerState.getComponentServerState(InteractionComponentServerState.class);
            if (icss == null) {
                origCollidable = true;
                origSelectable = true;
            } else {
                origCollidable = icss.isCollidable();
                origSelectable = icss.isSelectable();
            }
            
            collidableCB.setSelected(origCollidable);
            selectableCB.setSelected(origSelectable);
            
            NewInteractionComponentServerState hcss = (NewInteractionComponentServerState) cellServerState
                    .getComponentServerState(NewInteractionComponentServerState.class);
            
            if(hcss != null) {
                origHighlightColor = new Color(hcss.getRed(), hcss.getGreen(), hcss.getBlue());
                origHighlightEnable = hcss.isHighlightEnable();
                origCursorEnable = hcss.isCursorEnable();
                origStadardCursor = hcss.isStandardCursor();
                origCursorFilePath = hcss.getCursorFilePath();
            } else {
                origHighlightColor = Color.yellow;
                origHighlightEnable = false;
                origCursorEnable = false;
                origStadardCursor = true;
                origCursorFilePath = "";
            }
            
            highlightCB.setSelected(origHighlightEnable);
            colorChipPanle.setBackground(origHighlightColor);
            rbStandardCursor.setSelected(origStadardCursor);
            rbOwnCursor.setSelected(!origStadardCursor);
            cursorCB.setSelected(origCursorEnable);
            txtUploadCursor.setText(origCursorFilePath);
            
            if(origStadardCursor && origCursorEnable) {
                int idx = getStandardCursorIndex(origCursorFilePath);
                cbStandardCursor.setSelectedIndex(idx);
            } else {
                cbStandardCursor.setSelectedIndex(0);
            }
            cursorFile = null;
            changeCursorFieldStatus();
        }
        checkDirty();
    }
                                                                        
    /**
     * @inheritDoc()
     */
    public void restore() {
        // Reset the GUI to the original name of the text field
        cellNameTextField.setText(originalCellName);
        collidableCB.setSelected(origCollidable);
        selectableCB.setSelected(origSelectable);
        highlightCB.setSelected(origHighlightEnable);
        colorChipPanle.setBackground(origHighlightColor);
        cursorCB.setSelected(origCursorEnable);
        rbOwnCursor.setSelected(!origStadardCursor);
        rbStandardCursor.setSelected(origStadardCursor);
        txtUploadCursor.setText(origCursorFilePath);
        if(origStadardCursor) {
            int idx = getStandardCursorIndex(origCursorFilePath);
            cbStandardCursor.setSelectedIndex(idx);
        }
        changeCursorFieldStatus();
        changeColorChipPanelStatus();
        checkDirty();
    }

    /**
     * @inheritDoc()
     */
    public void close() {
        // We do nothing here, since any changes in the GUI property sheet do
        // not take effect until apply(), so there is no state to revert here
        // and nothing to really clean up.
    }

    /**
     * @inheritDoc()
     */
    public void apply() {
        // Update the server-side state for the Cell.
        String name = cellNameTextField.getText();
        CellServerState cellServerState = editor.getCellServerState();
        ((CellServerState) cellServerState).setName(name);
        editor.addToUpdateList(cellServerState);
         
        saveInteractionComponent(cellServerState);
        
        saveNewInteractionComponent(cellServerState);
        
        checkDirty();
    }
    
    private void saveInteractionComponent(CellServerState cellServerState) {
        boolean collidable = collidableCB.isSelected();
        boolean selectable = selectableCB.isSelected();
        
        InteractionComponentServerState icss = (InteractionComponentServerState)
                cellServerState.getComponentServerState(InteractionComponentServerState.class);
                
        if (icss == null && collidable && selectable) {
            // if both collidable and selectable are the default, we don't
            // need to add the component
        } else if (icss == null) {
            // we need to add the component
            icss = new InteractionComponentServerState();
            icss.setCollidable(collidable);
            icss.setSelectable(selectable);
            addInteractionComponent(icss);
        } else {
            // update the interaction component
            icss.setCollidable(collidable);
            icss.setSelectable(selectable);
            editor.addToUpdateList(icss);
        }
    }
    
    private void saveNewInteractionComponent(CellServerState cellServerState) {
        boolean highlightEnable = highlightCB.isSelected();
        Color highlightColor = colorChipPanle.getBackground();
        boolean cursorEnable = cursorCB.isSelected();
        boolean isStandardCursor = rbStandardCursor.isSelected();
        
        NewInteractionComponentServerState newIC = (NewInteractionComponentServerState) cellServerState
                .getComponentServerState(NewInteractionComponentServerState.class);
        
        if(newIC == null && (!highlightEnable) && (!cursorEnable)) {
            // if both both are the default, we don't
            // need to add the component
        } else if(newIC == null) {
            // we need to add the component
            newIC = new NewInteractionComponentServerState();
            
            newIC.setHighlightEnable(highlightEnable);
            newIC.setCursorEnable(cursorEnable);
            if(highlightEnable) {
                float[] comps = highlightColor.getComponents(null);
                newIC.setRed(comps[0]);
                newIC.setGreen(comps[1]);
                newIC.setBlue(comps[2]);
            }
            if(cursorEnable) {
                newIC.setStandardCursor(isStandardCursor);
                String path = "";
                if(isStandardCursor) {
                    List l = (List) cbStandardCursor.getSelectedItem();
                    String sel_url = (String) l.get(0);
                    path = sel_url;
                } else {
                    path = uploadCursorFile(cursorFile);
                }
                newIC.setCursorFilePath(path);
            }
            addNewInteractionComponent(newIC);
        } else {
            // update the highlight component
            newIC.setHighlightEnable(highlightEnable);
            newIC.setCursorEnable(cursorEnable);
            if(highlightEnable) {
                float[] comps = highlightColor.getComponents(null);
                newIC.setRed(comps[0]);
                newIC.setGreen(comps[1]);
                newIC.setBlue(comps[2]);
            }
            if(cursorEnable) {
                newIC.setStandardCursor(isStandardCursor);
                String path = origCursorFilePath;
                if(isStandardCursor) {
                    List l = (List) cbStandardCursor.getSelectedItem();
                    String sel_url = (String) l.get(0);
                    path = sel_url;
                } else {
                    if(cursorFile!=null) {
                        path = uploadCursorFile(cursorFile);
                    }
                }
                newIC.setCursorFilePath(path);
            }
            editor.addToUpdateList(newIC);
        }
    }
    
    /*
     * populate standard cursors in dropdown
     */
    private void populateStandardCursor() {
        cursors = new Vector<List>();
        int i=0;
        for(i=0;;i++) {
            List cursor = new ArrayList();
            cursor.add("/org/jdesktop"
                    + "/wonderland/modules/celleditor/client/resources/cursor"+(i+1)+".png");
            URL url = this.getClass().getResource("/org/jdesktop"
                    + "/wonderland/modules/celleditor/client/resources/cursor"+(i+1)+".png");
            if(url==null) {
                break;
            }
            cursor.add(new ImageIcon(url));
            cursors.add(cursor);
            if(i==0) {
                firstCursor = cursor;
            }
        }
        DefaultComboBoxModel dcbm = new DefaultComboBoxModel(cursors);
        cbStandardCursor.setRenderer(new StandardCursorCBRenderer());
        cbStandardCursor.setModel(dcbm);
    }
    
    private int getStandardCursorIndex(String path) {
        int index=-1;
        int i=0;
        for (List l : cursors) {
            String p = (String) l.get(0);
            if(p.equals(path)) {
                index=i;
                break;
            }
            i++;
        }
        return index;
    }
    
    /*
     * custom renderer which renders image file in dropdown
     */
    class StandardCursorCBRenderer extends JLabel
                           implements ListCellRenderer {
        
        public StandardCursorCBRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index
                , boolean isSelected, boolean cellHasFocus) {
            
            //Get the selected index. (The index param isn't
            //always valid, so just use the value.)
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setHorizontalTextPosition(SwingConstants.LEFT);
            
            //Set the icon and text.  If icon was null, say so.
            if(value!=null) {
                List cursor = (List) value;
                //String text = (String) cursor.get(0);
                ImageIcon icon = (ImageIcon) cursor.get(1);
                
                Image img = icon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                
                setIcon(new ImageIcon(img));
                if (icon != null) {
                    //setText(text);
                }
            }
            return this;
        }
    }


    /**
     * Add the interaction component by sending an add component message
     * to the cell.
     */
    private void addInteractionComponent(InteractionComponentServerState icss) {
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newAddMessage(
                editor.getCell().getCellID(), icss);
        ResponseMessage response = editor.getCell().sendCellMessageAndWait(cscm);
        if (response instanceof ErrorMessage) {
            LOGGER.log(Level.WARNING, "Unable to add interaction component "
                    + "for Cell " + editor.getCell().getName() + " with ID "
                    + editor.getCell().getCellID(),
                    ((ErrorMessage) response).getErrorCause());
        }
    }
    
    /**
     * Add the interaction component by sending an add component message
     * to the cell.
     */
    private void addNewInteractionComponent(NewInteractionComponentServerState icss) {
        CellServerComponentMessage cscm =
                CellServerComponentMessage.newAddMessage(
                editor.getCell().getCellID(), icss);
        ResponseMessage response = editor.getCell().sendCellMessageAndWait(cscm);
        
        if (response instanceof ErrorMessage) {
            LOGGER.log(Level.WARNING, "Unable to add highlight component "
                    + "for Cell " + editor.getCell().getName() + " with ID "
                    + editor.getCell().getCellID(),
                    ((ErrorMessage) response).getErrorCause());
        }
    }
    
    /**
     * Inner class to listen for changes to the text field and fire off dirty
     * or clean indications to the cell properties editor.
     */
    class BasicPanelDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void removeUpdate(DocumentEvent e) {
            checkDirty();
        }

        public void changedUpdate(DocumentEvent e) {
            checkDirty();
        }
    }

    /*
     * enable/disable components according to different actions
     */
    private void changeCursorFieldStatus() {
        if(cursorCB.isSelected()) {
            if(rbStandardCursor.isSelected()) {
                txtUploadCursor.setEnabled(false);
                btnChooseFile.setEnabled(false);
                //btnClear.setEnabled(false);
                lblUploadCursor.setEnabled(false);
                cbStandardCursor.setEnabled(true);
                lblStandardCursor.setEnabled(true);
                rbStandardCursor.setEnabled(true);
                rbOwnCursor.setEnabled(true);
            } else {
                txtUploadCursor.setEnabled(true);
                btnChooseFile.setEnabled(true);
                //btnClear.setEnabled(true);
                lblUploadCursor.setEnabled(true);
                cbStandardCursor.setEnabled(false);
                lblStandardCursor.setEnabled(false);
                rbOwnCursor.setEnabled(true);
                rbStandardCursor.setEnabled(true);
            }
        } else {
            txtUploadCursor.setEnabled(false);
            btnChooseFile.setEnabled(false);
            //btnClear.setEnabled(false);
            lblUploadCursor.setEnabled(false);
            cbStandardCursor.setEnabled(false);
            lblStandardCursor.setEnabled(false);
            rbStandardCursor.setEnabled(false);
            rbOwnCursor.setEnabled(false);
        }
    }
    
    /*
     * enable/disable components according to different actions
     */
    private void changeColorChipPanelStatus() {
        if(!highlightCB.isSelected()) {
            lblColor.setEnabled(false);
        } else {
            lblColor.setEnabled(true);
        }
    }
    
    /*
     * open color chip panel
     */
    private void openColorChipPanel() {
        //choose color for highlightiing border of object
        final JColorChooser chooser = new JColorChooser();
        JDialog dialog = JColorChooser.createDialog(this, "Select color", true, chooser, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                 Color newColor = chooser.getColor();
                 if(newColor != null){
                    colorChipPanle.setBackground(newColor);
                    colorChipPanle.repaint();
                    checkDirty();
                }
            }
        },null);
        dialog.setVisible(true);
    }
    
    /*
     * open file chooser with file type images
     */
    private void openFileChooser() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            cursorFile = file;
            txtUploadCursor.setText(file.getAbsolutePath());
        } 
    } 
    
    /*
     * upload user's own image file to be used as cursor
     */
    private String uploadCursorFile(File file) {
        
        //resize image
        Image scaledBimg = null;
        File temp=null; 
        try {
            temp = File.createTempFile("Temp_Image", file.getName().split("\\.")[1]);
        } catch (IOException ex) {
            Logger.getLogger(BasicJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedImage bimg = ImageIO.read(file);
            
            if(bimg.getWidth()>32 || bimg.getHeight()>32) {
                if(bimg.getWidth()>32 && bimg.getHeight()>32) {
                    if(bimg.getWidth()-32>bimg.getHeight()-32) {
                        float nw = 32;
                        float nh = (32*bimg.getHeight())/bimg.getWidth();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                        //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                    } else {
                        float nh = 32;
                        float nw = (32*bimg.getWidth())/bimg.getHeight();
                        scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                        //scaledBimg.getGraphics().drawImage(bimg, 0, 0, null);
                    }
                } else if(bimg.getWidth()>32) {
                    float nw = 32;
                    float nh = (32*bimg.getHeight())/bimg.getWidth();
                    scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                } else if(bimg.getHeight()>32) {
                    float nh = 32;
                    float nw = (32*bimg.getWidth())/bimg.getHeight();
                    scaledBimg = bimg.getScaledInstance((int)nw, (int)nh,BufferedImage.SCALE_SMOOTH);
                }
            } else {
                scaledBimg = bimg;
            }
            BufferedImage new_bimg = new BufferedImage(scaledBimg.getWidth(null),
                    scaledBimg.getHeight(null), BufferedImage.TRANSLUCENT);
            new_bimg.getGraphics().drawImage(scaledBimg, 0, 0, null);
            ImageIO.write(new_bimg, file.getName().split("\\.")[1], temp);
        } catch (IOException ex) {
            Logger.getLogger(BasicJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //upload image
        String path = origCursorFilePath;
        ContentRepositoryRegistry registry = ContentRepositoryRegistry.getInstance();
        ContentRepository repo = registry.getRepository(LoginManager.getPrimary());
        try {
            ContentCollection c = repo.getUserRoot();
            ContentResource r = (ContentResource) c.getChild(cursorFile.getName());

            if(r!=null) {

                //file is already exist
                Object[] options = {
                    BUNDLE.getString("Replace"),
                    BUNDLE.getString("Use_Existing"),
                    BUNDLE.getString("Cancel")
                };
                String msg = MessageFormat.format(
                        BUNDLE.getString("Replace_Question"), file.getName());
                String title = BUNDLE.getString("Replace_Title");

                int result = JOptionPane.showOptionDialog(this, msg, title,
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                // If the user hits Cancel or a "closed" action (e.g. Escape key)
                // then just return
                if ((result == JOptionPane.CANCEL_OPTION)
                        || (result == JOptionPane.CLOSED_OPTION)) {
                    path = origCursorFilePath;
                }

                // If the content exists and we do not want to upload a new version,
                // then simply create it and return.
                if (result == JOptionPane.NO_OPTION) {
                    path = "wlcontent:/"+r.getPath();
                }
                if(result == JOptionPane.YES_OPTION) {
                    try {
                        r.put(temp);
                        path = "wlcontent:/"+r.getPath();
                    } catch (IOException ex) {
                        Logger.getLogger(BasicJPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                
                //create a new file
                r = (ContentResource) c.createChild(cursorFile.getName()
                            , ContentNode.Type.RESOURCE);
                try {
                    r.put(temp);
                    path = "wlcontent:/"+r.getPath();
                } catch (IOException ex) {
                    Logger.getLogger(BasicJPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(BasicJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }
    
    private void checkDirty() {
        // see if the name has changed
        boolean dirty = !cellNameTextField.getText().equals(originalCellName);

        // see if the checkboxes match the server state
        dirty |= collidableCB.isSelected() != origCollidable;
        dirty |= selectableCB.isSelected() != origSelectable;
        dirty |= highlightCB.isSelected() != origHighlightEnable;
        dirty |= !colorChipPanle.getBackground().equals(origHighlightColor);
        dirty |= cursorCB.isSelected() != origCursorEnable;
        if(origCursorEnable && cursorCB.isSelected()) {
            dirty |= rbStandardCursor.isSelected() != origStadardCursor;
            if(origStadardCursor && rbStandardCursor.isSelected()) {
                List l = (List) cbStandardCursor.getSelectedItem();
                if(l!=null) {
                    String sel_url = (String) l.get(0);
                    dirty |= !sel_url.equals(origCursorFilePath);
                }
            }
            if(!origStadardCursor && rbOwnCursor.isSelected()) {
                if(cursorFile!=null) {
                    try {
//                        BufferedImage bi = ImageIO.read(cursorFile);
//                        System.out.println("");
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    dirty = true;
                }
            }
        }

        if (editor != null) {
            editor.setPanelDirty(BasicJPanel.class, dirty);
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
        fileChooser = new javax.swing.JFileChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cellIDLabel = new javax.swing.JLabel();
        cellNameTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cellClassLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        collidableCB = new javax.swing.JCheckBox();
        selectableCB = new javax.swing.JCheckBox();
        highlightCB = new javax.swing.JCheckBox();
        lblColor = new javax.swing.JLabel();
        colorChipPanle = new javax.swing.JPanel();
        cursorCB = new javax.swing.JCheckBox();
        rbStandardCursor = new javax.swing.JRadioButton();
        cbStandardCursor = new javax.swing.JComboBox();
        lblStandardCursor = new javax.swing.JLabel();
        rbOwnCursor = new javax.swing.JRadioButton();
        lblUploadCursor = new javax.swing.JLabel();
        txtUploadCursor = new javax.swing.JTextField();
        btnChooseFile = new javax.swing.JButton();

        fileChooser.setDialogTitle("Select Image");
        FileNameExtensionFilter fnef = new FileNameExtensionFilter
        ("Image File","jpeg","JPEG","bmp","BMP","jpg","JPG","gif","GIF","png","PNG");
        fileChooser.addChoosableFileFilter(fnef);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(fnef);

        setRequestFocusEnabled(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/celleditor/client/resources/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("BasicJPanel.jLabel1.text")); // NOI18N

        jLabel2.setText(bundle.getString("BasicJPanel.jLabel2.text")); // NOI18N

        cellIDLabel.setText(bundle.getString("BasicJPanel.cellIDLabel.text")); // NOI18N

        jLabel3.setText(bundle.getString("BasicJPanel.jLabel3.text")); // NOI18N

        cellClassLabel.setText(bundle.getString("BasicJPanel.cellClassLabel.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("BasicJPanel.jPanel1.border.title"))); // NOI18N

        collidableCB.setSelected(true);
        collidableCB.setText(bundle.getString("BasicJPanel.collidableCB.text")); // NOI18N
        collidableCB.setBorder(null);
        collidableCB.setBorderPainted(true);
        collidableCB.setPreferredSize(new java.awt.Dimension(179, 23));
        collidableCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collidableCBActionPerformed(evt);
            }
        });

        selectableCB.setSelected(true);
        selectableCB.setText(bundle.getString("BasicJPanel.selectableCB.text")); // NOI18N
        selectableCB.setBorder(null);
        selectableCB.setBorderPainted(true);
        selectableCB.setPreferredSize(new java.awt.Dimension(147, 23));
        selectableCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectableCBActionPerformed(evt);
            }
        });

        highlightCB.setText(bundle.getString("BasicJPanel.highlightCB.text")); // NOI18N
        highlightCB.setBorder(null);
        highlightCB.setBorderPainted(true);
        highlightCB.setPreferredSize(new java.awt.Dimension(100, 23));
        highlightCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightCBActionPerformed(evt);
            }
        });

        lblColor.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lblColor.setText(bundle.getString("BasicJPanel.lblColor.text")); // NOI18N
        lblColor.setPreferredSize(new java.awt.Dimension(35, 23));

        colorChipPanle.setBackground(new java.awt.Color(255, 255, 0));
        colorChipPanle.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        colorChipPanle.setPreferredSize(new java.awt.Dimension(23, 23));
        colorChipPanle.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                colorChipPanleMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout colorChipPanleLayout = new org.jdesktop.layout.GroupLayout(colorChipPanle);
        colorChipPanle.setLayout(colorChipPanleLayout);
        colorChipPanleLayout.setHorizontalGroup(
            colorChipPanleLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 19, Short.MAX_VALUE)
        );
        colorChipPanleLayout.setVerticalGroup(
            colorChipPanleLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 19, Short.MAX_VALUE)
        );

        cursorCB.setText(bundle.getString("BasicJPanel.cursorCB.text")); // NOI18N
        cursorCB.setBorder(null);
        cursorCB.setBorderPainted(true);
        cursorCB.setPreferredSize(new java.awt.Dimension(133, 23));
        cursorCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cursorCBActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbStandardCursor);
        rbStandardCursor.setText(bundle.getString("BasicJPanel.rbStandardCursor.text")); // NOI18N
        rbStandardCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbStandardCursorActionPerformed(evt);
            }
        });

        cbStandardCursor.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbStandardCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStandardCursorActionPerformed(evt);
            }
        });

        lblStandardCursor.setText(bundle.getString("BasicJPanel.lblStandardCursor.text")); // NOI18N

        buttonGroup1.add(rbOwnCursor);
        rbOwnCursor.setText(bundle.getString("BasicJPanel.rbOwnCursor.text")); // NOI18N
        rbOwnCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbOwnCursorActionPerformed(evt);
            }
        });

        lblUploadCursor.setText(bundle.getString("BasicJPanel.lblUploadCursor.text")); // NOI18N

        txtUploadCursor.setText(bundle.getString("BasicJPanel.txtUploadCursor.text")); // NOI18N
        txtUploadCursor.setEditable(false);

        btnChooseFile.setText(bundle.getString("BasicJPanel.btnChooseFile.text_1")); // NOI18N
        btnChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseFileActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(lblUploadCursor)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(txtUploadCursor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnChooseFile, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(rbStandardCursor)
                            .add(rbOwnCursor)
                            .add(jPanel1Layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(lblStandardCursor)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(cbStandardCursor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, cursorCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1Layout.createSequentialGroup()
                                .add(highlightCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(lblColor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 37, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(colorChipPanle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 58, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, selectableCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, collidableCB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(67, 67, 67))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(collidableCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(selectableCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(colorChipPanle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(highlightCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(lblColor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(4, 4, 4)
                .add(cursorCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbStandardCursor)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cbStandardCursor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .add(lblStandardCursor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbOwnCursor)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblUploadCursor)
                    .add(txtUploadCursor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnChooseFile))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, cellNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 300, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, cellClassLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, cellIDLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cellIDLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(cellNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(cellClassLabel))
                .add(18, 18, 18)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void collidableCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collidableCBActionPerformed
        checkDirty();
    }//GEN-LAST:event_collidableCBActionPerformed

    private void selectableCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectableCBActionPerformed
        checkDirty();
    }//GEN-LAST:event_selectableCBActionPerformed

    private void colorChipPanleMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_colorChipPanleMouseClicked
        if(lblColor.isEnabled()) {
            openColorChipPanel();
        }
    }//GEN-LAST:event_colorChipPanleMouseClicked

    private void highlightCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightCBActionPerformed
        // TODO add your handling code here:
        changeColorChipPanelStatus();
        checkDirty();
    }//GEN-LAST:event_highlightCBActionPerformed

    private void cursorCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cursorCBActionPerformed
        // TODO add your handling code here:
        changeCursorFieldStatus();
        checkDirty();
    }//GEN-LAST:event_cursorCBActionPerformed

    private void rbStandardCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbStandardCursorActionPerformed
        // TODO add your handling code here:
        changeCursorFieldStatus();
        checkDirty();
    }//GEN-LAST:event_rbStandardCursorActionPerformed

    private void rbOwnCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbOwnCursorActionPerformed
        // TODO add your handling code here:
        changeCursorFieldStatus();
        checkDirty();
    }//GEN-LAST:event_rbOwnCursorActionPerformed

    private void cbStandardCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbStandardCursorActionPerformed
        // TODO add your handling code here:
//        JComboBox comboBox = (JComboBox)evt.getSource();
//        Cursor item = (Cursor)comboBox.getSelectedItem();
        checkDirty();
    }//GEN-LAST:event_cbStandardCursorActionPerformed

    private void btnChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseFileActionPerformed
        // TODO add your handling code here:
        openFileChooser();
    }//GEN-LAST:event_btnChooseFileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChooseFile;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbStandardCursor;
    private javax.swing.JLabel cellClassLabel;
    private javax.swing.JLabel cellIDLabel;
    private javax.swing.JTextField cellNameTextField;
    private javax.swing.JCheckBox collidableCB;
    private javax.swing.JPanel colorChipPanle;
    private javax.swing.JCheckBox cursorCB;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JCheckBox highlightCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblStandardCursor;
    private javax.swing.JLabel lblUploadCursor;
    private javax.swing.JRadioButton rbOwnCursor;
    private javax.swing.JRadioButton rbStandardCursor;
    private javax.swing.JCheckBox selectableCB;
    private javax.swing.JTextField txtUploadCursor;
    // End of variables declaration//GEN-END:variables
}
