package com.afs.cio;

import com.afs.cio.model.RawLogInput;
import com.afs.cio.model.SearchCluster;
import com.afs.cio.model.SpreadSheetQueryRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Flatten the search clusters into a series of records with three fields. This can later be imported via CSV
 */
public class ExcelXMLExporter extends BaseXmlProcessor<SearchCluster,SpreadSheetQueryRow>{

    public static void main( String[] args )
    {
        ExcelXMLExporter app = new ExcelXMLExporter();
        app.cmdLine(args);
        app.run();
    }

    public ExcelXMLExporter() {
        super(SearchCluster.class,SpreadSheetQueryRow.class);
    }

    @Override
    public List<SpreadSheetQueryRow> process(List<SearchCluster> inputItems) {
        List<SpreadSheetQueryRow> results = new ArrayList<>();
        for (SearchCluster eachCluster:inputItems) {
            String label = eachCluster.getLabel();
            for (RawLogInput logItem : eachCluster.getMembers()) {
                results.add(new SpreadSheetQueryRow(label, logItem.getTerm(), logItem.getUser()));
            }
        }
        return results;
    }
}


