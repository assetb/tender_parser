/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gosreestr;

import com.altaik.bo.Lot;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.IParser;
import com.altaik.parser.Parser;
import com.altaik.parser.Sources;
import com.altaik.storage.Storage;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author admin
 */
public class GosReestrParser extends Parser implements IParser {


    public GosReestrParser(Document doc, IDatabaseManager dbManager) {
        super(doc, dbManager);
        setIsToDoAuth(true);
        setIsToDoLots(true);
    }

    @Override
    public void FillPurchase() {
        GosReestrCrawler crawler = new GosReestrCrawler();
        currentData.setSource(Sources.GOSREESTR);
        Element linkPurchase = cols.select("div.auction-list-information div.alist-div-first a").first();
        if (linkPurchase != null) {
            currentData.link = crawler.baseUrl + linkPurchase.attr("href");
            currentData.setNumber(cols.select("div.auction-list-information div.alist-div-second span.alist-number")
                    .text()
                    .replaceAll("^.*№\\s*", ""));
//            currentData.setNumber(currentData.getNumber());
            Element ruNameElement = cols.select("div.auction-list-information div.alist-div-first span.alist-object-name").first();
            currentData.ruName = ruNameElement != null ? ruNameElement.text() : null;
            currentData.startDay = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        }
        crawler.close();
    }

    @Override
    public Elements GetRowsWithPurchases() {
        return doc.select("table#auction-guest-list tr");
    }

    @Override
    public void LotsDo() {
        GosReestrCrawler crawler = new GosReestrCrawler();
        try {
            crawler.setUrl(currentData.link);
            Document auctionDetails = crawler.getDoc();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
            String date = auctionDetails.select("td.auction-main-info div.auction-main-info-details span.auction-main-info-row span.auction-start-date").text();

            if (date != null && date.equals("")) {
                date = auctionDetails.select("div.auction-main-info-details > span.auction-main-info-row > span.auction-status-AcceptingApplications").text();
                date = StringUtils.substringBetween(date, "(до ", ")").trim();
                System.out.println("new Date : " + date);
            }
            Date tempDate = sdf.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(tempDate);
            c.add(Calendar.DATE, 1);
            currentData.endDay = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
            currentData.method = auctionDetails.select("td.auction-main-info div.auction-main-info-details span.auction-main-info-row:nth-child(2) span:nth-child(2)").text();
            currentData.lots = new ArrayList<>();
            currentData.urlsToUploadFiles = new HashMap<>();
            Lot lot = new Lot();
            lot.setPurchaseNumber(currentData.getNumber());
            lot.setLotNumber(String.format("%s-1", lot.getPurchaseNumber()));
            lot.setSourceId(Sources.GOSREESTR);
            lot.sum = auctionDetails.select("div.auction-main-info-details  span.auction-start-cost").text();
            lot.ruDescription = auctionDetails.select("td.auction-all-description span.auction-object-description").text();
            currentData.lots.add(lot);
            Elements rows = auctionDetails.select("td.auction-all-description tr.tableinfo-row-title");

            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(i);
                Element rowData = auctionDetails.select(String.format("td.auction-all-description tr.tableinfo-row-text:nth-child(%s)", (i + 1) * 2)).first();
                switch (row.select("span").text()) {
                    case ("Продавец"):
                    case ("Наймодатель"): {
                        currentData.customer = rowData.text();
                    }
                    break;
                    case ("Примечание"): {
                        currentData.additionalinformation = rowData.text();
                    }
                    break;
                    case ("Эл. документы"): {
                        Elements fileItems = rowData.select("div.auction-file-info-item");
                        fileItems.stream().forEach((fileItem) -> {
                            currentData.urlsToUploadFiles.put(fileItem.select("a").text(), fileItem.select("a").attr("href"));
                        });
                    }
                    break;
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(GosReestrParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            crawler.close();
        }

    }

    @Override
    public void UploadFiles() {
        GosReestrCrawler crawler = new GosReestrCrawler();
        if (currentData.urlsToUploadFiles.size() > 0) {
            Storage storage = new Storage("gosreestr");
            storage.CreateFolder(currentData.getNumber());
            for (String title : currentData.urlsToUploadFiles.keySet()) {
                crawler.setUrlPath(currentData.urlsToUploadFiles.get(title));
                crawler.setMethod(Connection.Method.GET);
                crawler.setIgnoreContentType(true);
                crawler.getDoc();
                Connection.Response res = crawler.getResponse();
                if (res == null) {
                    logger.log(Level.WARNING, "Error upload the file from {0}. File not found.", currentData.urlsToUploadFiles.get(title));
                    continue;
                }
                storage.LoadFile(title, res.bodyAsBytes());
            }

            if (storage.GetCountFiles() > 0) {
                if (storage.ExtractZip(true)) {
                    currentData.isDocs = 1;
                    currentData.pathToStogare = storage.GetPathToZip();
                } else {
                    currentData.isDocs = 0;
                    currentData.pathToStogare = storage.GetPath();
                }
            }
        }
        crawler.close();
    }

}
