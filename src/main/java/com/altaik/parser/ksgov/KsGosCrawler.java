/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.ksgov;

import com.altaik.crawler.Crawler;
import com.altaik.crawler.ICrawler;
import org.jsoup.nodes.Document;

import java.util.HashMap;

/**
 *
 * @author Vladimir
 */
public class KsGosCrawler extends Crawler implements ICrawler {

    public KsGosCrawler(String url) {
        super();

        headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        headers.put("Pragma", "no-cache");
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept", "text/html, */*; q=0.01");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        headers.put("Accept-Encoding", "gzip, deflate, sdch");
        headers.put("Accept-Language", "ru,kk;q=0.8,en;q=0.6");

        isUseProxy = false;
        baseUrl = url;
    }

    @Override
    public Document getDoc() {
        close();
        return GetNewDoc();
    }

    public Document getCurrentDoc() {
        return doc;
    }
}
