package com.agorro.subtitledownloader.smb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;

public class FileMediaFilter implements SmbFileFilter
{
    private static final Pattern PATTERN = Pattern.compile(".*\\.(avi|mp4|mkv|srt)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean accept(SmbFile smbFile) throws SmbException
    {
        final Matcher matcher = PATTERN.matcher(smbFile.getName());
        return (smbFile.isDirectory() | matcher.matches()) ? true : false;
    }
}