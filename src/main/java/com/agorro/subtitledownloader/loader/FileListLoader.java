package com.agorro.subtitledownloader.loader;

import android.content.Context;
import android.util.Log;

import com.agorro.subtitledownloader.smb.AuthInfo;
import com.agorro.subtitledownloader.smb.File;
import com.agorro.subtitledownloader.smb.FileMediaFilter;
import com.agorro.subtitledownloader.smb.SmbFileComparable;
import com.agorro.subtitledownloader.utils.Samba;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import jcifs.smb.SmbFile;

public class FileListLoader extends AsyncTaskLoader<List<File>>
{
    private static final String TAG = FileListLoader.class.getName();

    private AuthInfo authInfo;
    private String basePath;
    private String path;

    public FileListLoader(Context context, AuthInfo authInfo, String basePath, String path)
    {
        super(context);
        this.authInfo = authInfo;
        this.basePath = basePath;
        this.path = path;
    }

    @Override
    protected void onStartLoading()
    {
        forceLoad(); // Starts the loadInBackground method
    }

    @Nullable
    @Override
    public List<File> loadInBackground()
    {
        try
        {
            List<File> items = new ArrayList<>();

            SmbFile current = Samba.getFile(path, authInfo);

            if (!basePath.equals(path))
            {
                SmbFile parent = Samba.getFile(current.getParent(), authInfo);
                items.add(new File(parent.isDirectory(), parent.getParent(), parent.getPath(), ".."));
            }

            SmbFile[] files = current.listFiles(new FileMediaFilter());
            SmbFileComparable[] filesComparables = SmbFileComparable.getArray(files);
            for (SmbFileComparable file : filesComparables)
            {
                String fileName = file.getName();
                if (file.isDirectory() && fileName.endsWith("/"))
                {
                    fileName = fileName.substring(0, fileName.length() - 1);
                }
                items.add(new File(file.isDirectory(), file.getParent(), file.getPath(), fileName));
            }

            return items;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error getting list files", e);
            return null;
        }
    }
}
