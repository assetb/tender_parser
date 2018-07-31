/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.bo.Purchase;
import org.jsoup.select.Elements;

import java.util.List;

/**
 *
 * @author Aset
 */
public interface IParser {

    void Close();

    void FillPurchase();

    Elements GetRowsWithPurchases();

    void LotsDo();

    void PurchasesDo();

    void UploadFiles();

    boolean isIsToDoLots();

    void setIsToDoLots(boolean isToDoLots);

    boolean isIsToDoAuth();

    void setIsToDoAuth(boolean isToDoAuth);
    
    public List<Purchase> getData();
    
    public void setData(List<Purchase> data);
    
}
