package com.altaik.parser.eauc;


import com.altaik.crawler.ICrawler;
import com.altaik.parser.eauc.Interfaces.IDataSource;
import org.jsoup.nodes.Document;

public class CrawlerDataSourceImpl implements IDataSource {


    private Document document;

//    @Inject
//    private ICrawler crawler;

    public CrawlerDataSourceImpl(){

    }

    public CrawlerDataSourceImpl(ICrawler crawler){
        this.document = crawler.getDoc();
    }


    @Override
    public Document getDocument() {
        return this.document;
    }


}
