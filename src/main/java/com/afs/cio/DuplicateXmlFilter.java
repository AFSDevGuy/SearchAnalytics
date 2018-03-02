package com.afs.cio;

import com.afs.cio.model.RawLogInput;

import java.util.HashMap;
import java.util.Map;

public class DuplicateXmlFilter extends BaseXmlFilter {

    public static void main( String[] args )
    {
        DuplicateXmlFilter app = new DuplicateXmlFilter();
        app.cmdLine(args);
        app.run();
    }

    protected Map<String,RawLogInput> lastQueryCache = new HashMap<>();

    @Override
    public RawLogInput filter(RawLogInput inputItem) {
        RawLogInput result = inputItem;
        // Check to see what the last query for this user was
        RawLogInput lastQueryItem = lastQueryCache.get(inputItem.getUser());
        if (lastQueryItem!=null&&lastQueryItem.getTerm().equalsIgnoreCase(inputItem.getTerm())) {
            result = null;
        }
        lastQueryCache.put(inputItem.getUser(),inputItem);
        return result;
    }
}
