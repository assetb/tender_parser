package com.altaik.parser.ktzh;


import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class MSDocExtrator {

    private String path;

    public MSDocExtrator(){

    }

    public void downloadFile(String url) throws IOException {
        URL website = new URL("http://www.ktzh-gp.kz"+url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        String fileName = url.replaceAll("\\D+", "") + ".doc";
        FileOutputStream fos = new FileOutputStream(this.path+fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    public void setFilePath(String path){
        this.path = path;
    }

    public String getFIlePath(){
        return path;
    }



    public String extractText(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        HWPFDocument doc = new HWPFDocument(fis);
        WordExtractor extractor = new WordExtractor(doc);
        System.out.println(extractor.getText());
        return extractor.getText();
    }



}
