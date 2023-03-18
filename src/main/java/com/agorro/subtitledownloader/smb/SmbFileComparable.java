package com.agorro.subtitledownloader.smb;

import java.util.Arrays;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbFileComparable implements Comparable
{
    private SmbFile file;

    private SmbFileComparable(SmbFile file)
    {
        this.file = file;
    }

    public String getName()
    {
        return file.getName();
    }

    public boolean isDirectory()
    {
        try
        {
            return file.isDirectory();
        }
        catch (SmbException e)
        {
            throw new RuntimeException("Error isDirectory", e);
        }
    }

    public String getParent()
    {
        return file.getParent();
    }

    public String getPath()
    {
        return file.getPath();
    }

    public static SmbFileComparable[] getArray(SmbFile[] files)
    {
        int len = files.length;
        SmbFileComparable[] newFiles = new SmbFileComparable[len];
        for (int i = 0; i < len; i++)
        {
            newFiles[i] = new SmbFileComparable(files[i]);
        }
        Arrays.sort(newFiles);
        return newFiles;
    }

    @Override
    public int compareTo(Object o)
    {
        if (!(o instanceof SmbFileComparable)) return -1;

        SmbFileComparable other = (SmbFileComparable) o;

        if (isDirectory() && !other.isDirectory()) return -1;
        if (!isDirectory() && other.isDirectory()) return 1;
        return this.getName().compareTo(other.getName());
    }
}
