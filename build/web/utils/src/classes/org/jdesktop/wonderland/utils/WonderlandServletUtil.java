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
package org.jdesktop.wonderland.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;

/**
 * Common utilities used by servlets
 * @author jkaplan
 */
public class WonderlandServletUtil {
    /** pattern for substitution */
    private static final Pattern subst = Pattern.compile("\\$\\{.+?\\}");
            
    /**
     * Read a property with null as the default value.  
     * @see getProperty(String, ServletContext, String)
     */
    public static String getProperty(final String prop,
                                     final ServletContext context)
    {
        return getProperty(prop, context, null);
    }
    
    /**
     * Read a property in a servlet.  The properties are read in the
     * following order: 
     * <ul><li>System.getProperty()
     *     <li>ServletContext.getInitParameter()
     *     <li>The supplied default
     * </ul>
     * <p>
     * Further, for any non-null return value, substitution will be 
     * peformed as follows:
     * <p>
     * For an occurence of ${property}, the value of property will be
     * substituted using a call to this method, using the same rules
     * as above.
     * 
     * @param prop the name of the property
     * @param context the context to check for the property in
     * @param defVal the default value of the property
     * @return the value of the given property, or the supplied default if no
     * value can be found for the given property
     */
    public static String getProperty(final String prop,
                                     final ServletContext context,
                                     final String defVal) 
    {
        // first try a system property
        String out = System.getProperty(prop);

        // next try a servlet config parameter
        if (out == null) {
            out = context.getInitParameter(prop);
        }

        // finally, try the default value
        if (out == null) {
            out = defVal;
        }
        
        // substitute
        if (out != null) {
            out = substitute(out, context);
        }
        
        // return what we found
        return out;
    }

    /**
     * Substitute patterns.  Searches for the pattern "${value}" in the text,
     * and replaces "..." with the value of a call to 
     * <code>getProperty(value)</code>.  If the value is not found, the 
     * original pattern is left unmodified.
     * <p>
     * Substitutions can also specify a default value, in the form 
     * "${value:default}".  When a default is specified, if value is not
     * found the default value will be returned instead of the original
     * pattern.
     * 
     * @param str the string to subsitute in
     * @param context the ServletContext used for calls to
     * <code>getProperty</code>
     * @return the string after substitution
     */
    public static String substitute(String str, ServletContext context) {
        Matcher m = subst.matcher(str);
        StringBuffer buf = new StringBuffer();
        
        while (m.find()) {            
            String expr = str.substring(m.start() + 2, m.end() - 1);             
            
            // see if there is a default value
            String defVal = null;
            if (expr.contains(":")) {
                String[] vals = expr.split(":");
                if (vals.length != 2) {
                    throw new IllegalArgumentException("Format must be " +
                                "${value:default} found " + expr); 
                }
                
                expr = vals[0];
                defVal = vals[1];
            }
            
            // resolve the result as a system property
            String res = getProperty(expr, context, defVal);
            if (res == null && defVal != null) {
                // use the default
                res = defVal;
            } else if (res == null) {
                // add back the original text
                res = "${" + expr + "}";
            }
            
            // replace '\' with '\\' and '$' with '\$' to fix substitution 
            // problems
            res = res.replace("\\", "\\\\");
            res = res.replace("$", "\\$");
            
            m.appendReplacement(buf, res);
        }
        m.appendTail(buf);
        
        return buf.toString();
    }
}
