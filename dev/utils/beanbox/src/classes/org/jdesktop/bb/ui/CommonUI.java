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

package org.jdesktop.bb.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * A collection of static methods and constants to make the UI uniform.
 *
 * @version %I% %G%
 * @author  Mark Davidson
 */
public class CommonUI {
    
    // Buttons
    public static final String BUTTONTEXT_ADD = "Add";
    public static final String BUTTONTEXT_APPLY = "Apply";
    public static final String BUTTONTEXT_EDIT = "Edit";
    public static final String BUTTONTEXT_NEW = "New";
    public static final String BUTTONTEXT_REMOVE = "Remove";
    public static final String BUTTONTEXT_OK = "Ok";
    public static final String BUTTONTEXT_CANCEL = "Cancel";
    public static final String BUTTONTEXT_CLOSE = "Close";
    public static final String BUTTONTEXT_NEXT = "Next";
    public static final String BUTTONTEXT_BACK = "Back";
    public static final String BUTTONTEXT_FINISH = "Finish";
    public static final String BUTTONTEXT_MOVEUP = "Move Up";
    public static final String BUTTONTEXT_MOVEDOWN = "Move Down";
    
    public static final int MNEMONIC_ADD = 'A';
    public static final int MNEMONIC_APPLY = 'A';
    public static final int MNEMONIC_EDIT = 'E';
    public static final int MNEMONIC_NEW = 'N';
    public static final int MNEMONIC_REMOVE = 'R';
    public static final int MNEMONIC_OK = 'O';
    public static final int MNEMONIC_CANCEL = 'C';
    public static final int MNEMONIC_CLOSE = 'L';
    public static final int MNEMONIC_NEXT = 'N';
    public static final int MNEMONIC_BACK = 'B';
    public static final int MNEMONIC_FINISH = 'F';
    public static final int MNEMONIC_MOVEUP = 'U';
    public static final int MNEMONIC_MOVEDOWN = 'D';
    
    // Size of a standard Button Size
    public static final int BUTTON_WIDTH     = 100;
    public static final int BUTTON_HEIGHT    = 26;
    
    // Size of a standard toolbar button
    public static final int BUTTCON_WIDTH     = 28;
    public static final int BUTTCON_HEIGHT    = 28;
    
    // Size of a smaller Button Size
    public static final int SM_BUTTON_WIDTH = 72;
    public static final int SM_BUTTON_HEIGHT = 26;
    
    // Size of a label
    public static final int LABEL_WIDTH     = 100;
    public static final int LABEL_HEIGHT    = 20;
    
    // Size of a textfield
    public static final int TEXT_WIDTH     = 150;
    public static final int TEXT_HEIGHT    = 20;

    // Command Strings
    public static final String BUTTON_CMD_OK = "ok-command";
    public static final String BUTTON_CMD_CANCEL = "cancel-command";
    public static final String BUTTON_CMD_NEXT = "next-command";
    public static final String BUTTON_CMD_BACK = "back-command";
    public static final String BUTTON_CMD_FINISH = "finish-command";
    public static final String BUTTON_CMD_ADD = "add-command";
    public static final String BUTTON_CMD_REMOVE = "remove-command";
    public static final String BUTTON_CMD_MOVEUP = "moveup-command";
    public static final String BUTTON_CMD_MOVEDOWN = "movedown-command";

    // Preferred size for buttons
    public static Dimension buttonPrefSize = new Dimension(BUTTON_WIDTH, 
                                                           BUTTON_HEIGHT);

    public static Dimension buttconPrefSize = new Dimension(BUTTCON_WIDTH,
                                                           BUTTCON_HEIGHT);
                                
    public static Dimension smbuttonPrefSize = new Dimension(SM_BUTTON_WIDTH, 
                                                             SM_BUTTON_HEIGHT);
    // Preferred size for labels
    public static Dimension labelPrefSize = new Dimension(LABEL_WIDTH, 
                                                          LABEL_HEIGHT);
                                                          
    // Preferred size for textfields
    public static Dimension textPrefSize = new Dimension(TEXT_WIDTH, 
                                                          TEXT_HEIGHT);

    ///////////////////////////////////////////////////////////////////////////
    //
    // Widget Factory methods 
    //
    ///////////////////////////////////////////////////////////////////////////
    
    /** 
     * Creates a label which will be displayed.
     *
     * @param text Text for the label
     * @param mnemonic Hot key
     * @param comp Component that this label represents.
     */
    public static JLabel createLabel(String text, int mnemonic, 
                                        Component comp) {
        JLabel label = new JLabel(/*NOI18N*/"  " + text);
        label.setMinimumSize(labelPrefSize);
        
        if (mnemonic != -1)
            label.setDisplayedMnemonic(mnemonic);
            
        if (comp != null)
            label.setLabelFor(comp);

        if (text.length() == 0)  {
            label.setPreferredSize(labelPrefSize);
        }

        return label;
    }
    
    public static JLabel createLabel(String text) {
        return createLabel(text, -1, null);
    }
    
    /** 
     * Creates a text field
     *
     * @param text Text for the field
     * @param listener KeyListener
     * @param numbers Indicates that this field represents numbers only
     */
    public static JTextField createTextField(String text, KeyListener listener,
                                    boolean numbers) {
        JTextField field = new JTextField(text);
        
        field.setMinimumSize(textPrefSize);
        
        if (text.length() == 0)  {
            field.setPreferredSize(textPrefSize);
        }

        if (listener != null)  {
            field.addKeyListener(listener);
        }

        if (numbers)  {
            field.setDocument(new NumberDocument());
        }
        
        return field;
    }
    
    public static JTextField createTextField(String text, boolean numbers) {
        return createTextField(text, null, numbers);
    }
 
    public static JTextField createTextField(String text, KeyListener listener) {
        return createTextField(text, listener, false);
    }
 
    public static JTextField createTextField(String text) {
        return createTextField(text, null, false);
    }
 
    /** 
     * Document class which accepts only numbers. 
     * For the text fields only.
     */
    private static class NumberDocument extends PlainDocument  {
        public void insertString(int offs, String str, AttributeSet atts) 
                    throws BadLocationException {
                    
            if (!Character.isDigit(str.charAt(0)))  {
                return;
            }
            super.insertString(offs, str, atts);
        }
    }

    /** 
     * Creates a Radio Button
     * @param text Text to display
     * @param mnemonic Hot key
     * @param listener ActionListener
     * @param selected Flag to indicate if button should be selected
     */
    public static JRadioButton createRadioButton(String text, int mnemonic, 
                    ActionListener listener, boolean selected)  {
        JRadioButton button = new JRadioButton(text);
        button.setMnemonic(mnemonic);
        button.setSelected(selected);
        button.setMinimumSize(labelPrefSize);

        if (listener != null)  {
            button.addActionListener(listener);
        }

        if (text.length() == 0)  {
            button.setPreferredSize(labelPrefSize);
        }

        return button;
    }
    
    public static JRadioButton createRadioButton(String text, int mnemonic,
                            boolean selected)  {
        return createRadioButton(text, mnemonic, null, selected);
    }
    
    public static JRadioButton createRadioButton(String text, int mnemonic,
                            ActionListener listener)  {
        return createRadioButton(text, mnemonic, listener, false);
    }
    
    public static JRadioButton createRadioButton(String text, int mnemonic)  {
        return createRadioButton(text, mnemonic, null, false);
    }
    
    public static JRadioButton createRadioButton(String text)  {
        return createRadioButton(text, -1, null, false);
    }
 
    /** 
     * Create a checkbox
     * @param text to display
     * @param mnemonic Hot key
     * @param listener ActionListener
     * @param selected Flag to indicate if button should be selected
     */
    public static JCheckBox createCheckBox(String text, int mnemonic, 
                    ActionListener listener, boolean selected) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setMinimumSize(labelPrefSize);
        
        if (mnemonic != -1)
            checkbox.setMnemonic(mnemonic);
            
        checkbox.setSelected(selected);
        
        if (text.length() == 0)  {
            checkbox.setPreferredSize(labelPrefSize);
        }
        
        if (listener != null)  {
            checkbox.addActionListener(listener);
        }

        return checkbox;        
    }
 
    public static JCheckBox createCheckBox(String text, int mnemonic, 
                                    ActionListener listener)  {
        return createCheckBox(text, mnemonic, listener, false);
    }
 
    public static JCheckBox createCheckBox(String text, int mnemonic, 
                                    boolean selected)  {
        return createCheckBox(text, mnemonic, null, selected);
    }
 
    public static JCheckBox createCheckBox(String text, int mnemonic)  {
        return createCheckBox(text, mnemonic, null, false);
    }
    
    public static JCheckBox createCheckBox(String text)  {
        return createCheckBox(text, -1, null, false);
    }

    /** 
     * Creates a JComboBox
     * @param items Object array
     * @param listener The action listener which handles events
     * @param editable Flag that indicates if this combo box is editable
     */
	public static JComboBox createComboBox(Object[] items, ActionListener listener,
		boolean editable) {
		JComboBox comboBox = new JComboBox(items);
		
		if (listener != null)
			comboBox.addActionListener(listener);
			
		comboBox.setEditable(editable);

		return comboBox;
	}

	public static JComboBox createComboBox(Object[] items, boolean editable) {
		return createComboBox(items, null, editable);
	}

    /** 
     * Creates a JComboBox
     * @param items Vector of items.
     * @param listener The action listener which handles events
     * @param editable Flag that indicates if this combo box is editable
     */
	public static JComboBox createComboBox(Vector items, ActionListener listener,
		boolean editable) {
		JComboBox comboBox = new JComboBox(items);
		
		if (listener != null)
			comboBox.addActionListener(listener);
			
		comboBox.setEditable(editable);

		return comboBox;
	}

	public static JComboBox createComboBox(Vector items, boolean editable) {
		return createComboBox(items, null, editable);
	}

    /** 
     * Creates a JButton
     * @param text to display
     * @param listener The action listener which handles events
     * @param mnemonic Letter combination
     */
    public static JButton createButton(String text, ActionListener listener, 
                                            int mnemonic) {
        JButton button = new JButton(text);
        button.setMinimumSize(buttonPrefSize);
        
        if (listener != null)
            button.addActionListener(listener);
            
        if (mnemonic != -1)
            button.setMnemonic(mnemonic);
            
        if (text.length() == 0)  {
            button.setPreferredSize(buttonPrefSize);
        } else {
            // Make sure that the button has a minimum size.
            // This is a bit of a hack since setMinimumSize doens't work for all
            // layout managers.
            Dimension size = button.getPreferredSize();
            if (size.width < buttonPrefSize.width)  {
                button.setPreferredSize(buttonPrefSize);
            }
        }

        return button;
    }
    
    public static JButton createButton(String text, ActionListener listener)  {
        return createButton(text, listener, -1);
    }
    
    /** 
     * Creates a Smaller JButton
     * @param text to display
     * @param listener The action listener which handles events
     * @param mnemonic Letter combination
     */
    public static JButton createSmallButton(String text, ActionListener listener, 
                                            int mnemonic) {
        JButton button = createButton(text, listener, mnemonic);
        button.setMinimumSize(smbuttonPrefSize);
        
        if (text.length() == 0)  {
            button.setPreferredSize(smbuttonPrefSize);
        } else {
            Dimension size = button.getPreferredSize();
            if (size.width < smbuttonPrefSize.width)  {
                button.setPreferredSize(smbuttonPrefSize);
            }
        }
        
        return button;
    }    
    
    public static JButton createSmallButton(String text, ActionListener listener)  {
        return createSmallButton(text, listener, -1);
    }

                                            
    /** 
     * Creates an etched border with the displayed text
     */
    public static Border createBorder(String text)  {
        Border border = BorderFactory.createEtchedBorder();
        
        return BorderFactory.createTitledBorder(border, text, 
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP);
    }
    
    /** 
     * Creates an blank border with the displayed text
     */
    public static Border createBorder()  {
        return BorderFactory.createEmptyBorder(4, 4, 4, 4);
    }
    
    /** 
     * Wraps a scrollpane with and etched border and a title around a JList.
     * @param list JList to wrap.
     * @param text Text to display in the border.
     */
    public static JScrollPane createListPane(JList list, String text)  {
        JScrollPane pane = new JScrollPane(list);
        
        pane.setBorder(BorderFactory.createCompoundBorder(createBorder(text),
            				BorderFactory.createLoweredBevelBorder()));
        return pane;        
    }
 
    ///////////////////////////////////////////////////////////////////////////
    //
    // Utility methods.
    //
    ///////////////////////////////////////////////////////////////////////////

    /** 
     * Centers a component (source) in it's parent component. If parent is null
     * then the window is centered in screen.
     * 
     * The source and parent components should be correctly sized
     */
    public static void centerComponent(Component source, Component parent)  {
        
        Rectangle rect;
        Dimension dim = source.getSize();
            
        if (parent != null)
            rect = parent.getBounds();
        else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            rect = new Rectangle(0, 0, d.width, d.height);
        }
        
        int x = rect.x + (rect.width - dim.width)/2;
        int y = rect.y + (rect.height - dim.height)/2;
        
        source.setLocation(x, y);
    }

    /** 
     * Centers the Component on the screen.
     */
    public static void centerComponent(Component source)  {
        centerComponent(source, null);
    }
    
    /** 
     * Retrieves the parent JFrame for a component. This is handy when you
     * want the frame as an owner for modal dialogs.
     *
     * @param source the source component
     * @return The JFrame which contains the component or null if not found
     */
    public static JFrame getParentFrame(Component source)  {
        Container parent = source.getParent();
        
        while (parent != null) {
            if (parent instanceof JFrame)  {
                break;
            } else {
                parent = parent.getParent();
            }
        }
        if (parent == null)
            return null;
        else 
            return (JFrame)parent;
    }


    /** 
     * Converts miliseconds to seconds
     */
    public static Integer msToSec(Integer ms)  {
        int value = ms.intValue();
        
        value /= 1000;
        
        return new Integer(value);
    }
    
    /** 
     * Converts seconds to miliseconds
     */
    public static Integer secToMs(Integer sec)  {
        int value = sec.intValue();
        
        value *= 1000;
        
        return new Integer(value);
    }
    
    /** 
     * Takes an array of strings and returns a concatenation.
     * <p>
     * @param strings - Array to concatenate
     * @param delim - Delimiter to use. If this is null or empty then
     *        then a space will be used
     */
    public static String stringFromStringArray(String[] strings, String delim)  {
        String string = /*NOI18N*/"";
        String separator;
        
        if (delim == null || delim.equals(/*NOI18N*/""))  {
            separator = /*NOI18N*/" ";
        } else {
            separator = delim;
        }
        
        for (int i = 0; i < strings.length; i++) {
            string += strings[i];
            string += separator;
        }
        
        return string;
    }
    
    public static String stringFromStringArray(String[] strings)  {
        return stringFromStringArray(strings, /*NOI18N*/"");
    }

    /** 
     * Parses the string into an array of strings.
     * <p>
     * @param string - String to parse
     * @param delim - Delimiter to use. If this is null or empty then
     *        then the set [ ' ', '\t', '\n', '\r' ] is used.
     */
    public static String[] stringArrayFromString(String string, String delim)  {
        StringTokenizer st;
        String[] strings;
        
        if (delim == null || delim.equals(/*NOI18N*/""))  {
            st = new StringTokenizer(string);
        } else {
            st = new StringTokenizer(string, delim);
        }
        int numTokens = st.countTokens();
        strings = new String[numTokens];
        
        int index = 0;
        while(st.hasMoreTokens()) {
            strings[index++] = st.nextToken();
        }
        
        return strings;
    }

    public static String[] stringArrayFromString(String string)  {
        return stringArrayFromString(string, /*NOI18N*/"");
    }
    
    // Cursor operations
    public static void setWaitCursor(Component comp)  {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    public static void setDefaultCursor(Component comp)  {
        comp.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
