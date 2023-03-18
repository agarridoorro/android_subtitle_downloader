package com.agorro.subtitledownloader.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.agorro.subtitledownloader.R;
import com.agorro.subtitledownloader.html.Subtitle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SubtitleAdapter extends ArrayAdapter<Subtitle>
{
    private Context mContext;
    private List<Subtitle> subtitleList;

    public SubtitleAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<Subtitle> list)
    {
        super(context, 0 , list);
        mContext = context;
        subtitleList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        View listItem = convertView;
        if (listItem == null)
        {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.subtitle_item, parent, false);
        }

        Subtitle currentSubtitle = subtitleList.get(position);
        TextView tvDetails = listItem.findViewById(R.id.tv_details);
        tvDetails.setText(currentSubtitle.getDetails());
        listItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_grey));

        return listItem;
    }
}
