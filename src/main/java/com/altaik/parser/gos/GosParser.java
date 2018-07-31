/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gos;

import com.altaik.bo.ErrorsEnum;
import com.altaik.bo.Lot;
import com.altaik.crawler.CrawlerContext;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Parser;
import com.altaik.parser.Sources;
import com.altaik.parser.processes.gos.GosParameters;
import com.altaik.storage.Storage;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Aset
 */
public class GosParser extends Parser {
    private String purchaseType;
    private GosAuthCrawler authCrawler;
    private final CrawlerContext crawlerContext;

    public GosParser(Document doc, IDatabaseManager dbManager, GosAuthCrawler authCrawler, CrawlerContext crawlerContext, String type) {
        super(doc, dbManager);
        this.authCrawler = authCrawler;
        this.setIsToDoAuth(true);
        this.setIsToDoLots(true);
        this.crawlerContext = crawlerContext;
        startingRow = 2;
        parserName = "GosZakupParser";
        this.purchaseType = type;
    }




    @Override
    public Elements GetRowsWithPurchases() {
        //Element table = doc.select("table").get(4);
        Element table = doc.select("table.zebra").first();
        rows = null;
        if (null != table && !table.select("tr").isEmpty()) {
            rows = table.select("tr");
        }
        return rows;
    }

    @Override
    public void FillPurchase() {
        if (cols.size() > 0 && null != cols.get(0).select("a")) {
            currentData.setNumber(cols.get(0).select("a").text());
            currentLink.setNumber(cols.get(0).select("a").attr("href"));
        }

        currentData.setSource(Sources.GOS);
        switch (purchaseType) {
            case GosParameters.TYPE_BUYS:
                currentData.setType("1");
                break;
            case GosParameters.TYPE_OPEN_BUYS:
                currentData.setType("2");
                break;
            case GosParameters.TYPE_AUCS:
                currentData.setType("3");
        }

        currentData.link = currentLink.getNumber();
        currentData.customer = SelectCol(cols, 1);
        currentData.ruName = SelectCol(cols, 2);
        try {
            currentData.nLots = Integer.parseInt(SelectCol(cols, 3));
        } catch (NumberFormatException nfex) {
            logger.log(Level.WARNING, "{0} nLots is not integer.", parserName);
        }
        currentData.sum = SelectCol(cols, 4);
        currentData.method = SelectCol(cols, 5);
        currentData.status = SelectCol(cols, 6);
        currentData.startDay = SelectCol(cols, 7);
        currentData.endDay = SelectCol(cols, 8);
    }

    @Override
    public void LotsDo() {
        try {
            GosCrawler gosCrawler = new GosCrawler(currentLink.getNumber(), crawlerContext);
            Document docLot = gosCrawler.getDoc();
            Elements tables = docLot.select("table");
            if (null == tables || tables.size() == 0) {
                logger.log(Level.SEVERE, "{1}. Not found tables for {0}. Document title {2}.", new Object[]{parserName, currentData.getNumber(), docLot.title()});
                currentData.error = ErrorsEnum.PURCHASE_CAPTHA.GetValue();
                return;
            }

            Element negTable = tables.get(1);
            Elements negRows = negTable.select("tr:not(id)");

            if (GosParameters.TYPE_OPEN_BUYS.equals(purchaseType)) {
//                String str = negRows.get(0).select("td").get(1).text();
                currentData.ruName = SelectTD1(negRows, 0);
                currentData.kzName = SelectTD1(negRows, 1);
                currentData.publishDay = SelectTD1(negRows, 2);
                currentData.startDay = SelectTD1(negRows, 3);
                currentData.endDay = SelectTD1(negRows, 4);
                currentData.method = SelectTD1(negRows, 5);
                currentData.status = SelectTD1(negRows, 6);
                currentData.customer = SelectTD1(negRows, 7);
                currentData.attribute = SelectTD1(negRows, 8);
                currentData.attribute2 = SelectTD1(negRows, 9);
            } else {
                String str = SelectTD1(negRows, 0);
                if (str.contains("/")) {
                    currentData.kzName = str.substring(0, str.indexOf("/"));
                    currentData.ruName = str.substring(str.indexOf("/") + 1);
                } else {
                    currentData.ruName = str;
                }
                if (currentData.method.contains("порядок")) {
                    currentData.publishDay = SelectTD1(negRows, 1);
                    currentData.startDay = SelectTD1(negRows, 2);
                    currentData.endDay = SelectTD1(negRows, 3);
                    currentData.method = SelectTD1(negRows, 4);
                    currentData.status = SelectTD1(negRows, 7);
                    currentData.customer = SelectTD1(negRows, 8);
                    currentData.organizer = SelectTD1(negRows, 8);
                    currentData.attribute = "Нет данных по НДС";
                } else {
                    currentData.method = SelectTD1(negRows, 1);
                    currentData.status = SelectTD1(negRows, 2);
                    currentData.customer = SelectTD1(negRows, 3);
                    currentData.organizer = SelectTD1(negRows, 4);
                    currentData.publishDay = SelectTD1(negRows, 5);
                    currentData.startDay = SelectTD1(negRows, 6);
                    currentData.endDay = SelectTD1(negRows, 7);
                    currentData.attribute = SelectTD1(negRows, 8);
                }
            }

            Elements lotTable = docLot.select("table.points");
            Elements lotRows = lotTable.select("tr[id^=\"lot_\"]");
            List<Lot> lots = new ArrayList<>();
            for (int i = 0; i < lotRows.size(); i++) {
                Element lotrow = lotRows.get(i);
                Elements lotcols = lotrow.select("td");
                Lot lotData = new Lot();
                lotData.purchaseNumber = currentData.getNumber();
                lotData.lotNumber = SelectCol(lotcols, 0);
                if (lotcols.size() > 1) {
                    if (null != lotcols.get(1).select("a")) {
                        String strName = lotcols.get(1).select("a").text();
                        if (strName.contains("/")) {
                            lotData.ruName = strName.substring(0, strName.indexOf("/"));
                            lotData.kzName = strName.substring(strName.indexOf("/") + 1);
                        } else {
                            lotData.ruName = strName;
                        }
                    }

                    if (null != lotcols.get(1).select("small") && lotcols.get(1).select("small").size() > 0) {
                        String strDescription = lotcols.get(1).select("small").get(0).text();
                        if (strDescription.contains("/")) {
                            lotData.ruDescription = strDescription.substring(0, strDescription.indexOf("/"));
                            lotData.kzDescription = strDescription.substring(strDescription.indexOf("/") + 1);
                        } else {
                            lotData.ruDescription = strDescription;
                        }
                    }
                }
                lotData.ktru = SelectCol(lotcols, 2);
                lotData.quantity = SelectCol(lotcols, 3);
                lotData.unit = SelectCol(lotcols, 4);
                lotData.vid = SelectCol(lotcols, 5);
                lotData.price = SelectCol(lotcols, 6);
                lotData.sum = SelectCol(lotcols, 7);
                lotData.deliveryTerms = "Размер авансового платежа " + SelectCol(lotcols, 8);
                lotData.kato = SelectCol(lotcols, 9);
                lotData.deliveryPlace = SelectCol(lotcols, 10);
                lotData.deliverySchedule = SelectCol(lotcols, 11);
                lotData.setSourceId(Sources.GOS);
                lots.add(lotData);
            }
            currentData.lots = lots;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}: Error Lot Parsing: {1}", new Object[]{parserName, ex.getMessage()});
            currentData.error = ErrorsEnum.PURCHASE_ERROR_PARSING_LOTS.GetValue();
        }
    }

    protected void UploadFileanOpenByus(Storage storage) {

        String urlMain = "https://goszakup.gov.kz/";
        String urlForPurchase = "app/index.php/ru/publictrade/showbuy/";
        String typeFile = ".pdf";

        authCrawler.setMethod(Method.GET);
        authCrawler.setUrl(urlMain + urlForPurchase + currentData.getNumber());
        Document _doc = authCrawler.getDoc();
        if (null == _doc) {
            logger.log(Level.WARNING, "{0}: Error get document for GosOpenBuys {1}", new Object[]{parserName, currentData.getNumber()});
            return;
        }

        storage.CreateFolder(currentData.getNumber());

        Elements els = _doc.select("td#contract_container a");
        if (null != els) {
            for (Element el : els) {
                authCrawler.setIgnoreContentType(true);
                authCrawler.setUrl(el.attr("href"));
                if (authCrawler.getDoc() == null || !storage.LoadFile(el.text() + typeFile, authCrawler.getResponse().bodyAsBytes())) {
                    logger.log(Level.SEVERE, "{0}: Load File Failed", parserName);
                }
            }
        }

        els = _doc.select("tr[id^=\"lot_\"] td:first-child");
        if (null != els) {
            for (Element el : els) {
                authCrawler.setIgnoreContentType(true);
                Elements a = el.select("a");
                if (null == a || a.size() == 0) {
                    break;
                    //СЃС‚СЂР°РЅРЅРѕ
                    //continue;
                }
                authCrawler.setUrl(a.attr("href"));
//                    authCrawler.setBodySize(sizeBody);
                if (authCrawler.getDoc() == null || !storage.LoadFile(el.ownText() + typeFile, authCrawler.getResponse().bodyAsBytes())) {
                    logger.log(Level.SEVERE, "{0}: Load File Failed", parserName);
                }
            }
        }

        //СЃС‚СЂР°РЅРЅРѕ
        authCrawler.setIgnoreContentType(false);
        authCrawler.setIgnoreContentType(true);
    }

    protected void UploadFilesByusAndAuction(Storage storage) {
        authCrawler.setIgnoreContentType(true);
        Document homeTradingDoc = authCrawler.getCurrentDoc();
        Map<String, String> homeTradingData = authCrawler.getFormData(homeTradingDoc, "button#GoBtn");
        homeTradingData.put("SearchKeyword", currentData.getNumber());
        Elements homeTradingElms = homeTradingDoc.select("select#SearchCriteriaPoplist");
        homeTradingData.put(homeTradingElms.attr("name"), homeTradingElms.select("option[value^=\"Number\"]").attr("value"));

        Document searchingDoc = authCrawler.SubmitAction(homeTradingData);

        homeTradingData = null;
        homeTradingDoc = null;
        homeTradingElms.clear();

        if (searchingDoc == null) {
            logger.log(Level.WARNING, "{0}: Error get document {1}", new Object[]{parserName, currentData.getNumber()});
            return;
        }

        Elements searchingElms = searchingDoc.select("form a[title=\"" + currentData.getNumber() + "\"]");

        String urlBack = authCrawler.baseUrl + searchingDoc.select("a#PON_SOURCING_TAB_S").attr("href");

        if (searchingElms.size() == 0) {
            authCrawler.clearData();
            authCrawler.setUrl(urlBack);
            authCrawler.setMethod(Method.GET);
            authCrawler.getDoc();
        } else {
            authCrawler.clearData();
            authCrawler.setUrl(authCrawler.fullOA_HTML + searchingElms.get(0).attr("href"));
            authCrawler.setMethod(Method.GET);
            Document homePurchaseDoc = authCrawler.getDoc();

            searchingDoc = null;
            searchingElms.clear();

            storage.CreateFolder(currentData.getNumber());

            Elements purchaseFilesListElms = homePurchaseDoc.select("span#FileListRNEx table a[name*=\":FileListRNEx:\"]");
            for (Element elpurchaseFileElm : purchaseFilesListElms) {
                authCrawler.clearData();
                authCrawler.setMethod(Method.GET);
                authCrawler.setUrl(authCrawler.baseUrl + elpurchaseFileElm.attr("href"));
                authCrawler.getDoc();
                if (!storage.LoadFile(elpurchaseFileElm.attr("title"), authCrawler.getResponse().bodyAsBytes())) {
                    logger.log(Level.SEVERE, "{0}: Load File Failed", parserName);
                }
            }

            String selectorFromDocs = homePurchaseDoc.select("span#SubTabRegion table a[onclick^=\"submitForm\"]").get(2).attr("onclick");
            selectorFromDocs = "span#SubTabRegion table a[onclick=\"" + selectorFromDocs + "\"]";
            Document docsPurchaseDoc = authCrawler.SubmitAction(authCrawler.getFormData(homePurchaseDoc, selectorFromDocs));
            String selectorForFindDocs = "span[id=\"xxDocumentationRN\"] a[name*=\":deliverableNameLink:\"]";
            int countDocs = docsPurchaseDoc.select(selectorForFindDocs).size();
            for (int i = 0; i < countDocs; i++) {
                Elements docsPurchaseElms = docsPurchaseDoc.select(selectorForFindDocs);
                if (docsPurchaseElms.size() < i) {
                    logger.log(Level.WARNING, "Error count docs.");
                    continue;
                }
                Element docPurchase = docsPurchaseElms.get(i);
                Document pageLoadDocs = authCrawler.SubmitAction(authCrawler.getFormData(docsPurchaseDoc, "a[name=\"" + docPurchase.attr("name") + "\"]"));
                authCrawler.setMethod(Method.GET);

                Elements pageLoadDocsElms = pageLoadDocs.select("span#FileListRNEx a[name^=\"FileListRNEx:CHECKED:\"]:not([title$=\".xml\"])");
                for (Element docElm : pageLoadDocsElms) {
                    authCrawler.setUrl(authCrawler.baseUrl + docElm.attr("href"));
                    authCrawler.getDoc();
                    if (!storage.LoadFile(docElm.attr("title"), authCrawler.getResponse().bodyAsBytes())) {
                        logger.log(Level.SEVERE, "{0}: Load File Failed", parserName);
                    }
                }

                Elements pageLoadFilesElms = pageLoadDocs.select("span#Attachments table a[id]:not([id^=\"OASH\"])");

                for (Element fileElm : pageLoadFilesElms) {
                    authCrawler.SubmitAction(authCrawler.getFormData(pageLoadDocs, "span#Attachments table a[id=\"" + fileElm.attr("id") + "\"]"));
                    if (!storage.LoadFile(fileElm.text() + ".doc", authCrawler.getResponse().bodyAsBytes())) {
                        logger.log(Level.SEVERE, "{0}: Load File Failed", parserName);
                    }
                }

                String urlReturnRFQ = pageLoadDocs.select("span#mainPageLayout a[title*=\"Return\"]").attr("href");
                authCrawler.setMethod(Method.GET);
                authCrawler.setUrl(authCrawler.fullOA_HTML + urlReturnRFQ);
                Document rfqDoc = authCrawler.getDoc();

                String selectorForLots = rfqDoc.select("span#SubTabRegion table a[onclick^=\"submitForm\"]").get(2).attr("onclick");
                selectorForLots = "span#SubTabRegion table a[onclick=\"" + selectorForLots + "\"]";
                docsPurchaseDoc = authCrawler.SubmitAction(authCrawler.getFormData(rfqDoc, selectorForLots));
            }
            authCrawler.setUrl(urlBack);
            authCrawler.getDoc();
        }
        authCrawler.setIgnoreContentType(false);
    }

    @Override
    public void UploadFiles() {
        Storage storage;

        if (authCrawler == null) {
            logger.log(Level.SEVERE, "Not set authCrawler for parser {0}", parserName);
            return;
        }
        if (GosParameters.TYPE_OPEN_BUYS.equals(purchaseType)) {
            storage = new Storage("GosOpenByus");
            UploadFileanOpenByus(storage);
        } else {
            if (GosParameters.TYPE_BUYS.equals(purchaseType)) {
                storage = new Storage("GosBuys");
            } else if (GosParameters.TYPE_AUCS.equals(purchaseType)) {
                storage = new Storage("GosAuction");
            } else {
                logger.log(Level.WARNING, "{0}: Error type parser. Type = {1}", new Object[]{parserName, purchaseType});
                return;
            }
            UploadFilesByusAndAuction(storage);
        }

        if (null != storage && null != storage.GetPath() && !storage.GetPath().isEmpty() && storage.GetCountFiles() > 0) {
            if (storage.ExtractZip(true)) {
                currentData.isDocs = 1;
                currentData.pathToStogare = storage.GetPathToZip();
            } else {
                currentData.isDocs = 0;
                currentData.pathToStogare = storage.GetPath();
            }
        }
        logger.log(Level.INFO, "Files for {0} uploaded.", currentData.getNumber());
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, ex.getMessage());
//        }
    }
}
