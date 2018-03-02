package com.afs.cio;

import com.afs.cio.model.RawLogInput;
import com.afs.cio.outputhandler.XmlFileOutput;
import org.apache.commons.cli.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseXmlFilter {

    protected InputStream inStreams[] = {System.in};

    protected XMLStreamWriter outStream = null;

    public abstract RawLogInput filter(RawLogInput inputItem);

    protected void cmdLine(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("infile").hasArgs().desc("input file name").type(String.class).build());
        options.addOption(Option.builder("outfile").hasArg().desc("output file name").type(String.class).build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "RawLogFilter", options );
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

    protected Map<String,Integer> columnMap = new HashMap<String, Integer>();

    public void run() {
        try {
            XmlFileOutput<RawLogInput> handler = new XmlFileOutput<RawLogInput>(RawLogInput.class);
            handler.setOutStream(outStream);
            handler.init();

            JAXBContext jc = JAXBContext.newInstance(RawLogInput.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            for (InputStream inStream : inStreams) {
                XMLInputFactory xif = XMLInputFactory.newFactory();
                StreamSource xml = new StreamSource(inStream);
                XMLStreamReader xsr = xif.createXMLStreamReader(xml);
                xsr.nextTag();
                while(!xsr.getLocalName().equals("logItem")) {
                    xsr.nextTag();
                }
                String tagName = "N/A";
                while (xsr.getLocalName().equals("logItem")) {
                    JAXBElement<RawLogInput> jb = unmarshaller.unmarshal(xsr, RawLogInput.class);
                    RawLogInput filtered = filter(jb.getValue());
                    if (filtered != null) {
                        handler.handleItem(filtered);
                    }
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
            handler.close();

        } catch (Exception ex) {
            System.err.println("error processing input: " + ex.toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }



}
