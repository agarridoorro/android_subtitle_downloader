package com.agorro.subtitledownloader.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.agorro.subtitledownloader.R;
import com.agorro.subtitledownloader.smb.File;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class FileAdapter extends ArrayAdapter<File>
{
    private Context mContext;
    private List<File> fileList;
    //private Typeface font;

    public FileAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<File> list)
    {
        super(context, 0 , list);
        mContext = context;
        fileList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if (listItem == null)
        {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.file_item, parent, false);
        }

        File currentFile = fileList.get(position);

        ImageView ivIcon = listItem.findViewById(R.id.iv_icon);

        TextView tvPath = listItem.findViewById(R.id.tv_path);
        tvPath.setText(currentFile.getName());
        if (currentFile.isDirectory())
        {
            listItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_grey));
            ivIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_folder));
        }
        else if (currentFile.isSubtitle())
        {
            listItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            ivIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_subtitles));
        }
        else
        {
            listItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            ivIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_baseline_local_movies));
        }

        return listItem;
    }
}
