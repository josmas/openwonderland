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
package org.jdesktop.wonderland.modules.securitysession.auth.weblib;

import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The UserPluginConfigList class represents a collection of UserPluginConfig
 * classes serialized to XML.  This describes the format of the user plugin
 * configuration file.
 * 
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="user-plugin-list")
class UserPluginConfigList {
    private static final Logger logger =
            Logger.getLogger(UserPluginConfigList.class.getName());

    /* An array of plugin configs */
    private UserPluginConfig[] configs = new UserPluginConfig[0];
    
    /* The JAXBContext for later use */
    private static JAXBContext context = null;
    
    /* Create the XML marshaller and unmarshaller once for all ModuleInfos */
    static {
        try {
            context = JAXBContext.newInstance(UserPluginConfigList.class);
        } catch (javax.xml.bind.JAXBException excp) {
            logger.log(Level.WARNING, "Unable to create context", excp);
        }
    }
        
    /** Default constructor */
    public UserPluginConfigList() {
    }

    @XmlElement(name="user-plugin")
    UserPluginConfig[] getUserPluginConfigs() {
        return configs;
    }

    void setUserPluginConfigs(UserPluginConfig[] configs) {
        this.configs = configs;
    }
    
    public static UserPluginConfigList decode(Reader r) throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (UserPluginConfigList) unmarshaller.unmarshal(r);
    }
    
    public void encode(Writer w) throws JAXBException {
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(this, w);
    }

    static class UserPluginConfig {
        /** the name of the plugin class */
        private String className;

        /** the properties associated with that class */
        private Properties properties = new Properties();

        public UserPluginConfig() {}

        @XmlElement(name="class")
        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        @XmlElement
        @XmlJavaTypeAdapter(PropertiesAdapter.class)
        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }

    static class PropertiesAdapter extends XmlAdapter<Property[], Properties> {
        @Override
        public Properties unmarshal(Property[] v) throws Exception {
            Properties out = new Properties();
            for (Property p : v) {
                out.setProperty(p.key, p.value);
            }
            return out;
        }

        @Override
        public Property[] marshal(Properties v) throws Exception {
            Property[] out = new Property[v.size()];
            int c = 0;
            for (Enumeration e = v.propertyNames(); e.hasMoreElements();) {
                Property prop = new Property();
                prop.key = (String) e.nextElement();
                prop.value = v.getProperty(prop.key);
                out[c++] = prop;
            }

            return out;
        }
    }

    static class Property {
        @XmlAttribute
        String key;
        @XmlElement
        String value;
    }
}
