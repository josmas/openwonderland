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

package org.jdesktop.wonderland.common.cell.setup.spi;

/**
 * The CellSetupSPI service provider interface returns fully-qualified class
 * names that represent some sort of JAXB annotated object. This is used for
 * the cell setup classes, so that a JAXBContext class may be created with all
 * of the necessary fully-qualified class names for parsing.
 * <p>
 * This SPI has no methods -- the class name may be obtained from the Java VM
 * which is sufficient to create the JAXBContext class.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellSetupSPI {
}
