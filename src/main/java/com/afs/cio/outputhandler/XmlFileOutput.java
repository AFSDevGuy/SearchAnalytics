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

/**
 * <p>This output handler exports items to an XML file. It uses the JDK-provided JAXB serializer to output
 * the classes, so the item objects will need to be properly annotated with JAXB annotations.</p>
 *
 */
public class XmlFileOutput<T>  {

    protected String outputFilename;
    protected String outputFileExtension;

    protected Marshaller marshaller;

    protected XMLStreamWriter outStream;

    protected int emptyItems = 0;
    protected int fullItems = 0;

    protected Class itemClass;

    private XmlFileOutput() {

    };

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
    public XmlFileOutput(Class itemClass) {
        this.itemClass = itemClass;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
    
	public String getOutputFileExtension() {
		return outputFileExtension;
	}

	public void setOutputFileExtension(String outputFileExtension) {
		this.outputFileExtension = outputFileExtension;
	}

    public XMLStreamWriter getOutStream() {
        return outStream;
    }

    public void setOutStream(XMLStreamWriter outStream) {
        this.outStream = outStream;
    }

    public void handleItem(T item) {
        // Extract text from attachments (if any)
        try {
            synchronized (marshaller) {
                marshaller.marshal(item,outStream);
            }
        } catch (Exception e) {
            throw new IllegalStateException("can't write candidate to output",e);
        }

    }


    public void init() {

        try {
            JAXBContext ctx = JAXBContext.newInstance(itemClass);
            // TimeStamp is suffixed with the file name
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String suffixTimeStamp = sdf.format(new Date());
            outStream.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            outStream.writeStartElement("Export");
            marshaller = ctx.createMarshaller();
            marshaller.setListener(new MarshallerListener(outStream));
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        } catch (Exception e) {
            throw new IllegalStateException("error initializing output: ", e);
        }

    }

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
            results = "[XmlFileOutput: "+outputFilename+" ("+ emptyItems +" empty, "
                    + fullItems +" full, total="+(emptyItems + fullItems)+" ]";
        }
        return results;
    }
}
