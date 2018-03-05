package com.afs.cio;

import com.afs.cio.model.RawLogInput;

public class NullXmlFilter extends BaseXmlFilter<RawLogInput,RawLogInput> {

    public static void main( String[] args )
    {
        NullXmlFilter app = new NullXmlFilter();
        app.cmdLine(args);
        app.run();
    }

    protected NullXmlFilter() {
        super(RawLogInput.class,RawLogInput.class);
    }

    @Override
    public RawLogInput filter(RawLogInput inputItem) {
        return inputItem;
    }
}
