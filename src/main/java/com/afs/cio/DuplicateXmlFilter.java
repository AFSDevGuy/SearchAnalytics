package com.afs.cio;

import com.afs.cio.model.RawLogInput;

import java.util.HashMap;
import java.util.Map;

public class DuplicateXmlFilter extends BaseXmlFilter<RawLogInput,RawLogInput> {



    public static void main( String[] args )
    {
        DuplicateXmlFilter app = new DuplicateXmlFilter();
        app.cmdLine(args);
        app.run();
    }

    public DuplicateXmlFilter() {
        super(RawLogInput.class, RawLogInput.class);
    }

    protected Map<String,RawLogInput> lastQueryCache = new HashMap<>();

    @Override
    public RawLogInput filter(RawLogInput objectItem) {
        RawLogInput result = (RawLogInput)objectItem;
        // Check to see what the last query for this user was
        RawLogInput lastQueryItem = lastQueryCache.get(result.getUser());
        lastQueryCache.put(result.getUser(),result);
        if (lastQueryItem!=null&&lastQueryItem.getTerm().equalsIgnoreCase(result.getTerm())) {
            result = null;
        }
        return result;
    }
}
