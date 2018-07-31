/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.ets;

import com.altaik.crawler.Crawler;

/**
 *
 * @author Vladimir
 */
public class ETSCrawler extends Crawler {
    
    public ETSCrawler() {
        super();
        userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0";
        baseUrl = "http://www.ets.kz/";
    }

    public void setUrlPath(String path) {
        setUrl(baseUrl + path);
    }
}
