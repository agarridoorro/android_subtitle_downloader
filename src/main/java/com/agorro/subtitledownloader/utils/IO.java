package com.agorro.subtitledownloader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IO
{
    public static void copy(InputStream input, OutputStream output) throws IOException
    {
        byte[] data = new byte[8192];
        int count;
        while ((count = input.read(data)) > 0)
        {
            output.write(data, 0, count);
        }
        output.flush();
    }
}
