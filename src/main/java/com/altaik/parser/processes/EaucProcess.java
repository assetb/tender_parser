package com.altaik.parser.processes;

import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.auctionya.utils.EaucSaver;
import com.altaik.parser.eauc.EaucParser;

import java.util.List;
import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class EaucProcess extends ParserProcess {
    private IDatabaseManager dbManager;
    private EaucParser eaucParser = new EaucParser();
    private EaucSaver saver;

    public EaucProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
        saver = new EaucSaver(dbManager);
    }

    @Override
    protected void onClose() {
        dbManager.close();
    }

    @Override
    protected void onStart() {
        for (String ulr : eaucParser.getUrls()) {
            List<String> info = eaucParser.getPurchaseInfo(ulr);
            eaucParser.createPurchase(info);
            eaucParser.createLot(info);
        }
//        System.out.println(eaucParser.purchases);
//        System.out.println(eaucParser.lots);
        saver.save(eaucParser.getPurchases(), eaucParser.getLots());
    }
}
