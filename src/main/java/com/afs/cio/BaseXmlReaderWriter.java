package com.afs.cio;

import com.afs.cio.outputhandler.XmlFileOutput;
import org.apache.commons.cli.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseXmlReaderWriter<S,T> {

    /**
     * Input files that will be consumed by the tool
     */
    protected InputStream inStreams[] = {System.in};

    /**
     * Output location
     */
    protected XMLStreamWriter outStream = null;

    /**
     * Output item writer
     */
    protected XmlFileOutput<T> handler = null;

    protected void eachItem(S inputItem) {return;}

    protected void allItemsComplete() {return;}


    protected Class<S> inClass;
    protected Class<T> outClass;


    protected BaseXmlReaderWriter(Class<S> sClass, Class<T> tClass) {
        this.inClass = sClass;
        this.outClass = tClass;

    }
    protected BaseXmlReaderWriter() {}

    /**
     *  Override this method to attach your own options
     */
    protected void addCustomOptions(Options options) { return ;}

    /**
     * Override this method to extract the value(s) of your options
     */
    protected void extractCustomOptionValues(CommandLine cmdLine) throws ParseException {return;}

    /**
     * Parse the command line and store input/output file options
     * @param args
     */
    protected void cmdLine(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("infile").hasArgs().desc("input file name").type(String.class).build());
        options.addOption(Option.builder("outfile").hasArg().desc("output file name").type(String.class).build());
        addCustomOptions(options);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
            extractCustomOptionValues(cmd);
        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("RawLogFilter", options);
        }

        if (cmd.hasOption("infile")) {
            String[] filenames = cmd.getOptionValues("infile");
            String errFile = null;
            try {
                inStreams = new InputStream[filenames.length];
                int pos = 0;
                for (String filename : filenames) {
                    errFile = filename;
                    inStreams[pos++] = new FileInputStream(filename);
                }
            } catch (Exception ex) {
                System.err.println("cannot open: "+errFile+" "+ex.getMessage());
                System.exit(-1);
            }
        }
        XMLOutputFactory xof = XMLOutputFactory.newFactory();

        if (cmd.hasOption("outfile")) {
            String filename = cmd.getOptionValue("outfile");
            try {
                outStream = xof.createXMLStreamWriter(new OutputStreamWriter(new FileOutputStream(filename),
                        StandardCharsets.UTF_8));
            } catch (Exception ex) {
                System.err.println("cannot open: "+filename+" "+ex.getMessage());
                System.exit(-1);
            }
        } else {
            try {
                outStream = xof.createXMLStreamWriter(System.out);
            } catch (Exception ex) {
                System.err.println("cannot create XMLStreamWriter on stdout");
                System.exit(-1);
            }
        }
    }

    /**
     * Actually perform the iteration over the input file, and call the filter() method for each item.
     */
    public void run() {
        try {
            handler = new XmlFileOutput<>(this.outClass);
            handler.setOutStream(outStream);
            handler.init();

            JAXBContext jc = JAXBContext.newInstance(this.inClass);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            for (InputStream inStream : inStreams) {
                XMLInputFactory xif = XMLInputFactory.newFactory();
                StreamSource xml = new StreamSource(inStream);
                XMLStreamReader xsr = xif.createXMLStreamReader(xml);
                xsr.nextTag();
                String itemClassName = inClass.getSimpleName();
                // Override with root XML element if specified
                if (inClass.getAnnotation(XmlRootElement.class)!=null){
                    itemClassName = inClass.getAnnotation(XmlRootElement.class).name();
                }
                while(!xsr.getLocalName().equals(itemClassName)) {
                    xsr.nextTag();
                }
                String tagName = "N/A";
                while (xsr.getLocalName().equals(itemClassName)) {
                    JAXBElement<S> jb = unmarshaller.unmarshal(xsr, inClass);
                    eachItem(jb.getValue());
                    // Advance to next tag event
                    while(xsr.hasNext()) {
                        if (xsr.isStartElement()||xsr.isEndElement()) {
                            break;
                        }
                        xsr.nextTag();
                    }
                    tagName = xsr.getLocalName();
                }
                xsr.close();
            }
            allItemsComplete();
            handler.close();

        } catch (Exception ex) {
            System.err.println("error processing input: " + ex.toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }


}
