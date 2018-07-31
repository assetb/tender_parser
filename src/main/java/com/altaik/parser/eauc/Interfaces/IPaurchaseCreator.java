package com.altaik.parser.eauc.Interfaces;


import com.altaik.bo.Lot;
import com.altaik.bo.Purchase;

public interface IPaurchaseCreator {

    Purchase createPurchases(ISelector selector);
    Lot createLots(ISelector selector);
}
