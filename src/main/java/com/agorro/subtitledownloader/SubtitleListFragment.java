package com.agorro.subtitledownloader;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agorro.subtitledownloader.adapter.SubtitleAdapter;
import com.agorro.subtitledownloader.html.Subtitle;
import com.agorro.subtitledownloader.loader.DownloadSubtitleLoader;
import com.agorro.subtitledownloader.loader.SearchSubtitleLoader;
import com.agorro.subtitledownloader.smb.AuthInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubtitleListFragment extends Fragment implements LoaderManager.LoaderCallbacks
{
    public static final String EXTRA_PARENT_NAME = "com.agorro.subtitledownloader.extra.EXTRA_PARENT_NAME";
    public static final String EXTRA_FILE_NAME = "com.agorro.subtitledownloader.extra.EXTRA_FILE_NAME";
    public static final String EXTRA_FOLDER_NAME = "com.agorro.subtitledownloader.extra.EXTRA_FOLDER_NAME";
    public static final String EXTRA_AUTH = "com.agorro.subtitledownloader.extra.EXTRA_AUTH";

    private static final Pattern PATTERN = Pattern.compile(".*s\\d{1,2}e\\d{1,2}", Pattern.CASE_INSENSITIVE);

    private boolean fetching = false;

    private String parentPath;
    private String folderName;
    private String fileName;
    private AuthInfo auth;

    private ProgressBar progressBar;
    private FloatingActionButton fabSearch;
    private EditText etSearch;
    private ListView lvSubtitles;
    private TextView tvSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_subtitle_list, container, false);

        progressBar = getActivity().findViewById(R.id.pbSearch);
        tvSearch = view.findViewById(R.id.tv_search_text);
        etSearch = view.findViewById(R.id.et_search_text);
        fabSearch = view.findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!fetching) search();
            }
        });
        lvSubtitles = view.findViewById(R.id.lv_subtitles);
        lvSubtitles.setAdapter(new SubtitleAdapter(getActivity(), new ArrayList<Subtitle>()));
        lvSubtitles.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!fetching)
                {
                    Subtitle subtitle = (Subtitle) parent.getItemAtPosition(position);
                    download(subtitle);
                }
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                Bundle bundle = new Bundle();
                bundle.putInt(FileListFragment.EXTRA_INIT, 0);
                bundle.putString(FileListFragment.EXTRA_PARENT_PATH, parentPath);
                bundle.putString(FileListFragment.EXTRA_CURRENT_PATH, folderName);
                NavHostFragment.findNavController(SubtitleListFragment.this)
                        .navigate(R.id.action_SubtitleListFragment_to_FileListFragment, bundle);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        parentPath = getArguments().getString(EXTRA_PARENT_NAME);
        folderName = getArguments().getString(EXTRA_FOLDER_NAME);
        fileName = getArguments().getString(EXTRA_FILE_NAME);
        auth = (AuthInfo) getArguments().getSerializable(EXTRA_AUTH);

        tvSearch.setText(fileName);

        Matcher matcher = PATTERN.matcher(fileName);
        if (matcher.find())
        {
            String searchName = matcher.group(0);
            etSearch.setText(searchName);
        }
        else
        {
            etSearch.setText(fileName);
        }
        if (LoaderManager.getInstance(this).getLoader(0) != null)
        {
            LoaderManager.getInstance(this).initLoader(0,null,this);
        }
        if (LoaderManager.getInstance(this).getLoader(1) != null)
        {
            LoaderManager.getInstance(this).initLoader(1,null,this);
        }
    }

    private void search()
    {
        fetching = true;
        SubtitleAdapter adapter = (SubtitleAdapter) lvSubtitles.getAdapter();
        adapter.clear();
        adapter.notifyDataSetChanged();

        String text = etSearch.getText().toString();

        Bundle args = new Bundle();
        args.putString("url", Subtitle.MAIN_PAGE + Subtitle.SEARCH_PARAMETERS);
        args.putString("text", text);
        LoaderManager.getInstance(SubtitleListFragment.this).restartLoader(0, args, SubtitleListFragment.this);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void download(Subtitle subtitle)
    {
        fetching = true;
        Bundle args = new Bundle();
        args.putString("url", subtitle.getUrl());
        args.putString("folderName", folderName);
        args.putString("fileName", fileName);
        args.putSerializable("auth", auth);
        LoaderManager.getInstance(SubtitleListFragment.this).restartLoader(1, args, SubtitleListFragment.this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args)
    {
        String url = args.getString("url");
        switch (id)
        {
            case 0:
                String text = args.getString("text");
                return new SearchSubtitleLoader(getActivity(), url + text);
            case 1:
                String folderName = args.getString("folderName");
                String fileName = args.getString("fileName");
                AuthInfo auth = (AuthInfo) args.getSerializable("auth");
                return new DownloadSubtitleLoader(getActivity(), url, folderName, fileName, auth);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data)
    {
        switch (loader.getId())
        {
            case 0:
                if (null == data)
                {
                    Snackbar.make(getView(), R.string.err_search_sub, Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    SubtitleAdapter adapter = (SubtitleAdapter) lvSubtitles.getAdapter();
                    adapter.clear();
                    List<Subtitle> subtitles = (List<Subtitle>) data;
                    for (Subtitle subtitle : subtitles)
                    {
                        adapter.add(subtitle);
                    }
                    adapter.notifyDataSetChanged();
                    lvSubtitles.setSelection(0);
                }
                break;
            case 1:
                Boolean res = (Boolean) data;
                String text = res ? getString(R.string.msg_download_sub) : getString(R.string.err_download_sub);
                Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
                break;
        }
        progressBar.setVisibility(View.INVISIBLE);
        fetching = false;
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) { }

}