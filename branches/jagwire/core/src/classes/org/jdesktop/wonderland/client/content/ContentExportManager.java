/**
 * Open Wonderland
 *
 * Copyright (c) 2011 - 2012, Open Wonderland Foundation, All Rights Reserved
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
package org.jdesktop.wonderland.client.content;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.content.spi.ContentExporterSPI;

/**
 * Manages the collection export handlers for different kinds of cells. Each
 * cell is given by the cell class, as returned by the class that implements
 * the ContentExporterSPI. 
 * 
 * This class is heavily borrowed from ContentImportManager.java by Jordan Slott
 *
 * @author JagWire
 */
public enum ContentExportManager {
    INSTANCE;
    
    // set of known export handlers
    private SortedSet<ExportClassRecord> records = 
            new TreeSet<ExportClassRecord>();
    
    
    public synchronized void registerContentExporter(ContentExporterSPI exporter) {
        // add a new record for every class this exporter exports
        for (Class<?> exportClass : exporter.getCellClasses()) {
            records.add(new ExportClassRecord((Class<? extends Cell>) exportClass, exporter));
        }
    }
    
    public synchronized void unregisterContentExporter(ContentExporterSPI exporter) {
        // remove any records with this as the exporter
        for (Iterator<ExportClassRecord> i = records.iterator(); i.hasNext();) {
            ExportClassRecord record = i.next();
            if (record.getExporter().equals(exporter)) {
                i.remove();
            }
        }
    }
    
    /**
     * Find the most-specific exporter for the given class.
     */
    public synchronized ContentExporterSPI getContentExporter(Class clazz) {
        if(clazz == null) {
            return null;
        }
        
        // go through the records in order. The first match will be the
        // most specific
        for (ExportClassRecord record : records) {
            if (record.getCellClass().isAssignableFrom(clazz)) {
                return record.getExporter();
            }
        }
        
        // no exporter found
        return null;
    }
    
    private static class ExportClassRecord 
        implements Comparable<ExportClassRecord> 
    {
        private final Class<? extends Cell> clazz;
        private final ContentExporterSPI exporter;
        private final Integer sortOrder;
        
        public ExportClassRecord(Class<? extends Cell> clazz,
                                 ContentExporterSPI exporter)
        {
            this.clazz = clazz;
            this.exporter = exporter;
            
            // sort order is based on the number of calls required to
            // getSuperclass() before we reach Cell.class. This ensures
            // sorting is done from most-specific to least-specific match.
            int calls = 0;
            Class<?> parent = clazz;
            while (!parent.equals(Cell.class)) {
                calls++;
                parent = parent.getSuperclass();
            }
            
            this.sortOrder = calls;
        }
        
        public Class<? extends Cell> getCellClass() {
            return clazz;
        }
        
        public ContentExporterSPI getExporter() {
            return exporter;
        }

        protected Integer getSortOrder() {
            return sortOrder;
        }
        
        public int compareTo(ExportClassRecord o) {
            // reverse search
            return o.getSortOrder().compareTo(getSortOrder());
        }        
    }
}
