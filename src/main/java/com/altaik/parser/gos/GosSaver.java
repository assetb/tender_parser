/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.gos;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Saver;
import com.altaik.parser.ZakupTypesEnum;

/**
 *
 * @author Aset
 */
public class GosSaver extends Saver{
    
    //private ZakupTypesEnum type;

    public GosSaver(IDatabaseManager dbManager,ZakupTypesEnum type) {
        super(dbManager);
        saverName = type == ZakupTypesEnum.GosBuys ? "BuysGosSaver" : type == ZakupTypesEnum.GosOpenBuys ? "OpenBuysGosSaver":"AucsGosSaver";
    }

}
