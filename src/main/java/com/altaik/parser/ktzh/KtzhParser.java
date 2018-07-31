package com.altaik.parser.ktzh;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Asus-X555LD_101 on 22.05.2017.
 */
public class KtzhParser {

    private static List<String> urls = new ArrayList<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");


    private static Elements getMainPage(){
        KtzhCrawler crawler = new KtzhCrawler();
        crawler.setUrl("http://www.ktzh-gp.kz/tenders/auction/");
        crawler.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36");
        crawler.isToSleep = false; // FIXME: 05.05.2017
        crawler.minIntervalSleep = 5;
        crawler.maxIntervalSleep = 10;
        Document document = crawler.getDoc();
        Elements elements = document.select("div.dlock_nuse_list>div");
        return elements;

    }

    private static void isPurchaseNew(Elements elements){
        elements.forEach(element -> {
            try {
                Date date = dateFormat.parse(element.select("span").text());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                if(date.before(new Date()) || date.equals(new Date())){ // FIXME: 22.05.2017
                    urls.add(element.select("a").attr("href"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    public void createPurchase(){

    }







    public static void main(String[] args) throws Exception {
        isPurchaseNew(getMainPage());
        MSDocExtrator extrator = new MSDocExtrator();
        extrator.setFilePath("D:\\temp\\test\\");
        extrator.downloadFile(urls.get(1));
        extrator.extractText(new File("D:\\temp\\test\\62162017.doc"))  ;



    }

}
