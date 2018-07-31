package com.altaik.parser.processes;

import com.altaik.db.DatabaseManager;
import com.altaik.db.IDatabaseManager;
import com.altaik.db.RepositoryFactory;
import com.altaik.parser.Saver;
import com.altaik.parser.ets.ETSCrawler;
import com.altaik.parser.ets.ETSParser;
import com.altaik.parser.ets.ETSSaver;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Vladimir Kovalev (v.kovalev@altatender.kz) on 29.03.2018
 */
public class EtsProcess extends ParserProcess {
    private IDatabaseManager dbManager;
    private ETSCrawler memberCrawler = new ETSCrawler();
    private ETSCrawler crawler = new ETSCrawler();
    private ETSParser parser = null;
    private List<Saver> savers = new ArrayList<>();

    public EtsProcess(Properties properties) {
        super(properties);
        dbManager = RepositoryFactory.getRepository(DatabaseManager.class, properties);
    }

    @Override
    protected void onClose() {
        dbManager.close();
        memberCrawler.close();
        crawler.close();

        if (parser != null)
            parser.Close();

        savers.forEach(Saver::Close);
    }

    @Override
    protected void onStart() {
// Загрузка страницы "Члены биржи"
        memberCrawler.setUrlPath("ru/members.aspx");
        Document membersDoc = memberCrawler.getDoc();

        if (membersDoc != null) {
            logger.log(Level.INFO, "members is loaded");
        }
        // Загрузка страницы "Объявленые аукционы"
        crawler.setUrlPath("ru/auctions.aspx");
//        Map<String, String> data = new HashMap<>();
//        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//        Calendar currentDate = Calendar.getInstance();
//        currentDate.setTime(new Date());
//        currentDate.add(Calendar.DATE, -7);
//        data.put("ctl00$PageContent$txtDateFrom", dateFormat.format(currentDate.getTime()));
//        data.put("ctl00$PageContent$hdnDateFrom", dateFormat.format(currentDate.getTime()));
//        data.put("ctl00$PageContent$txtDateTo", "");
//        data.put("ctl00$PageContent$hdnDateTo", "");
//        data.put("ctl00$PageContent$rblSortDate", "2");
//        data.put("ctl00$PageContent$ddlTypes", "");
//        data.put("ctl00$PageContent$ddlSections", "");
//        data.put("ctl00$PageContent$ddlBrokers", "");
//        data.put("ctl00$PageContent$txtGoods", "");
//        data.put("ctl00$PageContent$btnSearch", "Искать");
//        crawler.setData(data, true);
//        crawler.setMethod(Connection.Method.POST);
        Document auctionDoc = crawler.getDoc();

        if (auctionDoc != null) {
            logger.log(Level.INFO, "auction page is loaded");
        }

        parser = new ETSParser(auctionDoc, dbManager);
        parser.parseMembers(membersDoc);
        parser.PurchasesDo();

        if (null != parser.getData() && !parser.getData().isEmpty()) {
            ETSSaver saver = new ETSSaver(dbManager);
            saver.Do(parser.getData());
            savers.add(saver);
        }
    }
}
