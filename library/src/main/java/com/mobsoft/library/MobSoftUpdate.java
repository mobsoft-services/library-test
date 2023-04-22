package com.mobsoft.library;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

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

public class MobSoftUpdate {

    private String appId;

    public interface InitializedProvider<T> {
        /**
         * Método chamado quando a verificação de inicialização estiver completa.
         * @param initialized Indica se o SDK foi inicializado.
         * @param error Objeto contendo informações sobre um possível erro que ocorreu durante a verificação.
         */
        void onInitialized(T initialized, Exception error);
    }

    /**
     * Verifica se SDK foi inicializado.
     * @param context O contexto da aplicação.
     * @param initialized O objeto de retorno que será chamado quando a verificação estiver completa.
     *                    O método "onInitialized" será chamado com o resultado da verificação.
     */
    public void appIsInitialized(Context context, InitializedProvider<Boolean> initialized) {
        Bundle metaData = null;
        try {
            metaData = context
                    .getApplicationContext()
                    .getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            if (metaData != null) {
                appId = metaData.getString("com.mobsoft.APP_ID");
            }
            initialized.onInitialized(appId != null, null);
        } catch (Exception e) {
            initialized.onInitialized(false, e);
        }
    }

    /**
     * Exibe o diálogo de verificação de atualizações.
     * @param manager Gerenciador de fragmentos.
     */
    public void verifyUpdate(Context context, FragmentManager manager) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
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

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    assert response.body() != null;
                    if (response.isSuccessful()) {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = null;
                        try {

                            jsonObject = new JSONObject(jsonResponse);

                            DialogFragment dialog = new FragmentDialogUpdate();
                            Bundle args = new Bundle();
                            args.putString("url", jsonObject.getString("url"));
                            args.putString("size", jsonObject.getString("size"));
                            dialog.setArguments(args);
                            dialog.show(manager, "new_update");

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        System.out.println("Response not successful " + response.body().string());
                    }
                }
            });

        } catch (Exception e) {
            Log.d("Error", String.valueOf(e));
        }
    }

}
