package com.myhoard.app.images;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Sebastian Peryt on 1.05.14.
 */
public class Utils {

    public static void CopyStream(InputStream iStream, OutputStream oStream)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=iStream.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                oStream.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
