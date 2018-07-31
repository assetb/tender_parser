/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.crawler;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aset
 */
public class Crawler implements ICrawler {

    protected static final Logger logger = Logger.getLogger(Crawler.class.getName());
    protected String crawlerName;
    protected CrawlerContext context;

    private int bodySize = 0;

    protected Connection con = null;
    protected Document doc = null;
    protected String url = null;
    protected String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
    protected int timeout = 14000;
    protected Method method = Method.POST;
    private boolean ignoreContentType;
    private boolean followRedirects = true;
    protected Map<String, String> headers;
    protected Map<String, String> data;
    protected Map<String, String> cookies;
    protected String referrer;

    public String baseUrl = null;
    public ArrayList<String[]> params = null;

    public boolean isToSleep = false;
    public int minIntervalSleep = 10;
    public int maxIntervalSleep = 20;

    public int NUMBER_CRAWLING_ATTEMPTS = 10;
    protected boolean isUseProxy = false;
    protected boolean isSinglProxy = false;
    public boolean isToInitializeResponse = false;

    public String docTitle;

    public Crawler() {
        isUseProxy = false;
    }

    public Crawler(CrawlerContext context) {
        this.context = context;
        isUseProxy = !(this.context == null || this.context.proxies == null);
    }

    // <editor-fold defaultstate="collapsed" desc="Methods">
    protected void Sleep() {
        Random random = new Random();
        if (minIntervalSleep > maxIntervalSleep) {
            logger.log(Level.WARNING, "Error Sleep minIntervalSleep > maxIntervalSleep");
            return;
        }
        try {
            long range = (long) maxIntervalSleep - (long) minIntervalSleep + 1;
            long fraction = (long) (range * random.nextDouble());
            int randomNumber = (int) (fraction + minIntervalSleep);
            logger.log(Level.INFO, "Main thread sleep ({0} second).", randomNumber);
            Thread.sleep(randomNumber * 1000);
        } catch (InterruptedException ex) {
            logger.log(Level.WARNING, "{0}: Error during sleep in DoProcess({1})", ex.getMessage());
        }
    }

    protected boolean DocProcess() {
        if (isToSleep) {
            Sleep();
        }

        if (isUseProxy) {
            ConClose();
            context.proxies.SetNextProxy();
        }

        con = GetConnection();

        if (getTimeout() > 0) {
            con.timeout(getTimeout());
        }

        if (getBodySize() > 0) {
            con.maxBodySize(getBodySize());
        }
        con.ignoreContentType(isIgnoreContentType());
        con.url(url);
        if (headers != null) {
            headers.keySet().stream().forEach((key) -> {
                con.header(key, headers.get(key));
            });
        }
        
        if (data != null) {
            con.data(data);
        }

        con.followRedirects(isFollowRedirects());

        if (!isUseProxy) {
            if (cookies != null) {
                con.cookies(cookies);
            }
            if (referrer != null) {
                con.referrer(referrer);
            }
        }

        if (null != params && params.size() > 0) {
            for (String[] param : params) {
                con = con.data(param[0], param[1]);
                logger.log(Level.INFO, "Con params: {0}  {1}", new Object[]{param[0], param[1]});
            }
        }

        logger.log(Level.INFO, "Crawler.getDoc: doc loading for: {0}", url);

        try {
            con.method(method).execute();

            if (!isUseProxy) {
                if (cookies == null) {
                    cookies = new HashMap<>();
                }
                if (getResponse().cookies() != null) {
                    cookies.putAll(getResponse().cookies());
                }
                referrer = (getResponse() == null) ? "" : getResponse().url().toString();
            }

            
            if (null != getResponse()) {
                if (getResponse().contentType() != null && getResponse().contentType().contains("text/html;")) {
                    doc = getResponse().parse();
                    docTitle = doc.title();
                }
            } else {
                doc = null;
                return false;
            }

            if (getRequest() != null) {
                getRequest().data().clear();
            }
            data = null;
            
            logger.log(Level.INFO, "Crawler.getDoc: doc loaded for: {0}", url);

            return true;

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Crawler.getDoc: {0}", ex.getMessage());
            return false;
        }
    }

    @Override
    public Document getDoc() {
        if (doc == null) {
            return GetNewDoc();
        }
        return doc;
    }

    protected Connection GetConnection() {
        if (con == null) {
            con = Jsoup.connect(url).userAgent(userAgent);
        }
        return con;
    }

    protected Document GetNewDoc() {
        if (Verify()) {
            for (int i = 0; i < NUMBER_CRAWLING_ATTEMPTS && !DocProcess(); i++) {
                close();
            }

            if (!isIgnoreContentType() && doc == null) {
                logger.log(Level.WARNING, "Crawler.getDoc for Url: {0} FAILED.", url);
            }

            return doc;
        }
        return null;
    }

    public boolean Verify() {
        if (url == null) {
            logger.log(Level.WARNING, "URL not set.");
            return false;
        }

        return true;
    }

    protected void DocClose() {
        if (null != doc) {
            doc.empty();
            doc = null;
        }
    }

    protected void ConClose() {
        if (null != getResponse()) {
            if (getResponse().cookies() != null) {
                getResponse().cookies().clear();
            }
            if (getResponse().headers() != null) {
                getResponse().headers().clear();
            }
//            setResponse(null);
        }

        if (null != getRequest()) {
            if (getRequest().cookies() != null) {
                getRequest().cookies().clear();
            }
            if (null != getRequest().headers()) {
                getRequest().headers().clear();
            }
            if (null != getRequest().data()) {
                getRequest().data().clear();
            }
            setRequest(null);
        }

        if (con != null) {
            con = null;
        }
    }

    @Override
    public void close() {
        DocClose();
        ConClose();
    }
    
    @Override
    public void setHeader(String name, String value) {
        if (null == headers) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    @Override
    public void clearData() {
        getRequest().data().clear();
    }

    @Override
    public void setData(Map<String, String> data, boolean overide) {
        if (overide) {
            this.data = data;
        } else {
            if (this.data == null) {
                this.data = new HashMap<>();
            }
            this.data.putAll(data);
        }
    }

    @Override
    public Connection.Response getResponse() {
        if (con != null) {
            return con.response();
        }
        return null;
    }

    @Override
    public Connection.Request getRequest() {
        if (con != null) {
            return con.request();
        }
        return null;
    }

    @Override
    public void setRequest(Connection.Request request) {
        if (con != null) {
            con.request(request);
        }
    }

    @Override
    public Map<String, String> getCookies() {
        return cookies;
    }

    @Override
    public void setCookie(String key, String value) {
        if (cookies == null) {
            cookies = new HashMap<>();
        }
        cookies.put(key, value);
    }
    
    @Override
    public void setUrlPath(String path) {
        setUrl(baseUrl + path);
    }
    // </editor-fold>
//
//    public Map<String,String> getHeaders(){
//        if(getRequest()!=null) return getRequest().headers();
//        return null;
//    }
//
//    public Collection<KeyVal> getData(){
//        if(getRequest()!=null) return getRequest().data();
//        return null;
//    }
// <editor-fold defaultstate="collapsed" desc="Setters and Gettes">

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int getBodySize() {
        return bodySize;
    }

    @Override
    public void setBodySize(int size) {
        bodySize = size;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public ArrayList<String[]> getParams() {
        return params;
    }

    @Override
    public void setParams(ArrayList<String[]> params) {
        this.params = params;
    }

    @Override
    public boolean isIgnoreContentType() {
        return ignoreContentType;
    }

    @Override
    public void setIgnoreContentType(boolean ignoreContentType) {
        this.ignoreContentType = ignoreContentType;
    }

    @Override
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    @Override
    public boolean isFollowRedirects() {
        return this.followRedirects;
    }

    @Override
    public boolean IsUseProxy() {
        return isUseProxy;
    }

    @Override
    public void IsUseProxy(boolean useproxy) {
        isUseProxy = useproxy;
    }

    @Override
    public boolean IsSinglProxy() {
        return isSinglProxy;
    }

    @Override
    public void IsSinglProxy(boolean issingproxy) {
        isSinglProxy = issingproxy;
    }

// </editor-fold>
}
