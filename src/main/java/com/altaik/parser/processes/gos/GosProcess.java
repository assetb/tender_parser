package com.altaik.parser.processes.gos;

import com.altaik.bp.proxy.Proxies;
import com.altaik.crawler.CrawlerContext;
import com.altaik.crawler.ICrawler;
import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.IParser;
import com.altaik.parser.ZakupTypesEnum;
import com.altaik.parser.gos.GosAuthCrawler;
import com.altaik.parser.gos.GosCrawler;
import com.altaik.parser.gos.GosParser;
import com.altaik.parser.gos.GosSaver;
import com.altaik.parser.processes.ParserProcess;
import org.jsoup.Connection;

import java.util.*;
import java.util.logging.Level;

import static com.altaik.parser.processes.gos.GosParameters.*;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 27.03.2018
 */
public class GosProcess extends ParserProcess {

    private final List<ICrawler> crawlers = new ArrayList<>();
    private final List<IParser> parsers = new ArrayList<>();
    private boolean isNightly = false;
    private String password = null;
    private Properties secureProperties = new Properties();
    private IDatabaseManager dbManager;
    private CrawlerContext crawlerContext;
    private String currentType = null;

    public GosProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
        Proxies proxies = new Proxies(dbManager);
        crawlerContext = new CrawlerContext(proxies);
        initialize();
    }


    private void initialize() {
        Properties properties = getProperties();
        currentType = properties.getProperty(SPEC_PROPERTY);
        password = properties.getProperty(BUYS_PASSWORD);
        secureProperties.setProperty("keyStore", properties.getProperty(KEY_STORE));
        secureProperties.setProperty("keyStoreType", properties.getProperty(KEY_STORE_TYPE));
        secureProperties.setProperty("keyStorePassword", properties.getProperty(KEY_PASSWORD));
        secureProperties.setProperty("trustStore", properties.getProperty(TRUST_STORE));
        secureProperties.setProperty("trustStorePassword", properties.getProperty(TRUST_STORE_PASSWORD));

        String nightlyProperty = properties.getProperty(SPEC_PROPERTY_IS_NIGHTLY);

        if (nightlyProperty != null && !nightlyProperty.isEmpty()) {
            isNightly = "true".equalsIgnoreCase(nightlyProperty);
        }

        String countPagesProperty;

        if (isNightly) {
            countPagesProperty = properties.getProperty(BUYS_COUNT_PAGES_NIGHTLY);
        } else {
            countPagesProperty = properties.getProperty(BUYS_COUNT_PAGES);
        }

        if (countPagesProperty != null && !countPagesProperty.isEmpty())
            setCount(Integer.parseInt(countPagesProperty));


    }

    @Override
    protected void onClose() {
        parsers.forEach(IParser::Close);
        parsers.clear();
        crawlers.forEach(ICrawler::close);
        crawlers.clear();
    }

    @Override
    protected void onStart() {

        if (currentType == null || currentType.isEmpty()) {
            logger.log(Level.WARNING, "Not set property {0} for current process", SPEC_PROPERTY);
            return;
        }

        int count = getCount();
        GosAuthCrawler gosAuthCrawler = new GosAuthCrawler();
        gosAuthCrawler.AuthInit(secureProperties);
        boolean isAuthGosBuys = gosAuthCrawler.authGosAuction(password);
        logger.log(Level.INFO, "Authorizate is {0}", isAuthGosBuys);
        logger.log(Level.INFO, "Count load page = {0}", count);

        for (int i = count; i >= 0; i = i - 1) {
            String baseUrl = getUrl(currentType);
            ICrawler gosCrawler = new GosCrawler(baseUrl, crawlerContext);
            String url = gosCrawler.getUrl();

            if (i > 0) {
                gosCrawler.setUrl(url + "/" + i);
            }

            gosCrawler.setMethod(Connection.Method.POST);
            Map<String, String> param = new HashMap<>();
            param.put("filter[lotstatus]", "NOTSELECTED");
            param.put("filter[method]", "NOTSELECTED");
            param.put("filter[region]", "NOTSELECTED");
            gosCrawler.setData(param, true);

            if (null != gosCrawler.getDoc()) {
                GosParser gosParser = new GosParser(gosCrawler.getDoc(), dbManager, gosAuthCrawler, crawlerContext, currentType);
                gosParser.setIsToDoAuth(isAuthGosBuys);
                parsers.add(gosParser);
            }
            crawlers.add(gosCrawler);
        }

        for (IParser parser : parsers) {
            parser.PurchasesDo();
            if (null != parser.getData() && !parser.getData().isEmpty()) {
                GosSaver gosSaver = new GosSaver(dbManager, ZakupTypesEnum.GosBuys);
                gosSaver.isToSaveAuth = isAuthGosBuys;
                gosSaver.Do(parser.getData());
                gosSaver.Close();
            }
        }

    }

    private String getUrl(String type) {
        String url = null;

        switch (type) {
            case TYPE_BUYS:
                url = "http://portal.goszakup.gov.kz/portal/index.php/ru/oebs/buys";
                break;
            case TYPE_OPEN_BUYS:
                url = "http://portal.goszakup.gov.kz/portal/index.php/ru/publictrade/openbuys";
                break;
            case TYPE_AUCS:
                url = "http://portal.goszakup.gov.kz/portal/index.php/ru/oebs/aucs";
                break;
        }

        return url;
    }
}
