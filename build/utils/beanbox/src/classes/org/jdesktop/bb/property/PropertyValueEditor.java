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

package org.jdesktop.bb.property;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.util.Hashtable;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.bb.editors.SwingEditorSupport;

/**
 * An editor for types which have a property editor.
 * Note: Should consolidate with the PropertyValueRenderer
 */
public class PropertyValueEditor extends AbstractCellEditor 
	implements TableCellEditor, TableCellRenderer, PropertyChangeListener {
    
    private PropertyEditor editor;
    private DefaultCellEditor cellEditor;
    private Class type;
    
    private Border selectedBorder;
    private Border emptyBorder;
    
    private Hashtable editors;
    
    public PropertyValueEditor()  {
        editors = new Hashtable();
        cellEditor = new DefaultCellEditor(new JTextField());
    }
    
    /**
     * Get the renderer component
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, 
            boolean hasFocus, 
            int row, int column) {
        return getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    
    /**
     * Get UI for current editor, including custom editor button
     * if applicable.
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        
        PropertyTableModel model = (PropertyTableModel)table.getModel();
        type = model.getPropertyType(row);
        
        if (type != null)  {
            editor = (PropertyEditor)editors.get(type);
            if (editor == null)  {
                PropertyEditor ed = model.getPropertyEditor(row);
                
                // Make a copy of this prop editor and register this as a
                // prop change listener.
                // We have to do this since we want a unique PropertyEditor
                // instance to be used for an editor vs. a renderer.
                if (ed != null)  {
                    Class editorClass = ed.getClass();
                    try {
                        editor = (PropertyEditor)editorClass.newInstance();
                        editor.addPropertyChangeListener(this);
                        editors.put(type, editor);
                        
                    } catch (Exception ex) {
                        System.out.println("Couldn't instantiate type editor \"" +
                                editorClass.getName() + "\" : " + ex);
                    }
                }
            }
        } else {
            editor = null;
        }
        
        if (editor != null)  {
            // Special case for the enumerated properties. Must reinitialize
            // to reset the combo box values.
            if (editor instanceof SwingEditorSupport)  {
                ((SwingEditorSupport)editor).init(model.getPropertyDescriptor(row));
            }
            
            editor.setValue(value);
            
            Component comp = editor.getCustomEditor();
            if (comp != null)  {
                comp.setEnabled(isSelected);
                
                if (comp instanceof JComponent)  {
                    if (isSelected)  {
                        if (selectedBorder == null)
                            selectedBorder = BorderFactory.createLineBorder(table.getSelectionBackground(), 1);
                        
                        ((JComponent)comp).setBorder(selectedBorder);
                    } else {
                        if (emptyBorder == null)
                            emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
                        
                        ((JComponent)comp).setBorder(emptyBorder);
                    }
                }
                return comp;
            }
        }
        return cellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    /**
     * Get cellEditorValue for current editor
     */
    public Object getCellEditorValue() {
        Object obj = null;
        
        if (editor != null)  {
            obj = editor.getValue();
        } else {
            obj = cellEditor.getCellEditorValue();
        }
        
        if (type != null && obj != null && 
                !type.isPrimitive() && !type.isAssignableFrom(obj.getClass()))  {
            try {
                obj = type.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return obj;
    }
    
    //
    // Property Change handler.
    // 
    
    public void propertyChange(PropertyChangeEvent evt)  {
        stopCellEditing();
    }
}
