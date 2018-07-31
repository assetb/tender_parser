/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.samruk;

import com.altaik.crawler.AuthCrawler;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.logging.Level;

/**
 * @author Aset
 */
public class SamrukAuthCrawler extends AuthCrawler {
    public String loginUrl;
    public String fullOA_HTML;

    public SamrukAuthCrawler() {
        baseUrl = "https://tender.sk.kz";
        loginUrl = baseUrl + "/OA_HTML/AppsLocalLogin.jsp?langCode=RU";
        fullOA_HTML = baseUrl + "/OA_HTML/";
        setBodySize(20 * (int) Math.pow(10, 6));
    }

    public boolean authSamruk(String password) {
        if (password == null || password.isEmpty()) {
            logger.log(Level.WARNING, "Password is null or empty");
            return false;
        }

        setUrl(loginUrl);
        setMethod(Connection.Method.GET);
        Document _doc = getDoc();
        if (_doc == null) return false;

        Map<String, String> submitData = getFormData(_doc, "button#SubmitButton");
        if (submitData == null) return false;
        submitData.put("passwordField", password);

        if (SubmitAction(submitData) == null) {
            return false;
        }

        logger.log(Level.FINE, "Samruk authorized.");
        return true;
    }

}
