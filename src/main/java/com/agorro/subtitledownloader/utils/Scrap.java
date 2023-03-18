package com.agorro.subtitledownloader.utils;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public class Scrap
{
    private static final String TAG = Scrap.class.getName();

    /**
     * 		200 OK			300 Multiple Choices
     * 		301 Moved Permanently	305 Use Proxy
     * 		400 Bad Request		403 Forbidden
     * 		404 Not Found		500 Internal Server Error
     * 		502 Bad Gateway		503 Service Unavailable
     */
    public static int getStatusConnectionCode(String url)
    {
        Connection.Response response = null;
        try
        {
            response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(15000).ignoreHttpErrors(true).execute();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error getting status code: " + url, e);
        }
        return (null == response) ? 0 : response.statusCode();
    }

    public static Document getHtmlDocument(String url)
    {
        Document doc = null;
        try
        {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(15000).get();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error getting html: " + url, e);
        }
        return doc;
    }
}