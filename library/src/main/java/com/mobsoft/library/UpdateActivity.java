package com.mobsoft.library;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;

public class UpdateActivity extends AppCompatActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        linearDown = findViewById(R.id.linear_progress_download);
        textProgress = findViewById(R.id.textDownloadProgress);
        progressBar = findViewById(R.id.progressbar1);
        linearButtonUpdate = findViewById(R.id.linear_button_update);
        textBtnDown = findViewById(R.id.textBtnDown);
        icon = findViewById(R.id.icon);
        textTitleApp = findViewById(R.id.text_title_app);
        textSizeApp = findViewById(R.id.text_size_app);
        textDescription = findViewById(R.id.textDescription);


        animationView = findViewById(R.id.animationView);
        animationView.setAnimation(R.raw.renewable_energy);
        animationView.playAnimation();

        Intent args = getIntent();

        if (args != null) {
            textSizeApp.setText(getString(R.string.text_length).concat(" " + args.getStringExtra("size")));
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
                downloadApp(args.getStringExtra("url"));
            }
        });

        PackageManager pm = getPackageManager();
        ApplicationInfo appInfo = null;

        try {
            appInfo = pm.getApplicationInfo(getPackageName(), 0);

            ApplicationInfo applicationInfo = pm.getApplicationInfo(getPackageName(), 0);
            String appName = (String) pm.getApplicationLabel(applicationInfo);

            textTitleApp.setText(appName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            icon.setImageDrawable(appInfo.loadIcon(pm));
        }

    }

    private void downloadApp(String url) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app.apk");
        request.setDestinationUri(Uri.fromFile(apkFile));

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
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
                    this,
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