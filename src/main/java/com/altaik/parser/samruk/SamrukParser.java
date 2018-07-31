/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.samruk;

import com.altaik.bo.Lot;
import com.altaik.crawler.CrawlerContext;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Parser;
import com.altaik.parser.Sources;
import com.altaik.storage.Storage;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Aset
 */
public class SamrukParser extends Parser {

    private final SamrukAuthCrawler authCrawler;
    private final CrawlerContext crawlerContext;

    public SamrukParser(Document doc, IDatabaseManager dbManager, CrawlerContext crawlerContext, SamrukAuthCrawler authCrawler) {
        super(doc, dbManager);
        this.setIsToDoLots(true);
        this.crawlerContext = crawlerContext;
        this.authCrawler = authCrawler;
        parserName = "SamrukParser";
    }

    @Override
    public Elements GetRowsWithPurchases() {
        Element table = doc.select("table").get(3);
        rows = null;
        if (null != table && !table.select("tr").isEmpty()) {
            rows = table.select("tr");
        }
        return rows;
    }

    @Override
    public void FillPurchase() {
        currentData.setSource(Sources.Samruk);
        currentData.setNumber(SelectCol(cols, 0));
        currentData.status = SelectCol(cols, 6);
        if (cols.size() > 0 && null != cols.get(0).select("a")) {
            currentLink.setNumber(cols.get(0).select("a").attr("href"));
        }
        currentData.link = currentLink.getNumber();
    }

    @Override
    public void LotsDo() {
        int i = 0;
        int nAttemps = 5;
        boolean isOk = false;
        while (i < nAttemps && !isOk) {
            try {
                LotsDoProcess();
                isOk = true;
            } catch (Exception ex) {
                logger.log(Level.INFO, "{0}: Error Lot Parsing: {1}\n {2}: Lot Parsing Trying Next ...",
                        new Object[]{parserName, ex.getMessage(), parserName});
                i++;
                if (i == nAttemps) {
                    logger.log(Level.WARNING, "{0}: Lot Parsing FAILED.", parserName);
                }
            }
        }
    }

    private void LotsDoProcess() throws Exception {
        SamrukCrawler samrukCrawler = new SamrukCrawler(currentData.getNumber(), crawlerContext);
        samrukCrawler.setUrl(currentLink.getNumber());
        Document docLot = samrukCrawler.getDoc();
        Elements tables = docLot.select("table.showtab");

        if (null == tables || tables.size() == 0) {
            logger.log(Level.WARNING, "{1}. Not found tables for {0}",
                    new Object[]{parserName, currentData.getNumber()});
            return;
        }

        Element negTable = tables.first();
        Elements negRows = negTable.select("tr");
        String str = SelectTD1(negRows, 0);
        if (null != str) {
            currentData.ruName = str.substring(0, str.indexOf("/")).trim();
            currentData.kzName = str.substring(str.indexOf("/") + 1).trim();
        }
        currentData.customer = SelectTD1(negRows, 1);
        currentData.organizer = SelectTD1(negRows, 2);
        currentData.venue = SelectTD1(negRows, 3);
        currentData.method = SelectTD1(negRows, 4);
        currentData.startDay = SelectTD1(negRows, 5);
        currentData.endDay = SelectTD1(negRows, 6);

        Element lotTable = docLot.select("table.showtab").last();
        Elements lotRows = lotTable.select("tr");
        List<Lot> lots = new ArrayList<>();
        for (int i = 1; i < lotRows.size(); i++) {
            Element lotrow = lotRows.get(i);
            Elements lotcols = lotrow.select("td");
            Lot samrukLotData = new Lot();
            samrukLotData.setSourceId(Sources.Samruk);
            samrukLotData.purchaseNumber = currentData.getNumber();
            samrukLotData.lotNumber = SelectCol(lotcols, 0);
            samrukLotData.link = "http://tender.sk.kz/index.php/ru/negs" + "/show/" + currentData.getNumber();
            String tmpName = SelectCol(lotcols, 1);

            if (null != tmpName) {

                if (tmpName.contains("/")) {
                    samrukLotData.kzName = tmpName.substring(0, tmpName.indexOf("/")).trim();
                    samrukLotData.ruName = tmpName.substring(tmpName.indexOf("/") + 1).trim();
                } else {
                    samrukLotData.ruName = tmpName;
                }
            }

            String tmpDesc = SelectCol(lotcols, 2);

            if (null != tmpDesc) {

                if (tmpDesc.contains("/")) {
                    samrukLotData.kzDescription = tmpDesc.substring(0, tmpDesc.indexOf("/")).trim();
                    samrukLotData.ruDescription = tmpDesc.substring(tmpDesc.indexOf("/") + 1).trim();
                } else {
                    samrukLotData.ruDescription = tmpDesc.substring(0, tmpDesc.indexOf("/")).trim();
                }
            }

            samrukLotData.quantity = SelectCol(lotcols, 3);
            samrukLotData.price = SelectCol(lotcols, 4);
            samrukLotData.sum = SelectCol(lotcols, 5);
            samrukLotData.deliveryPlace = SelectCol(lotcols, 6);
            samrukLotData.deliverySchedule = SelectCol(lotcols, 7);
            samrukLotData.deliveryTerms = SelectCol(lotcols, 8);
            lots.add(samrukLotData);
        }

        currentData.lots = lots;
    }

    @Override
    public void UploadFiles() {
        Document homePurchaseDoc = authCrawler.getCurrentDoc();
        authCrawler.setIgnoreContentType(true);
        Map<String, String> homePurchaseData = authCrawler.getFormData(homePurchaseDoc, "button#GoBtn");
        homePurchaseData.put("SearchKeyword", currentData.getNumber());
        Elements selectFindElms = homePurchaseDoc.select("select#SearchCriteriaPoplist");
        homePurchaseData.put(selectFindElms.attr("name"), selectFindElms.select("option[value^=\"Number\"]").attr("value"));

        Document purchaseSearchingDoc = authCrawler.SubmitAction(homePurchaseData);
        Elements aToPurchase = purchaseSearchingDoc.select("a[title=\"" + currentData.getNumber() + "\"]");
        if (aToPurchase.isEmpty()) {
            logger.log(Level.WARNING, "Error not found purchase({0})", currentData.getNumber());
            authCrawler.setIgnoreContentType(false);
            authCrawler.setUrl(authCrawler.baseUrl + purchaseSearchingDoc.select("a#PON_SOURCING_SUPPLIER").attr("href"));
            authCrawler.getDoc();
            return;
        }

        authCrawler.setUrl(authCrawler.fullOA_HTML + aToPurchase.attr("href"));
        authCrawler.setMethod(Connection.Method.GET);
        Document purchaseDoc = authCrawler.getDoc();

        String urlToTheBack = purchaseDoc.select("a#PON_SOURCING_SUPPLIER").attr("href");

        Map<String, String> purchaseData = authCrawler.getFormData(purchaseDoc, "button#DownloadAllButton");
        purchaseData.put("event", "p_download_all_files");
        authCrawler.SubmitAction(purchaseData);

        Storage storage = new Storage("Samruk");
        File storageFile = storage.CreateFile(currentData.getNumber() + ".zip");
        storage.LoadData(storageFile, authCrawler.getResponse().bodyAsBytes());
        if (storage.GetCountFiles() > 0) {
            currentData.isDocs = 1;
            currentData.pathToStogare = storage.GetPath() + "/" + storageFile.getName();
        }
        authCrawler.setIgnoreContentType(false);
        authCrawler.setUrl(authCrawler.baseUrl + urlToTheBack);
        authCrawler.getDoc();

        logger.log(Level.INFO, "Upload file:{0} finished.", currentData.pathToStogare);
    }
}
