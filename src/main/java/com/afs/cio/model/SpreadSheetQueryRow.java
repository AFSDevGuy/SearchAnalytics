package com.afs.cio.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Records used to convert the one-level cluster hierarchy to a simple flat list of cluster members, individually
 * labeled. This can then be imported into your choice of spreadsheet tool. If you are unfortunate enough to need
 * .csv format for your input, this can be achieved using PowerShell:
 *
 * <code>
 *     [xml]$inputFile = Get-Content "input.xml"
 *     $inputFile.Export.ChildNodes | Export-Csv "output.csv" -NoTypeInformation -Delimiter:"," -Encoding:UTF8
 * </code>
 */
@XmlRootElement(name="Row")
public class SpreadSheetQueryRow {

    /**
     * Cluster name
     */
    protected String clusterLabel;

    /**
     * Query text
     */
    protected String queryTerm;

    /**
     * Username that created the query
     */
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
