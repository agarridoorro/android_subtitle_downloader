package com.agorro.subtitledownloader.loader;

import android.content.Context;
import android.util.Log;

import com.agorro.subtitledownloader.html.Subtitle;
import com.agorro.subtitledownloader.utils.Scrap;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class SearchSubtitleLoader extends AsyncTaskLoader<List<Subtitle>>
{
    private static final String TAG = SearchSubtitleLoader.class.getName();

    private String url;

    public SearchSubtitleLoader(Context context, String url)
    {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading()
    {
        forceLoad(); // Starts the loadInBackground method
    }

    @Nullable
    @Override
    public List<Subtitle> loadInBackground()
    {
        try
        {
            int status = Scrap.getStatusConnectionCode(url);
            if (status != 200)
            {
                Log.e(TAG, "Scrapping error, bad status code " + status + " " + url);
                return null;
            }

            Document document = Scrap.getHtmlDocument(url);
            if (null == document)
            {
                Log.e(TAG, "Scrapping error, document not found: " + url);
                return null;
            }
            Elements divs = document.select("div#buscador_detalle_sub");
            if (null == divs || divs.size() <= 0)
            {
                return new ArrayList<>();
            }

            Elements links = document.select("a.titulo_menu_izq");
            if (null == links || links.size() <= 0)
            {
                Log.e(TAG, "Scrapping error, links not found: " + url);
                return null;
            }

            int len = divs.size();
            if (len != links.size())
            {
                Log.e(TAG, "Scrapping error, divs and links does not match: " + url);
                return null;
            }

            List<Subtitle> items = new ArrayList<>();
            for (int i = 0; i < len; i++)
            {
                String href = links.get(i).attr("href");
                String details = divs.get(i).text();
                items.add(new Subtitle(href, details));
            }
            return items;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Scrapping error, searching subtitles: " + url, e);
            return null;
        }
    }
}
