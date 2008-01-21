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
import java.beans.PropertyDescriptor;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * Column model for the PropertyTable
 *
 * @version 1.7 04/16/03
 * @author  Mark Davidson
 */
public class PropertyColumnModel extends DefaultTableColumnModel  {
    private final static String COL_LABEL_PROP = "Property";
    private final static String COL_LABEL_DESC = "Description";
    private final static String COL_LABEL_VALUE = "Value";
    
    private static final int minColWidth = 150;
    
    public PropertyColumnModel()  {
        // Configure the columns and add them to the model
        TableColumn column;
        
        // Property
        column = new TableColumn(0);
        column.setHeaderValue(COL_LABEL_PROP);
        column.setPreferredWidth(minColWidth);
        column.setCellRenderer(new PropertyNameRenderer());
        addColumn(column);
        
        // Value
        column = new TableColumn(1);
        column.setHeaderValue(COL_LABEL_VALUE);
        column.setPreferredWidth(minColWidth * 2);
        column.setCellEditor(new PropertyValueEditor());
        column.setCellRenderer(new PropertyValueEditor());
        addColumn(column);
    }
    
    /**
     * Renders the name of the property. Sets the short description of the
     * property as the tooltip text.
     */
    class PropertyNameRenderer extends DefaultTableCellRenderer  {
        
        /**
         * Get UI for current editor, including custom editor button
         * if applicable.
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            PropertyTableModel model = (PropertyTableModel)table.getModel();
            PropertyDescriptor desc = model.getPropertyDescriptor(row);
            
            setToolTipText(desc.getShortDescription());
            setBackground(UIManager.getColor("control"));
            
            return super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
        }
    }
    
}
