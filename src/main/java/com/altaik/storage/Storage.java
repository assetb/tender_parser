/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.altaik.storage;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Smaile
 */
public class Storage {
    private static final Logger logger = Logger.getLogger(Storage.class.getName());
    
    private final String nameMainFolder = "storage";
    private String pathStorage;
    private String folderName;
    private String pathToZip;
    private File directory = null;
    private int countFiles = 0;

    public int GetCountFiles(){
        return countFiles;
    }

    public Storage(String platform){
        String osName = System.getProperty("os.name").toLowerCase();
        String folder = null;
        if(osName.contains("win")){
            folder = "c:/";
        } else if(osName.contains("nix") || osName.contains("nux")){
            folder = "/home/archive/";
        }
        DateFormat formatdate = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String dateForFormat = formatdate.format(date);
        pathStorage = nameMainFolder +"/"+dateForFormat+"/"+platform;
        directory = new File(folder + pathStorage);
        if(!directory.exists())
            directory.mkdirs();
    }
    
    public void CreateFolder(String folder){
        folderName = folder;
        pathStorage = pathStorage + "/" + folderName;
        directory = new File(directory.getPath()+"/"+folderName);
        if(!directory.exists())
            directory.mkdirs();
    }
    
    public String GetPath(){
        return pathStorage;
    }
    public String GetPathToZip(){
        return pathToZip;
    }
    
    public File CreateFile(String name){
        try {
            name = name.replace("/", " ");
            File f = new File(directory.toString(), name);
            if(!f.exists() || !f.isFile()){
                f.createNewFile();
                countFiles ++;
                logger.log(Level.FINE, "File {0} created.", f.getName());
                return f;
            } else {
                logger.log(Level.WARNING, "Storage: CreateFile. File exist or is not file. File name:{0}", name);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error create file \"{0}\" :{1}", new Object[]{name, ex});
        }
        return null;
    }

    public boolean LoadData(File f, byte[] data){
        if (null != f) {
            try (FileOutputStream out = new FileOutputStream(f)) {
                out.write(data);
            } catch (IOException ioex){
                logger.log(Level.WARNING, "Storage: loadData. Error write data for file {0}", f.getName());
                return false;
            }
        } else {
            logger.log(Level.WARNING, "Storage: loadData. File is null.");
            return false;
        }
        return true;
    }
    
    
    public boolean LoadFile(String name, byte[] data){
        File f = CreateFile(name);
        return LoadData(f, data);
    }
    
    public boolean DeleteFolder(){
        return DeleteFolder(directory);
    }
    
    public boolean DeleteFolder(File dir){
        if(dir.isDirectory()){
            for(File file : dir.listFiles()){
                DeleteFolder(file);
            }
        }
        return dir.delete();
    }
    
    public boolean ExtractZip(boolean isRemoveFolder){
        if(!ExtractZip()){
           return false; 
        }
        if(isRemoveFolder){
            return DeleteFolder();
        }
        return true;
    }
    
    public boolean ExtractZip(){
        String pathFile = directory.getPath()+".zip";
        try {
            ZipFile zipFile = new ZipFile(pathFile);
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipFile.addFolder(directory.getPath(), parameters);
            this.pathToZip = pathStorage+".zip";
        } catch (ZipException ex) {
            logger.log(Level.SEVERE, "Error archiving folders in the zip file.");
            return false;
        }
        return true;
    }
}
