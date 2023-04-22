package com.mobsoft.library;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import com.mobsoft.library.BuildConfig;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import java.io.File;
import java.util.Objects;

public class FragmentDialogUpdate extends DialogFragment {

    private ProgressBar progressBar;
    private TextView textProgress;
    private LinearLayout linearDown;
    private TextView textBtnDown;
    private LinearLayout linearButtonUpdate;

    private Boolean isDownload = false;
    private boolean shouldAnimate = true;

    private File apkFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        if (shouldAnimate) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
            view.startAnimation(animation);
        }

        linearDown = view.findViewById(R.id.linear_progress_download);
        textProgress = view.findViewById(R.id.textDownloadProgress);
        progressBar = view.findViewById(R.id.progressbar1);
        linearButtonUpdate = view.findViewById(R.id.linear_button_update);
        textBtnDown = view.findViewById(R.id.textBtnDown);
        ImageView icon = view.findViewById(R.id.icon);
        TextView textTitleApp = view.findViewById(R.id.text_title_app);
        TextView textSizeApp = view.findViewById(R.id.text_size_app);

        Bundle args = getArguments();

        textBtnDown.setText("ATUALIZAR");
        if (args != null) {
            System.out.println(args.getString("url"));
            textSizeApp.setText("Tamanho: ".concat(args.getString(("size"))));
        }

        textBtnDown.setOnClickListener(view1 -> {
            if (isDownload) {
                installApk(apkFile);
            } else if (args != null) {
                linearButtonUpdate.setVisibility(View.GONE);
                linearDown.setVisibility(View.VISIBLE);
                downloadApp(args.getString("url"));
            }
        });

        PackageManager pm = requireContext().getPackageManager();
        ApplicationInfo appInfo = null;

        try {

            appInfo = pm.getApplicationInfo("com.mobsoft", 0);

            ApplicationInfo applicationInfo = pm.getApplicationInfo(requireActivity().getPackageName(), 0);
            String appName = (String) pm.getApplicationLabel(applicationInfo);

            textTitleApp.setText(appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            icon.setImageDrawable(appInfo.loadIcon(pm));
        }


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shouldAnimate) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                dialog.getWindow().setLayout(width, height);
                dialog.getWindow().setWindowAnimations(R.style.AppTheme_FullScreenDialog);
            }
            shouldAnimate = false;
        }
    }

    private void downloadApp(String url) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app.apk");
        request.setDestinationUri(Uri.fromFile(apkFile));

        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        progressBar.setMax(100);
        progressBar.setProgress(0);

        final Handler handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            boolean downloading = true;
            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                try (Cursor cursor = downloadManager.query(query)) {
                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        int downloadedBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                            handler.post(() -> {
                                isDownload = true;
                                textBtnDown.setText("INSTALAR");
                                linearButtonUpdate.setVisibility(View.VISIBLE);
                                linearDown.setVisibility(View.GONE);
                            });
                        }
                        int progress = (int) ((downloadedBytes * 100L) / totalBytes);
                        handler.post(() -> {
                            progressBar.setProgress(progress);
                            textProgress.setText(String.valueOf(progress).concat("%"));
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void installApk(File apkFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.mobsoft.library.provider",
                    apkFile);

            Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            installIntent.setData(contentUri);
            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(installIntent);
        } else {
            Uri apkUri = Uri.fromFile(apkFile);
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installIntent);
        }
    }


}
