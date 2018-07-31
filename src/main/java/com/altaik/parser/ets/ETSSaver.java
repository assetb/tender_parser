/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.ets;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.Saver;

/**
 *
 * @author admin
 */
public class ETSSaver extends Saver {

    public ETSSaver(IDatabaseManager dbManager) {
        super(dbManager);
        isToSaveAuth = true;
    }
    
}
