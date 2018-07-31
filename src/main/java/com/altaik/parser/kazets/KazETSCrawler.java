package com.altaik.parser.kazets;

import com.altaik.crawler.Crawler;

/**
 * Created by Asus-X555LD_101 on 09.06.2017.
 */
public class KazETSCrawler extends Crawler {

    private String  baseURL = "http://www.kazets.kz/index.php/ru/infotsentr/raspisanie.html";



    public KazETSCrawler(Boolean isSleep){
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

