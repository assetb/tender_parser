package com.altaik.parser.caspy;


import com.altaik.db.IDatabaseManager;
import com.altaik.parser.auctionya.utils.EaucSaver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaspySaver {

    private static final Logger logger = Logger.getLogger(CaspySaver.class.getName());

    public static void execute(int count, IDatabaseManager databaseManager) {
        CaspyParser parser = new CaspyParser();
        try {
            parser.execute(count);
        } catch (IOException e) {
            logger.log(Level.WARNING,e.toString());
            e.printStackTrace();
        }
        System.out.println("Begin transaction");
        logger.log(Level.INFO,"Begin transaction");
        EaucSaver saver = new EaucSaver(databaseManager);
        saver.save(parser.purchaseList,parser.lotList);
    }



}
