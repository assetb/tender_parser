/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.db.IDatabaseManager;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Aset
 */
public class Saver {

    private static final Logger logger = Logger.getLogger(Saver.class.getName());

    private final IDatabaseManager dbManager;
    public boolean isToSaveAuth = false;
    protected String saverName;

    public Saver(IDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void save(List<Purchase> purchases, List<Lot> lots) {
        purchases.forEach(dbManager::addPurchase);
        lots.forEach(dbManager::addLot);
    }

    @Deprecated
    public boolean Do(List<Purchase> data) {
        for (Purchase purchaseData : data) {
            if (purchaseData.error > 0) {
                continue;
            }

            dbManager.addPurchase(purchaseData);
            purchaseData.lots.stream().forEach(dbManager::addLot);

            if (isToSaveAuth && null != purchaseData.pathToStogare && !purchaseData.pathToStogare.isEmpty()) {
                dbManager.Insert(InsertPurchaseDoscQuery(purchaseData, purchaseData.getId()));
            }
        }
        return true;
    }

//    private String FormattingValue(String value) {
//        return value.replace("\"", "\\\"").replace("'", "\\'").trim();
//    }

//    public String InsertLotQuery(Lot lotData, int purchaseid) {
//
//        String queryFields = "insert into lots (source,purchaseid";
//        String queryValues = ") values ('" + source + "','" + String.valueOf(purchaseid) + "'";
//        if (lotData.purchaseNumber != null) {
//            queryFields = queryFields + ",negnumber";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.purchaseNumber) + "'";
//        }
//        if (lotData.lotNumber != null) {
//            queryFields = queryFields + ",number";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.lotNumber) + "'";
//        }
//        if (lotData.kzName != null) {
//            queryFields = queryFields + ",kzname";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.kzName) + "'";
//        }
//        if (lotData.ruName != null) {
//            queryFields = queryFields + ",runame";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.ruName) + "'";
//        }
//        if (lotData.ruDescription != null) {
//            queryFields = queryFields + ",rudescription";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.ruDescription) + "'";
//        }
//        if (lotData.kzDescription != null) {
//            queryFields = queryFields + ",kzdescription";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.kzDescription) + "'";
//        }
//        if (lotData.quantity != null) {
//            queryFields = queryFields + ",quantity";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.quantity) + "'";
//        }
//        if (lotData.price != null) {
//            queryFields = queryFields + ",price";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.price) + "'";
//        }
//        if (lotData.sum != null) {
//            queryFields = queryFields + ",sum";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.sum) + "'";
//
//            queryFields = queryFields + ",isum";
//            if (lotData.sum.contains(",") && lotData.sum.length() - lotData.sum.indexOf(",") > 3) {
//                lotData.sum = lotData.sum.substring(0, lotData.sum.indexOf(",") + 2);
//            }
//            queryValues = queryValues + ",'" + FormattingValue(lotData.sum.replaceAll(",", ".").replaceAll("\\s", "").replaceAll(" ", "")) + "'";
//        }
//        if (lotData.deliveryPlace != null) {
//            queryFields = queryFields + ",deliveryplace";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.deliveryPlace) + "'";
//        }
//        if (lotData.deliverySchedule != null) {
//            queryFields = queryFields + ",deliveryschedule";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.deliverySchedule) + "'";
//        }
//        if (lotData.deliveryTerms != null) {
//            queryFields = queryFields + ",deliveryterms";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.deliveryTerms) + "'";
//        }
//        if (lotData.ktru != null) {
//            queryFields = queryFields + ",ktru";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.ktru) + "'";
//        }
//        if (lotData.kind != null) {
//            queryFields = queryFields + ",kind";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.kind) + "'";
//        }
//        if (lotData.kato != null) {
//            queryFields = queryFields + ",kato";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.kato) + "'";
//        }
//        if (lotData.unit != null) {
//            queryFields = queryFields + ",unit";
//            queryValues = queryValues + "," + "'" + FormattingValue(lotData.unit) + "'";
//        }
//        String queryAll = queryFields + queryValues + ")";
//        return queryAll;
//
//    }
//
//    public String InsertPurchaseQuery(Purchase purchaseData) {
//        String queryFields = "insert into purchase (source";
//        String queryValues = ") values (" + source;
//        if (type != null) {
//            queryFields = queryFields + ",type";
//            queryValues = queryValues + ",'" + type + "'";
//        }
//
//        String number = purchaseData.getNumber();
//        if (number != null) {
//            queryFields = queryFields + ",number";
//            queryValues = queryValues + ",'" + FormattingValue(number) + "'";
//        }
//        if (purchaseData.kzName != null) {
//            queryFields = queryFields + ",kzname";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.kzName) + "'";
//        }
//        if (purchaseData.ruName != null) {
//            queryFields = queryFields + ",runame";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.ruName) + "'";
//        }
//        if (purchaseData.customer != null) {
//            queryFields = queryFields + ",customer";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.customer) + "'";
//        }
//        if (purchaseData.organizer != null) {
//            queryFields = queryFields + ",organizer";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.organizer) + "'";
//        }
//        if (purchaseData.venue != null) {
//            queryFields = queryFields + ",venue";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.venue) + "'";
//        }
//        if (purchaseData.method != null) {
//            queryFields = queryFields + ",method";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.method) + "'";
//        }
//        if (purchaseData.status != null) {
//            queryFields = queryFields + ",status";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.status) + "'";
//        }
//        if (purchaseData.publishDay != null) {
//            queryFields = queryFields + ",publishday";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.publishDay) + "'";
//        }
//        if (purchaseData.startDay != null) {
//            queryFields = queryFields + ",startday";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.startDay) + "'";
//        }
//        if (purchaseData.endDay != null) {
//            queryFields = queryFields + ",endday";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.endDay) + "'";
//        }
//        if (purchaseData.attribute != null) {
//            queryFields = queryFields + ",attribute";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.attribute) + "'";
//        }
//        if (purchaseData.priceSuggestion != null) {
//            queryFields = queryFields + ",pricesuggestion";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.priceSuggestion) + "'";
//        }
//        if (purchaseData.link != null) {
//            queryFields = queryFields + ",link";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.link) + "'";
//        }
//        if (purchaseData.additionalinformation != null) {
//            queryFields = queryFields + ",additionalinformation";
//            queryValues = queryValues + "," + "'" + FormattingValue(purchaseData.additionalinformation) + "'";
//        }
//        if (isToSaveAuth && null != purchaseData.pathToStogare && !purchaseData.pathToStogare.isEmpty()) {
//            if (purchaseData.isDocs == 1) {
//                queryFields = queryFields + ",isdocs";
//                queryValues = queryValues + "," + purchaseData.isDocs;
//                queryFields = queryFields + ",docszip";
//                queryValues = queryValues + "," + "'" + purchaseData.pathToStogare + "'";
//            }
//        }
//        String queryAll = queryFields + queryValues + ")";
//        return queryAll;
//
//    }

    public String InsertPurchaseDoscQuery(Purchase purchaseData, int purchaseid) {
        String query = "insert into purchasedocs(purchaseid, path) values(" + purchaseid + ", '" + purchaseData.pathToStogare + "');";
        return query;
    }

    public void Close() {
    }
}
