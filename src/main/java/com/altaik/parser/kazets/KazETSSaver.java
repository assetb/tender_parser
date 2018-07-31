package com.altaik.parser.kazets;

import com.altaik.db.IDatabaseManager;
import com.altaik.parser.auctionya.utils.EaucSaver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Asus-X555LD_101 on 09.06.2017.
 */
public class KazETSSaver {

    private static final Logger logger = Logger.getLogger(KazETSSaver.class.getName());

    public static void execute(IDatabaseManager databaseManager) {

        KazETSParser parser = new KazETSParser();
        try {
            parser.execute();
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
