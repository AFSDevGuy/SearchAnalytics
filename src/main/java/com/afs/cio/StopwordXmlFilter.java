package com.afs.cio;

import com.afs.cio.model.RawLogInput;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StopwordXmlFilter extends BaseXmlFilter {

    public static void main( String[] args )
    {
        StopwordXmlFilter app = new StopwordXmlFilter();
        app.cmdLine(args);
        app.run();
    }

    @Override
    public RawLogInput filter(RawLogInput inputItem) {
        List<String> cleanQuery = new ArrayList<>(Arrays.asList(inputItem.getTerm().split("\\s")));
        try {
            cleanQuery = normalize(inputItem.getTerm().toLowerCase());
        } catch (IOException ex) {
            throw new RuntimeException("Error normalizing query", ex);
        }
        StringBuilder normalized = new StringBuilder();
        for (String term : cleanQuery) {
            if (normalized.length()>0) {
                normalized.append(' ');
            }
            normalized.append(term);
        }
        inputItem.setTerm(normalized.toString());
        return inputItem;
    }

    protected List<String> normalize(String query) throws IOException {
            Analyzer analyzer = new StandardAnalyzer();
            TokenStream result = analyzer.tokenStream(null, query);
            result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
            CharTermAttribute resultAttr = result.addAttribute(CharTermAttribute.class);
            result.reset();

            List<String> tokens = new ArrayList<>();
            while (result.incrementToken()) {
                tokens.add(resultAttr.toString());
            }
        return tokens;
    }

}
