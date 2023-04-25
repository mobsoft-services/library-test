package com.mobsoft.library;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import java.io.File;
import java.util.UUID;

public class UpdateFragment extends DialogFragment {

    private ProgressBar progressBar;
    private TextView textProgress;
    private LinearLayout linearDown;
    private TextView textBtnDown;
    private LinearLayout linearButtonUpdate;
    private Boolean isDownload = false;
    private File apkFile;
    private LottieAnimationView animationView;
    private ImageView icon;
    private TextView textTitleApp;
    private TextView textSizeApp;
    private TextView textDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.DialogSlideUpAnimation);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = dialog.getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(Color.WHITE);
        }

        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_update, container, false);

        linearDown = view.findViewById(R.id.linear_progress_download);
        textProgress = view.findViewById(R.id.textDownloadProgress);
        progressBar = view.findViewById(R.id.progressbar1);
        linearButtonUpdate = view.findViewById(R.id.linear_button_update);
        textBtnDown = view.findViewById(R.id.textBtnDown);
        icon = view.findViewById(R.id.icon);
        textTitleApp = view.findViewById(R.id.text_title_app);
        textSizeApp = view.findViewById(R.id.text_size_app);
        textDescription = view.findViewById(R.id.textDescription);


        animationView = view.findViewById(R.id.animationView);
        animationView.setAnimation(R.raw.renewable_energy);
        animationView.setRepeatMode(LottieDrawable.RESTART);
        animationView.loop(true);
        animationView.playAnimation();

        Bundle args = getArguments();

        if (args != null) {
            textSizeApp.setText(getString(R.string.text_length).concat(" " + args.getString("size")));
        }

        textBtnDown.setOnClickListener(view1 -> {
            if (isDownload) {
                installApk(apkFile);
            } else if (args != null) {
                linearButtonUpdate.setVisibility(View.INVISIBLE);
                linearDown.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.VISIBLE);
                textTitleApp.setVisibility(View.GONE);
                textSizeApp.setVisibility(View.GONE);
                icon.setVisibility(View.GONE);
                textDescription.setVisibility(View.GONE);
                downloadApp(args.getString("url"));
            }
        });

        PackageManager pm = requireContext().getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = pm.getApplicationInfo(requireContext().getPackageName(), 0);

            ApplicationInfo applicationInfo = pm.getApplicationInfo(requireContext().getPackageName(), 0);
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

    private void downloadApp(String url) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String uuid = UUID.randomUUID().toString();
        apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), uuid + ".apk");
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
                            isDownload = true;
                            handler.post(() -> {
                                textBtnDown.setText(getString(R.string.text_install));
                                animationView.setVisibility(View.GONE);
                                linearButtonUpdate.setVisibility(View.VISIBLE);
                                linearDown.setVisibility(View.GONE);
                                textTitleApp.setVisibility(View.VISIBLE);
                                textSizeApp.setVisibility(View.VISIBLE);
                                icon.setVisibility(View.VISIBLE);
                                textDescription.setVisibility(View.VISIBLE);
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
        // install(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".provider",
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
