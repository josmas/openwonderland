/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.common;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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

    private Set<GroupDTO> groups = new LinkedHashSet<GroupDTO>();

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
        groups = new LinkedHashSet<GroupDTO>();
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
}
