package com.myhoard.app.test.crudengine;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.myhoard.app.Managers.UserManager;
import com.myhoard.app.crudengine.MediaCrudEngine;
import com.myhoard.app.model.IModel;
import com.myhoard.app.model.Media;
import com.myhoard.app.model.Token;
import com.myhoard.app.model.User;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Description
 *
 * @author Tomasz Nosal
 *         Date: 03.04.14
 */
public class MediaCrudTest extends TestCase {

    private String email;
    private String username;
    private String password;
    private Token token;
    private MediaCrudEngine<Media> mediaEngine;
    public static final List<String> URLS = Arrays.asList("http://78.133.154.39:2080/",
            "http://78.133.154.39:1080/");
    public byte [] image;

    public MediaCrudTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        //username = "android" + Calendar.getInstance().getTimeInMillis();
        email = "android" + Calendar.getInstance().getTimeInMillis() + "@op.pl";
        password = "haselko";
        setImages(false);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testCreate() {
        for (String url : URLS) {
            registerAndGetToken(url);
            IModel imodel = mediaEngine.create(new Media(image),token);
            String returnedId = imodel.getId();
            assertNotNull(returnedId);
        }
    }

    public void testGet() {
        Media media=null;
        String returnedId=null;
        for (String url : URLS) {
            registerAndGetToken(url);
            IModel imodel = mediaEngine.create(new Media(image), token);
            returnedId = imodel.getId();
            assertNotNull(returnedId);
            media = mediaEngine.get(returnedId, token);
            assertNotNull(media);
        }
        saveImageToExternalStorage(media.getFile(), returnedId);
    }

    public void testUpdate() {
        Media media=null;
        String returnedId=null;
        for (String url : URLS) {
            registerAndGetToken(url);
            IModel imodel = mediaEngine.create(new Media(image), token);
            returnedId = imodel.getId();

            setImages(true);
            imodel = mediaEngine.update(new Media(image), returnedId, token);
            returnedId = imodel.getId();
            assertNotNull(returnedId);
            media = mediaEngine.get(returnedId, token);
        }
        saveImageToExternalStorage(media.getFile(), returnedId);
    }

    public void testDelete() {
        for (String url : URLS) {
            registerAndGetToken(url);
            IModel imodel = mediaEngine.create(new Media(image), token);
            String returnedId = imodel.getId();

            assertTrue(mediaEngine.remove(returnedId, token));
        }
    }

    private void registerAndGetToken(String url) {
        UserManager uM = UserManager.getInstance();
        //rejestracja
        uM.setIp(url);
        uM.register(new User(username, email, password));
        //pobranie tokena = logowanie
        uM.login(new User(username, email, password));

        token = uM.getToken();
        mediaEngine = new MediaCrudEngine<Media>(url+"media/");
    }

    private void setImages(boolean update) {
        InputStream is = null;
        try {
            if(update)
                is = (InputStream) new URL("http://crackberry.com/sites/crackberry.com/files/styles/large/public/topic_images/2013/ANDROID.png?itok=xhm7jaxS").getContent();
            else
                is = (InputStream) new URL("http://3.bp.blogspot.com/-uxwWt5jDVbs/T8fHQA4LoaI/AAAAAAAAA20/Cug1OyAP588/s1600/browar.jpg").getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte data[] = new byte[1024];
        int count;

        try {
            while ((count = is.read(data)) != -1)
            {
                bos.write(data, 0, count);
            }
            image = bos.toByteArray();
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveImageToExternalStorage(byte[] imageInBytes, String id) {
        //image=scaleCenterCrop(image,200,200);

        Bitmap image = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);
        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        try
        {
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            OutputStream fOut = null;
            File file = new File(fullPath, "photo"+id+".png");

            if(file.exists())
                file.delete();

            file.createNewFile();
            fOut = new FileOutputStream(file);
            // 100 means no compression, the lower you go, the stronger the compression
            image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        }
        catch (Exception e)
        {
            //...
        }
    }
}
