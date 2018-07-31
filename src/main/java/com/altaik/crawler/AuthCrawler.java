/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.crawler;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Aset
 */
public class AuthCrawler extends Crawler implements IAuthCrawler{

    public boolean isCertificated = false;
    protected Properties certsProps;
    
    public boolean isWorking = false;

    public AuthCrawler() {
        super();
    }

    @Override
    public void AuthInit(Properties certsProps) {
        close();
        this.certsProps = certsProps;
    }

    protected void Certificate() {
        if(certsProps.getProperty("keyStore"          ) != null && !certsProps.getProperty("keyStore"          ).isEmpty()
        && certsProps.getProperty("keyStorePassword"  ) != null && !certsProps.getProperty("keyStorePassword"  ).isEmpty()
        && certsProps.getProperty("keyStoreType"      ) != null && !certsProps.getProperty("keyStoreType"      ).isEmpty()
        && certsProps.getProperty("trustStore"        ) != null && !certsProps.getProperty("trustStore"        ).isEmpty()
        && certsProps.getProperty("trustStorePassword") != null && !certsProps.getProperty("trustStorePassword").isEmpty()) {
            System.setProperty("javax.net.ssl.trustStore",          certsProps.getProperty("trustStore"));
            System.setProperty("javax.net.ssl.trustStorePassword",  certsProps.getProperty("trustStorePassword" ));
            System.setProperty("javax.net.ssl.keyStore",            certsProps.getProperty("keyStore"  ));
            System.setProperty("javax.net.ssl.keyStorePassword",    certsProps.getProperty("keyStorePassword"   ));
            System.setProperty("javax.net.ssl.keyStoreType",        certsProps.getProperty("keyStoreType"   ));
            isCertificated = true;
            isWorking = true;
        }
    }

    
    @Override
    protected Connection GetConnection() {
        if (con == null) {
            if(!isCertificated){
                Certificate();
            }
            con = Jsoup.connect(url).userAgent(userAgent);
            logger.log(Level.INFO, "Auth Connection created.");
        }
        return con;
    }
    
    
    @Override
    public Document getDoc(){
//        Proxies.SetNullProxy();
        return GetNewDoc();
    }
    

    @Override
    public Document getCurrentDoc(){
        return doc;
    }
    

    @Override
    public void setRedirect(boolean isRedirect) {
        con.followRedirects(isRedirect);
    }

    @Override
    public void close() {
        isCertificated = false;

        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.keyStoreType");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");

        super.close();
    }

    
    @Override
    public Map<String, String> getFormData(Document doc) {
        if (doc == null) {
            return null;
        }

        Elements els = doc.select("form input[name]");
        if (els.isEmpty()) {
            return null;
        }
    
        Map<String, String> localData = new HashMap<>();

        for (Element el : els) {
            localData.put(el.attr("name"), el.attr("value"));
        }
        
        return localData;
        
    }


    
    @Override
    public Map<String, String> getFormData(Document doc, String onClickObjectRef) {
        return getFormData(doc, onClickObjectRef,"onclick");
    }
    
    
    @Override
    public Map<String, String> getFormData(Document doc, String onClickObjectRef, String event) {
        if (onClickObjectRef == null) {
            return null;
        }
        
        Map<String, String> localData = getFormData(doc);
        if (localData == null) {
            return null;
        }

        String text = doc.select(onClickObjectRef).attr(event);
        if ("".equals(text)) {
            return null;
        }

        text = text.replace("\\", "");

        while (text.length() > 0 && text.contains("{") && text.contains("})")) {
            int i = text.indexOf("})");
            String sText = text.substring(text.indexOf("{") + 1, i);
            text = text.substring(i + 1);
            while (sText.length() > 0) {
                String name = sText.substring(0, sText.indexOf(":")).replace("'", "");

                i = sText.indexOf(",");
                if (i == -1) {
                    i = sText.length();
                }

                String value = sText.substring(sText.indexOf(":") + 1, i).replace("'", "");
                
                if(localData == null) 
                    localData = new HashMap<>();
                localData.put(name, value);

                if (sText.length() == i) {
                    sText = "";
                } else {
                    sText = sText.substring(i + 1);
                }
            }
        }
        return localData;
    }
    
    
    @Override
    public Document SubmitAction(Map<String,String> data){
        if(data == null || data.isEmpty()) return null;
        setMethod(Connection.Method.POST);
        setData(data,true);
        setUrl(baseUrl + doc.select("form").attr("action"));
        return getDoc();
    }

}
