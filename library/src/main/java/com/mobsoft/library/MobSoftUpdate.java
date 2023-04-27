package com.mobsoft.library;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MobSoftUpdate {

    private final String PUT_URL = "https://mobsoft-console.up.railway.app/update";

    private String appId;
    private Activity activity;
    private FragmentManager manager;

    public interface OnUpdateResponse {
        void onFailure(String error);
    }

    /**
     * Define o appId do SDK.
     *
     * @param appId o ID do aplicativo
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Define o contexto do SDK.
     *
     * @param activity o contexto do aplicativo
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setFragment(FragmentManager manager) {this.manager = manager;}

    /**
     * Exibe o diálogo de verificação de atualizações.
     * @param onUpdate Gerenciador de fragmentos.
     */
    public void verifyUpdate(OnUpdateResponse onUpdate) {
        try {

            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String deviceId = Build.ID;

            HashMap<String, String> data = new HashMap<>();
            data.put("packageName", pInfo.packageName);
            data.put("versionCode", String.valueOf(pInfo.versionCode));
            data.put("device", deviceId);
            data.put("appId", appId);

            Handler handler = new Handler();
            new Thread(() -> {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(PUT_URL);

                    // Configura o HttpURLConnection
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Cria o objeto JSON com os dados a serem enviados
                    JSONObject data1 = new JSONObject();
                    data1.put("key1", "value1");
                    data1.put("key2", "value2");

                    // Converte o objeto JSON em bytes e escreve no corpo da requisição
                    OutputStream os = conn.getOutputStream();
                    os.write(data1.toString().getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    InputStream inputStream;
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    String response = stringBuilder.toString();
                    bufferedReader.close();
                    inputStream.close();

                    JSONObject jsonObject = new JSONObject(response);
                    handler.post(() -> {
                        try {
                            onUpdate.onFailure(jsonObject.getString("message"));
                            UpdateFragment dialog = new UpdateFragment();
                            Bundle args = new Bundle();
                            args.putString("size", "12MB");
                            args.putString("url", "https://teste.com");
                            dialog.setArguments(args);
                            dialog.show(manager, "dialog_update");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception e) {
                    Log.e("PUT Error", e.getMessage(), e);

                    assert conn != null;
                    InputStream errorStream = ((HttpURLConnection) conn).getErrorStream();
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                    StringBuilder errorBuilder = new StringBuilder();
                    String errorLine;
                    while (true) {
                        try {
                            if ((errorLine = errorReader.readLine()) == null) break;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        errorBuilder.append(errorLine);
                    }
                    String errorResponse = errorBuilder.toString();
                    try {
                        errorReader.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        errorStream.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(errorResponse);
                        System.out.println(jsonObject.getString("message"));
                    } catch (JSONException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }).start();

        } catch (PackageManager.NameNotFoundException e) {
            onUpdate.onFailure(e.getMessage());
        }
    }




}
