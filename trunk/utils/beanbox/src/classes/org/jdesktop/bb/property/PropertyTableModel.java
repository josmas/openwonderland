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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.bb.util.BeanInfoFactory;
import org.jdesktop.bb.util.DescriptorComparator;

/**
 * Table model used to obtain property names and values. This model encapsulates an array 
 * of PropertyDescriptors.
 *
 * @version 1.19 03/05/04
 * @author  Mark Davidson
 */
public class PropertyTableModel extends AbstractTableModel {
    
    private PropertyDescriptor[] descriptors;
    private BeanDescriptor beanDescriptor;
    private BeanInfo info;
    private Object bean;
    private boolean editable = true;
    
    // Cached property editors.
    private static Hashtable propEditors;
    
    // Shared instance of a comparator
    private static DescriptorComparator comparator = new DescriptorComparator();
    
    private static final int NUM_COLUMNS = 2;
    
    public static final int COL_NAME = 0;
    public static final int COL_VALUE = 1;
    
    // Filter options
    public static final int VIEW_ALL       = 0;
    public static final int VIEW_STANDARD  = 1;
    public static final int VIEW_EXPERT    = 2;
    public static final int VIEW_READ_ONLY = 3;
    public static final int VIEW_BOUND     = 4;
    public static final int VIEW_CONSTRAINED = 5;
    public static final int VIEW_HIDDEN    = 6;
    public static final int VIEW_PREFERRED = 7;
    
    private int currentFilter = VIEW_STANDARD;
    
    // Sort options
    public static final int SORT_DEF   = 0;
    public static final int SORT_NAME  = 1;
    public static final int SORT_TYPE  = 2;
    
    private int sortOrder = SORT_NAME;
    
    public PropertyTableModel()  {
        
        if (propEditors == null) {
            propEditors = new Hashtable();
            registerPropertyEditors();
        }
    }
    
    public PropertyTableModel(Object bean)  {
        this();
        setObject(bean);
    }
    
    /**
     * Sets the current filter of the Properties.
     *
     * @param filter one of VIEW_ constants
     */
    public void setFilter(int filter) {
        filterTable(filter);
    }
    
    
    /** 
     * Returns the current filter type
     */
    public int getFilter() {
        return currentFilter;
    }
    
    /**
     * Sets the current sort order on the data
     * @param sort one of the SORT_ constants
     */
    public void setSortOrder(int sort) {
        this.sortOrder = sort;
        sortTable(sortOrder);
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    /** 
     * Set the table model to represents the properties of the object.
     */
    public void setObject(Object bean)  {
        this.bean = bean;
        
        info = BeanInfoFactory.getBeanInfo(bean.getClass());
        
        if (info != null)  {
            beanDescriptor = info.getBeanDescriptor();
            filterTable(getFilter());
        }
    }
    
    /** 
     * Return the current object that is represented by this model.
     */
    public Object getObject()  {
        return bean;
    }
    
    /**
     * Get row count (total number of properties shown)
     */
    public int getRowCount() {
        if (descriptors == null)  {
            return 0;    
        }
        return descriptors.length;
    }
    
    /**
     * Get column count (2: name, value)
     */
    public int getColumnCount() {
        return NUM_COLUMNS;
    }
    
    /**
     * Check if given cell is editable
     * @param row table row
     * @param col table column
     */
    public boolean isCellEditable(int row, int col) {
        if (editable && (col == COL_VALUE) && (descriptors != null)) {
            return (descriptors[row].getWriteMethod() == null) ? false : true;
        }

	return false;
    }
    
    /**
     * Get text value for cell of table
     * TODO: Try to handle the exceptions better.
     * 
     * @param row table row
     * @param col table column
     */
    public Object getValueAt(int row, int col) {
        
        Object value = null;
        
        if (col == COL_NAME)  {
            value = descriptors[row].getDisplayName();
        } else {
            // COL_VALUE is handled        
            Method getter = descriptors[row].getReadMethod();
            
            if (getter != null)  {
                Class[] paramTypes = getter.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                try {
                    for (int i = 0; i < paramTypes.length; i++) {
                        Logger.getAnonymousLogger().warning("\tShouldn't happen! getValueAt getter = " 
                                + getter + " parameter = " + paramTypes[i]);
                        args[i] = paramTypes[i].newInstance();
                    }    
                    value = getter.invoke(bean, args);
                } catch (NoSuchMethodError e) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("Bean: ").append(bean.toString());
                    buffer.append("Getter: ").append(getter.getName());
                    buffer.append("Getter args: ");
                    for (int i = 0; i < args.length; i++) {
                        buffer.append("\t" + "type: " + paramTypes[i] + " value: " + args[i]);
                    }
                    Logger.getAnonymousLogger().log(Level.WARNING, buffer.toString(), e);
                } catch (Exception ex) {
                    Logger.getAnonymousLogger().log(Level.WARNING, "Exception getting value", ex);
                }
            }
            
        }
        return value;
    }
    
    /** 
     * Set the value of the Values column.
     */
    public void setValueAt(Object value, int row, int column)  {
        
        if (column != COL_VALUE || descriptors == null 
                || row > descriptors.length)  {
            return;
        }
        
        Method setter = descriptors[row].getWriteMethod();
        if (setter != null)  {
            try {
                setter.invoke(bean, new Object[] { value });
            } catch (Exception ex) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("Setter: ").append(setter);
                buffer.append("\nArgument: ").append(value.getClass().toString());
                buffer.append("Row: ").append(row);
                buffer.append(" Column: ").append(column);

                Logger.getAnonymousLogger().log(Level.SEVERE, buffer.toString(), ex);
            }
        }
    }
    
    /** 
     * Returns the Java type info for the property at the given row.
     */
    public Class getPropertyType(int row)  {
        return descriptors[row].getPropertyType();
    }
    
    /** 
     * Returns the PropertyDescriptor for the row.
     */
    public PropertyDescriptor getPropertyDescriptor(int row)  {
        return descriptors[row];
    }
    
    /** 
     * Returns a new instance of the property editor for a given class. If an
     * editor is not specified in the property descriptor then it is looked up
     * in the PropertyEditorManager.
     */
    public PropertyEditor getPropertyEditor(int row)  {
        Class cls = descriptors[row].getPropertyEditorClass();
        
        PropertyEditor editor = null;
        
        if (cls != null)  {
            try {
                editor = (PropertyEditor)cls.newInstance();
            } catch (Exception ex) {
                Logger.getAnonymousLogger().log(Level.WARNING,
                "PropertyTableModel: Instantiation exception creating PropertyEditor", ex);
            }
        } else {
            // Look for a registered editor for this type.
            Class type = getPropertyType(row);
            if (type != null)  {
                editor = (PropertyEditor)propEditors.get(type);
                
                if (editor == null)  {
                    // Load a shared instance of the property editor.
                    editor = PropertyEditorManager.findEditor(type);
                    if (editor != null)
                        propEditors.put(type, editor);
                }
                
                if (editor == null)  {
                    // Use the editor for Object.class
                    editor = (PropertyEditor)propEditors.get(Object.class);
                    if (editor == null)  {
                        editor = PropertyEditorManager.findEditor(Object.class);
                        if (editor != null)
                            propEditors.put(Object.class, editor);
                    }
                    
                }
            }
        }
        return editor;
    }
    
    /** 
     * Returns a flag indicating if the encapsulated object has a customizer.
     */
    public boolean hasCustomizer()  {
        if (beanDescriptor != null)  {
            Class cls = beanDescriptor.getCustomizerClass();
            return (cls != null);
        }
        
        return false;
    }
    
    /** 
     * Gets the customizer for the current object.
     * @return New instance of the customizer or null if there isn't a customizer.
     */
    public Component getCustomizer()  {
        Component customizer = null;
        
        if (beanDescriptor != null)  {
            Class cls = beanDescriptor.getCustomizerClass();
            
            if (cls != null)  {
                try {
                    customizer = (Component)cls.newInstance();
                } catch (Exception ex) {
                    Logger.getAnonymousLogger().log(Level.WARNING,
                            "PropertyTableModel: Instantiation exception creating Customizer", ex);
                }
            }
        }
        
        return customizer;
    }
    
    /** 
     * Sorts the table according to the sort type.
     * 
     */
    public void sortTable(int sort)  {
        if (sort == SORT_DEF || descriptors == null)
            return;
        
        if (sort == SORT_NAME)  {
            Arrays.sort(descriptors, comparator);
        } else {
            Arrays.sort(descriptors, comparator);
        }
        fireTableDataChanged();
    }

    /**
     * Enable/Disable cell editing...
     */
    public void setEditable(boolean editable) {
	this.editable = editable;
    }
    
    /** 
     * Filters the table to display only properties with specific attributes.
     * Will sort the table after the data has been filtered.
     *
     * @param view The properties to display.
     */
    private void filterTable(int view)  {
        if (info == null)
            return;
        
        this.currentFilter = view;
        
        List list = null;
        
        descriptors = info.getPropertyDescriptors();
        switch (view) {
        // FeatureDescriptor types
        case VIEW_EXPERT:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].isExpert()) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
        case VIEW_PREFERRED:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].isPreferred()) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
        case VIEW_HIDDEN:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].isHidden()) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
            // PropertyDesctiptor types
            
        case VIEW_BOUND:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].isBound() && descriptors[i].getWriteMethod() != null) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
        case VIEW_CONSTRAINED:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].isConstrained() && descriptors[i].getWriteMethod() != null) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
            // Extended types
        case VIEW_READ_ONLY:
            list  = new ArrayList();
            for (int i = 0; i < descriptors.length; i++) {
                if (descriptors[i].getWriteMethod() == null) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
        case VIEW_STANDARD:
            list  = new ArrayList();
            PropertyDescriptor desc;
            for (int i = 0; i < descriptors.length; i++) {
                desc = descriptors[i];
                if (desc.getWriteMethod() != null && desc.getReadMethod() != null &&
                        !desc.isHidden() && !desc.isExpert()) {
                    list.add(descriptors[i]);
                }
            }
            break;
            
        default:
        case VIEW_ALL:
            break;
            
        }
        if (list != null) {
            descriptors = (PropertyDescriptor[])list.toArray(new PropertyDescriptor[list.size()]);
        }
        sortTable(getSortOrder());
    }
    
    /** 
     * Method which registers property editors for types.
     */
    private static void registerPropertyEditors()  {
        PropertyEditorManager.registerEditor(Color.class, 
                org.jdesktop.bb.editors.SwingColorEditor.class);
        PropertyEditorManager.registerEditor(Font.class, 
                org.jdesktop.bb.editors.SwingFontEditor.class);
        PropertyEditorManager.registerEditor(Border.class, 
                org.jdesktop.bb.editors.SwingBorderEditor.class);
        PropertyEditorManager.registerEditor(Boolean.class, 
                org.jdesktop.bb.editors.SwingBooleanEditor.class);
        PropertyEditorManager.registerEditor(boolean.class, 
                org.jdesktop.bb.editors.SwingBooleanEditor.class);
        PropertyEditorManager.registerEditor(Integer.class, 
                org.jdesktop.bb.editors.SwingIntegerEditor.class);
        PropertyEditorManager.registerEditor(int.class, 
                org.jdesktop.bb.editors.SwingIntegerEditor.class);
        PropertyEditorManager.registerEditor(Float.class, 
                org.jdesktop.bb.editors.SwingNumberEditor.class);
        PropertyEditorManager.registerEditor(float.class, 
                org.jdesktop.bb.editors.SwingNumberEditor.class);
        PropertyEditorManager.registerEditor(java.awt.Dimension.class,
                org.jdesktop.bb.editors.SwingDimensionEditor.class);
        PropertyEditorManager.registerEditor(java.awt.Point.class,
                org.jdesktop.bb.editors.SwingPointEditor.class);
        PropertyEditorManager.registerEditor(java.awt.Rectangle.class, 
                org.jdesktop.bb.editors.SwingRectangleEditor.class);
        PropertyEditorManager.registerEditor(java.awt.Insets.class, 
                org.jdesktop.bb.editors.SwingInsetsEditor.class);
        PropertyEditorManager.registerEditor(String.class, 
                org.jdesktop.bb.editors.SwingStringEditor.class);
        PropertyEditorManager.registerEditor(java.net.URL.class, 
                org.jdesktop.bb.editors.URLEditor.class);
        PropertyEditorManager.registerEditor(Object.class, 
                org.jdesktop.bb.editors.SwingObjectEditor.class);
    }
}
