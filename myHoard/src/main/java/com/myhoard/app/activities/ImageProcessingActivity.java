package com.myhoard.app.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.myhoard.app.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Image Processing Activity
 *
 * @author Marcin Łaszcz
 *         Date: 24.06.14
 */
public class ImageProcessingActivity extends BaseActivity {

    public static final String IMAGE_URI = "IP";

    private static final int NONE_ID = 0;
    private static final int NONE_ORDER = 0;
    private static final int BLUR_ID = 1;
    private static final int BLUR_ORDER = 1;
    private static final int GRAYSCALE_ID = 2;
    private static final int GRAYSCALE_ORDER = 2;
    private static final int EDGE_DETECTION_ID = 3;
    private static final int EDGE_DETECTION_ORDER = 3;
    private static final int EMBOSS_ID = 4;
    private static final int EMBOSS_ORDER = 4;
    private static final int SHARPEN_ID = 5;
    private static final int SHARPEN_ORDER = 5;
    private static final int NEGATIVE_ID = 6;
    private static final int NEGATIVE_ORDER = 6;

    private Uri imageUri;
    private ImageView imageView;
    private Bitmap originalImage;
    private Bitmap modifiedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_processing);

        imageView = (ImageView) findViewById(R.id.imageView);
        resetToOriginalImage();
        modifiedImage = originalImage.copy(originalImage.getConfig(), true);

    }

    void toGrayScale(Bitmap src, Bitmap dst)
    {
        for (int i = 0; i < src.getWidth(); ++i) {
            for (int j = 0; j < src.getHeight(); ++j) {
                int value = (int) (Color.red(src.getPixel(i,j)) * 0.2989f + Color.blue(src.getPixel(i,j)) * 0.1140  + Color.green(src.getPixel(i,j)) * 0.5870);
                dst.setPixel(i,j, Color.rgb(value, value, value));
            }
        }
    }

    void toNegative(Bitmap src, Bitmap dst)
    {
        for (int i = 0; i < src.getWidth(); ++i) {
            for (int j = 0; j < src.getHeight(); ++j) {
                int color = src.getPixel(i,j);
                dst.setPixel(i,j, Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color) ));
            }
        }
    }

    void changeBrightness(Bitmap src, Bitmap dst, int value)
    {
        for (int i = 0; i < src.getWidth(); ++i) {
            for (int j = 0; j < src.getHeight(); ++j) {
                int color = src.getPixel(i,j);
                dst.setPixel(i,j, Color.rgb(Color.red(color) + value, Color.green(color) + value, Color.blue(color) + value ));
            }
        }
    }

    void blur(Bitmap src, Bitmap dst, int size)
    {
        applyConvolutionArray(src, dst, createLowpassConvolutionArray(size), true, true);
        applyConvolutionArray(dst, src, createLowpassConvolutionArray(size), false, true);
        swapBitmaps();
    }

    void swapBitmaps()
    {
        Bitmap tmp;
        tmp = originalImage;
        originalImage = modifiedImage;
        modifiedImage = tmp;
    }

    void emboss(Bitmap src, Bitmap dst)
    {
        final int bias = 100;

        float[][] c = new float[][]{
                { -2, -2, 0},
                { -2, 6, 0},
                { 0, 0, 0}
        };

        applyConvolutionMatrix(src, dst, c, false);
        changeBrightness(dst, src, bias);
        swapBitmaps();
    }

    void edgeDetection(Bitmap src, Bitmap dst)
    {
        float[][] c = new float[][]{
                { -1, -1, -1},
                { 0, 0, 0},
                { 1, 1, 1}
        };

        applyConvolutionMatrix(src, dst, c, false);
        toNegative(dst, src);
        swapBitmaps();
    }

    void sharpen(Bitmap src, Bitmap dst)
    {
        float[][] c = new float[][]{
                { 1, 1, 1},
                { 1, -7, 1},
                { 1, 1, 1}
        };

        applyConvolutionMatrix(src, dst, c, true);
    }


    float[] createLowpassConvolutionArray(int size)
    {
        float[] c = new float[size];
        for (int i = 1; i < size; ++i)
            c[i] = 1;

        return c;
    }

    void applyConvolutionMatrix(Bitmap src, Bitmap dst, float[][] c, boolean divide)
    {
        float r,b,g;

        for (int x = 0; x < src.getWidth(); ++x) {
            for (int y = 0; y < src.getHeight(); ++y) {

                float weightSum = 0;
                r = g = b = 0;

                for (int i = 0; i < c.length; ++i) {
                    int offsetX = i - c.length / 2;
                    for (int j = 0; j < c[i].length; ++j) {
                        int offsetY = j - c[i].length / 2;
                        weightSum += c[i][j];

                        int color = clampToEdge(src, x + offsetX, y + offsetY);
                        r += Color.red(color) * c[i][j];
                        g += Color.green(color) * c[i][j];
                        b += Color.blue(color) * c[i][j];
                    }
                }

                if (divide) {
                    r /= weightSum;
                    g /= weightSum;
                    b /= weightSum;
                }

                r = r < 0 ? 0 : r > 255 ? 255 : r;
                g = g < 0 ? 0 : g > 255 ? 255 : g;
                b = b < 0 ? 0 : b > 255 ? 255 : b;

                dst.setPixel(x,y, Color.rgb((int) r, (int) g, (int)b));
            }
        }
    }

    void applyConvolutionArray(Bitmap src, Bitmap dst, float[] c, boolean horizontal, boolean divide)
    {
        float r,b,g;

        for (int x = 0; x < src.getWidth(); ++x) {
            for (int y = 0; y < src.getHeight(); ++y) {

                float weightSum = 0;
                r = g = b = 0;

                for (int i = 0; i < c.length; ++i) {
                    int offset = i - c.length / 2;

                    weightSum += c[i];

                    int color;
                    if (horizontal) {
                        color = clampToEdge(src, x + offset, y);
                    } else {
                        color = clampToEdge(src, x, y + offset);
                    }
                    r += Color.red(color) * c[i];
                    g += Color.green(color) * c[i];
                    b += Color.blue(color) * c[i];
                }

                if (divide) {
                    r /= weightSum;
                    g /= weightSum;
                    b /= weightSum;
                }

                r = r < 0 ? 0 : r > 255 ? 255 : r;
                g = g < 0 ? 0 : g > 255 ? 255 : g;
                b = b < 0 ? 0 : b > 255 ? 255 : b;

                dst.setPixel(x,y, Color.rgb((int) r, (int) g, (int)b));
            }
        }
    }

    int clampToEdge(Bitmap bitmap, int x, int y)
    {
        return bitmap.getPixel(x > bitmap.getWidth() - 1 ? bitmap.getWidth() - 1 : x < 0 ? 0 : x,
                y > bitmap.getHeight() - 1 ? bitmap.getHeight() - 1 : y < 0 ? 0 : y);
    }

    public Bitmap getBitmapFromUri(Uri uri) throws IOException {
        InputStream input = this.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        //double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;
        double ratio = 1;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig= Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }

    public Bitmap convertToMutable(Bitmap imgIn) throws IOException {

        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temp.tmp");

        //Open an RandomAccessFile
        //Make sure you have added uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
        //into AndroidManifest.xml file
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

        // get the width and height of the source bitmap.
        int width = imgIn.getWidth();
        int height = imgIn.getHeight();
        Bitmap.Config type = imgIn.getConfig();

        //Copy the byte to the file
        //Assume source bitmap loaded using options.inPreferredConfig = Config.ARGB_8888;
        FileChannel channel = randomAccessFile.getChannel();
        MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes()*height);
        imgIn.copyPixelsToBuffer(map);
        //recycle the source bitmap, this will be no longer used.
        imgIn.recycle();
        System.gc();// try to force the bytes from the imgIn to be released

        //Create a new bitmap to load the bitmap again. Probably the memory will be available.
        imgIn = Bitmap.createBitmap(width, height, type);
        map.position(0);
        //load it back from temporary
        imgIn.copyPixelsFromBuffer(map);
        //close the temporary file and channel , then delete that also
        channel.close();
        randomAccessFile.close();

        // delete the temp file
        file.delete();

        return imgIn;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, NONE_ID, NONE_ORDER, R.string.ip_filter_none);
        menu.add(0, BLUR_ID, BLUR_ORDER, R.string.ip_filter_blur);
        menu.add(0, GRAYSCALE_ID, GRAYSCALE_ORDER, R.string.ip_filter_grayscale);
        menu.add(0, EDGE_DETECTION_ID, EDGE_DETECTION_ORDER, R.string.ip_filter_edge_detection);
        menu.add(0, EMBOSS_ID, EMBOSS_ORDER, R.string.ip_filter_emboss);
        menu.add(0, SHARPEN_ID, SHARPEN_ORDER, R.string.ip_filter_sharpen);
        menu.add(0, NEGATIVE_ID, NEGATIVE_ORDER, R.string.ip_filter_negative);

        return super.onCreateOptionsMenu(menu);
    }

    void resetToOriginalImage()
    {
        try {
            imageUri = getIntent().getExtras().getParcelable(IMAGE_URI);
            originalImage = convertToMutable(getBitmapFromUri(imageUri));
            imageView.setImageBitmap(originalImage);
        }
        catch (Exception e) {
            finish();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case NONE_ID:
                resetToOriginalImage();
                return true;

            case BLUR_ID:
                final int blurKernelSize = 10;
                blur(originalImage, modifiedImage, blurKernelSize);
                resetToOriginalImage();
                imageView.setImageBitmap(modifiedImage);
                return true;

            case GRAYSCALE_ID:
                toGrayScale(originalImage, modifiedImage);
                imageView.setImageBitmap(modifiedImage);
                return true;

            case EDGE_DETECTION_ID:
                edgeDetection(originalImage, modifiedImage);
                resetToOriginalImage();
                imageView.setImageBitmap(modifiedImage);
                return true;

            case EMBOSS_ID:
                emboss(originalImage, modifiedImage);
                imageView.setImageBitmap(modifiedImage);
                return true;

            case SHARPEN_ID:
                sharpen(originalImage, modifiedImage);
                imageView.setImageBitmap(modifiedImage);
                return true;

            case NEGATIVE_ID:
                toNegative(originalImage, modifiedImage);
                imageView.setImageBitmap(modifiedImage);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
