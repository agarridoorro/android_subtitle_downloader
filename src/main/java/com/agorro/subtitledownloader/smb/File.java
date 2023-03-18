package com.agorro.subtitledownloader.smb;

public class File
{
    private static final String SUBTITLE_EXTENSION = ".srt";
    private static final String SAMBA_PREFIX = "smb://";
    public static final String SAMBA_SEPARATOR = "/";

    private String name;
    private String path;
    private String parentPath;
    private boolean isDirectory;
    private boolean isSubtitle;

    public File(boolean isDirectory, String parentPath, String path, String name)
    {
        this.path = path;
        this.parentPath = parentPath;
        this.isDirectory = isDirectory;
        this.name = name;
        this.isSubtitle = name.toLowerCase().endsWith(SUBTITLE_EXTENSION);
    }

    public static String getParentPath(String basePath, String currentPath)
    {
        if (basePath.equals(currentPath)) return currentPath;
        currentPath = currentPath.substring(0, currentPath.length() - 1); //Le sacamos la ultima barra
        int pos = currentPath.lastIndexOf(SAMBA_SEPARATOR);
        return currentPath.substring(0, pos + 1);
    }

    public static String getBasePath(String ip, String folder)
    {
        final StringBuilder sb = new StringBuilder(SAMBA_PREFIX);
        sb.append(ip);
        if (!folder.startsWith(SAMBA_SEPARATOR)) sb.append(SAMBA_SEPARATOR);
        sb.append(folder);
        if (!folder.endsWith(SAMBA_SEPARATOR)) sb.append(SAMBA_SEPARATOR);
        return sb.toString();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPath()
    {
        return path;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public boolean isSubtitle()
    {
        return isSubtitle;
    }
}