/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.samruk;

import com.altaik.crawler.Crawler;
import com.altaik.crawler.CrawlerContext;

/**
 *
 * @author Aset
 */
public class SamrukCrawler extends Crawler {

    private String negsUrl = "http://tender.sk.kz/index.php/ru/negs";
    private String lotUrl = "/show/";

    public static final String urlMain = "https://tender.sk.kz";
    public static final String urlFormLogin = "/OA_HTML/AppsLocalLogin.jsp?langCode=US";

    public SamrukCrawler(CrawlerContext context) {
        super(context);
        setUrl(negsUrl);
        crawlerName = "SamrukCrawler";
    }

    public SamrukCrawler(String lot, CrawlerContext context) {
        super(context);
        setUrl(negsUrl + lotUrl + lot);
    }

    public String getNegsUrl() {
        return negsUrl;
    }

    public void setNegsUrl(String negsUrl) {
        this.negsUrl = negsUrl;
    }

    public String getLotUrl() {
        return lotUrl;
    }

    public void setLotUrl(String lotUrl) {
        this.lotUrl = lotUrl;
    }

}
