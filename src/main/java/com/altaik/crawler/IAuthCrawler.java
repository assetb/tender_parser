/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.crawler;

import java.util.Map;
import java.util.Properties;
import org.jsoup.nodes.Document;

/**
 *
 * @author admin
 */
public interface IAuthCrawler extends ICrawler {

    void AuthInit(Properties certsProps);

    Document SubmitAction(Map<String, String> data);

    @Override
    void close();

    Document getCurrentDoc();

    @Override
    Document getDoc();

    Map<String, String> getFormData(Document doc);

    Map<String, String> getFormData(Document doc, String onClickObjectRef);

    Map<String, String> getFormData(Document doc, String onClickObjectRef, String event);

    void setRedirect(boolean isRedirect);
    
}
