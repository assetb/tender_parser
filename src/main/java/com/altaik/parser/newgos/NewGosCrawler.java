/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.newgos;

import com.altaik.crawler.AuthCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Smaile
 */
public class NewGosCrawler extends AuthCrawler {

    public NewGosCrawler() {
        super();
        isToSleep = false;
        setTimeout(10000);
        baseUrl = "https://v3bl.goszakup.gov.kz";

    }

    
//    @Override
//    protected void Certificate() {
//        super.Certificate();
////        isCertificated = true;
//    }

    @Override
    public void AuthInit(Properties certsProps) {
        super.AuthInit(certsProps);

//        setHeader("Host", "v3bl.goszakup.gov.kz");
//        setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
//        setHeader("Accept-Encoding", "gzip, deflate");
//        setHeader("Connection", "keep-alive");
        referrer = "none";
        setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

        setUrl(baseUrl);
    }

    @Override
    protected Connection GetConnection() {
        if (con == null || !isCertificated) {
            Certificate();
            con = Jsoup.connect(url).userAgent(userAgent);
            con.validateTLSCertificates(false);
            logger.log(Level.INFO, "Auth Connection created.");
        }

        return con;
    }

}
