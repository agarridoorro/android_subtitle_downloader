package com.agorro.subtitledownloader.html;

public class Subtitle
{
    public static final String MAIN_PAGE = "https://www.subdivx.com/";
    public static final String SEARCH_PARAMETERS = "index.php?accion=5&q=";

    private String url;
    private String details;

    public Subtitle(String url, String details)
    {
        this.url = url;
        this.details = details;
    }

    public String getUrl()
    {
        return url;
    }

    public String getDetails()
    {
        return details;
    }
}
