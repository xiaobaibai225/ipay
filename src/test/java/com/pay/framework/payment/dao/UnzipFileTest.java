package com.pay.framework.payment.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

public class UnzipFileTest {
	
    public static final int BUFFER = 1024; 
    public static final String EXT = ".gz";
	
	 /**
     * 文件解压缩
     * 
     * @param file
     * @param delete
     *            是否删除原始文件
     * @throws Exception
     */ 
    public static void decompress(File file, boolean delete) throws Exception { 
        FileInputStream fis = new FileInputStream(file); 
        FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT, 
                "")); 
        decompress(fis, fos); 
        fis.close(); 
        fos.flush(); 
        fos.close(); 

        if (delete) { 
            file.delete(); 
        } 
    }  

	public static void main(String[] args) throws Exception{
		String str="d:\\tt.gz";   
        
		decompress(new File(str),false);   

	}
	
	/**
     * 数据解压缩
     * 
     * @param is
     * @param os
     * @throws Exception
     */ 
    public static void decompress(InputStream is, OutputStream os) 
            throws Exception { 

        GZIPInputStream gis = new GZIPInputStream(is); 

        int count; 
        byte data[] = new byte[BUFFER]; 
        while ((count = gis.read(data, 0, BUFFER)) != -1) { 
            os.write(data, 0, count); 
        } 

        gis.close(); 
    }  

}
