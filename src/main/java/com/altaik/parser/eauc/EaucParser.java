package com.altaik.parser.eauc;


import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.crawler.Crawler;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Sources;
import com.altaik.parser.auctionya.utils.EaucSaver;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class EaucParser {
    List<Purchase> purchases = new ArrayList<>();
    List<Lot> lots = new ArrayList<>();
    Crawler crawler = new EaucCrawler();
//    private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public static void execute(IDatabaseManager databaseManager) {
        EaucParser eaucParser = new EaucParser();
        EaucSaver saver = new EaucSaver(databaseManager);

        for (String ulr : eaucParser.getUrls()) {
            List<String> info = eaucParser.getPurchaseInfo(ulr);
            eaucParser.createPurchase(info);
            eaucParser.createLot(info);
        }
//        System.out.println(eaucParser.purchases);
//        System.out.println(eaucParser.lots);
        saver.save(eaucParser.purchases, eaucParser.lots);
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public List<Lot> getLots() {
        return lots;
    }

    public List<String> getUrls() {
        crawler.setUrl("http://www.eauc.kz/AuctionOne/Auction.nsf/AUCTION_Active.xsp");
        crawler.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        Document document = crawler.getDoc();
//        Document document = Jsoup.connect("").get();
        int iPage = 1;
        int countPages = 10;
        List<String> urls = new ArrayList<>();
        Elements formInputs = document.select("form[id=\"view:_id1\"] input[type=\"hidden\"][name=\"$$viewid\"]");
        do {
            Elements elements = document.select("div.row.search-result a");

            elements.forEach(element -> {
                if (elements.indexOf(element) % 2 == 0) {
                    urls.add(element.attr("href"));
                }
            });
            if (++iPage <= countPages) {

                document = scrollPage(iPage, formInputs);
            }
        } while (iPage <= countPages);
        return urls;
    }

    private Document scrollPage(int page, Elements formInputs) {
        int countRows = 10;
        crawler.setUrl("http://www.eauc.kz/AuctionOne/Auction.nsf/AUCTION_Active.xsp/getrows");
        ArrayList<String[]> params = new ArrayList<>();
        for (Element input :
                formInputs) {
            params.add(new String[]{input.attr("name"), input.val()});
        }
        params.add(new String[]{"$$axtarget", "view:_id1:searchlist"});
        params.add(new String[]{"first", "" + (page * 10)});
        params.add(new String[]{"rows", "" + (countRows)});
        crawler.setParams(params);
        return crawler.getDoc();
    }

    public List<String> getPurchaseInfo(String url) {
        List<String> purchaseInfo = new ArrayList<>();
        crawler.params = null;
        crawler.setUrl("http://www.eauc.kz/AuctionOne/Auction.nsf/" + url);
        Document document1 = crawler.getDoc();
//        Document document1 = Jsoup.connect("http://www.eauc.kz/AuctionOne/Auction.nsf/" + url).get();
        purchaseInfo.add(document1.select("h1.page-header>span:not(:first-child)").text());
        purchaseInfo.add(document1.select("p.lead> span").text());
        Elements elements1 = document1.select("div.row dd");
        elements1.forEach(element -> purchaseInfo.add(element.text()));
        return purchaseInfo;
    }

    public void createPurchase(List<String> purchaseInfo) {
        Purchase purchase = new Purchase();
        purchase.startDay = purchaseInfo.get(3);
        purchase.status = purchaseInfo.get(5);
        purchase.method = purchaseInfo.get(6);
        purchase.venue = purchaseInfo.get(7);
        purchase.type = purchaseInfo.get(8);
        purchase.setNumber(purchaseInfo.get(0));
        purchase.ruName = purchaseInfo.get(12);
        purchase.publishDay = purchaseInfo.get(14);
        purchase.customer = purchaseInfo.get(10);
        purchase.organizer = purchaseInfo.get(15);
        purchase.endDay = purchaseInfo.get(3).replaceAll("\\s+\\d+:\\d+:\\d+$", "");
        purchase.setSource(Sources.EAUC);
        purchases.add(purchase);
    }

    public void createLot(List<String> purchaseInfo) {
        Lot lot = new Lot();
        lot.setSourceId(Sources.EAUC);
        lot.setLotNumber("1");
        lot.setPurchaseNumber(purchaseInfo.get(0));
        lot.setRuName(purchaseInfo.get(12));
        lot.setRuDescription(purchaseInfo.get(1));
        lot.setQuantity("1");
        lot.setDeliveryPlace(purchaseInfo.get(9));
        lot.setPrice(purchaseInfo.get(4).replaceAll(" ", " ").replaceAll(" тңг.", ""));
        lot.setSum(lot.getPrice());
//        lot.setSource("9");
        lots.add(lot);
    }


}

