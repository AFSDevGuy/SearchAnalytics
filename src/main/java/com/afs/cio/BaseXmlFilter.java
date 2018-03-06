package com.afs.cio;

/**
 * Extend base XML reader/writer to expose one record at a time to the filter class.
 *
 * @param <S> class to expect in the input XML
 * @param <T> class that will be written at the top level in the output XML
 */
public abstract class BaseXmlFilter<S,T> extends BaseXmlReaderWriter<S,T>{

    /**
     * The actual filter class needs to implement (at least) this method. It is given one input item and should emit
     * one (or none) output items.
     *
     * @param inputItem
     * @return item to be written to output stream
     */
    public abstract T filter(S inputItem);

    public BaseXmlFilter(Class<S> sClass, Class<T> tClass) {
        super(sClass,tClass);
    }

    @Override
    protected void eachItem(S inputItem) {
        T filtered = filter(inputItem);
        if (filtered != null) {
            handler.handleItem(filtered);
        }
    }

}
