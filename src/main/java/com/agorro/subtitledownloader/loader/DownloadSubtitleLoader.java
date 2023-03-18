package com.agorro.subtitledownloader.loader;

import android.content.Context;
import android.util.Log;

import com.agorro.subtitledownloader.html.Subtitle;
import com.agorro.subtitledownloader.smb.AuthInfo;
import com.agorro.subtitledownloader.utils.Compress;
import com.agorro.subtitledownloader.utils.HTTP;
import com.agorro.subtitledownloader.utils.IO;
import com.agorro.subtitledownloader.utils.Samba;
import com.agorro.subtitledownloader.utils.Scrap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import kotlin.Pair;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadSubtitleLoader extends AsyncTaskLoader<Boolean>
{
    private static final String TAG = SearchSubtitleLoader.class.getName();

    private String url;
    private String folderName;
    private String fileName;
    private AuthInfo auth;

    public DownloadSubtitleLoader(Context context, String url, String folderName, String fileName, AuthInfo auth)
    {
        super(context);
        this.url = url;
        this.folderName = folderName;
        this.fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".srt";
        this.auth = auth;

    }

    @Override
    protected void onStartLoading()
    {
        forceLoad(); // Starts the loadInBackground method
    }

    @Nullable
    @Override
    public Boolean loadInBackground()
    {
        if (Scrap.getStatusConnectionCode(url) != 200)
        {
            return  false;
        }

        Document document = Scrap.getHtmlDocument(url);
        if (null == document)
        {
            return false;
        }

        Elements links = document.select("a.link1").not("[target]");
        if (null == links || links.size() <= 0)
        {
            Log.e(TAG, "Scrapping error, div download not found: " + url);
            return false;
        }
        if (links.size() > 1)
        {
            Log.e(TAG, "Scrapping error, found more than one download div: " + url);
            return false;
        }

        String urlToDownload = links.get(0).attr("href");
        if (!urlToDownload.startsWith("https:") && !urlToDownload.startsWith("http:"))
        {
            urlToDownload = Subtitle.MAIN_PAGE + urlToDownload;
        }
        return downloadFile(urlToDownload);
    }

    private boolean downloadFile(String downloadUrl)
    {
        try
        {
            //Se preparan los ficheros
            File fileCompressed = prepareFile("subtitle.comp");
            File fileDecompressed = prepareFile("subtitle.srt");

            //Descarga del fichero comprimido
            String cookies = HTTP.getCookies(downloadUrl);
            boolean isRar = HTTP.copyResponse(downloadUrl, cookies, fileCompressed);

            //Se descomprimen
            if (isRar)
            {
                Compress.decompressRAR(fileCompressed, fileDecompressed);
            }
            else
            {
                Compress.decompressZIP(fileCompressed, fileDecompressed);
            }

            //Se copian a la ruta samba
            SmbFile remoteFile = Samba.getFile(folderName + fileName, auth);
            if (remoteFile.exists()) remoteFile.delete();
            try (SmbFileOutputStream out = new SmbFileOutputStream(remoteFile);
                 FileInputStream fis = new FileInputStream(fileDecompressed.getAbsolutePath()))
            {
                IO.copy(fis, out);
            }
            return true;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Download error: " + url, e);
            return false;
        }
    }

    //https://developer.android.com/training/data-storage
    //https://mkyong.com/java/java-httpurlconnection-follow-redirect-example/
    private boolean downloadFile_bak(String downloadUrl)
    {
        HttpURLConnection connection = null;
        try
        {
            //Se preparan los ficheros
            File fileCompressed = prepareFile("subtitle.comp");
            File fileDecompressed = prepareFile("subtitle.srt");
            //Descarga del fichero comprimido
            connection  = HTTP.getHttpURLConnection(downloadUrl);

            try (InputStream input = connection.getInputStream();
                 OutputStream output = new FileOutputStream(fileCompressed))
            {
                IO.copy(input, output);
            }
            //Se descomprimen
            if (HTTP.isRAR(connection))
            {
                Compress.decompressRAR(fileCompressed, fileDecompressed);
            }
            else if (HTTP.isZIP(connection))
            {
                Compress.decompressZIP(fileCompressed, fileDecompressed);
            }
            else
            {
                Log.e(TAG, "Content-Type not recognized");
                return false;
            }
            //Se copian a la ruta samba
            SmbFile remoteFile = Samba.getFile(folderName + fileName, auth);
            if (remoteFile.exists()) remoteFile.delete();
            try (SmbFileOutputStream out = new SmbFileOutputStream(remoteFile);
                 FileInputStream fis = new FileInputStream(fileDecompressed.getAbsolutePath()))
            {
                IO.copy(fis, out);
            }
            return true;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Download error: " + url, e);
            return false;
        }
        finally
        {
            if (null != connection)
            {
                connection.disconnect();
            }
        }
    }

    private File prepareFile(String name)
    {
        String fileName = getContext().getExternalFilesDir(null) + "/" + name;
        File file = new File(fileName);
        if (file.exists())
        {
            file.delete();
        }
        return file;
    }
}
