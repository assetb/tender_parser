package com.altaik.parser.eauc;

import com.altaik.crawler.Crawler;
import org.jsoup.nodes.Document;

public class EaucCrawler extends Crawler {

    @Override
    public Document getDoc() {
    return GetNewDoc();
    }
}
