/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.ets;

import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Parser;
import com.altaik.parser.Sources;
import com.altaik.storage.Storage;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir
 */
public class ETSParser extends Parser {
    private Map<String, String> members = new HashMap<>();

    public ETSParser(Document doc, IDatabaseManager dbManager) {
        super(doc, dbManager);
        setIsToDoAuth(true);
        setIsToDoLots(true);
    }


    public void parseMembers(Document membersDoc) {
        Elements tableMember = membersDoc.select("form[name=\"member\"] table:first-child");
        Elements rows = tableMember.select("tr:nth-child(n+3)");
        for (Element tr : rows) {
            Elements td = tr.select("td");
            String code = td.get(0).text();
            String name = td.get(1).text();
            members.put(code, name);
        }

        logger.log(Level.INFO, "Member count: {0}", members.size());
    }

    @Override
    public void PurchasesDo() {

        try {
            data = new ArrayList<>();

            rows = GetRowsWithPurchases();
            if (null == rows || rows.isEmpty()) {
                logger.log(Level.SEVERE, "Not found rows. Document title {0}", doc.title());
                return;
            }

            for (int i = 0; i < rows.size() - 1; i++) {
//            for (int i = 0; i < 10; i++) {
                row = rows.get(i);
                if (null == row) {
                    logger.log(Level.WARNING, "{0}: Empty row {1}", new Object[]{parserName, i});
                    continue;
                }
                cols = row.select("td");
                if (null == cols || cols.isEmpty()) {
                    logger.log(Level.WARNING, "{0}: Empty cols", parserName);
                    continue;
                }
                Elements rowsPurchase = new Elements();
                rowsPurchase.add(row);
                Purchase purchaseData = new Purchase();
                Purchase purchaseLink = new Purchase();
                currentData = purchaseData;
                currentLink = purchaseLink;
                currentData.setSource(Sources.ETS);
                FillPurchase(rowsPurchase);
                String number = purchaseData.getNumber();
                if (null != number && !number.isEmpty()) {
                    try (ResultSet res = dbManager.Execute("select count(*) as count from purchase where number = '" + number + "'")) {
                        if (null == res || !res.next() || res.getString("count").equals("0")) {
                            if (isIsToDoLots()) {
                                LotsDo(rowsPurchase);
                            }
                            if (isIsToDoAuth() && purchaseData.error == 0) {
                                UploadFiles();
                            }
                            data.add(purchaseData);
                            logger.log(Level.INFO, "{0}: {1} with number {2} loadeded.", new Object[]{parserName, purchaseData, number});
                        } else {
                            logger.log(Level.INFO, "purchaseData {0} is already in database.", number);
                        }
                        logger.log(Level.INFO, "{0}: {1} with number {2} processed.", new Object[]{parserName, purchaseData, number});
                    }
                } else {
                    logger.log(Level.WARNING, "purchaseData is empty");
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0} : Error PurchasesDo (number purchase{1}): {2}", new Object[]{parserName, currentData.getNumber(), ex});
        }
    }

    @Override
    public void FillPurchase() {

    }

    @Override
    public Elements GetRowsWithPurchases() {
        return doc.select("table#ctl00_PageContent_gvAuctions tr:not(:first-child)");
    }

    @Override
    public void LotsDo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void UploadFiles() {
        ETSCrawler crawler = new ETSCrawler();
        Storage storage = new Storage("ets");
        storage.CreateFolder(currentData.getNumber());
        for (String title : currentData.urlsToUploadFiles.keySet()) {
            crawler.setUrl(currentData.urlsToUploadFiles.get(title));
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

    private void FillPurchase(Elements rowsPurchase) {
        currentData.setSource(Sources.ETS);
        currentData.setNumber("");
        currentData.link = "http://www.ets.kz/ru/auctions.aspx";
        Double sum = 0.0;
        String firstLotNumber = "", lastLotNumber = "";
        for (Element rowPurchase : rowsPurchase) {
            String numberLot = rowPurchase.select("td:nth-child(7)").text();
            String priceLot = rowPurchase.select("td:nth-child(8)").text().replace("\u00a0", "").replace(",", ".");
            if (firstLotNumber.isEmpty()) {
                firstLotNumber = numberLot.replaceAll("[Лл]от\\s*№\\s*\\d*\\s*", "");
            } else {
                lastLotNumber = numberLot.replaceAll("[Лл]от\\s*№\\s*\\d*\\s*", "");
            }
            sum += Double.parseDouble(priceLot);
        }
        currentData.setNumber(firstLotNumber + (!lastLotNumber.isEmpty() ? " - " + lastLotNumber : ""));
        Elements colsPurchase = rowsPurchase.select("tr:first-child > td");
        currentData.type = SelectCol(cols, 10);
        currentData.sum = String.format("%.2f", sum);
        currentData.ruName = "Закупки на товарной бирже ETS";
        String customer = SelectCol(cols, 3);
        Pattern p = Pattern.compile("^.+?(?=\\(\\d+\\))");
        Matcher m = p.matcher(customer);
        if (m.find()) {
            currentData.customer = m.group();
        } else {
            logger.log(Level.WARNING, "Not match pattern customer name");
            currentData.customer = customer;
        }

        String organizerCode = SelectCol(cols, 4);

        if (members.size() > 0) {
            currentData.organizer = members.get(organizerCode);
        } else {
            currentData.organizer = organizerCode;
        }

        currentData.method = SelectCol(cols, 8);
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        currentData.startDay = (df.format(new Date()));
//        currentData.startDay = SelectCol(cols, 12);
        currentData.endDay = SelectCol(cols, 1);
        String information = String.format("Направление аукциона %s. %s. %s. Размер биржевого обеспечения %s%%.",
                SelectCol(cols, 9).toLowerCase(),
                SelectCol(cols, 2),
                SelectCol(cols, 15),
                SelectCol(cols, 11).toLowerCase());
        currentData.additionalinformation = information;
    }

    private void LotsDo(Elements rowsPurchase) {
        currentData.lots = new ArrayList<>();
        currentData.urlsToUploadFiles = new HashMap<>();
        for (Element rowPurchase : rowsPurchase) {
            Lot lot = new Lot();
            lot.setSourceId(Sources.ETS);
            Elements colsLot = rowPurchase.select("td");
            lot.setPurchaseNumber(currentData.getNumber());
            lot.lotNumber = SelectCol(colsLot, 6);
            lot.ruName = SelectCol(colsLot, 5);
            lot.quantity = "Указанно в тех.спец.";
            lot.unit = "Указанно в тех.спец.";
            lot.deliveryPlace = "Указанно в тех.спец.";
            lot.deliverySchedule = "Указанно в тех.спец.";
            lot.deliveryTerms = "Указанно в тех.спец.";
            lot.price = SelectCol(colsLot, 7).replace("\u00a0", "").replace(",", ".");
            lot.sum = SelectCol(colsLot, 7).replace("\u00a0", "").replace(",", ".");

            String urlAttachment = SelectElement(colsLot, 18).select("a").attr("href");
            if (urlAttachment != null && !urlAttachment.isEmpty()) {
                currentData.urlsToUploadFiles.put(lot.lotNumber + "(Приложение).pdf", urlAttachment);
            }

            String urlRegulations = SelectElement(colsLot, 19).select("a").attr("href");
            if (urlRegulations != null && !urlRegulations.isEmpty()) {
                currentData.urlsToUploadFiles.put(lot.lotNumber + "(Регламент торгов).pdf", urlRegulations);
            }
            currentData.lots.add(lot);
        }
    }
}
