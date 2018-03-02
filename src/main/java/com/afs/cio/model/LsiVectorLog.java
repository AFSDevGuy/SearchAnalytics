package com.afs.cio.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement(name="lsiLog")
@XmlAccessorType(XmlAccessType.FIELD)
public class LsiVectorLog extends RawLogInput{

    public int dimensions;
    @XmlElement(name = "v")
    public List<Double> vector;
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
