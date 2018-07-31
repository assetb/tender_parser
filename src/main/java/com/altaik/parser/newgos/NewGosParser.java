/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.newgos;

import com.altaik.bo.Lot;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Parser;
import com.altaik.storage.Storage;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Smaile
 */
public class NewGosParser extends Parser {

    protected NewGosCrawler newGosCrawler;

    private String urlToUploadFile = "/ru/announce/actionAjaxModalShowFiles/";

    public NewGosParser(Document doc, IDatabaseManager dbManager, NewGosCrawler newGosCrawler) {
        super(doc, dbManager);
        setIsToDoLots(true);
//        setIsToDoAuth(true);
        this.newGosCrawler = newGosCrawler;
    }

    @Override
    public void FillPurchase() {
//        String urlPur = SelectElement(cols, 3).select("a").attr("href");
        currentData.type = "4";
        currentData.setNumber(SelectCol(cols, 0));
        currentData.customer = SelectCol(cols, 1);
        Element td = SelectElement(cols, 2);
        if (!td.select("a div:first-child").isEmpty() && !td.select("a div:last-child").isEmpty()) {
            currentData.ruName = td.select("a div:first-child").text();
            currentData.kzName = td.select("a div:last-child").text();
        } else {
            currentData.ruName = td.text();
        }
        currentData.method = SelectCol(cols, 3);
        currentData.startDay = SelectCol(cols, 5);
        currentData.endDay = SelectCol(cols, 6);
        currentData.nLots = Integer.parseInt(SelectCol(cols, 7));
        currentData.sum = SelectCol(cols, 8);
        currentData.status = SelectCol(cols, 9);
        String url = SelectElement(cols, 2).select("a").attr("href");
        if (url != null) {
            currentLink.setNumber(newGosCrawler.baseUrl + url);
            currentData.link = currentLink.getNumber();
        }
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Elements GetRowsWithPurchases() {
        Elements elements = doc.select("table.table.table-bordered tr");
        return elements;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void LoadGeneralInformation(Elements geniral) {
        for (Element geniralEl : geniral) {
            Elements geniralTH = geniralEl.select("th");
            Elements geniralTD = geniralEl.select("td");
            if (geniralTH.isEmpty() || geniralTD.isEmpty()) {
                logger.log(Level.WARNING, "Not found elements in row geniral table.");
                continue;
            }
            switch (geniralTH.text()) {
                case ("Способ проведения закупки"): {
                    currentData.method = geniralTD.text();
                } break;
                case ("Организатор"): {
                    currentData.organizer = geniralTD.text();
                } break;
                case ("Кол-во лотов в объявлении"): {
                    String nLots = geniralTD.text();
                    if (nLots != null && !"".equals(nLots)) {
                        currentData.nLots = Integer.parseInt(nLots);
                    } else {
                        logger.log(Level.WARNING, "Can not read the \"number of items\"");
                    }
                } break;
                case ("Сумма закупки"): {
                    currentData.sum = geniralTD.text();
                } break;
                case ("Признаки"): {
                    currentData.attribute = geniralTD.text();
                } break;
            }
        }
    }

    @Override
    public void LotsDo() {
        newGosCrawler.setUrl(currentLink.getNumber());
        Document doc = newGosCrawler.getDoc();
        Elements elements = doc.select("div.panel-body div.row div.form-group input");
        // body purchase
        currentData.setSource(1);
        currentData.setNumber(SelectElement(elements, 0).val());
        currentData.ruName = SelectElement(elements, 1).val();
        currentData.status = SelectElement(elements, 2).val();
        currentData.publishDay = SelectElement(elements, 3).val();
        currentData.startDay = SelectElement(elements, 4).val();
        currentData.endDay = SelectElement(elements, 5).val();

        Elements elementsGeniral = doc.select("div#general table td");
        if (elementsGeniral.isEmpty()) {
            elementsGeniral = doc.select("div[role=\"tabpanel\"] > div:first-child");
            elementsGeniral = elementsGeniral.select("table tr");
        }
        LoadGeneralInformation(elementsGeniral);
        List<Lot> lots = new ArrayList<>();
        newGosCrawler.setUrl(currentLink.getNumber() + "?tab=lots");

        newGosCrawler.setMethod(Connection.Method.GET);
        Document ldoc = newGosCrawler.getDoc();
        Elements lotsElements = ldoc.select("div#lots table tr:not(:first-child)");
        if (lotsElements.isEmpty()) {
            lotsElements = ldoc.select("div[role=\"tabpanel\"] div.tab-content div.panel-body table tr:not(:first-child)");
        }
        for (int i = 0; i < lotsElements.size(); i++) {
            Lot lot = new Lot();
            Elements lotElement = lotsElements.get(i).select("td");

            lot.purchaseNumber = currentData.getNumber();
            lot.lotNumber = SelectCol(lotElement, 1);
            lot.ruName = SelectCol(lotElement, 3);
            lot.ruDescription = SelectCol(lotElement, 4);
            lot.price = SelectCol(lotElement, 5);
            lot.quantity = SelectCol(lotElement, 6);
            lot.unit = SelectCol(lotElement, 7);
            lot.sum = SelectCol(lotElement, 8);
            lots.add(lot);
        }
//        setUrlsToUploadFiles(doc);

        newGosCrawler.setUrl(currentLink.getNumber() + "?tab=documents");
        if(isIsToDoAuth()) setUrlsToUploadFiles(newGosCrawler.getDoc());
        currentData.lots = lots;
//        currentData.
        //        elements.select(parserName)
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void UploadFiles() {

        if (null == currentData.urlsToUploadFiles || currentData.urlsToUploadFiles.isEmpty()) {
            logger.log(Level.INFO, "Not files in purchers number {0}", currentData.getNumber());
            return;
        }
        logger.log(Level.INFO, "Start load files. Count files = {0}", currentData.urlsToUploadFiles.size());
        Storage storage = new Storage("NewGos");
        storage.CreateFolder(currentData.getNumber());
        for (String title : currentData.urlsToUploadFiles.keySet()) {
//            NadlocCrawler nadlocCrawler = new NadlocCrawler();
            newGosCrawler.setUrl(currentData.urlsToUploadFiles.get(title));
            newGosCrawler.setMethod(Connection.Method.GET);
            newGosCrawler.setIgnoreContentType(true);
            newGosCrawler.getDoc();
            Connection.Response res = newGosCrawler.getResponse();
            if (res == null) {
                logger.log(Level.WARNING, "Error upload the file from {0}. File not found.", currentData.urlsToUploadFiles.get(title));
                continue;
            }
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
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void setUrlsToUploadFiles(Document pageDocuments) {
//        Document pageDocuments = newGosCrawler.getDoc();
        Elements tabDocRows = pageDocuments.select("div[role=\"tabpanel\"] div.tab-content div.panel-body table tr:not(:first-child)");
        currentData.urlsToUploadFiles = new HashMap<>();
        for (Element tabDocRow : tabDocRows) {
            Elements colsTabDoc = tabDocRow.select("td");
            if (colsTabDoc.size() < 3) {
                logger.log(Level.WARNING, "Error columns tab documentation.");
                continue;
            }
            Element titleDoc = SelectElement(colsTabDoc, 0);
            if (titleDoc.select("a").isEmpty()) {
                String title = titleDoc.ownText();
                Element btnAction = SelectElement(colsTabDoc, 2);
                if(btnAction.select("button").isEmpty()){
                    continue;
                }
                String paramReqForDoc = btnAction.select("button").attr("onclick");
                if (paramReqForDoc.isEmpty()) {
                    continue;
                }
                paramReqForDoc = paramReqForDoc.replace(", ", "/").replaceAll("[^\\d/]", "");
                newGosCrawler.setMethod(Connection.Method.GET);
                newGosCrawler.setUrlPath(urlToUploadFile + paramReqForDoc);
                Document tableDocs = newGosCrawler.getDoc();
                if(tableDocs == null){
                    logger.log(Level.WARNING, "Error get document with url "+urlToUploadFile + paramReqForDoc);
                    continue;
                }
                Elements tableDocsRows = tableDocs.select("table tr:not(:first-child)");
                for (int i = 0; i < tableDocsRows.size(); i++) {
                    Element row = tableDocsRows.get(i);
                    Element tagA = SelectElement(row.select("td a"), 0);
                    if (tagA == null) {
                        continue;
                    }
                    String finalTitle = (i + 1) + " " + (title.length() > 100 ? title.substring(0, 100) : title) + tagA.text().replaceAll(".*([.].{1,5}$)", "$1");
                    currentData.urlsToUploadFiles.put(finalTitle, tagA.attr("href"));
                }
//              /ru/announce/actionAjaxModalShowFiles/159213/263
            } else {
                String title = titleDoc.select("a").text();
                title = (title.length() > 100 ? title.substring(0, 100) : title) + titleDoc.select("a").attr("href").replaceAll(".*([.].{1,5}$)", "$1");
//                String title = titleDoc.select("a").text();
                String urlForDonw = newGosCrawler.baseUrl + titleDoc.select("a").attr("href");
                currentData.urlsToUploadFiles.put(title, urlForDonw);
            }
        }
    }

}
