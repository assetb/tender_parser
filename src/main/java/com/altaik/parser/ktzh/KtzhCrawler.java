package com.altaik.parser.ktzh;

import com.altaik.crawler.Crawler;
import org.jsoup.nodes.Document;

/**
 * Created by Asus-X555LD_101 on 22.05.2017.
 */
public class KtzhCrawler extends Crawler {

    @Override
    public Document getDoc(){
        return GetNewDoc();
    }


}
