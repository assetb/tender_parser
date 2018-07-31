package com.altaik.parser.processes;

import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.Saver;
import com.altaik.parser.caspy.CaspyParser;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class CaspyProcess extends ParserProcess {
    private IDatabaseManager dbManager;

    public CaspyProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
    }

    @Override
    protected void onClose() {
        int count = getCount();
        CaspyParser parser = new CaspyParser();
        try {
            parser.execute(count);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            e.printStackTrace();
        }
        logger.log(Level.INFO, "Begin transaction");
        Saver saver = new Saver(dbManager);
        saver.save(parser.getPurchases(), parser.getLots());
    }

    @Override
    protected void onStart() {
        dbManager.close();
    }
}
