package com.mobsoft.library;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class MobSoftUpdate {

    private String appId;
    private Activity activity;

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

    /**
     * Exibe o diálogo de verificação de atualizações.
     * @param onUpdate Gerenciador de fragmentos.
     */
    public void verifyUpdate(OnUpdateResponse onUpdate) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String deviceId = Build.ID;

            OkHttpClient client = new OkHttpClient();

            HashMap<String, String> data = new HashMap<>();
            data.put("packageName", pInfo.packageName);
            data.put("versionCode", String.valueOf(pInfo.versionCode));
            data.put("device", deviceId);
            data.put("appId", appId);

            Gson gson = new Gson();
            String json = gson.toJson(data);

            RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

            String url = "https://mobsoft-console.up.railway.app/update";

            Request request = new Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .build();

            Handler handler = new Handler(Looper.getMainLooper());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    assert response.body() != null;
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(jsonResponse);
                    } catch (JSONException e) {
                        handler.post(() -> onUpdate.onFailure("Error verifying request. Please try again later or contact support."));
                        return;
                    }

                    try {

                        if (response.isSuccessful()) {
                            Intent i = new Intent();
                            i.setClass(activity, UpdateActivity.class);
                            i.putExtra("size", jsonObject.getString("size"));
                            i.putExtra("url", jsonObject.getString("url"));
                            activity.startActivity(i);
                            activity.finish();
                        } else {
                            Intent i = new Intent();
                            i.setClass(activity, UpdateActivity.class);
                            i.putExtra("size", jsonObject.getString("size"));
                            i.putExtra("url", jsonObject.getString("url"));
                            activity.startActivity(i);
                            activity.finish();
                            String message = jsonObject.getString("message");
                            handler.post(() -> onUpdate.onFailure(message));
                        }

                    } catch (JSONException e) {
                        String message = "Error verifying request, please contact support";
                        handler.post(() -> onUpdate.onFailure(message));
                    }


                }
            });

        } catch (PackageManager.NameNotFoundException e) {
            onUpdate.onFailure(e.getMessage());
        }
    }



}
