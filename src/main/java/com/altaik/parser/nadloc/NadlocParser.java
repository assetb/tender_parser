/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.nadloc;

import com.altaik.bo.Lot;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Parser;
import com.altaik.parser.Sources;
import com.altaik.storage.Storage;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Aset
 */
public class NadlocParser extends Parser {

    //    private final CrawlerContext crawlerContext;
    private final NadlocCrawler nadlocCrawler;

    public NadlocParser(Document doc, IDatabaseManager dbManager, NadlocCrawler nadlocCrawler) {
        super(doc, dbManager);
//        this.crawlerContext = null;
        this.nadlocCrawler = nadlocCrawler;
        parserName = "NadlocParser";
        startingRow = 2;
        setIsToDoAuth(true);
        setIsToDoLots(true);
    }

    @Override
    public Elements GetRowsWithPurchases() {
        Element table = doc.select("table").get(0);
        rows = null;
        if (null != table && !table.select("tr").isEmpty()) {
            rows = table.select("tr");
        }
        return rows;
    }

    @Override
    public void FillPurchase() {
        currentData.setSource(Sources.Nadloc);

        if (cols.size() > 0) {
            if (SelectElement(cols, 0) != null) {
                if (SelectElement(cols, 0).select("a") != null) {
                    currentData.setNumber(SelectElement(cols, 0).select("a").text());
                    currentLink.setNumber(SelectElement(cols, 0).select("a").attr("href"));
                }
                if (null != SelectElement(cols, 0).select("small")) {
                    currentData.ruName = SelectElement(cols, 0).select("small").text();
                }
            }
        } else {
            logger.log(Level.SEVERE, "Error with FillPurchase cols.size <= 0");
            return;
        }

        currentData.customer = SelectCol(cols, 1);

        if (SelectElement(cols, 1) != null && SelectElement(cols, 1).select("a") != null) {
            currentLink.customer = SelectElement(cols, 1).select("a").attr("href");
        }

        String tmpNLots = SelectCol(cols, 2);
        if (null != tmpNLots) {
            try {
                currentData.nLots = Integer.parseInt(tmpNLots);
            } catch (NumberFormatException nfex) {
                logger.log(Level.WARNING, "{0} nLots is not integer.", parserName);
            }
        }
        String tmpPriceSugg = SelectCol(cols, 3);
        if (null != tmpPriceSugg && !tmpPriceSugg.equals("-")) {
            currentData.priceSuggestion = tmpPriceSugg;
        }
        currentData.sum = SelectCol(cols, 4);
        currentData.method = SelectCol(cols, 5);
        currentData.status = SelectCol(cols, 6);
        if (SelectElement(cols, 6) != null && null != SelectElement(cols, 6).select("a")) {
            currentLink.status = SelectElement(cols, 6).select("a").attr("href");
        }
        String tmp = SelectCol(cols, 7);
        if (null != tmp && tmp.length() > 19) {
            currentData.startDay = tmp.substring(0, 19);
            currentData.endDay = tmp.substring(19);
        }
    }

    private String SelectCol(Elements cols, int numberCol, String prevValue) {
        String value = SelectCol(cols, numberCol);
        return value != null || !"".equals(value) ? value : prevValue;
    }

    @Override
    public void LotsDo() {
        String status = currentData.status;

        if (null != status && status.length() > 0 && (status.toLowerCase().contains("завершен") || status.toLowerCase().contains("отменен") || status.toLowerCase().contains("итоги") || status.toLowerCase().contains("вскрыто") || status.toLowerCase().contains("окончен"))) {
            return;
        }

//        NadlocCrawler nadlocCrawler = new NadlocCrawler();
        nadlocCrawler.setPath(currentLink.getNumber());
        nadlocCrawler.setMethod(Method.GET);
        Document docLot = nadlocCrawler.getDoc();

        Elements lotTables = docLot.select("div#plain_content_details > table");
        if (null == lotTables || lotTables.size() == 0) {
            logger.log(Level.WARNING, "Not found container div#plain_content_details for {0}. Name page {1}",
                    new Object[]{currentData.getNumber(), docLot.title()});
            return;
        }

        int countLots = 0;
        Elements lotRows, lotRowsDesc;
        int iLotsTableDesc, iMainDoc = 0;
        if (currentData.method.toLowerCase().contains("ценов")) {
            iLotsTableDesc = 6;
        } else {
            iMainDoc = 6;
            iLotsTableDesc = 7;
        }
        if (SelectElement(lotTables, 3) != null && null != SelectElement(lotTables, 3).select("tr") && SelectElement(lotTables, iLotsTableDesc) != null && SelectElement(lotTables, iLotsTableDesc).select("tr") != null) {
            lotRows = SelectElement(lotTables, 3).select("tr");
            lotRowsDesc = SelectElement(lotTables, iLotsTableDesc).select("tr");
            countLots = lotRows.size();
        } else {
            logger.log(Level.WARNING, "{1}. Not found tables for {0}",
                    new Object[]{parserName, currentData.getNumber()});
            return;
        }
        currentData.urlsToUploadFiles = new HashMap<>();
        if (iMainDoc != 0) {
            Elements mainDoc = lotTables.get(iMainDoc).select("tr");
            for (int i = 1; i < mainDoc.size(); i++) {
                if (mainDoc.size() > i && null != mainDoc.get(i).select("a")) {
                    currentData.urlsToUploadFiles.put(mainDoc.get(i).select("a").text(), mainDoc.get(i).select("a").attr("href"));
                }
            }
        }
        List<Lot> lots = new ArrayList<>();
        Lot prevLot = new Lot();
        for (int i = 2; i < countLots; i++) {

            Elements colsLot = SelectElement(lotRows, i).select("td");
            Elements colsLotDesc = SelectElement(lotRowsDesc, i).select("td");

            Lot lotData = new Lot();

            lotData.purchaseNumber = currentData.getNumber();
            lotData.lotNumber = SelectCol(colsLot, 0);
            if (SelectElement(colsLot, 1) != null) {
                lotData.skp = SelectElement(colsLot, 1).ownText();
                if (null != SelectElement(colsLot, 1).select("small")) {
                    lotData.ruName = SelectElement(colsLot, 1).select("small").text();
                }
            }
            lotData.ruDescription = SelectCol(colsLot, 2, prevLot.ruDescription);
            lotData.unit = SelectCol(colsLot, 3, prevLot.unit);
            lotData.quantity = SelectCol(colsLot, 4, prevLot.quantity);
//            lotData.price = SelectCol(colsLot, 5);
            lotData.sum = SelectCol(colsLot, 5, prevLot.sum);
            lotData.deliverySchedule = SelectCol(colsLot, 6, prevLot.deliverySchedule);
            lotData.deliveryPlace = SelectCol(colsLot, 7, prevLot.deliveryPlace);
            lotData.deliveryTerms = SelectCol(colsLot, 8, prevLot.deliveryTerms);
            if (colsLotDesc.size() > 5) {
                Element descEle = SelectElement(colsLotDesc, 0);
                Element hrefEle = SelectElement(descEle.select("a"), 0);
                if (hrefEle != null) {
                    if (!currentData.method.toLowerCase().contains("цено")) {
                        currentData.urlsToUploadFiles.put(lotData.lotNumber + " " + hrefEle.ownText(), hrefEle.attr("href"));
                    } else if (currentData.urlsToUploadFiles.isEmpty()) {
                        currentData.urlsToUploadFiles.put(lotData.lotNumber + " " + hrefEle.ownText(), hrefEle.attr("href"));
                    }
                }
            }
            lots.add(lotData);
            prevLot = lotData;
        }
        prevLot = null;
        currentData.lots = lots;
    }

    @Override
    public void UploadFiles() {
        Response res;
        Storage storage;
        File f;
        if (null == currentData.urlsToUploadFiles || currentData.urlsToUploadFiles.isEmpty()) {
            logger.log(Level.INFO, "Not files in purchers number {0}", currentData.getNumber());
            return;
        }
        logger.log(Level.INFO, "Start load files. Count files = {0}", currentData.urlsToUploadFiles.size());
        storage = new Storage("Nadloc");
        storage.CreateFolder(currentData.getNumber());
        for (String title : currentData.urlsToUploadFiles.keySet()) {
//            NadlocCrawler nadlocCrawler = new NadlocCrawler();
            nadlocCrawler.setUrl(currentData.urlsToUploadFiles.get(title));
            nadlocCrawler.setMethod(Method.GET);
            nadlocCrawler.setIgnoreContentType(true);
            nadlocCrawler.getDoc();
            res = nadlocCrawler.getResponse();
            storage.LoadFile(title, res.bodyAsBytes());
        }
        if (storage != null && storage.GetCountFiles() > 0) {
            if (storage.ExtractZip(true)) {
                currentData.isDocs = 1;
                currentData.pathToStogare = storage.GetPathToZip();
            } else {
                currentData.isDocs = 0;
                currentData.pathToStogare = storage.GetPath();
            }
        }
    }

}
