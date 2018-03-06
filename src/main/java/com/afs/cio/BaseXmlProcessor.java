package com.afs.cio;

import com.afs.cio.outputhandler.XmlFileOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extend Base XML reader/writer so that the derived classes can process all records at once
 * @param <S>
 * @param <T>
 */
public abstract class BaseXmlProcessor<S,T> extends BaseXmlReaderWriter<S,T>{


    public abstract List<T> process(List<S> inputItems);

    protected Map<String,String> exportAttributes = new HashMap<>();

    protected List<S> itemList = new ArrayList<>();

    public BaseXmlProcessor(Class<S> sClass, Class<T> tClass) {
        super(sClass,tClass);
    }

    /**
     * Aggregate the input items into an internal list
     * @param inputItem
     */
    @Override
    protected void eachItem(S inputItem) {
        super.eachItem(inputItem);
        itemList.add(inputItem);
    }

    /**
     * Call process() and write the resulting list
     */
    @Override
    protected void allItemsComplete() {
        List<T> resultList = process(itemList);
        for (T result : resultList) {
            handler.handleItem(result);
        }
        super.allItemsComplete();
    }

}
