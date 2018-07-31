package com.altaik.parser.auctionya.utils;


import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;
import com.altaik.db.IDatabaseManager;

import java.util.List;
import java.util.logging.Logger;

public class EaucSaver {

    private IDatabaseManager dbManager;
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public EaucSaver(IDatabaseManager databaseManager) {
        dbManager = databaseManager;
    }

    public void save(List<Purchase> purchases, List<Lot> lots) {
        purchases.forEach(dbManager::addPurchase);
        lots.forEach(dbManager::addLot);
    }
}
