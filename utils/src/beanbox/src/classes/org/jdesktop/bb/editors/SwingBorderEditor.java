/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

package org.jdesktop.bb.editors;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Swing version of a Border property editor.
 *
 * @version %I% %G%
 * @author  Tom Santos
 * @author  Mark Davidson
 */
public class SwingBorderEditor extends SwingEditorSupport {
  
    private JComboBox borderCombo;
    private JButton  borderButton;
    private BorderDialog borderDialog;

    private Border etched = BorderFactory.createEtchedBorder();
    private Border bevelLowered = BorderFactory.createLoweredBevelBorder();
    private Border bevelRaised =BorderFactory.createRaisedBevelBorder();
    private Border line = BorderFactory.createLineBorder(Color.black);
    private Border borders[] = { etched, bevelLowered, bevelRaised, line };

    private Border border;

    public SwingBorderEditor(){
	createComponents();
	addComponentListeners();
    }

    private void createComponents() {
	panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	panel.setAlignmentX(Component.LEFT_ALIGNMENT);
	UIDefaults table = UIManager.getDefaults();
	table.put("beaninfo.BorderIcon", 
		  LookAndFeel.makeIcon(getClass(), "resources/BorderIcon.gif"));
        table.put("beaninfo.BorderBevelLowered", 
		  LookAndFeel.makeIcon(getClass(), "resources/BorderBevelLowered.gif"));
        table.put("beaninfo.BorderBevelRaised", 
		  LookAndFeel.makeIcon(getClass(), "resources/BorderBevelRaised.gif"));
        table.put("beaninfo.BorderEtched", 
		  LookAndFeel.makeIcon(getClass(), "resources/BorderEtched.gif"));
        table.put("beaninfo.BorderLine", 
		  LookAndFeel.makeIcon(getClass(), "resources/BorderLine.gif"));
	Icon buttonIcon = UIManager.getIcon("beaninfo.BorderIcon");

        borderCombo = createComboBox();

	// need rigid area match up
	borderButton = new JButton(buttonIcon);
	Dimension d = new Dimension(buttonIcon.getIconWidth(), buttonIcon.getIconHeight());
	borderButton.setMargin(SwingEditorSupport.BUTTON_MARGIN);

	setAlignment(borderCombo);
	setAlignment(borderButton);
	panel.add(borderCombo);
	panel.add(Box.createRigidArea(new Dimension(5,0)));
	panel.add(borderButton);
	panel.add(Box.createHorizontalGlue());
    }

    private void addComponentListeners() {
	borderButton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (borderDialog == null)
			borderDialog = new BorderDialog(panel.getParent(), "Border Chooser");
		    border = borderDialog.showDialog();
		    if (!(borderDialog.isCancelled()))
			setValue(border);
		}
	    });
    }

    public void setValue(Object value){
	super.setValue(value);
	// update our GUI state
	// set ComboBox to any equal border value
	//	borderCombo.setSelectedItem(value);
	// set BorderChooser - setSelectedBorder to any equal value as well
    }
  
    private JComboBox createComboBox(){
	DefaultComboBoxModel model = new DefaultComboBoxModel();
	for (int i = 0; i < 4; i++){
	    model.addElement(new Integer(i));
	}
          
	JComboBox c = new JComboBox(model); // borders);
	c.setRenderer(new TestCellRenderer(c));
	c.setPreferredSize(SwingEditorSupport.MEDIUM_DIMENSION); // new Dimension(120,20));
	c.setMinimumSize(SwingEditorSupport.MEDIUM_DIMENSION);
	c.setMaximumSize(SwingEditorSupport.MEDIUM_DIMENSION);
	c.setSelectedIndex(-1);
	c.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JComboBox cb = (JComboBox)e.getSource();
		    border = borders[cb.getSelectedIndex()];
		    setValue(border);
		}
	    }); 
	return c;
    }


    class TestCellRenderer extends JLabel implements ListCellRenderer   {
	JComboBox combobox;
	Icon images[] = {
	    UIManager.getIcon("beaninfo.BorderEtched"),                  
	    UIManager.getIcon("beaninfo.BorderBevelLowered"),
	    UIManager.getIcon("beaninfo.BorderBevelRaised"),
	    UIManager.getIcon("beaninfo.BorderLine") };

	String desc[] = {
	    "Etched",                  
	    "BevelLowered",
	    "BevelRaised",
	    "Line" };
          
	public TestCellRenderer(JComboBox x) {
	    this.combobox = x;
	    setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
						      int modelIndex,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    if (value == null) {
		setText("");
		setIcon(null);
	    } else {
		int index = ((Integer)value).intValue();
		if (index < 0){
		    setText("");
		    setIcon(null);
		} else {
		    String text = " " + desc[index];
		    setIcon(images[index]);                          
		    setText(text);
		    if (isSelected){
			setBackground(UIManager.getColor("ComboBox.selectionBackground"));
			setForeground(UIManager.getColor("ComboBox.selectionForeground"));
		    } else {
			setBackground(UIManager.getColor("ComboBox.background"));
			setForeground(UIManager.getColor("ComboBox.foreground"));
		    }
		}
	    }
	    return this;
	}
    }

    class BorderDialog extends JDialog {
	JPanel pane;
	JButton okButton;
	BorderChooser borderChooser;
	Border border = null;
	boolean cancel = false;

	public BorderDialog(Component c, String title){
	    super(JOptionPane.getFrameForComponent(c), title, true);
	    Container contentPane = getContentPane();
	    pane = new JPanel();

	    contentPane.setLayout(new BorderLayout());
	    okButton = new JButton("OK"); // new BorderTracker(pane);
	    ActionListener okListener = new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			// get the Border from the pane
			border = getBorder();
		    }
		};

	    getRootPane().setDefaultButton(okButton);
	    okButton.setActionCommand("OK");
	    if (okListener != null) {
		okButton.addActionListener(okListener);
	    }
	    okButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			setVisible(false);
		    }
		});
	    JButton cancelButton = new JButton("Cancel");
	    cancelButton.setActionCommand("cancel");
	    cancelButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			cancel = true;
			setVisible(false);
		    }
		});
	    // borderlayout
	    addBorderChooser(pane);      
	    pane.add(okButton);
	    pane.add(cancelButton);
	    contentPane.add(pane, BorderLayout.CENTER);
	    pack();
	    this.addWindowListener(new Closer());
	    this.addComponentListener(new DisposeOnClose());
	}

	public void addBorderChooser(JPanel panel){
	    borderChooser = new BorderChooser();
	    panel.add(borderChooser);
	}

	public void setBorder(){ // called from pane
	}

	public Border getBorder(){
	    return borderChooser.getSelectedBorder();
	    //      return this.border;
	}

	public Border showDialog(){
	    this.cancel = false;
	    this.setVisible(true);
	    return getBorder(); // border should be ok
	}

	public boolean isCancelled(){
	    return this.cancel;
	}

	class Closer extends WindowAdapter {
	    public void windowClosing(WindowEvent e) {
		Window w = e.getWindow();
		w.setVisible(false);
	    }
	}

	class DisposeOnClose extends ComponentAdapter {
	    public void componentHidden(ComponentEvent e) {
		Window w = (Window)e.getComponent();
		w.dispose();
	    }
	}
    }

    public static void main(String args[]){
	JFrame f = new JFrame();
	f.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    System.exit(0);
		}
	    });
	SwingBorderEditor editor = new SwingBorderEditor();
	f.getContentPane().add(editor.getCustomEditor());
	f.pack();
	f.setVisible(true);
    }
}
