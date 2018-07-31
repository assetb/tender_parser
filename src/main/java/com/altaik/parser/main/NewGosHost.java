/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.main;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.newgos.NewGosCrawler;
import com.altaik.parser.newgos.NewGosParser;
import com.altaik.parser.newgos.NewGosSaver;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Smaile
 */
public class NewGosHost {

    protected static final Logger logger = Logger.getLogger(NewGosHost.class.getName());

    protected boolean isNigthly = false;

    private final Properties storeCertProp;
    private final IDatabaseManager databaseManager;
    private final NewGosCrawler newGosCrawler = new NewGosCrawler();
    private final ArrayList<NewGosParser> newGosParsers = new ArrayList<>();

    private int countPages = 100;

    public NewGosHost(IDatabaseManager databaseManager, Properties storeCertProp) {
        this.storeCertProp = storeCertProp;
        this.databaseManager = databaseManager;
    }

    public NewGosHost(IDatabaseManager databaseManager, Properties storeCertProp, boolean isNigthly) {
        this.storeCertProp = storeCertProp;
        this.isNigthly = isNigthly;
        this.databaseManager = databaseManager;
    }

    private String gen_guid() {
        return (S4() + S4() + "-" + S4() + "-4" + S4().substring(0, 3) + "-" + S4() + "-" + S4() + S4() + S4()).toLowerCase();
    }

    private String S4() {
        int ran = (int) ((1 + Math.random()) * 0x10000) | 0;
        return Integer.toHexString(ran).substring(1);
    }
//        return (((1+Math.random())*0x10000)|0).toString(16).substring(1);

    public void Run() {
        Document page = null;
        boolean isAuth = false;
        newGosCrawler.AuthInit(storeCertProp);
//        newGosCrawler.getDoc();

        newGosCrawler.setUrlPath("/ru/user/login");
        newGosCrawler.setFollowRedirects(true);

        Document pageAuth = newGosCrawler.getDoc();
        if (pageAuth != null) {
            Elements forms = pageAuth.select("form");
            if (!forms.isEmpty()) {
                Element form = forms.get(0);
                Elements inputs = form.select("input");
                Map<String, String> data = new HashMap<>();
                for (Element element : inputs) {
                    data.put(element.attr("name"), element.val());
                }
                data.put("password", "!Q@W3e4r5t6y");

                newGosCrawler.setData(data, true);
                newGosCrawler.setUrl(form.attr("action"));
                page = newGosCrawler.getDoc();
                isAuth = true;
            }
        }
        String path;
        for (int i = 1; i <= countPages; i++) {
            if (i == 1) {
                path = "/ru/searchanno";
            } else {
                path = "/ru/searchanno?&page=" + i;
            }
            newGosCrawler.setUrlPath(path);
            Document doc = newGosCrawler.getDoc();
            if (doc == null) {
                logger.log(Level.SEVERE, "Error connect to {0}", newGosCrawler.getUrl());
                continue;
            }
            NewGosParser newGosParser = new NewGosParser(doc, databaseManager, newGosCrawler);
            newGosParsers.add(newGosParser);
        }
        for (NewGosParser parser : newGosParsers) {
            parser.setIsToDoAuth(isAuth);
            parser.PurchasesDo();
            if (null != parser.getData() && !parser.getData().isEmpty()) {
                NewGosSaver saver = new NewGosSaver(databaseManager, "1", null);
                saver.isToSaveAuth = isAuth;
                saver.Do(parser.getData());
                saver.Close();
            }
            parser.Close();
        }
    }

    public void setCountPages(int count) {
        countPages = count;
    }

    public int getCountPages() {
        return countPages;
    }

}
