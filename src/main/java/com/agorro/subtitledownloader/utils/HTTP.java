package com.agorro.subtitledownloader.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTP
{
    public static String getCookies(String downloadUrl) throws IOException
    {
        StringBuilder cookies = new StringBuilder();
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        try (Response response = httpClient.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Response unsuccessful " + response.message());
            }

            Headers headers = response.headers();
            Iterator<Pair<String, String>> itHeaders = headers.iterator();
            while (itHeaders.hasNext())
            {
                Pair<String, String> header = itHeaders.next();
                if ("cf-request-id".equals(header.getFirst()))
                {
                    cookies.insert(0, "__cfduid=" + header.getSecond());
                }
                else if ("set-cookie".equals(header.getFirst()) && header.getSecond().endsWith("path=/"))
                {
                    String[] parts = header.getSecond().split(";");
                    cookies.append("; ");
                    cookies.append(parts[0]);
                }
            }
        }
        return cookies.toString();
    }

    public static boolean copyResponse(String downloadUrl, String cookies, File fileCompressed) throws IOException
    {
        OkHttpClient httpClient = new OkHttpClient().newBuilder().followRedirects(true).followSslRedirects(true).build();
        Request request = new Request.Builder().url(downloadUrl).addHeader("cookie", cookies).build();
        try (Response response = httpClient.newCall(request).execute();)
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Response unsuccessful " + response.message());
            }
            int code = response.code();

            boolean isRar = HTTP.isRAR(response);
            if (!isRar && !HTTP.isZIP(response))
            {
                throw new IOException("Content-Type not recognized " + response.header("Content-Type"));
            }

            try (InputStream input = response.body().byteStream();
                 OutputStream output = new FileOutputStream(fileCompressed))
            {
                IO.copy(input, output);
            }

            return isRar;
        }
    }

    public static boolean isRAR(Response response)
    {
        String contentType = response.header("Content-Type");
        return ("application/x-rar-compressed".equals(contentType) || "application/rar".equals(contentType));
    }

    public static boolean isZIP(Response response)
    {
        String contentType = response.header("Content-Type");
        return ("application/zip".equals(contentType));
    }

    @Deprecated
    public static HttpURLConnection getHttpURLConnection(String downloadUrl) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.connect();

        boolean redirect = false;
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK)
        {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER)
            {
                redirect = true;
            }
            else
            {
                throw new IOException("Server returned HTTP " + status + " " + connection.getResponseMessage() + " " + downloadUrl);
            }
        }

        if (redirect)
        {
            String newUrl = connection.getHeaderField("Location"); //get redirect url from "location" header field
            String cookies = connection.getHeaderField("Set-Cookie"); //get the cookie if need, for login
            connection = (HttpURLConnection) new URL(newUrl).openConnection();
            connection.setRequestProperty("Cookie", cookies);
            try
            {
                connection.connect();
            }
            catch (IOException e)
            {
                if (e.getMessage() != null && e.getMessage().contains("Cleartext HTTP traffic to ") && e.getMessage().contains(" not permitted"))
                {
                    newUrl = newUrl.replace("http", "https");
                    connection = (HttpURLConnection) new URL(newUrl).openConnection();
                    connection.setRequestProperty("Cookie", cookies);
                    connection.connect();
                }
                else
                {
                    throw new IOException("Error connecting to " + newUrl, e);
                }
            }
            status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK)
            {
                throw new IOException("Server returned HTTP " + status + " " + connection.getResponseMessage() + " " + newUrl);
            }
        }
        return connection;
    }

    @Deprecated
    public static boolean isRAR(HttpURLConnection connection)
    {
        String contentType = connection.getHeaderField("Content-Type");
        return ("application/x-rar-compressed".equals(contentType));
    }

    @Deprecated
    public static boolean isZIP(HttpURLConnection connection)
    {
        String contentType = connection.getHeaderField("Content-Type");
        return ("application/zip".equals(contentType));
    }
}
