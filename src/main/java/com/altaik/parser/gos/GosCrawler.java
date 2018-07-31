/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gos;

import com.altaik.crawler.Crawler;
import com.altaik.crawler.CrawlerContext;
import com.altaik.parser.ZakupTypesEnum;
import org.jsoup.Jsoup;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.jsoup.Connection.Method.GET;

/**
 *
 * @author Aset
 */
public class GosCrawler extends Crawler {

    private HttpURLConnection httpURLConnection;
    private Proxy proxy;
    private String[] proxyList = null;

    public String buysUrl = "http://portal.goszakup.gov.kz/portal/index.php/ru/oebs/buys";
    public String openbuysUrl = "http://portal.goszakup.gov.kz/portal/index.php/ru/publictrade/openbuys";
    public String aucsUrl = "http://portal.goszakup.gov.kz/portal/index.php/ru/oebs/aucs";

    public GosCrawler(String url, CrawlerContext context) {
        super(context);
        IsUseProxy(true);
        this.setUrl(url);
        NUMBER_CRAWLING_ATTEMPTS = 15;
        isToSleep = false;
        setTimeout(20000);
        setMethod(GET);
    }

    public GosCrawler(CrawlerContext context, ZakupTypesEnum type) {
        super(context);
        IsUseProxy(true);
        isToSleep = false;
        setTimeout(20000);
        NUMBER_CRAWLING_ATTEMPTS = 15;
        setMethod(GET);
        switch (type) {
            case GosBuys:
                this.setUrl(buysUrl);
                crawlerName = "BuysGosCrawler";
                break;
            case GosOpenBuys:
                this.setUrl(openbuysUrl);
                crawlerName = "OpenBuysGosCrawler";
                break;
            case GosAucs:
                this.setUrl(aucsUrl);
                crawlerName = "AucsGosCrawler";
                break;
            default:
                break;
        }
    }


    @Override
    protected boolean DocProcess() {

        if (isToSleep) {
            Sleep();
        }

        if (IsUseProxy()) {
            if (!IsSinglProxy()) {
                context.proxies.SetNextIProxy();
            }
            proxyList = context.proxies.GetProxy(context.proxies.GetIProxy());
        }

        if (proxyList != null) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyList[0], Integer.parseInt(proxyList[1])));
        }

        try {
            httpURLConnection = GetHTTPUrlConnection();
            httpURLConnection.setRequestProperty("User-Agent", getUserAgent());

            if (getTimeout() > 0) {
                httpURLConnection.setConnectTimeout(getTimeout());
                httpURLConnection.setReadTimeout(getTimeout());
            }

            if (cookies != null) {
                String stringCookie = "";
                for (String key : cookies.keySet()) {
                    stringCookie = stringCookie.concat((key + "=" + cookies.get(key) + ";"));
                }
                httpURLConnection.setRequestProperty("Cookie", stringCookie);
            }

            if (headers != null) {
                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }

            logger.log(Level.INFO, "Crawler.getDoc: doc loading for: {0}", url);
            if (getMethod() == GET) {
                httpURLConnection.setRequestMethod("GET");
            } else {
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(GetPostDataString(data));
                writer.flush();
                writer.close();
                outputStream.close();
            }

            int responseCode = 0;
            responseCode = httpURLConnection.getResponseCode();
            String contentType = httpURLConnection.getContentType();

//            String setcookie = httpURLConnection.getHeaderField("Set-Cookie");
            List<String> responseCookie = httpURLConnection.getHeaderFields().get("Set-Cookie");
            if (responseCookie != null) {
                for (String setcookie : responseCookie) {
                    String key = setcookie.substring(0, setcookie.indexOf("="));
                    String value = setcookie.contains(";") ? setcookie.substring(setcookie.indexOf("=") + 1, setcookie.indexOf(";")) : setcookie.substring(setcookie.indexOf("=") + 1);
                    setCookie(key, value);
                }
            }

            if (responseCode == HTTP_OK && contentType.contains("text/html;")) {
                String line;
                StringBuffer buffer_page = new StringBuffer();
                BufferedReader buffer_input = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                while ((line = buffer_input.readLine()) != null) {
                    buffer_page.append(line);
                }
                doc = Jsoup.parse(String.valueOf(buffer_page));
            } else {
                logger.log(Level.WARNING, "Error code not OK(200) or content type not text/html");
                return false;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Crawler.getDoc: {0}", ex.getMessage());
            return false;
        }

        logger.log(Level.INFO, "Crawler.getDoc: doc loaded for: {0}", url);

        return true;
    }

    protected HttpURLConnection GetHTTPUrlConnection() throws Exception {
        if (httpURLConnection != null) {
            httpURLConnection.disconnect();
            httpURLConnection = null;
        }
        if (proxy != null) {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection(proxy);
        } else {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        }
        return httpURLConnection;
    }

    private String GetCookiesString(Map<String, String> cookies) {
        StringBuilder result = new StringBuilder();
        for (String key : cookies.keySet()) {
            result.append(key);
            result.append("=");
            result.append(cookies.get(key));
            result.append(";");
        }

        return result.toString();
    }

    private String GetPostDataString(Map<String, String> formdata) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String key : formdata.keySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(formdata.get(key), "UTF-8"));
        }
        return result.toString();
    }
}
