package com.altaik.parser.caspy;

import com.altaik.crawler.Crawler;

/**
 * Created by Asus-X555LD_101 on 09.06.2017.
 */
public class CaspyCrawler extends Crawler {

    private String  baseURL = "http://torgi.comex.kz/";



    public CaspyCrawler(Boolean isSleep){
        this.setUrl(baseUrl);
        this.isToSleep = isSleep;
    }

    public void setSleepInterval(int min, int max){
        this.minIntervalSleep = min;
        this.maxIntervalSleep = max;
    }


    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
}
