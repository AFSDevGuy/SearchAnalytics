package com.afs.cio;

import com.afs.cio.model.RawLogInput;
import com.afs.cio.outputhandler.XmlFileOutput;
import org.apache.commons.cli.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sun.jvm.hotspot.utilities.Assert;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class RawLogXmlConverter {

    protected InputStream inStreams[] = {System.in};

    protected XMLStreamWriter outStream = null;

    public static void main( String[] args )
    {
        RawLogXmlConverter app = new RawLogXmlConverter();
        app.cmdLine(args);
        app.run();
    }

    private void cmdLine(String[] args) {
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

            for (InputStream inStream : inStreams) {
                Workbook wb = new XSSFWorkbook(inStream);
                // Do all sheets
                for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = wb.getSheetAt(sheetIndex);
                    short topIndex = sheet.getTopRow();
                    int topIndexInt = topIndex & 0xFFFF;
                    Row topRow = sheet.getRow(0);
                    Iterator<Cell> cellIt = topRow.cellIterator();
                    // Capture the column indexes where data of interest resides
                    while (cellIt.hasNext()) {
                        Cell cell = cellIt.next();
                        int colNum = cell.getColumnIndex();
                        String label = cell.getStringCellValue();
                        columnMap.put(label, colNum);
                    }
                    // Now process the rows (skip the first one!)
                    Iterator<Row> rowIt = sheet.rowIterator();
                    int rowCount = 0;
                    RawLogInput newItem = null;
                    Assert.that(columnMap.get("Created")!=null, "No Date Column in input (Created)");
                    Assert.that(columnMap.get("Search term")!=null, "No Search term column in input");
                    Assert.that(columnMap.get("User")!=null, "No User ID column in input");
                    while (rowIt.hasNext()) {

                        Row row = rowIt.next();
                        rowCount++;
                        if (rowCount == 1) {
                            continue;
                        }

                        // Consume the row
                        Cell cell = row.getCell(columnMap.get("Created"));
                        Date itemDate = cell.getDateCellValue();
                        String searchTerm = cellStringValue(row, "Search term");
                        String userId = cellStringValue(row, "User");
                        newItem = new RawLogInput(itemDate, searchTerm, userId);

                        // TODO: a more elegant way to do this
                        if(row != null) {

                            //Process simple fields
//                            newRole.setcareerTrack(cellStringValue(row, "Role_Career_Track"));

                            //Process date information
//                            newRole.setStartDate(getDate(cellStringValue(row, "Role_Requested_Start_Date")));

                        }

                        //Final check before passing along to handler
                        if (newItem != null) {
                            handler.handleItem(newItem);
                        }

                    }
                }
            }
            handler.close();

        } catch (Exception ex) {
            System.err.println("error processing input: " + ex.toString() + " " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    protected String getDate(String givenDate) {

        boolean isDate = Pattern.matches("\\d{2}\\/\\d{2}\\/\\d{4}", givenDate);
        boolean isExcelDate = Pattern.matches("\\d{5}", givenDate);
        if(isDate) { //matches conventional date format
            return givenDate;
        }
        else if(isExcelDate) { //matches excel 1900/1904 date formatting
            double date = Double.parseDouble(givenDate);
            Date javaDate = DateUtil.getJavaDate(date, true);
            givenDate = new SimpleDateFormat("MM/dd/yyyy").format(javaDate);
            return givenDate;
        }
        else { //doens't match any date format, errors out date with zeros
            givenDate = "00/00/0000";
            return givenDate;
        }
    }
    protected String cellStringValue (Row row, String columnName) {
        if (columnMap.get(columnName) == null) {
            throw new IllegalStateException("sheet does not contain requested column name: "+columnName);
        }
        Cell cell = row.getCell(columnMap.get(columnName));
        String value = null;
        if (cell != null) {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                value = Integer.toString((int) cell.getNumericCellValue());
            } else {
                value = cell.getStringCellValue();
            }
            value = value.replaceAll("_x000d_","")
                    .replaceAll("(?![\\x{000d}\\x{000a}\\x{0009}])\\p{C}", "");
        }
        return value;
    }
}
