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
package org.jdesktop.wonderland.common.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

/**
 * A classloader that scans its contents for annotations. It can then be
 * queried by annotation type for all classes implementing the given annotation.
 * @author jkaplan
 */
public class ScannedClassLoader extends URLClassLoader {
    private static final Logger logger =
            Logger.getLogger(ScannedClassLoader.class.getName());

    /** the database of annotations */
    private AnnotationDB annotationDB;

    /**
     * Create a scannned classloader that scans the contents of
     * the system classloader.
     */
    public ScannedClassLoader() {
        super (new URL[0]);

        createDB(ClasspathUrlFinder.findClassPaths());
    }

    /**
     * Create a scanned classloader that scans the given urls.
     * @param urls the urls to scan
     * @param parent the parent classlaoder to delegate to
     */

    public ScannedClassLoader(URL[] urls, ClassLoader parent) {
        super (urls, parent);

        createDB(urls);
    }

    /**
     * Get the name of all classes that are annotated with the given
     * annotation.
     * @param annotation the annotation to search for
     */
    public Set<String> getClasses(Class<? extends Annotation> clazz) {
        String name = clazz.getName();

        // search the index for the given annotation
        Set<String> out = annotationDB.getAnnotationIndex().get(name);
        if (out == null) {
            out = Collections.emptySet();
        }

        return out;
    }

    @Override
    protected void addURL(URL url) {
        super.addURL(url);

        try {
            annotationDB.scanArchives(getURLs());
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error rescanning " + this +
                                     " for annotations", ioe);
        }
    }

    /**
     * Create the annotation database from the given URLs.
     * @param urls the urls to use
     */
    protected synchronized void createDB(URL[] urls) {
        try {
            long start = System.currentTimeMillis();

            annotationDB = new AnnotationDB();
            annotationDB.scanArchives(urls);

            long time = System.currentTimeMillis() - start;
            logger.warning("Scanned classes in " + time + " ms.");
        } catch (IOException ioe) {
            logger.log(Level.WARNING, "Error scanning " +
                       Arrays.toString(urls) + " for annotation", ioe);
        }
    }
}
