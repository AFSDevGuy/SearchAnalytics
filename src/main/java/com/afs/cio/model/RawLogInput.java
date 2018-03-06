package com.afs.cio.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Basic query log records, capture timestamp, query term, and username
 */
@XmlRootElement(name="RawLogInput")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RawLogInput {


    /**
     * Query timestamp
     */
    protected Date date;

    /**
     * Unparsed query
     */
    protected String term;

    /**
     * Username
     */
    protected String user;

    public RawLogInput() {
    }

    public RawLogInput(Date date, String term, String user) {
        this.date = date;
        this.term = term;
        this.user = user;
    }

    public long getDate() {
        return date.getTime();
    }

    public String getTerm() {
        return term;
    }

    public void setDate(long date) {
        this.date = new Date(date);
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;

    }
}
