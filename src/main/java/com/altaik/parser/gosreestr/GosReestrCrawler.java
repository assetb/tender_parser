/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gosreestr;

import com.altaik.crawler.Crawler;
import com.altaik.crawler.ICrawler;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author admin
 */
public class GosReestrCrawler extends Crawler implements ICrawler{
    public GosReestrCrawler(){
        baseUrl = "https://e-auction.gosreestr.kz";
    }   
    
    public Map<String, String> getFormData(Document document, String selectorForm){
        Map formData = new HashMap();
        if(document != null){
            Elements elements = document.select(String.format("%s input", selectorForm));
            elements.stream().forEach((element) -> {
                formData.put(element.attr("name"), element.attr("value"));
            });
        }
        return formData;
    }
}
