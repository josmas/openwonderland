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

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A property editor for editing integers. This editor also supports enumerated
 * type properties which are identified if the "enumerationValues" key returns
 * a non-null value.
 * Note: the init() method must be called before the set/get methods can be
 * called.
 *
 * @version 1.10 02/27/02
 * @author  Mark Davidson
 */
public class SwingIntegerEditor extends SwingEditorSupport {
    
    // Property editor to use if the Integer represents an Enumerated type.
    private SwingEnumEditor enumEditor = new SwingEnumEditor();
    
    private JTextField textfield;
    
    private boolean isEnumeration = false;
    
    public void setValue(Object value) {
        if (isEnumeration)  {
            enumEditor.setValue(value);
        } else {
            super.setValue(value);
            
            if (value != null)  {
                textfield.setText(value.toString());
            } 
        }
    }
    
    public Object getValue() {
        if (isEnumeration)  {
            return enumEditor.getValue();
        } else {
            return super.getValue();
        }
    }
    
    /** 
     * Must overloade the PropertyChangeListener registration because
     * this class is the only interface to the SwingEnumEditor.
     */
    public void addPropertyChangeListener(PropertyChangeListener l)  {
        enumEditor.addPropertyChangeListener(l);
        super.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l)  {
        enumEditor.removePropertyChangeListener(l);
        super.removePropertyChangeListener(l);
    }
    
    /**
     * Initializes this property editor with the enumerated items.
     */
    public void init(PropertyDescriptor descriptor) {
        Object[] en = (Object[])descriptor.getValue("enumerationValues");
        if ( en != null ) {
            // The property descriptor describes an enumerated item.
            isEnumeration = true;
            
            enumEditor.init(descriptor);
            
        } else {
            // This is an integer item
            isEnumeration = false;
            
            if (textfield == null)  {
                textfield = new JTextField();
                textfield.setDocument(new NumberDocument());
                // XXX - Textfield should sent an actionPerformed event.
                // this was broken for 1.3 beta
                textfield.addKeyListener(new KeyAdapter()  {
                    public void keyPressed(KeyEvent evt)  {
                        if (evt.getKeyCode() == KeyEvent.VK_ENTER)  {
                            setValue(new Integer(textfield.getText()));
                        }
                    }
                });
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(textfield);
            }
        }
    }
    
    /**
     * Return the custom editor for the enumeration or the integer.
     */
    public Component getCustomEditor()  {
        if (isEnumeration)  {
            return enumEditor.getCustomEditor();
        } else {
            return super.getCustomEditor();
        }
    }
}
