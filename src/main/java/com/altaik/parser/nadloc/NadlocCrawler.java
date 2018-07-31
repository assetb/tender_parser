/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.parser.nadloc;

import com.altaik.crawler.AuthCrawler;
import com.altaik.db.DatabaseManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Aset
 */
public class NadlocCrawler extends AuthCrawler {

    private final String FILE_COOKIES = "\\cookies.sqlite";
    private final String FILE_RECOVERY = "\\sessionstore-backups\\recovery.js";
    private final String REG_FIND_COOKIES = "\"cookies\":(\\[\\{([^\\]]*)\\}\\])";
    private final String REG_PARSE_COOKIES = "\"host\":\"[\\w]*.nadloc\\.kz\",\"value\":\"([^\"]*)\"[^}]*\"name\":\"([^\"]*)\"";

    public String negsUrl;
    public String lotUrl = "/show/";

    public NadlocCrawler() {
        this.userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
        this.isCertificated = true;
        this.isToSleep = true;
        this.baseUrl = "http://reestr.nadloc.kz";
        this.negsUrl = baseUrl + "/ru/protocol/announce";
    }

    public void Init() {
        setTimeout(300 * 1000);
        setBodySize(20 * (int) Math.pow(10, 6));
        setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        setHeader("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
        setHeader("Accept-Encoding", "gzip, deflate");
        setHeader("Connection", "keep-alive");
        setHeader("Cache-Control", "max-age=0");
    }

    public String readDoc(File f) {
        String text = "";
        int read, N = 1024 * 1024;
        char[] buffer = new char[N];

        try {
            try (FileReader fr = new FileReader(f); BufferedReader br = new BufferedReader(fr)) {
                while (true) {
                    read = br.read(buffer, 0, N);
                    text += new String(buffer, 0, read);

                    if (read < N) {
                        break;
                    }
                }
            }

            try ( // clear file
                    FileOutputStream fo = new FileOutputStream(f)) {
                fo.write(("").getBytes());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Open cookie file. \n {0}", ex);
            return null;
        }

        return text;
    }

    public boolean getCookiesMoz(String pathToCookiesMoz) {
        File fileRecoveryMoz = new File(pathToCookiesMoz + FILE_RECOVERY);

        String cookiesStr = readDoc(fileRecoveryMoz);
        if (fileRecoveryMoz.delete()) {
            logger.log(Level.INFO, "File \"recovery\" deleted");
        } else {
            logger.log(Level.WARNING, "File \"recovery\" not managed delete");
        }
        fileRecoveryMoz = null;
        
        if (null == cookiesStr) {
            logger.log(Level.SEVERE, "Error. Not data for file recovery {0}{1}", new Object[]{pathToCookiesMoz, FILE_RECOVERY});
            return false;
        }
        Pattern pattern = Pattern.compile(REG_FIND_COOKIES);
        Matcher matcherCookies = pattern.matcher(cookiesStr);
//        Map<String, String> cookies = new HashMap<>();
        while (matcherCookies.find()) {
            pattern = Pattern.compile(REG_PARSE_COOKIES, Pattern.DOTALL);
            Matcher matcherValues = pattern.matcher(matcherCookies.group(1));
            while (matcherValues.find()) {
                String val = matcherValues.group(2);
                String name = matcherValues.group(1);
                setCookie(val, name);
            }
        }
        if (getCookies() == null || getCookies().isEmpty()) {
            logger.log(Level.SEVERE, "Error regex cookies.");
            return false;
        }
        try {
            Properties properties = new Properties();
            properties.put("url", "jdbc:sqlite:" + pathToCookiesMoz + FILE_COOKIES);
            properties.put("driver", "org.sqlite.JDBC");
            try (DatabaseManager databaseManager = new DatabaseManager(properties)) {
                ResultSet res = databaseManager.Execute("SELECT name, value FROM moz_cookies WHERE baseDomain = 'nadloc.kz';");
                while (res.next()) {
                    setCookie(res.getString("name"), res.getString("value"));
                }
                databaseManager.Update("DELETE FROM moz_cookies WHERE baseDomain = 'nadloc.kz';");
            }
            File fcookies = new File(pathToCookiesMoz + FILE_COOKIES);
            if(fcookies.delete()){
                logger.log(Level.INFO, "Delete file {0}", pathToCookiesMoz + FILE_COOKIES);
                fcookies = null;
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error get cookies for sqliteMoz. \n {0}", ex);
        }
        logger.log(Level.INFO, "Cookies firefox loaded.");
        return true;
    }

    public void setPath(String path) {
        setUrl(baseUrl + path);
    }

    @Override
    protected void Certificate() {
    }

    @Override
    public void close() {
    }
}
