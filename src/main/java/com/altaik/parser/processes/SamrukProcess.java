package com.altaik.parser.processes;

import com.altaik.bp.proxy.Proxies;
import com.altaik.crawler.CrawlerContext;
import com.altaik.crawler.ICrawler;
import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.IParser;
import com.altaik.parser.samruk.SamrukAuthCrawler;
import com.altaik.parser.samruk.SamrukCrawler;
import com.altaik.parser.samruk.SamrukParser;
import com.altaik.parser.samruk.SamrukSaver;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Vladimir Kovalev (v.kovalev@com.altaik.db.altatender.kz) on 29.03.2018
 */
public class SamrukProcess extends ParserProcess {
    private IDatabaseManager dbManager;
    private Properties securyProperties = new Properties();
    private String password;
    private List<ICrawler> crawlers = new ArrayList<>();
    private ArrayList<SamrukParser> samrukParsers = new ArrayList<>();
    private CrawlerContext crawlerContext;

    public SamrukProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
        Proxies proxies = new Proxies(dbManager);
        crawlerContext = new CrawlerContext(proxies);
        initialize();
    }

    private void initialize() {
        Properties prop = getProperties();
        String samrukKeyStore = prop.getProperty("samrukKeyStore");

        if (samrukKeyStore != null && !samrukKeyStore.isEmpty())
            securyProperties.setProperty("keyStore", samrukKeyStore);

        String samrukKeyType = prop.getProperty("samrukKeyType");

        if (samrukKeyType != null && !samrukKeyType.isEmpty())
            securyProperties.setProperty("keyStoreType", samrukKeyType);

        String samrukKeyPassword = prop.getProperty("samrukKeyPassword");

        if (samrukKeyPassword != null && !samrukKeyPassword.isEmpty())
            securyProperties.setProperty("keyStorePassword", samrukKeyPassword);

        String samrukTrustStore = prop.getProperty("samrukTrustStore");

        if (samrukTrustStore != null && !samrukTrustStore.isEmpty())
            securyProperties.setProperty("trustStore", samrukTrustStore);

        String samrukTrustPassword = prop.getProperty("samrukTrustPassword");

        if (samrukTrustPassword != null && !samrukTrustPassword.isEmpty())
            securyProperties.setProperty("trustStorePassword", samrukTrustPassword);

        password = prop.getProperty("samrukPassword");
    }

    @Override
    protected void onClose() {
        dbManager.close();
        crawlers.forEach(ICrawler::close);
        samrukParsers.forEach(IParser::Close);
        crawlers.clear();
        samrukParsers.clear();
    }

    @Override
    protected void onStart() {
        SamrukAuthCrawler samrukAuthCrawler;
        boolean isAuthSamruk;
        samrukAuthCrawler = new SamrukAuthCrawler();
        samrukAuthCrawler.AuthInit(securyProperties);
        isAuthSamruk = samrukAuthCrawler.authSamruk(password);
        logger.log(Level.INFO, "Authorizate is {0}", isAuthSamruk);
        int countPage = getCount();
        logger.log(Level.INFO, "Count load page = {0}", countPage);

        for (int i = countPage; i >= 0; i = i - 10) {
            SamrukCrawler samrukCrawler = new SamrukCrawler(crawlerContext);
            String url = "http://tender.sk.kz/index.php/ru/negs";

            if (i > 0) {
                url = url + "/" + i;
            }

            samrukCrawler.setUrl(url);
            ArrayList<String[]> params = new ArrayList<>();
            params.add(new String[]{"method", "NONE"});
            params.add(new String[]{"status", "NONE"});
            params.add(new String[]{"submited", "Искать"});
            samrukCrawler.params = params;
            samrukCrawler.setMethod(Connection.Method.POST);

            if (null != samrukCrawler.getDoc()) {
                SamrukParser samrukParser = new SamrukParser(samrukCrawler.getDoc(), dbManager, crawlerContext, samrukAuthCrawler);
                samrukParser.setIsToDoAuth(isAuthSamruk);
                samrukParsers.add(samrukParser);
            }

            crawlers.add(samrukCrawler);
        }

        for (SamrukParser samrukParser : samrukParsers) {
            samrukParser.PurchasesDo();
            if (null != samrukParser.data && !samrukParser.data.isEmpty()) {
                SamrukSaver samrukSaver = new SamrukSaver(dbManager);
                samrukSaver.isToSaveAuth = isAuthSamruk;
                samrukSaver.Do(samrukParser.data);
            }

        }
    }
}
