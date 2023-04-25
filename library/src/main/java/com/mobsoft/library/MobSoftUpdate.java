package com.mobsoft.library;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MobSoftUpdate {

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

            String url = "https://mobsoft-console.up.railway.app/update";
            JSONObject requestBody = new JSONObject();
            requestBody.put("packageName", pInfo.packageName);
            requestBody.put("versionCode", String.valueOf(pInfo.versionCode));
            requestBody.put("device", deviceId);
            requestBody.put("appId", appId);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                    response -> {
                        try {
                            UpdateFragment dialog = new UpdateFragment();
                            Bundle args = new Bundle();
                            args.putString("size", response.getString("size"));
                            args.putString("url", response.getString("url"));
                            dialog.setArguments(args);
                            dialog.show(manager, "dialog_update");
                        } catch (Exception e) {
                            System.out.println("Error opening update activity");
                        }
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String json = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            try {
                                JSONObject jsonResponse = new JSONObject(json);
                                onUpdate.onFailure(jsonResponse.getString("message"));
                                UpdateFragment dialog = new UpdateFragment();
                                Bundle args = new Bundle();
                                args.putString("size", jsonResponse.getString("size"));
                                args.putString("url", jsonResponse.getString("url"));
                                dialog.setArguments(args);
                                dialog.show(manager, "dialog_update");
                            } catch (JSONException e) {
                                onUpdate.onFailure("Error verifying request. Please try again later or contact support.");
                            }
                        } else {
                            System.out.println("Network response is null");
                        }

                    }
            );

            RequestQueue queue = Volley.newRequestQueue(activity);
            queue.add(request);

        } catch (PackageManager.NameNotFoundException | JSONException e) {
            onUpdate.onFailure(e.getMessage());
        }
    }



}
