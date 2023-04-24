package com.mobsoft.library;

import android.app.Activity;

import androidx.fragment.app.FragmentManager;

public class MobSoftSdk {

    private String appId;
    private Activity activity;
    private FragmentManager fragmentManager;

    /**
     * Interface para lidar com sucesso/falha na inicialização do SDK.
     */
    public interface InitializedSdk {
        void onSuccess(String message);
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
     * Define o FragmentManager do SDK.
     *
     * @param fragmentManager o FragmentManager do aplicativo
     */
    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    /**
     * Verifica se o SDK foi inicializado com sucesso.
     * Caso contrário, chama o método onFailure da interface InitializedSdk passada como parâmetro.
     * Se todas as variáveis necessárias estiverem definidas, chama o método onSuccess da interface InitializedSdk passada como parâmetro.
     *
     * @param sdk a interface InitializedSdk passada como parâmetro para lidar com sucesso/falha na inicialização do SDK
     */
    public void onInitialized(InitializedSdk sdk) {
        if (appId == null) {
            sdk.onFailure("The appId has not been set. Please call setAppId() before initializing the SDK.");
            return;
        }
        if (activity == null) {
            sdk.onFailure("The activity has not been set. Please call setActivity() before initializing the SDK.");
            return;
        }
        if (fragmentManager == null) {
            sdk.onFailure("The fragmentManager has not been set. Please call setFragmentManager() before initializing the SDK.");
            return;
        }
        sdk.onSuccess("SDK initialized successfully.");
        MobSoftUpdate update = new MobSoftUpdate();
        update.setAppId(appId);
        update.setActivity(activity);
        update.verifyUpdate(sdk::onFailure);
    }

}
