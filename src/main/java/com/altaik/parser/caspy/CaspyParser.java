package com.altaik.parser.caspy;

import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.parser.Sources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CaspyParser {
    private static final Logger logger = Logger.getLogger(CaspyParser.class.getName());
    List<Purchase> purchaseList = new ArrayList<>();
    List<Lot> lotList = new ArrayList<>();
    private int caspyPageCount = 3;

    public List<Purchase> getPurchases() {
        return purchaseList;
    }

    public List<Lot> getLots() {
        return lotList;
    }

    public Elements getMainPage(int page) throws IOException {
        Document document = Jsoup.connect(String.format("http://torgi.comex.kz/index.php?page=%d", page)).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").get();
        Elements elements = document.select("table.table.table-stripped.responsive.no-wrap.display.dataTable.no-footer.dtr-inline>tbody>tr");
        return elements;
    }

    public void execute(int count) throws IOException {
        setCaspyPageCount(count);
        System.out.println("get Documents");
        logger.log(Level.INFO, "get Documents");
        for (int i = 1; i < caspyPageCount; i++) {
            Elements elements = getMainPage(i);
            createPurchase(elements);
            createLots(elements);
        }
        logger.log(Level.INFO, "Begin transaction");
    }

    public void setCaspyPageCount(int pageCount) {
        this.caspyPageCount = pageCount;
    }


    private void createPurchase(Elements elementss) {
        System.out.println("Creating purchase");
        logger.log(Level.INFO, "Creating purchase");
        for (Element e : elementss) {
            Purchase purchase = new Purchase();
            purchase.setNumber(e.select("td:eq(0)").text());
            purchase.setSource(Sources.CASPY);
            purchase.setRuName(e.select("td:eq(1)").text());
            purchase.setStartDay(e.select("td:eq(3)").text());
            purchase.setEndDay(e.select("td:eq(4)").text());
            purchase.setOrganizer(e.select("td:eq(5)").text());
            purchase.setCustomer(e.select("td:eq(5)").text());
            purchase.setStatus(e.select("td:eq(6)").text());
            purchase.setMethod(e.select("td:eq(7)").text());
            purchaseList.add(purchase);

        }
    }

    private void createLots(Elements elementss) {
        System.out.println("Creating lots");
        logger.log(Level.INFO, "Creating lots");
        for (Element e : elementss) {
            Lot lot = new Lot();
            lot.ruName = e.select("td:eq(1)").text();
            lot.setLotNumber("1");
            lot.setSourceId(Sources.CASPY);
            lot.setPurchaseNumber(e.select("td:eq(0)").text());
            lot.quantity = "1";
            lot.sum = e.select("td:eq(2)").text().replaceAll("&nbsp", " ").replaceAll("Тенге", "").trim();
            lotList.add(lot);
        }
    }


}
