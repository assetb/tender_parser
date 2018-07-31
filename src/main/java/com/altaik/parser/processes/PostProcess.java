package com.altaik.parser.processes;

import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.Postprocessing;

import java.util.Properties;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class PostProcess extends ParserProcess {
    private IDatabaseManager dbManager;
    private Postprocessing postprocessing;

    public PostProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
        postprocessing = new Postprocessing(dbManager);
    }

    @Override
    protected void onClose() {
        postprocessing.Close();
        dbManager.close();
    }

    @Override
    protected void onStart() {
        postprocessing.Do();
        postprocessing.DocsProcessing();
    }
}
