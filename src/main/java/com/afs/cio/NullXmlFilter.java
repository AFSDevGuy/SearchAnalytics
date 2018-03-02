package com.afs.cio;

import com.afs.cio.model.RawLogInput;

public class NullXmlFilter extends BaseXmlFilter {

    public static void main( String[] args )
    {
        NullXmlFilter app = new NullXmlFilter();
        app.cmdLine(args);
        app.run();
    }

    @Override
    public Class getInputClass() {
        return RawLogInput.class;
    }

    @Override
    public Object filter(Object inputItem) {
        return inputItem;
    }
}
