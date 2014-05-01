package com.myhoard.app.images;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Sebastian Peryt on 1.05.14.
 */
public class FileCache {

    private static final String TAG = FileCache.class.getSimpleName();
    private static final String DIR_NAME = "MyHoard_cache";
    private File cacheDir;

    public FileCache(Context context) {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),DIR_NAME);
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url){
        File file = null;
        try{
            String filename = URLEncoder.encode(url,"UTF-8");
            file = new File(cacheDir, filename);
            file.createNewFile();
//            return file;
        } catch(UnsupportedEncodingException uee) {
                uee.printStackTrace();;
//            return null;
        } catch (IOException e) {
            //TODO dodaj gora
            e.printStackTrace();
        }
        return file;
    }

    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File file:files)
            file.delete();
    }
}
