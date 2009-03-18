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
package org.jdesktop.wonderland.modules.securitygroups.common;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author jkaplan
 */
@XmlRootElement
public class GroupsDTO {
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;

    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(GroupsDTO.class, GroupDTO.class, MemberDTO.class);
            GroupsDTO.unmarshaller = jc.createUnmarshaller();
            GroupsDTO.marshaller = jc.createMarshaller();
            GroupsDTO.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }

    private Set<GroupDTO> groups = new TreeSet<GroupDTO>(new GroupComparator());

    public GroupsDTO() {
    }

    public GroupsDTO(Collection<GroupDTO> groups) {
        this.groups.addAll(groups);
    }

    @XmlTransient
    public Set<GroupDTO> getGroups() {
        return groups;
    }

    public void setGroups(Set<GroupDTO> groups) {
        this.groups = groups;
    }

    @XmlElement
    public GroupDTO[] getGroupsInternal() {
        return groups.toArray(new GroupDTO[0]);
    }

    public void setGroupsInternal(GroupDTO[] groupArr) {
        groups = new TreeSet<GroupDTO>(new GroupComparator());
        groups.addAll(Arrays.asList(groupArr));
    }

    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the GroupDTO class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to GroupDTO
     * @throw JAXBException Upon error reading the XML file
     */
    public static GroupsDTO decode(Reader r) throws JAXBException {
        return (GroupsDTO) unmarshaller.unmarshal(r);
    }

    /**
     * Writes the GroupDTO class to an output writer.
     * <p>
     * @param w The output writer to write to
     * @throw JAXBException Upon error writing the XML file
     */
    public void encode(Writer w) throws JAXBException {
        marshaller.marshal(this, w);
    }

    class GroupComparator implements Comparator<GroupDTO> {
        public int compare(GroupDTO o1, GroupDTO o2) {
            return o1.getId().compareToIgnoreCase(o2.getId());
        }
    }
}
