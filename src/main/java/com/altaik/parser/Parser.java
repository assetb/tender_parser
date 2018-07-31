/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.bo.Purchase;
import com.altaik.db.IDatabaseManager;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aset
 */
public abstract class Parser implements IParser {

    protected static final Logger logger = Logger.getLogger(Parser.class.getName());

    protected final IDatabaseManager dbManager;

    protected Document doc = null;
    protected Purchase currentData;
    protected Purchase currentLink;
    protected Elements rows;
    protected Elements cols;
    protected Element row;
    protected Element col;
    protected String parserName;

    public List<Purchase> data = null;

    public int startingRow = 1;

    private boolean isToDoLots = false;
    private boolean isToDoAuth = false;

    public Parser(Document doc, IDatabaseManager dbManager) {
        this.currentData = null;
        this.doc = doc;
        this.dbManager = dbManager;
        parserName = "Parser";
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

            for (int i = rows.size() - 1; i >= startingRow; i--) {
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

                Purchase purchaseData = new Purchase();
                Purchase purchaseLink = new Purchase();
                currentData = purchaseData;
                currentLink = purchaseLink;

                FillPurchase();
                String number = purchaseData.getNumber();
                if (null != number && !number.isEmpty()) {
                    try (ResultSet res = dbManager.Execute("select count(*) as count from purchase where number = '" + number + "'")) {

                        if (null == res || !res.next() || res.getString("count").equals("0")) {
                            if(isToDoLots) LotsDo();
                            if (isToDoAuth && purchaseData.error == 0) UploadFiles();
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
    
    protected Element SelectElement(Elements elements, int column){
        if(elements.size() > column) {
            return elements.get(column);
        } else {
            logger.log(Level.WARNING, "{0}. Can read element index {1} for purchase {2}", new Object[]{parserName, column, currentData.getNumber()});
        }
        return null;
    }
    
    protected String SelectTD1(Elements negRows, int row) {
        Element elRow = SelectElement(negRows, row);
        if(elRow != null && elRow.select("td").size() > 1) {
            return elRow.select("td").get(1).text();
        } else {
            logger.log(Level.WARNING, "{0}. Can read row index {1} for purchase {2}.", new Object[]{parserName, row, currentData.getNumber()});
        }
        return null;
    }
    
    protected String SelectCol(Elements negCols, int column){
        Element element = SelectElement(negCols, column);
        if(element != null){
            return element.text();
        } else {
            logger.log(Level.WARNING, "{0}. Can read column index {1} for purchase {2}", new Object[]{parserName, column, currentData.getNumber()});
        }
        return null;
    }
    

    @Override
    public void Close() {
        currentData = null;
        currentLink = null;
        doc.empty();
        doc = null;
    }

    @Override
    public boolean isIsToDoLots() {
        return isToDoLots;
    }

    @Override
    public void setIsToDoLots(boolean isToDoLots) {
        this.isToDoLots = isToDoLots;
    }

    @Override
    public boolean isIsToDoAuth() {
        return isToDoAuth;
    }

    @Override
    public void setIsToDoAuth(boolean isToDoAuth) {
        this.isToDoAuth = isToDoAuth;
    }

    @Override
    public List<Purchase> getData() {
        return data;
    }

    @Override
    public void setData(List<Purchase> data) {
        this.data = data;
    }
}
