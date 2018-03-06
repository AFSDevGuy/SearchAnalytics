/*******************************************************************************
 * Copyright (c) 2015 Agilex Technologies, Inc.
 * All Rights Reserved
 ******************************************************************************/
package com.afs.cio.outputhandler;




import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>This output handler exports items to an XML file. It uses the JDK-provided JAXB serializer to output
 * the classes, so the item objects will need to be properly annotated with JAXB annotations.</p>
 *
 */
public class XmlFileOutput<T>  {


    /**
     * XML marshaller - used to listen to specific XML events so we can help out with formatting
     */
    protected Marshaller marshaller;

    /**
     * Where the output will be written
     */
    protected XMLStreamWriter outStream;

    /**
     * While the writer is class-agnostic, if you tell it what the base class is, it will annotate the
     * export metadata with the full classname (TODO)
     */
    protected Class itemClass;

    /**
     * name-value pairs that will be added to the document root element. Used to contain various types of
     * metadata about the export
     */
    protected Map<String,String> initAttributes;

    /**
     * Zero-argument constructor, used by XML serialization frameworks
     */
    private XmlFileOutput() {

    };

    public void setInitAttributes(Map<String, String> initAttributes) {
        this.initAttributes = initAttributes;
    }

    /**
     * Listener that injects formatting between objects as they are written.
     */
    private class MarshallerListener extends Marshaller.Listener {

        private XMLStreamWriter xsw;

        public  MarshallerListener(XMLStreamWriter xsw) {
            this.xsw = xsw;
        }

        protected Object previousObject = null;
        @Override
        public void beforeMarshal(Object source)  {
        }

        @Override
        public void afterMarshal(Object source) {
            try {
                xsw.writeCharacters("\n");
            } catch (XMLStreamException e) {
                throw new RuntimeException("error writing XML", e);
            }
        }



    }
    public XmlFileOutput(Class<T> itemClass) {
        this.itemClass = itemClass;
    }

    public XMLStreamWriter getOutStream() {
        return outStream;
    }

    public void setOutStream(XMLStreamWriter outStream) {
        this.outStream = outStream;
    }

    /**
     * Write the specific item to the output stream.
     *
     * @param item
     */
    public void handleItem(T item) {
        try {
            synchronized (marshaller) {
                marshaller.marshal(item,outStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("can't write object to output",e);
        }

    }


    /**
     * Initialization - call before writing any items.
     */
    public void init() {

        try {
            JAXBContext ctx = JAXBContext.newInstance(itemClass);
            // TimeStamp is suffixed with the file name
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String suffixTimeStamp = sdf.format(new Date());
            outStream.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            // TODO: customize with item class name if possible
            outStream.writeStartElement("Export");
            if (this.initAttributes != null) {
                for (Map.Entry<String,String> attribute:initAttributes.entrySet()) {
                    outStream.writeAttribute(attribute.getKey(),attribute.getValue());
                }
            }
            outStream.writeAttribute("attribute","value");
            marshaller = ctx.createMarshaller();
            marshaller.setListener(new MarshallerListener(outStream));
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("error initializing output: ", e);
        }

    }

    /**
     * Close out XML document
     */
    public void close() {
        try {
        	//outStream.write("<completedDate>DATE</completedDate>");
            outStream.writeEndElement();
            outStream.writeEndDocument();
            
            outStream.close();
        } catch (Exception e) {
            throw new IllegalStateException("error initializing output: ", e);
        }
    }

    public Class<T> itemClass() {
        return itemClass;
    }

    public String toString() {
        String results = null;
        synchronized(this) {
            results = "[XmlFileOutput: "+itemClass.getTypeName()+" ]";
        }
        return results;
    }
}
