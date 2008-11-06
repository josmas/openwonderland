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
package org.jdesktop.wonderland.common.config;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Represents Wonderland configuration options common to the entire system (server/client)
 */
public class WonderlandConfigUtil implements Serializable {
    
    public static Logger logger = Logger.getLogger("wonderland.config");

    private static String usernameDir = "";

    public static String getSystemConfigDir() {
        return System.getProperty("wonderland.config.system-dir");
    }

    public static void setUsername(String username) {
        usernameDir = File.separatorChar+username;
    }
    
    /**
     * Get the root directory in the user account where all wonderland data
     * will be stored
     */
    public static String getWonderlandDir() {
        String homeDir = System.getProperty("user.home");
        if (homeDir!=null) {
            homeDir = homeDir.concat(File.separator);
        }

        if (System.getProperty("wonderland.benchmark")!=null) {
            return homeDir + ".wonderland-"+System.getProperty("wonderland.benchmark");
        }       
            
        if (System.getProperty("wonderland.user.dir")!=null) {
            return System.getProperty("wonderland.user.dir");
        }
        
        return homeDir + ".wonderland"+File.separatorChar+"0.5-dev"+usernameDir;
    }
    
    public static String getUserConfigDir() {
        return getWonderlandDir() + File.separator + "config" + File.separator;
    }
    
    public static String getConfigBaseName(Class configClass) {
        String pkgPrefix = configClass.getPackage().getName() + ".";
        String className = configClass.getName();
        
        if (className.startsWith(pkgPrefix))
            className = className.substring(pkgPrefix.length());
        
        return className;
    }
    
    public static String getConfigBaseFileName(Class configClass) {
        return getConfigBaseName(configClass) + ".xml";
    }
    
    public static String getSystemConfigFileName(Class configClass) {
        String sysDir = getSystemConfigDir();
        
        if (sysDir != null)
            return sysDir + File.separator + getConfigBaseFileName(configClass);
        
        return null;
    }
    
    public static String getUserConfigFileName(Class configClass) {
        return getWonderlandDir() + File.separator+"config"+File.separator + getConfigBaseFileName(configClass);
    }
    
    public static String getConfigFileName(Object obj, boolean userMode) {
	if (obj == null)
	    return null;

	return userMode ? getUserConfigFileName(obj.getClass()) :
			  getSystemConfigFileName(obj.getClass());
    }
    
    public static InputStream getInputStream(String configFileName) {
        InputStream in = null;
        URL url = null;
        
        if (configFileName != null) {
            try {
                url = new URL(configFileName);
            } catch(java.net.MalformedURLException me) {
                url = null;
            }
            
            try {
                if (url == null) {
                    in = new FileInputStream(new File(configFileName));
                    in = new BufferedInputStream(in);
                } else {
		    URLConnection connection = url.openConnection();
		    in = connection.getInputStream();
		    in = new BufferedInputStream(in);
                }
                
                logger.info("Loading wonderland configuration file: " + configFileName);
            } catch (Exception ee) {
                logger.log(Level.WARNING, "Error loading config file "+configFileName+" : "+ee.getMessage());
            }
        }
                
        return in;
    }
    
    public static <T> T readSystemConfig(Class<T> configClass) {
	InputStream in = getInputStream(getSystemConfigFileName(configClass));        
        try {
            if (in == null) {
                T obj = configClass.newInstance();

		if (obj instanceof WonderlandConfigBase)
		    ((WonderlandConfigBase)obj).init();
		
                return obj;
	    }
            
            XMLDecoder reader = new XMLDecoder(in);
            T object = (T)reader.readObject();
            reader.close();
            
            return object;
        } catch (Exception e) {
            logger.warning("Error reading wonderland configuration file!");
        }
        
        return null;
    }

    public static <T> T readUserConfig(Class<T> configClass) {
	InputStream in = getInputStream(getUserConfigFileName(configClass));
        
        if (in == null)
            return null;
        
        try {
            XMLDecoder reader = new XMLDecoder(in);
            T object = (T)reader.readObject();
            reader.close();
            
            return object;
        } catch (Exception e) {
            logger.warning("Error reading wonderland configuration file!");
        }
        
        return null;
    }

    private static HashMap<Class, Object> userConfigMap = new HashMap<Class, Object>();
    private static HashMap<Class, Object> systemConfigMap = new HashMap<Class, Object>();

    public synchronized static <T> T getUserDefault(Class<T> configClass) {
	T object = (T)userConfigMap.get(configClass);

	if (object == null) {
	    if ((object = readUserConfig(configClass)) == null)
		object = getSystemDefault(configClass);

	    userConfigMap.put(configClass, object);
	}

	return object;
    }
    
    public synchronized static <T> T getSystemDefault(Class<T> configClass) {
	T object = (T)systemConfigMap.get(configClass);

	if (object == null) {
	    object = readSystemConfig(configClass);
	    systemConfigMap.put(configClass, object);
	}

	return object;
    }
    
    public synchronized static <T> T getDefault(Class<T> configClass) {
	return getUserDefault(configClass);
    }

    public static OutputStream getOutputStream(String configFileName) {
        OutputStream out = null;
        
        if (configFileName != null) {
            try {
		File f = new File(configFileName);
		(new File(f.getParent())).mkdirs();

                out = new FileOutputStream(f);
		out = new BufferedOutputStream(out);
            } catch (Exception e) {
		try {
		    URL url = new URL(configFileName);
		    URLConnection connection = url.openConnection();
		    connection.setDoOutput(true);
		    out = connection.getOutputStream();
		    out = new BufferedOutputStream(out);
		    
		} catch (Exception ee) {
		}
	    }
        }
        
        return out;
    }
    
    public static boolean writeConfig(OutputStream out, Object object) {
	if (out == null)
	    return false;

	try {
	    XMLEncoder writer = new XMLEncoder(out);
	    writer.writeObject(object);
	    writer.close();
	    return true;
	} catch (Exception e) {
	}

	logger.warning("Error writing wonderland configuration file!");
	return false;
    }

    public static boolean writeSystemConfig(Object object) {
	String configFileName = getSystemConfigFileName(object.getClass());
	OutputStream out = getOutputStream(configFileName);

        if (out != null) {
            logger.info("Writing wonderland SYSTEM configuration file: " + configFileName);
	    return writeConfig(out, object);
	}

	logger.warning("Could not write wonderland SYSTEM configuration file: " + configFileName);
	return false;
    }

    public static boolean writeUserConfig(Object object) {
	String configFileName = getUserConfigFileName(object.getClass());
	OutputStream out = getOutputStream(configFileName);

        if (out != null) {
            logger.info("Writing wonderland USER configuration file: " + configFileName);
	    return writeConfig(out, object);
	}

	logger.warning("Could not write wonderland USER configuration file: " + configFileName);
	return false;
    }

    private WonderlandConfigUtil() {}
}
