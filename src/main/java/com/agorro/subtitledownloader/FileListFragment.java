package com.agorro.subtitledownloader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agorro.subtitledownloader.adapter.FileAdapter;
import com.agorro.subtitledownloader.loader.FileListLoader;
import com.agorro.subtitledownloader.smb.AuthInfo;
import com.agorro.subtitledownloader.smb.File;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.fragment.NavHostFragment;

public class FileListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<File>>
{
    public static final String EXTRA_INIT = "com.agorro.subtitledownloader.extra.EXTRA_INIT";
    public static final String EXTRA_PARENT_PATH = "com.agorro.subtitledownloader.extra.EXTRA_PARENT_PATH";
    public static final String EXTRA_CURRENT_PATH = "com.agorro.subtitledownloader.extra.EXTRA_CURRENT_PATH";

    private boolean settingsEstablished = false;
    private boolean fetching = false;

    private ActionBar actionBar;
    private ProgressBar progressBar;
    private TextView tvCurrent;
    private ListView lvFiles;

    private String currentPath;
    private String basePath;
    private String parentPath;
    private AuthInfo authInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_file_list, container, false);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        progressBar = getActivity().findViewById(R.id.pbSearch);
        tvCurrent = view.findViewById(R.id.tv_current);
        lvFiles = view.findViewById(R.id.lv_files);
        lvFiles.setAdapter(new FileAdapter(getActivity(), new ArrayList<File>()));
        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!fetching)
                {
                    File file = (File) parent.getItemAtPosition(position);
                    navigate(file);
                }
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String ip = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_IP, ""); //192.168.1.200
        String seriesFolder = sharedPref.getString(SettingsActivity.KEY_PREF_SERIES_FOLDER, ""); // /hddp1/series/
        String domain = sharedPref.getString(SettingsActivity.KEY_PREF_DOMAIN, "");
        String username = sharedPref.getString(SettingsActivity.KEY_PREF_USERNAME, ""); //chuwi
        String password = sharedPref.getString(SettingsActivity.KEY_PREF_PASSWORD, ""); //chuwi

        if ("".equals(ip))
        {
            settingsEstablished = false;
            basePath = "";
        }
        else
        {
            settingsEstablished = true;
            basePath = File.getBasePath(ip, seriesFolder);
            authInfo = new AuthInfo(domain, username, password);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (!settingsEstablished || basePath.equals(currentPath))
                {
                    getActivity().finish();
                }
                else
                {
                    File fileToNavigate = new File(true, "", parentPath, "");
                    navigate(fileToNavigate);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null)
        {
            if (getArguments().getInt(EXTRA_INIT) == 1)
            {
                currentPath = basePath;
                parentPath = basePath;
            }
            else
            {
                currentPath = getArguments().getString(EXTRA_CURRENT_PATH);
                parentPath = getArguments().getString(EXTRA_PARENT_PATH);
            }
        }
        else
        {
            currentPath = savedInstanceState.getString("currentPath");
            parentPath = savedInstanceState.getString("parentPath");
        }

        showBar();

        if (settingsEstablished)
        {
            tvCurrent.setText(getCurrentSharedPath(currentPath));

            if (LoaderManager.getInstance(FileListFragment.this).getLoader(0) != null)
            {
                //Inicializa un loader si no existe, si existe se reasocia sin modificar el bundle
                LoaderManager.getInstance(FileListFragment.this).initLoader(0, null, FileListFragment.this);
            } else
            {
                listFiles();
            }
        }
        else
        {
            tvCurrent.setText(getResources().getString(R.string.err_settings));
        }
    }

    private void showBar()
    {
        if (!settingsEstablished || !currentPath.equals(basePath))
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void navigate(File file)
    {
        if (file.isDirectory())
        {
            currentPath = file.getPath();
            parentPath = File.getParentPath(basePath, currentPath);
            tvCurrent.setText(getCurrentSharedPath(currentPath));
            listFiles();
        }
        else if (!file.isSubtitle())
        {
            Bundle bundle = new Bundle();
            bundle.putString(SubtitleListFragment.EXTRA_PARENT_NAME, parentPath);
            bundle.putString(SubtitleListFragment.EXTRA_FOLDER_NAME, currentPath);
            bundle.putString(SubtitleListFragment.EXTRA_FILE_NAME, file.getName());
            bundle.putSerializable(SubtitleListFragment.EXTRA_AUTH, authInfo);
            NavHostFragment.findNavController(FileListFragment.this)
                    .navigate(R.id.action_FileListFragment_to_SubtitleListFragment, bundle);
        }
    }

    private String getCurrentSharedPath(String path)
    {
        if (path.startsWith(basePath))
        {
            path = path.replace(basePath, File.SAMBA_SEPARATOR);
        }
        return path;
    }

    private void listFiles()
    {
        fetching = true;
        Bundle args = new Bundle();
        args.putSerializable("auth", authInfo);
        args.putString("base_path", basePath);
        args.putString("path", currentPath);
        //Destruye el loader actual y recrea el nuevo bundle
        LoaderManager.getInstance(FileListFragment.this).restartLoader(0, args, FileListFragment.this);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString("currentPath", currentPath);
        outState.putString("parentPath", parentPath);
    }

    @NonNull
    @Override
    public Loader<List<File>> onCreateLoader(int id, @Nullable Bundle args)
    {
        AuthInfo authInfo = (AuthInfo) args.getSerializable("auth");
        String basePath = args.getString("base_path");
        String path = args.getString("path");
        return new FileListLoader(getActivity(), authInfo, basePath, path);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<File>> loader, List<File> data)
    {
        if (null == data)
        {
            Snackbar.make(getView(), R.string.err_list_dir, Snackbar.LENGTH_LONG).show();
        }
        else
        {
            FileAdapter adapter = (FileAdapter) lvFiles.getAdapter();
            adapter.clear();
            for (File file : data)
            {
                adapter.add(file);
            }
            adapter.notifyDataSetChanged();
            lvFiles.setSelection(0);
        }
        progressBar.setVisibility(View.INVISIBLE);
        fetching = false;
        showBar();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<File>> loader)
    {
    }
}