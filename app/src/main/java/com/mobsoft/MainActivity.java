package com.mobsoft;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.mobsoft.library.MobSoftSdk;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobSoftSdk mobSoftSdk = new MobSoftSdk();
        mobSoftSdk.setAppId("b712d6fd-b936-417e-846e-7f095cee3d6d");
        mobSoftSdk.setActivity(MainActivity.this);
        mobSoftSdk.setFragmentManager(getSupportFragmentManager());

        mobSoftSdk.onInitialized(new MobSoftSdk.InitializedSdk() {
            @Override
            public void onSuccess(String message) {

            }

            @Override
            public void onFailure(String error) {
                // Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

    }

}

