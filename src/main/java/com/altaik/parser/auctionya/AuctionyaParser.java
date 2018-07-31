package com.altaik.parser.auctionya;


import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.auctionya.utils.EaucSaver;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionyaParser {


    private static final Logger logger = Logger.getLogger(AuctionyaParser.class.getName());
    private AuctionyaCrawler crawler = new AuctionyaCrawler();
    private List<Elements> elementsList = new ArrayList<>();
    private Set<String> urlsSet = new LinkedHashSet<>();
    private List<Purchase> purchaseList = new ArrayList<>();
    private List<Lot> lotList = new ArrayList<>();
    private int purchaseCount = 0;
//    private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    private void getPurchaseURL() {
        crawler.setUrl("http://auction.ya.kz/");
        crawler.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36");
        crawler.isToSleep = false; // FIXME: 05.05.2017
        crawler.minIntervalSleep = 5;
        crawler.maxIntervalSleep = 10;
        Document document = crawler.getDoc();
        Elements elements = document.select("table#example tbody>tr>td.tt>a");
        elements.forEach(element -> urlsSet.add(element.attr("href")));
    }


    private void documentElements(AuctionyaCrawler crawler, Set<String> urls) {
        ArrayList<String> list = new ArrayList<>(urls);
        if(purchaseCount == 0){
            purchaseCount = list.size();
        }
        for (int i = 0; i < purchaseCount; i++) {
            crawler.setUrl(crawler.baseUrl + list.get(i));
            Document document = crawler.getDoc();
            elementsList.add(purchaseInfo(document));
        }
    }



    private Elements purchaseInfo(Document document) {
        Elements elements = document.select("table.table.table-bordered tr:not(:first-child)");
        return elements;
    }

    private void createPurchase(List<Elements> elementss) {
        for (Elements e : elementss) {
            Purchase purchase = new Purchase();
            purchase.setNumber(e.get(0).select("td:eq(1)").text());
            purchase.ruName = e.get(10).select("td:eq(1)").text();
            purchase.organizer = e.get(2).select("td:eq(1)").text();
            purchase.startDay = e.get(6).select("td:eq(1)").text();
            purchase.method = e.get(7).select("td:eq(1)").text();
            purchase.venue = e.get(9).select("td:eq(1)").text();
            purchase.status = e.get(11).select("td:eq(1)").text();
            purchase.type = e.get(8).select("td:eq(1)").text();
            purchase.setSource(8);
            purchaseList.add(purchase);
        }
    }

    private void createLots(List<Elements> elementss) {
        for (Elements e : elementss) {
            Lot lot = new Lot();
            lot.ruName = e.get(10).select("td:eq(1)").text();
            lot.setLotNumber("1");
            lot.setSourceId(8);
            lot.setPurchaseNumber(e.get(0).select("td:eq(1)").text());
            lot.ruDescription = e.get(1).select("td:eq(1)").text();
            lot.quantity = "1";
            lot.price = e.get(3).select("td:eq(1)").text();
            lot.sum = e.get(3).select("td:eq(1)").text();
            lot.deliveryPlace = e.get(5).select("td:eq(1)").text();
            lotList.add(lot);
        }
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }


    public static void execute(int purchaseCount, IDatabaseManager databaseManager) {
        EaucSaver saver = new EaucSaver(databaseManager);
        AuctionyaParser parser = new AuctionyaParser();

        parser.setPurchaseCount(purchaseCount);

        System.out.println("get URLS");
        logger.log(Level.INFO,"get URLS");
        parser.getPurchaseURL();

        System.out.println("get Documents");
        logger.log(Level.INFO,"get Documents");
        parser.documentElements(parser.crawler, parser.urlsSet);

        System.out.println("Closing parser");
        logger.log(Level.INFO,"Closing parser");
        parser.crawler.close();

        System.out.println("Creating purchase");
        logger.log(Level.INFO,"Creating purchase");
        parser.createPurchase(parser.elementsList);

        System.out.println("Creating lots");
        logger.log(Level.INFO,"Creating lots");
        parser.createLots(parser.elementsList);

        System.out.println("Begin transaction");
        logger.log(Level.INFO,"Begin transaction");
        saver.save(parser.purchaseList,parser.lotList);


    }




}
