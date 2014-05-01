package com.myhoard.app.images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.myhoard.app.R;
import com.myhoard.app.model.StaticMap;

/**
 * Created by Sebastian Peryt on 1.05.14.
 */
public class MapImageLoader {
	
	private static final int TIMEOUT = 30000; //30s

    private MemoryCache memoryCache=new MemoryCache();
    private FileCache fileCache;
    private StaticMap mapView = new StaticMap();
    private ExecutorService executorService;
    private Context context;
    Handler handler = new Handler();

    private final int emptyResource = R.drawable.ic_launcher;

    public MapImageLoader(Context context) {
        this.context = context;
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(3);
    }

    public void DisplayImage(String url, ImageView view) {
    	mapView.put(view, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap!=null) {
            view.setImageBitmap(bitmap);
        } else {
            queueCover(url, view);
            view.setImageResource(emptyResource);
        }
    }

    private void queueCover(String url, ImageView view) {
        CoverToLoad ctLoad= new CoverToLoad(url,view);
        executorService.submit(new CoversLoader(ctLoad));
    }

    private Bitmap getBitmap(String url) {
        File file=fileCache.getFile(url);

        //from SD cache
        Bitmap bmp = decodeFile(file);
        if(bmp!=null) {
            return bmp;
        }

        //from web
        Bitmap bitmap = null;
        OutputStream oStream = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            InputStream iStream=conn.getInputStream();
            oStream = new FileOutputStream(file);
            Utils.CopyStream(iStream, oStream);
            bitmap = decodeFile(file);
        } catch (Throwable ex){
            ex.printStackTrace();
            if(ex instanceof OutOfMemoryError)
                memoryCache.clear();
        } finally {
            try {
                if (oStream != null) {
                    oStream.close();
                }
            } catch(IOException e) {
                Toast.makeText(context,"IOException: " + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    //decodes image
    private Bitmap decodeFile(File file){
        FileInputStream stream1 = null;
        try {
            stream1=new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(stream1, null, null);
            return bitmap;

        } catch (FileNotFoundException e) {
            Toast.makeText(context,"FileNotFound: " + e,Toast.LENGTH_SHORT).show();
        } finally {
            try{
                if(stream1 != null) {
                    stream1.close();
                }
            } catch(IOException e) {
                Toast.makeText(context,"IOException: " + e, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return null;
    }

    private class CoverToLoad
    {
        public String url;
        public ImageView imageView;
        public CoverToLoad(String url, ImageView view){
            this.url=url;
            this.imageView = view;
        }
    }

    class CoversLoader implements Runnable {
        CoverToLoad coverToLoad;

        public CoversLoader(CoverToLoad coverToLoad){
            this.coverToLoad = coverToLoad;
        }

        @Override
        public void run() {
            if(imageViewReused(coverToLoad))
                return;
            Bitmap bmp=getBitmap(coverToLoad.url);
            memoryCache.put(coverToLoad.url, bmp);
            if(imageViewReused(coverToLoad)) {
                return;
            }
            BitmapDisplayer bitmapDisplayer=new BitmapDisplayer(bmp, coverToLoad);
            handler.post(bitmapDisplayer);
        }
    }

    boolean imageViewReused(CoverToLoad coverToLoad){
    	String tag = mapView.get(coverToLoad.imageView);
        if(tag==null || !tag.equals(coverToLoad.url)) {
            return true;
        }
        return false;
    }

    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        CoverToLoad coverToLoad;
        public BitmapDisplayer(Bitmap bitmap, CoverToLoad cover){
            this.bitmap = bitmap;
            this.coverToLoad = cover;
        }
        public void run()
        {
            if(imageViewReused(coverToLoad))
                return;
            if(bitmap!=null)
                coverToLoad.imageView.setImageBitmap(bitmap);
            else
                coverToLoad.imageView.setImageResource(emptyResource);
        }
    }

    // no idea when best to use
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
