/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.crawler;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Aset
 */
public interface ICrawler {
        
    Document getDoc();
    
    Method getMethod();

    int getTimeout();

    String getUrl();

    String getUserAgent();

    void setMethod(Method method);

    void setTimeout(int timeout);

    void setUrl(String url);
    
    void setUrlPath(String path);

    void setUserAgent(String userAgent);
    
    public ArrayList<String[]> getParams();

    public void setParams(ArrayList<String[]> params);
    
    public int getBodySize();
    
    public void setBodySize(int bodySize);
    
    public void setData(Map<String, String> data,boolean overide);
    
    public Connection.Response getResponse();
    
    public Connection.Request getRequest();
    
    public void setRequest(Connection.Request request);
    
    public void clearData();
    
    public void setHeader(String name, String value);
    
    public boolean isIgnoreContentType();
    
    public void setIgnoreContentType(boolean ignoreContentType);
    
    public void setFollowRedirects(boolean followRedirects);
    
    public boolean isFollowRedirects();
    
    public Map<String,String> getCookies();
    
    public void setCookie(String key, String value);
    
    public boolean IsUseProxy();
    
    public void IsUseProxy(boolean useproxy);
 
    public boolean IsSinglProxy();
    
    public void IsSinglProxy(boolean issingproxy);
    
    public void close();
}
