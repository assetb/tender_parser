package com.altaik.parser.auctionya;

import com.altaik.crawler.Crawler;
import org.jsoup.nodes.Document;

public class AuctionyaCrawler extends Crawler {

    String baseUrl = "http://auction.ya.kz/";

    @Override
    public Document getDoc() {
        return GetNewDoc();
    }


}
