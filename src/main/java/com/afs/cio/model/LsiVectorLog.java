package com.afs.cio.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;


/**
 * Log records that capture the basic query data as well as the additional vector components. Note the use of a custom
 * Xml element name for the vector components - this is important to keep the overall file size manageable, otherwise
 * since you have about 600 repetitions of the element name tag in one record, storage can go out of control.
 */
@XmlRootElement(name="lsiLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class LsiVectorLog extends RawLogInput{

    /**
     * Count of number of vector elements
     */
    public int dimensions;

    /**
     * Actual vector components (values)
     */
    @XmlElement(name = "v")
    public List<Double> vector;

    /**
     * Vector weight factor, if any
     */
    public Double weight;


    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public List<Double> getVector() {
        return vector;
    }

    public void setVector(List<Double> vector) {
        this.vector = vector;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
