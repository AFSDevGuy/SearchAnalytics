package com.afs.cio.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Container for query clusters (groups). Used primarily for XML serialization.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "SearchCluster")
public class SearchCluster {

    /**
     * Cluster name
     */
    protected String label;

    /**
     * Cluster members
     */
    protected List<RawLogInput> members;


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<RawLogInput> getMembers() {
        return members;
    }

    public void setMembers(List<RawLogInput> members) {
        this.members = members;
    }
}
