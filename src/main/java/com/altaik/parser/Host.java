/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser;

import com.altaik.bp.proxy.Proxies;
import com.altaik.crawler.CrawlerContext;
import com.altaik.crawler.ICrawler;
import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.parser.auctionya.AuctionyaParser;
import com.altaik.parser.caspy.CaspySaver;
import com.altaik.parser.eauc.EaucParser;
import com.altaik.parser.gos.GosAuthCrawler;
import com.altaik.parser.gos.GosCrawler;
import com.altaik.parser.gos.GosParser;
import com.altaik.parser.gos.GosSaver;
import com.altaik.parser.kazets.KazETSSaver;
import com.altaik.parser.main.ETSHost;
import com.altaik.parser.main.GosreestrHost;
import com.altaik.parser.main.KsGovHost;
import com.altaik.parser.main.NewGosHost;
import com.altaik.parser.nadloc.NadlocCrawler;
import com.altaik.parser.nadloc.NadlocParser;
import com.altaik.parser.nadloc.NadlocSaver;
import com.altaik.parser.processes.FactoryProcess;
import com.altaik.parser.processes.ParserProcess;
import com.altaik.parser.samruk.SamrukAuthCrawler;
import com.altaik.parser.samruk.SamrukCrawler;
import com.altaik.parser.samruk.SamrukParser;
import com.altaik.parser.samruk.SamrukSaver;
import com.altaik.parser.utb.Sirius;
import com.altaik.utils.Options;
import com.altaik.utils.OptionsSet;
import com.altaik.utils.Settings;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.*;

import static com.altaik.parser.Parameters.*;

/**
 * @author Aset
 */
public class Host {
    private static final String currentPropertiesFile = Paths.get(System.getProperty("java.io.tmpdir"), "_application.properties").toString();
    private static final String defaultApplicationPropertiesFile = "application.properties";
    private static final Logger logger = Logger.getLogger(Host.class.getName());
    private static String typeProcess = null;
    private static String settingFileName = null;

    private static Properties properties = new Properties();

    /**
     * Загрузка параметров по умолчанию. Параметры по умолчанию описываются в файле application.properties который
     * расположен в ресурсах проекта.
     *
     * @return Если параметры загружены то вернет true, иначе false
     */
    private static boolean loadDefaultParameters() {
        try (InputStream resource = Host.class.getClassLoader().getResourceAsStream(defaultApplicationPropertiesFile)) {
            properties.load(resource);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error load default properties");
            return false;
        }

        return true;
    }

    /**
     * Обработать параметры командной строки
     *
     * @param args Параметры командной строки
     * @throws Exception Исключение на случай казазания неверных параметров
     */
    private static void parseParameters(String[] args) {
        Options options = new Options(args);
        OptionsSet type = options.addSet(PARAMETER_TYPE_SHORT);
        OptionsSet count = options.addSet(PARAMETER_COUNT_SHORT);
        OptionsSet setting = options.addSet(PARAMETER_SETTINGS_SHORT);
        OptionsSet xparams = options.addSet("X[^\\s]+");

        if (type.isSet())
            typeProcess = type.getData();

        if (count.isSet())
            properties.setProperty(ParserProcess.SPEC_PARAMETER_COUNT, count.getData());

        if (setting.isSet())
            settingFileName = setting.getData();

        if (xparams.isSet()) {
            Map<String, String> all = xparams.getFindAll();
            all.forEach((key, value) -> properties.setProperty(key.replaceAll("X(.*)", "$1"), value));
        }
    }

    /**
     * Инициализация приложения
     *
     * @return Возвращаяет true если инитиализация прошла успешно, иначе false
     */
    private static boolean initialize() {
        Handler ch = new ConsoleHandler();
        logger.addHandler(ch);


        if (settingFileName != null && !settingFileName.isEmpty()) {
            try (InputStream settingsStream = new FileInputStream(settingFileName)) {
                properties.load(settingsStream);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error open settings file {0}", settingFileName);

                return false;
            }
        }

        return true;
    }

    private static String getGosType(ZakupTypesEnum type) {
        String t = null;
        switch (type) {
            case GosBuys:
                t = "buys";
                break;
            case GosOpenBuys:
                t = "openbuys";
                break;
            case GosAucs:
                t = "aucs";
                break;
        }
        return t;
    }

    public static void main(String[] args) {
        ParserProcess currentProcess = null;

        try {
            if (!loadDefaultParameters()) {
                logger.log(Level.SEVERE, "Error load default parameters");
                return;
            }

            parseParameters(args);

            if (!initialize()) {
                logger.log(Level.SEVERE, "Error initialize application");
                return;
            }

            Settings.setLogManagerSetting(LogManager.getLogManager(), properties);
            currentProcess = FactoryProcess.newInstance(typeProcess, properties);

            if (currentProcess != null) {
                currentProcess.run();
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error run application", ex);
        } finally {
            if (currentProcess != null) {
                currentProcess.close();
            }
        }
    }

    private static void run(String[] args) {

        String fileNameLogger = "./logging.properties";
        String fileNameApplication = "./application.properties";

        InputStream inputStreamLogger;
        InputStream inputStreamApplication;

        try {
            inputStreamLogger = new FileInputStream(fileNameLogger);
            inputStreamApplication = new FileInputStream(fileNameApplication);
        } catch (IOException ioex) {
            logger.log(Level.SEVERE, "Error open logger properties files {0} and {1}\n{2}", new Object[]{fileNameLogger, fileNameApplication, ioex.getMessage()});
            return;
        }

        try {
            LogManager.getLogManager().readConfiguration(inputStreamLogger);
        } catch (IOException ioex) {
            logger.log(Level.SEVERE, "Could not setup logger configuration: {0}", ioex.getMessage());
        }
        try {
            if (args.length < 1) {
                logger.log(Level.OFF, "Not specified argument");
                return;
            }

            Properties prop = new Properties();
            try {
                prop.load(inputStreamApplication);
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, "Not found file {0} \n{1}", new Object[]{fileNameApplication, ex.getMessage()});
                return;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error load file {0}\n{1}", new Object[]{fileNameApplication, ex.getMessage()});
                return;
            }

            ZakupTypesEnum zakupTypesEnum = null;
            RunningTypeEnum runningTypeEnum = RunningTypeEnum.NONE;

            for (ZakupTypesEnum item : ZakupTypesEnum.values()) {
                if (item.toString().equals(args[0])) {
                    zakupTypesEnum = item;
                    break;
                }
            }
            if (zakupTypesEnum == null) {
                logger.log(Level.INFO, "Invalid argument for the application.");
                return;
            }
            logger.log(Level.INFO, "Start {0}. Version 2.0 started.", args[0]);

            if (args.length > 1 && "nightly".equals(args[1])) {
                runningTypeEnum = RunningTypeEnum.Nightly;
            }

            IDatabaseManager dbManager;
            Proxies proxies = null;
            if (zakupTypesEnum == ZakupTypesEnum.Nadloc) {
                dbManager = (IDatabaseManager) new DatabaseManager(false, true);
            } else {
                dbManager = (IDatabaseManager) new DatabaseManager(false, true);
                proxies = new Proxies(dbManager);
            }
            if (!dbManager.Valid()) {
                logger.log(Level.SEVERE, "Not connetcion for database.");
                return;
            }

            ArrayList<ICrawler> crawlers;
            ArrayList<IParser> parsers;

            CrawlerContext crawlerContext = new CrawlerContext(proxies);

            switch (zakupTypesEnum) {
                case GosBuys: {
                    if (prop.getProperty("buysPassword") == null || prop.getProperty("buysPassword").isEmpty()) {
                        logger.log(Level.SEVERE, "{0}: No password.", args[0]);
                        return;
                    }

                    crawlers = new ArrayList<>();
                    parsers = new ArrayList<>();

                    Properties buysCertProps = new Properties();
                    buysCertProps.setProperty("keyStore", prop.getProperty("buysKeyStore"));
                    buysCertProps.setProperty("keyStoreType", prop.getProperty("buysKeyType"));
                    buysCertProps.setProperty("keyStorePassword", prop.getProperty("buysKeyPassword"));
                    buysCertProps.setProperty("trustStore", prop.getProperty("buysTrustStore"));
                    buysCertProps.setProperty("trustStorePassword", prop.getProperty("buysTrustPassword"));

                    GosAuthCrawler gosAuthCrawler = new GosAuthCrawler();
//                gosAuthCrawler.setUserAgent("Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko");
                    gosAuthCrawler.AuthInit(buysCertProps);
                    boolean isAuthGosBuys = gosAuthCrawler.authGosAuction(prop.getProperty("buysPassword"));
                    logger.log(Level.INFO, "Authorizate is {0}", isAuthGosBuys);

                    int countPage;
                    if (runningTypeEnum == RunningTypeEnum.Nightly) {
                        countPage = Integer.parseInt(prop.getProperty("buysCountPagesNightly", "1"));
                    } else {
                        countPage = Integer.parseInt(prop.getProperty("buysCountPages", "1"));
                    }
                    logger.log(Level.INFO, "Count load page = {0}", countPage);

                    for (int i = countPage; i >= 0; i = i - 1) {

                        ICrawler gosCrawler = new GosCrawler(crawlerContext, ZakupTypesEnum.GosBuys);
                        String url = gosCrawler.getUrl();

                        if (i > 0) {
                            gosCrawler.setUrl(url + "/" + i);
                        }

                        gosCrawler.setMethod(Method.POST);
                        Map<String, String> param = new HashMap<>();
                        param.put("filter[lotstatus]", "NOTSELECTED");
                        param.put("filter[method]", "NOTSELECTED");
                        param.put("filter[region]", "NOTSELECTED");
                        gosCrawler.setData(param, true);

                        if (null != gosCrawler.getDoc()) {
                            GosParser gosParser = new GosParser(gosCrawler.getDoc(), dbManager, gosAuthCrawler, crawlerContext, getGosType(ZakupTypesEnum.GosBuys));
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
                        parser.Close();
                    }

                    parsers.clear();

                    for (ICrawler gosCrawler : crawlers) {
                        gosCrawler.close();
                    }

                    crawlers.clear();
                }
                break;

                case GosAucs: {
                    if (prop.getProperty("aucsPassword") == null || prop.getProperty("aucsPassword").isEmpty()) {
                        logger.log(Level.SEVERE, "{0}: No password.", args[0]);
                        return;
                    }

                    Properties certProps = new Properties();
                    certProps.setProperty("keyStore", prop.getProperty("aucsKeyStore"));
                    certProps.setProperty("keyStoreType", prop.getProperty("aucsKeyType"));
                    certProps.setProperty("keyStorePassword", prop.getProperty("aucsKeyPassword"));
                    certProps.setProperty("trustStore", prop.getProperty("aucsTrustStore"));
                    certProps.setProperty("trustStorePassword", prop.getProperty("aucsTrustPassword"));
                    String password = prop.getProperty("aucsPassword");

                    GosAuthCrawler gosAuthCrawler = new GosAuthCrawler();
                    gosAuthCrawler.AuthInit(certProps);

                    boolean isAuthGosAucs = gosAuthCrawler.authGosAuction(password);
                    logger.log(Level.INFO, "Authorizate is {0}", isAuthGosAucs);

                    crawlers = new ArrayList<>();
                    parsers = new ArrayList<>();

                    int countPage;
                    if (runningTypeEnum == RunningTypeEnum.Nightly) {
                        countPage = Integer.parseInt(prop.getProperty("aucsCountPagesNightly", "1"));
                    } else {
                        countPage = Integer.parseInt(prop.getProperty("aucsCountPages", "1"));
                    }
                    logger.log(Level.INFO, "Count load page = {0}", countPage);

                    for (int i = countPage; i >= 0; i = i - 1) {
                        ICrawler gosCrawler = new GosCrawler(crawlerContext, ZakupTypesEnum.GosAucs);
                        String url = gosCrawler.getUrl();
                        if (i > 0) {
                            gosCrawler.setUrl(url + "/" + i);
                        }
                        Map<String, String> param = new HashMap<>();
                        gosCrawler.setMethod(Method.POST);
                        param.put("filter[lotstatus]", "NOTSELECTED");
                        param.put("filter[region]", "NOTSELECTED");
                        param.put("filter[submit]", "Применить");
                        gosCrawler.setData(param, true);
                        if (null != gosCrawler.getDoc()) {
                            GosParser gosParser = new GosParser(gosCrawler.getDoc(), dbManager, gosAuthCrawler, crawlerContext, getGosType(ZakupTypesEnum.GosAucs));
                            gosParser.setIsToDoAuth(isAuthGosAucs);
                            parsers.add(gosParser);
                        }
                        crawlers.add(gosCrawler);
                    }
                    for (IParser parser : parsers) {
                        parser.PurchasesDo();
                        if (null != parser.getData() && !parser.getData().isEmpty()) {
                            GosSaver gosSaver = new GosSaver(dbManager, ZakupTypesEnum.GosAucs);
                            gosSaver.isToSaveAuth = isAuthGosAucs;
                            gosSaver.Do(parser.getData());
                            gosSaver.Close();
                        }
                        parser.Close();
                    }
                    parsers.clear();
                    for (ICrawler gosCrawler : crawlers) {
                        gosCrawler.close();
                    }
                    crawlers.clear();
                }
                break;

                case GosOpenBuys: {
                    if (prop.getProperty("openBuysPassword") == null || prop.getProperty("openBuysPassword").isEmpty()) {
                        logger.log(Level.SEVERE, "{0}: No password.", args[0]);
                        return;
                    }

                    Properties certProps = new Properties();
                    certProps.setProperty("keyStore", prop.getProperty("openBuysKeyStore"));
                    certProps.setProperty("keyStoreType", prop.getProperty("openBuysKeyType"));
                    certProps.setProperty("keyStorePassword", prop.getProperty("openBuysKeyPassword"));
                    certProps.setProperty("trustStore", prop.getProperty("openBuysTrustStore"));
                    certProps.setProperty("trustStorePassword", prop.getProperty("openBuysTrustPassword"));
                    String password = prop.getProperty("openBuysPassword");
                    GosAuthCrawler gosAuthCrawler = new GosAuthCrawler();
                    gosAuthCrawler.AuthInit(certProps);
                    boolean isAuthGosOpenBuys = gosAuthCrawler.authGosOpenBuys(password);
                    logger.log(Level.INFO, "Authorizate is {0}", isAuthGosOpenBuys);
                    crawlers = new ArrayList<>();
                    parsers = new ArrayList<>();
                    int step = 5;
                    int countPage;

                    if (runningTypeEnum == RunningTypeEnum.Nightly) {
                        countPage = Integer.parseInt(prop.getProperty("openBuysCountPagesNightly", "1"));
                    } else {
                        countPage = Integer.parseInt(prop.getProperty("openBuysCountPages", "1"));
                    }

                    logger.log(Level.INFO, "Count load page = {0}", countPage);
                    int countMaxPage = countPage;

                    if (countMaxPage % step > 0) {
                        countMaxPage += step - (countMaxPage % step);
                    }

                    for (int i = step; i <= countMaxPage; ) {
                        ICrawler crawler = new GosCrawler(crawlerContext, ZakupTypesEnum.GosOpenBuys);
                        Map<String, String> param = new HashMap<>();
                        crawler.setMethod(Method.POST);
                        param.put("filter[buystatus]", "NOTSELECTED");
                        param.put("filter[method]", "NOTSELECTED");
                        param.put("filter[zak_region]", "NOTSELECTED");
                        param.put("filter[submit]", "Применить");
                        crawler.setData(param, true);

                        if (crawler.getDoc() != null && crawler.getCookies() != null && !crawler.getDoc().title().contains("429")) {
                            Map<String, String> cookie = crawler.getCookies();

                            for (int j = i - (step - 1); j <= i && j <= countPage; j++) {
                                crawler = new GosCrawler(crawlerContext, ZakupTypesEnum.GosOpenBuys);
                                crawler.IsSinglProxy(true);

                                for (String key : cookie.keySet()) {
                                    crawler.setCookie(key, cookie.get(key));
                                }

                                crawler.setMethod(Method.GET);
                                crawler.setUrl(crawler.getUrl() + "/" + j);

                                if (null != crawler.getDoc()) {
                                    GosParser gosParser = new GosParser(crawler.getDoc(), dbManager, gosAuthCrawler, crawlerContext, getGosType(ZakupTypesEnum.GosOpenBuys));
                                    gosParser.setIsToDoAuth(isAuthGosOpenBuys);
                                    parsers.add(gosParser);
                                }

                                cookie = crawler.getCookies();
                                crawlers.add(crawler);
                            }

                        } else {
                            logger.log(Level.WARNING, "Doc is null or cookies is null or the captcha.");
                            continue;
                        }

                        i = i + step;
                    }

                    for (IParser parser : parsers) {
                        parser.PurchasesDo();

                        if (null != parser.getData() && !parser.getData().isEmpty()) {
                            GosSaver gosSaver = new GosSaver(dbManager, ZakupTypesEnum.GosOpenBuys);
                            gosSaver.isToSaveAuth = isAuthGosOpenBuys;
                            gosSaver.Do(parser.getData());
                            gosSaver.Close();
                        }

                        parser.Close();
                    }

                    parsers.clear();

                    for (ICrawler gosCrawler : crawlers) {
                        gosCrawler.close();
                    }

                    crawlers.clear();
                }
                break;

                case Samruk: {
                    SamrukAuthCrawler samrukAuthCrawler = null;
                    boolean isAuthSamruk = false;


                    Properties certProps = new Properties();
                    certProps.setProperty("keyStore", prop.getProperty("samrukKeyStore"));
                    certProps.setProperty("keyStoreType", prop.getProperty("samrukKeyType"));
                    certProps.setProperty("keyStorePassword", prop.getProperty("samrukKeyPassword"));
                    certProps.setProperty("trustStore", prop.getProperty("samrukTrustStore"));
                    certProps.setProperty("trustStorePassword", prop.getProperty("samrukTrustPassword"));
                    String password = prop.getProperty("samrukPassword");

                    samrukAuthCrawler = new SamrukAuthCrawler();
                    samrukAuthCrawler.AuthInit(certProps);

                    isAuthSamruk = samrukAuthCrawler.authSamruk(password);
                    logger.log(Level.INFO, "Authorizate is {0}", isAuthSamruk);
                    crawlers = new ArrayList<>();
                    ArrayList<SamrukParser> samrukParsers = new ArrayList<>();

                    int countPage;
                    if (runningTypeEnum == RunningTypeEnum.Nightly) {
                        countPage = Integer.parseInt(prop.getProperty("samrukCountPagesNightly", "1"));
                    } else {
                        countPage = Integer.parseInt(prop.getProperty("samrukCountPages", "1"));
                    }

                    logger.log(Level.INFO, "Count load page = {0}", countPage);

                    for (int i = countPage; i >= 0; i = i - 10) {
                        SamrukCrawler samrukCrawler = new SamrukCrawler(crawlerContext);
                        String url = "http://tender.sk.kz/index.php/ru/negs";
                        if (i > 0) {
                            url = url + "/" + i;
                        }
                        samrukCrawler.setUrl(url);
                        samrukCrawler.params = new ArrayList<>();
                        String[] param1 = new String[2];
                        param1[0] = "method";
                        param1[1] = "NONE";
                        samrukCrawler.params.add(param1);
                        String[] param2 = new String[2];
                        param2[0] = "status";
                        param2[1] = "NONE";
                        samrukCrawler.params.add(param2);
                        String[] param4 = new String[2];
                        param4[0] = "submited";
                        param4[1] = "Искать";
                        samrukCrawler.params.add(param4);
//            String[] param5 = new String[2];
//            param5[0] = "customer";
//            param5[1] = "Объединение «Дальняя связь» - филиал Акционерного общества «Казахтелеком»";
                        //samrukCrawler.params.add(param5);
                        samrukCrawler.setMethod(Method.POST);
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
                            samrukSaver.Close();
                        }
                        samrukParser.Close();
                    }
                    samrukParsers.clear();
                    for (ICrawler crawler : crawlers) {
                        crawler.close();
                    }
                    crawlers.clear();
                }
                break;

                case Nadloc: {

                    String path = prop.getProperty("nadlocPath", "");
                    int intervalMin = Integer.parseInt(prop.getProperty("nadlocIntervalMin", "50"));
                    int intervalMax = Integer.parseInt(prop.getProperty("nadlocIntervalMax", "90"));
                    int countPage = Integer.parseInt(prop.getProperty("nadlocCountPages", "1"));

                    logger.log(Level.INFO, "Count load page = {0}", countPage);

                    if (path.isEmpty() || intervalMin > intervalMax) {
                        logger.log(Level.SEVERE, "{0}: Error read properties.", args[0]);
                        return;
                    }
                    NadlocCrawler nadlocCrawler = new NadlocCrawler();
                    nadlocCrawler.Init();
//                NadlocCrawler.setInterval(intervalMin, intervalMax);
                    nadlocCrawler.minIntervalSleep = intervalMin;
                    nadlocCrawler.maxIntervalSleep = intervalMax;

                    if (!nadlocCrawler.getCookiesMoz(path)) {
                        logger.log(Level.SEVERE, "Error read cookies");
                        return;
                    }

                    parsers = new ArrayList<>();
                    int size = 100;

                    for (int i = 1; i <= countPage; i++) {
                        String url = "/ru/tender/List?p=" + i + "&s=" + size + "&flt_by_status=0&flt_by_type=0";
//                    NadlocCrawler nadlocCrawler = new NadlocCrawler();
                        nadlocCrawler.setPath(url);
                        nadlocCrawler.setMethod(Method.GET);
                        Document document = nadlocCrawler.getDoc();
                        if (null != document) {
                            NadlocParser gosParser = new NadlocParser(document, dbManager, nadlocCrawler);
//                        gosParser.setIsToDoAuth(true);
                            parsers.add(gosParser);
                        }
                    }
                    for (IParser parser : parsers) {
                        parser.PurchasesDo();
                        if (null != parser.getData() && !parser.getData().isEmpty()) {
                            NadlocSaver gosSaver = new NadlocSaver(dbManager);
                            gosSaver.isToSaveAuth = true;
                            gosSaver.Do(parser.getData());
                            gosSaver.Close();
                        }
                        parser.Close();
                    }
                    parsers.clear();
                    nadlocCrawler.close();
                }
                break;

                case NEWGOS: {
                    int countPage = 1;

                    try {

                        if (runningTypeEnum == RunningTypeEnum.Nightly) {
                            countPage = Integer.parseInt(prop.getProperty("newgosCountPagesNightly"));
                        } else {
                            countPage = Integer.parseInt(prop.getProperty("newgosCountPages"));
                        }

                        logger.log(Level.INFO, "Count load page = {0}", countPage);
                    } catch (NumberFormatException ex) {
                        logger.log(Level.WARNING, "Error in the value format in fields \"CountPages\" or \"CountPagesNightly\" in {0} with property file.", zakupTypesEnum.name());
                    }

                    Properties propNewGos = new Properties();
                    propNewGos.setProperty("keyStore", prop.getProperty("newgosKeyStore"));
                    propNewGos.setProperty("keyStoreType", prop.getProperty("newgosKeyType"));
                    propNewGos.setProperty("keyStorePassword", prop.getProperty("newgosKeyPassword"));
                    propNewGos.setProperty("trustStore", prop.getProperty("newgosTrustStore"));
                    propNewGos.setProperty("trustStorePassword", prop.getProperty("newgosTrustPassword"));
                    NewGosHost newGos = new NewGosHost(dbManager, propNewGos);
                    newGos.setCountPages(countPage);
                    newGos.Run();
                }
                break;

                case ETS: {
                    ETSHost ets = new ETSHost(dbManager, zakupTypesEnum);
                    ets.Run();
                }
                break;

                case SIRIUS: {
                    Sirius s = new Sirius(dbManager, zakupTypesEnum);
                    s.Run();
                }
                break;

                case PostProcess: {
                    Postprocessing postprocessing = new Postprocessing(dbManager);
                    postprocessing.Do();
//                    postprocessing.setMaxSumFormPurchase();
                    postprocessing.DocsProcessing();
                    postprocessing.Close();
                }
                break;

                case KSGOV: {
                    KsGovHost govHost = new KsGovHost(dbManager, prop);
                    govHost.Run();
                }
                break;

                case GOSREESTR: {
                    int countPages = 10;

                    try {
                        countPages = Integer.parseInt(prop.getProperty("gosreestCoutPage"));
                    } catch (NumberFormatException ex) {
                        logger.log(Level.WARNING, "Property \"gosreestCoutPage\" incorrect number");
                    }

                    GosreestrHost gosReestrHost = new GosreestrHost(dbManager, countPages, (runningTypeEnum == RunningTypeEnum.Nightly), zakupTypesEnum);
                    gosReestrHost.Run();
                }
                break;
                case AUCYA: {
                    int countPurchase = 10;

                    try {
                        countPurchase = Integer.parseInt(prop.getProperty("auctionyaPurchaseCount"));
                    } catch (NumberFormatException ex) {
                        logger.log(Level.WARNING, "Property \"auctionyaPurchaseCount\" incorrect number");
                    }

                    AuctionyaParser.execute(countPurchase, dbManager);
                }
                break;
                case CASPY: {
                    int countPurchase = Integer.parseInt(prop.getProperty("caspyPageCount"));
                    CaspySaver.execute(countPurchase, dbManager);
                }
                break;
                case KAZETS: {
//                int countPurchase = Integer.parseInt(prop.getProperty("caspyPageCount"));
                    KazETSSaver.execute(dbManager);
                }
                break;
                case EAUC: {
//                int countPurchase = Integer.parseInt(prop.getProperty("caspyPageCount"));
                    EaucParser.execute(dbManager);
                }
                break;

                default: {
                    logger.log(Level.INFO, "Invalid argument for the application.");
                }
            }

            if (null != proxies) {
                proxies.close();
            }
//        dbManager.close();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error run application: {0}", ex);
        }

        logger.log(Level.INFO, "Finish.");
    }

}
