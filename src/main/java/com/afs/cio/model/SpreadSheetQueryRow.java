package com.afs.cio.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Row")
public class SpreadSheetQueryRow {

    protected String clusterLabel;
    protected String queryTerm;
    protected String username;

    public SpreadSheetQueryRow() {

    }

    public SpreadSheetQueryRow(String clusterLabel, String queryTerm, String username) {
        this.clusterLabel = clusterLabel;
        this.queryTerm = queryTerm;
        this.username = username;
    }

    public String getClusterLabel() {
        return clusterLabel;
    }

    public void setClusterLabel(String clusterLabel) {
        this.clusterLabel = clusterLabel;
    }

    public String getQueryTerm() {
        return queryTerm;
    }

    public void setQueryTerm(String queryTerm) {
        this.queryTerm = queryTerm;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
