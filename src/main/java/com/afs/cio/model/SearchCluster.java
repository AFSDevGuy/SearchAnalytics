package com.afs.cio.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "SearchCluster")
public class SearchCluster {
    protected String label;
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
