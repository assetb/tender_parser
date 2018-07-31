/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.utb;

import com.altaik.bo.Purchases;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.ZakupTypesEnum;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class Sirius {

    private final String HOST = "82.200.128.179";
//    private final String HOST = "127.0.0.1";
    private final String PORT = "3306";
    private final String USER = "parser";
    private final String PASS = "ikalta7&";
    private final String DBNAME = "broker-aliya";
    
    private static final Logger logger = Logger.getLogger(Sirius.class.getName());
    private final IDatabaseManager db;
    private final SiriusDBManager dbSirius;
    private final ZakupTypesEnum type;
    
    public Sirius(IDatabaseManager db, ZakupTypesEnum type) {        
        this.type = type;
        this.db = db;
        Properties props = new Properties();
        
        props.setProperty("host", HOST);
        props.setProperty("port", PORT);
        props.setProperty("dbuser", USER);
        props.setProperty("dbpassword", PASS);
        props.setProperty("dbname", DBNAME);
        
        this.dbSirius = new SiriusDBManager(props);
    }
    
    public void Run() {
        Purchases purchases = dbSirius.GetPurchases();
        if (purchases != null) {
            purchases.stream().forEach((p) -> {
                p.setSource(type.getSource());
            });
            SiriusSaver ss = new SiriusSaver(db, type.getSource().toString(), null);
            ss.Do(purchases);
            logger.log(Level.INFO, "Imported purchases({0})", purchases.size());
        } else {
            logger.log(Level.SEVERE, "Fatal error parsing of sirius.");
        }
    }
}
