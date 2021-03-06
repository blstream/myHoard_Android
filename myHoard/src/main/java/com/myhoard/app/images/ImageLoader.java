package com.myhoard.app.images;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.myhoard.app.R;

public class ImageLoader{

    private static final int COLLECTION_NO_PHOTO = 1;
    private static final int ELEMENT_NO_PHOTO = 2;
    private static final int NUMBER_OF_THREADS = 5;

    MemoryCache memoryCache = new MemoryCache();
    ImageCacheDatabase imageCacheDatabase;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    private static Context mContext;
    private int mStubId;
    public ImageLoader(Context mContext, int no_photo_resource) {
        ImageLoader.mContext = mContext;
        imageCacheDatabase = new ImageCacheDatabase(mContext);
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        if(no_photo_resource == ELEMENT_NO_PHOTO){
            mStubId = R.drawable.element_empty;
        } else if(no_photo_resource == COLLECTION_NO_PHOTO){
            mStubId = R.drawable.collection_empty;
        }
    }

    public void DisplayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(mStubId);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    public Bitmap getBitmap(String url){
        Bitmap bmp = imageCacheDatabase.getBitmap(url);
        if(bmp==null){
            bmp = decodeSampledBitmapFromResource(url, 100, 100);
            imageCacheDatabase.addBitmap(url,bmp);
        }
        return bmp;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
           BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(Uri.parse(path)),null,options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(Uri.parse(path)),null,options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }
        @Override
        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;
                //Bitmap bmp = decodeSampledBitmapFromResource(photoToLoad.url, 200, 200);
                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if (imageViewReused(photoToLoad))
                    return;
                BitmapDisplay bd = new BitmapDisplay(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        return tag == null || !tag.equals(photoToLoad.url);
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplay implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplay(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(mStubId);
        }
    }

    public void clearCache(){
        memoryCache.clear();
        imageCacheDatabase.clearDatabase();
        Log.d("CLEAR MEMORY cache", "CLEAR MEMORY CACHE");
    }
}