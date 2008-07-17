/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.wfs;

import com.sun.sgs.app.ManagedReference;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.StaticModelCellMO;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.server.setup.CellMOSetup;

/**
 * Class to write cell hierarchies to a WFS directory
 * @author jkaplan
 */
public class WFSWriter {
    private CellMO root;
    private File    rootDir;
    private int     rootCount = 0;
    
    public static void write(CellMO root, File directory) 
        throws IOException
    {
        new WFSWriter(root, directory).write();
    }
    
    public WFSWriter(CellMO root, File rootDir) {
        this.root = root;
        this.rootDir = rootDir;
    }

    public void write() throws IOException {
        writeCell(root, rootDir, 0);
    }
    
    protected void writeCell(CellMO cell, File directory, int count) 
        throws IOException 
    {    
        System.out.println("Writing " + cell + " to " + directory);

        // get our name
        String cellName = getCellName(cell, count);

        // write ourself
        if (cell instanceof BeanSetupMO) {
            CellMOSetup setup = ((BeanSetupMO) cell).getCellMOSetup();

            if (!(cell instanceof StaticModelCellMO)) {
                // origin of cells is currently relative to 50,0,50 -- make sure
                // the origin is relative to 0,0,0
//                BasicCellMOSetup bcgs = (BasicCellMOSetup) setup;
//                double[] origin = bcgs.getOrigin();
//                origin[0] -= 50;
//                origin[2] -= 50;
//                bcgs.setOrigin(origin);
//                
//                // overwrite 0 rotations with the default so it won't be
//                // included in the files
//                if (bcgs.getRotation() != null && bcgs.getRotation()[3] == 0) {
//                    bcgs.setRotation(new double[] { 0, 1, 0, 0 });
//                }
            }
            
//            if (cell instanceof AudioCellGLO) {
//                AudioCellGLOSetup acgs = (AudioCellGLOSetup) setup;
//                acgs.setAudioPosition(null);
//            }
            
            File outFile = new File(directory, cellName + "-wlc.xml");
            if (outFile.exists()) { 
                cellName = cellName + "-" + count;
                outFile = new File(directory, cellName + "-wlc.xml");
            }
            
            System.out.println("Attempt to write " + setup + " to " + outFile);

            FileOutputStream out = new FileOutputStream(outFile);
            XMLEncoder encoder = new XMLEncoder(out) {
                String[] ignoreMethods = {
                    "setBaseURL", "setChecksum", "setChecksums"
                };
                
                @Override
                public void writeExpression(Expression arg0) {
                    if (shouldWrite(arg0)) {
                        super.writeExpression(arg0);
                    }
                }

                @Override
                public void writeStatement(Statement arg0) {
                    if (shouldWrite(arg0)) {
                        super.writeStatement(arg0);
                    }
                }
                
                private boolean shouldWrite(Statement statement) {
                    for (int i = 0; i < ignoreMethods.length; i++) {
                        if (ignoreMethods[i].equals(statement.getMethodName())) {
                            return false;
                        }
                    }
                    
                    return true;
                }
                
            };
            encoder.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception exception) {
                    exception.printStackTrace();
                }
            });
            encoder.writeObject(setup);
            encoder.close();
        }
        
        // write children
        Collection<ManagedReference<CellMO>> children = cell.getAllChildrenRefs();
        if (!children.isEmpty()) {
            int childCount = 0;
            
            // create a directory
            File childDir = new File(directory, cellName + "-wld");
            childDir.mkdir();
            
            for (ManagedReference<CellMO> ref : children) {
                // write each child
                writeCell(ref.get(), childDir, childCount++);
            }
        }
    }

    protected String getCellName(CellMO cell, int count) {
        // if this is a model cell, get the model name
//        if (cell instanceof AnimatedCellGLO) {
//            AnimatedCellSetup setup = ((AnimatedCellGLO) cell).getClientSetupData();
//            
//            if (setup.getModelFiles() != null && 
//                setup.getModelFiles().length > 0) 
//            {
//                String name = setup.getModelFiles()[0];
//                if (name != null) {
//                    if (name.contains(".")) {
//                        name = name.substring(0, name.lastIndexOf("."));
//                    }
//                    if (name.contains("/")) {
//                        name = name.substring(name.lastIndexOf("/"));
//                    }
//
//                    return name;
//                }
//            }
//        }


        // no luck, return the default
        return String.valueOf(count);
    }
}
