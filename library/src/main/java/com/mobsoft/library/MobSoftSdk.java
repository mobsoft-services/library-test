package com.mobsoft.library;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

public class MobSoftSdk {

    /**
     * Interface para lidar com sucesso/falha na inicialização do SDK.
     */
    public interface InitializedSdk {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public MobSoftSdk(@NonNull String appId, @NonNull Activity activity, InitializedSdk sdk) {
        sdk.onSuccess("SDK initialized successfully.");
        MobSoftUpdate update = new MobSoftUpdate();
        update.setAppId(appId);
        update.setActivity(activity);
        update.verifyUpdate(sdk::onFailure);
    }

}
