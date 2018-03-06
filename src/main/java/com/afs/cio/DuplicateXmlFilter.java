package com.afs.cio;

import com.afs.cio.model.RawLogInput;

import java.util.HashMap;
import java.util.Map;

/**
 * Filter class implementation that removes obvious recent duplicates from the query stream. These are limited
 * to per-user duplicates.
 */
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
        // TODO: replace string equality test with a more permissive test (edit distance?)
        if (lastQueryItem!=null&&lastQueryItem.getTerm().equalsIgnoreCase(result.getTerm())) {
            result = null;
        }
        return result;
    }
}
