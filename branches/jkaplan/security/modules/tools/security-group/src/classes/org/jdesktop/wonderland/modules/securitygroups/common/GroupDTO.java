/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.common;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
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
public class GroupDTO {
    /* The XML marshaller and unmarshaller for later use */
    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;

    /* Create the XML marshaller and unmarshaller once for all ModuleRepositorys */
    static {
        try {
            JAXBContext jc = JAXBContext.newInstance(GroupDTO.class, MemberDTO.class);
            GroupDTO.unmarshaller = jc.createUnmarshaller();
            GroupDTO.marshaller = jc.createMarshaller();
            GroupDTO.marshaller.setProperty("jaxb.formatted.output", true);
        } catch (javax.xml.bind.JAXBException excp) {
            System.out.println(excp.toString());
        }
    }

    private String id;
    private int memberCount;
    private boolean editable;
    private Set<MemberDTO> members = new LinkedHashSet<MemberDTO>();

    public GroupDTO() {
    }

    public GroupDTO(String id) {
        this.id = id;
    }

    @XmlElement
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    @XmlElement
    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @XmlTransient
    public Set<MemberDTO> getMembers() {
        return members;
    }

    public void setMembers(Set<MemberDTO> members) {
        this.members = members;
    }

    @XmlElement
    public MemberDTO[] getMembersInternal() {
        return members.toArray(new MemberDTO[0]);
    }

    public void setMembersInternal(MemberDTO[] memberArr) {
        members = new LinkedHashSet<MemberDTO>();
        members.addAll(Arrays.asList(memberArr));
    }

    /**
     * Takes the input reader of the XML file and instantiates an instance of
     * the GroupDTO class
     * <p>
     * @param r The input stream of the version XML file
     * @throw ClassCastException If the input file does not map to GroupDTO
     * @throw JAXBException Upon error reading the XML file
     */
    public static GroupDTO decode(Reader r) throws JAXBException {
        return (GroupDTO) unmarshaller.unmarshal(r);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupDTO other = (GroupDTO) obj;
        if ((this.id == null) ? (other.id != null) :
            !this.id.equals(other.id))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }


}
