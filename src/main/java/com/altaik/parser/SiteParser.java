/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vladimir
 * @param <T>
 */
public abstract class SiteParser<T> implements ISiteParser<T> {

    private static final Logger LOG = Logger.getLogger(SiteParser.class.getName());

    protected String url;

    private int countPages = 0;

    public SiteParser(String url){
        this(url, 0);
    }
    
    public SiteParser(String url, int countPages) {
        this.url = url;
        this.countPages = countPages;
    }

    /**
     *Abstract method to get the home page
     * @param url base url
     * @param ipage index page
     * @return home page
     */
    protected abstract Document GetMainPage(String url, int ipage);

    protected abstract Elements ParseMainPage(Document page);

    protected abstract T InitBusinessObject(Element row, Document page);

    protected abstract boolean ParseDetails(T bo, Element row, Document page);

    public List<T> Proccess(int ipage) {
        Document mainPage = GetMainPage(url, ipage);
        if (mainPage == null) {
            LOG.log(Level.WARNING, "Main page is null");
            return null;
        }
        Elements rows = ParseMainPage(mainPage);
        if (rows != null && !rows.isEmpty()) {
            List<T> bList = new ArrayList<>();
            for (Element row : rows) {
                T businessObject = InitBusinessObject(row, mainPage);
                if (businessObject == null) {
                    LOG.log(Level.WARNING, "Error initializate {0}", businessObject.getClass().getName());
                    continue;
                }
                if (!ParseDetails(businessObject, row, mainPage)) {
                    LOG.log(Level.WARNING, "Error parse of the details  for {0}", businessObject.getClass().getName());
                    continue;
                }
                bList.add(businessObject);
            }
            return bList;
        } else {
            LOG.log(Level.WARNING, "Not found rows in Main page");
            return null;
        }
    }

    @Override
    public List<T> Do() {
        List<T> listBO = new ArrayList<>();
        for (int i = 0; i < countPages; i++) {
            List<T> l = Proccess(i);
            if (l != null) {
                listBO.addAll(l);
            }
        }
        return listBO;
    }

}
