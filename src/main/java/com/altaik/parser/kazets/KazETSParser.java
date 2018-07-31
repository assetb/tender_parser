package com.altaik.parser.kazets;

import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Asus-X555LD_101 on 09.06.2017.
 */
public class KazETSParser {
    private static final int DEFAULT_SOURCE = 11;
    KazETSCrawler crawler = new KazETSCrawler(false);
    private static final Logger logger = Logger.getLogger(KazETSParser.class.getName());
    List<Purchase> purchaseList = new ArrayList<>();
    List<Lot> lotList = new ArrayList<>();
    List<Elements> elementsList = new ArrayList<>();


    public List<Elements> getMainPage() throws IOException {
//        Document document = Jsoup.connect(String.format("http://torgi.comex.kz/index.php?page=%d",page)).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").get();
//        Elements elements = document.select("table.table.table-stripped.responsive.no-wrap.display.dataTable.no-footer.dtr-inline>tbody>tr");
        crawler.setUrl("http://www.kazets.kz/index.php/ru/infotsentr/raspisanie.html");
        crawler.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        Document document = crawler.getDoc();
        Elements elements = document.select("table:not(:first-child) tr:not(:first-child)");
        Elements elements2 = document.select("table:eq(0) tr:not(:first-child)");
//        elements.forEach(System.out::println);
//        System.out.println(elements.get(0));
        elementsList.add(elements);
        elementsList.add(elements2);
        return elementsList;
    }

    public void execute() throws IOException {
        getMainPage();
        createPurchase(elementsList);
//        parser.purchaseList.forEach(System.out::println);
        createLots(elementsList);
//        parser.lotList.forEach(System.out::println);
    }


    private void createPurchase(List<Elements> elementss){
        System.out.println("Creating purchase");
        logger.log(Level.INFO,"Creating purchase");
        for (Element e : elementss.get(0)) {
            Purchase purchase = new Purchase();
//            purchase.setNumber(e.select("td:eq(0)").text());
            purchase.setSource(DEFAULT_SOURCE);
            purchase.setNumber(e.select("td:eq(3)").text().replaceAll("[^А-Я||A-Z]+","").trim().concat("-").concat(e.select("td:eq(1)").text().replaceAll("\\D*0*","")).trim());
            purchase.setRuName(e.select("td:eq(3)").text().replaceAll(" ","").trim());
            purchase.setStartDay(e.select("td:eq(1)").text().replaceAll(" ","").trim());
//            purchase.setEndDay(e.select("td:eq(4)").text());
            purchase.setOrganizer(e.select("td:eq(2)").text().replaceAll(" ","").trim());
            purchase.setStatus(e.select("td:eq(6)").text().replaceAll(" ","").trim());
            purchase.setType(e.select("td:eq(7)").text().replaceAll(" ","").trim());
            purchase.setMethod("Специализированный аукцион");
            purchaseList.add(purchase);
//            System.out.println(purchase);
        }
        for(Element e:elementss.get(1)){
            Purchase purchase = new Purchase();
            purchase.setSource(DEFAULT_SOURCE);
            purchase.setMethod("Аукцион");
            purchase.setStartDay(e.select("td:eq(1)").text().replaceAll(" ","").trim());
            purchase.setRuName(e.select("td:eq(3)").text().replaceAll(" ","").trim());
            purchase.setVenue(e.select("td:eq(2)").text().replaceAll(" ","").trim());
            StringJoiner joiner = new StringJoiner(" ");
            joiner.add(e.select("td:eq(7)>p").get(0).text()).add(e.select("td:eq(7)>p").get(1).text());
            purchase.setStatus(joiner.toString().replaceAll(" ","").trim());
            purchase.setNumber(e.select("td:eq(3)").text().replaceAll("[^А-Я||A-Z]+","").trim().concat("-").concat(e.select("td:eq(1)").text().replaceAll("\\D*0*","")).trim());
            purchaseList.add(purchase);
        }
    }

    private void createLots(List<Elements> elementss) {
        System.out.println("Creating lots");
        logger.log(Level.INFO,"Creating lots");
        for (Element e : elementss.get(0)) {
            Lot lot = new Lot();
            lot.ruName = e.select("td:eq(3)").text().replaceAll(" ","").trim();
            lot.setLotNumber("1");
            lot.setSourceId(DEFAULT_SOURCE);
            lot.setPurchaseNumber(e.select("td:eq(3)").text().replaceAll("[^А-Я||A-Z]+", "").trim().concat("-").concat(e.select("td:eq(1)").text().replaceAll("\\D*0*", "")).trim());
//            lot.negnumber = e.select("td:eq(0)").text();
            lot.quantity = "1";
//            lot.sum = e.select("td:eq(2)").text().replaceAll("&nbsp"," ").replaceAll("Тенге","").trim();
            lotList.add(lot);
//            System.out.println(lot);
        }
        for(Element e:elementss.get(1)){
            Lot lot = new Lot();
            lot.setRuName(e.select("td:eq(3)").text().replaceAll(" ","").trim());
            lot.setSourceId(DEFAULT_SOURCE);
            lot.setLotNumber("1");
            lot.setDeliveryPlace(e.select("td:eq(2)").text().trim());
            lot.setQuantity(e.select("td:eq(4)").text().replaceAll("\\D*","").trim());
            lot.setPrice(e.select("td:eq(5)").text().trim());
            lot.setRuDescription(e.select("td:eq(4)").text().trim());
            lot.setPurchaseNumber(e.select("td:eq(3)").text().replaceAll("[^А-Я||A-Z]+", "").trim().concat("-").concat(e.select("td:eq(1)").text().replaceAll("\\D*0*", "")).trim());
            lotList.add(lot);
        }
    }

}
