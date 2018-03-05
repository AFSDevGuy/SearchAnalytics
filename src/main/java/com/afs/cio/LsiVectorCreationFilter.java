package com.afs.cio;

import com.afs.cio.model.LsiVectorLog;
import com.afs.cio.model.RawLogInput;
import com.agilex.phanerodb.db.DbConnectionManager;
import com.agilex.phanerodb.lsi.LsiQueryTool;
import com.agilex.phanerodb.model.LsiIndex;
import com.agilex.phanerodb.model.LsiVector;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class LsiVectorCreationFilter extends BaseXmlFilter<RawLogInput,LsiVectorLog> {

    public static void main( String[] args )
    {
        LsiVectorCreationFilter app = new LsiVectorCreationFilter();
        app.cmdLine(args);
        app.run();
    }

    protected LsiVectorCreationFilter() {
        super(RawLogInput.class,LsiVectorLog.class);
    }

    DbConnectionManager dbMgr = new DbConnectionManager();
    LsiIndex index = null;
    String features = null;
    int dimensions = 0;

    @Override
    protected void cmdLine(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("jdbcurl").hasArg().desc("JDBC URL for Phanero DB").type(String.class).build());
        options.addOption(Option.builder("username").hasArg().desc("Username for Phanero DB").type(String.class).build());
        options.addOption(Option.builder("password").hasArg().desc("Password for Phanero DB").type(String.class).build());
        options.addOption(Option.builder("index").hasArg().desc("Index name to use in Phanero DB").type(String.class).build());
        options.addOption(Option.builder("features").hasArg().desc("Object type for words in Phanero DB").type(String.class).build());
        options.addOption(Option.builder("dimensions").hasArg().desc("Number of dimensions of LSI vector to use").type(String.class).build());
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

        Properties dbMgrProps = new Properties();
        if (cmd.hasOption("jdbcurl")) {
            dbMgrProps.setProperty(DbConnectionManager.PROP_URL, cmd.getOptionValue("jdbcurl"));
        } else {
            throw new RuntimeException("No JDBC URL on command line!");
        }
        if (cmd.hasOption("username")) {
            dbMgrProps.setProperty(DbConnectionManager.PROP_USERNAME, cmd.getOptionValue("username"));
        } else {
            throw new RuntimeException("No JDBC username on command line!");
        }
        if (cmd.hasOption("password")) {
            dbMgrProps.setProperty(DbConnectionManager.PROP_PASSWORD, cmd.getOptionValue("password"));
        } else {
            throw new RuntimeException("No JDBC password on command line!");
        }
        if (cmd.hasOption("index")) {
            this.index = new LsiIndex();
            this.index.setIndexName(cmd.getOptionValue("index"));
        } else {
            throw new RuntimeException("No LSI Index name on command line!");
        }
        if (cmd.hasOption("features")) {
            this.features = cmd.getOptionValue("features");
        } else {
            throw new RuntimeException("No JDBC password on command line!");
        }
        if (cmd.hasOption("dimensions")) {
            this.dimensions = Integer.parseInt(cmd.getOptionValue("dimensions"));
        }
        dbMgrProps.setProperty(DbConnectionManager.PROP_DRIVER,"com.mysql.jdbc.Driver");
        dbMgrProps.setProperty(DbConnectionManager.PROP_INITSIZE, "1");
        dbMgrProps.setProperty(DbConnectionManager.PROP_MAXACTIVE, "1");
        this.dbMgr.initializeDbConnectionPool(dbMgrProps);
        super.cmdLine(args,options,cmd);
    }


    @Override
    public LsiVectorLog filter(RawLogInput inputItem) {
        Map<String, List<String>> keyMap = new HashMap<>();
        LsiVectorLog result = new LsiVectorLog();
        result.setTerm(inputItem.getTerm());
        result.setUser(inputItem.getUser());
        result.setDate(inputItem.getDate());
        List<String> queryTerms = Arrays.asList(result.getTerm().split("\\s"));
        keyMap.put(features,queryTerms);
        LsiVector vector = LsiQueryTool.createVectorForKeyMap(index,keyMap,dbMgr,null);
        if (vector == null) {
            result = null;
        } else {
            List<Double> trimmedVector = new ArrayList<>();
            if (dimensions==0) {
                dimensions = vector.getDimensions();
            }
            for (int index = 0; index < dimensions; index++) {
                trimmedVector.add(vector.getVectorValues()[index]);
            }
            result.setVector(trimmedVector);
        }
        return result;
    }

    /*
      public static LsiVector createVectorForKeyMap(LsiIndex index, Map<String, List<String>> query, DbConnectionManager dbManager,
    		List<WeightedVector> explained){
     */


}
