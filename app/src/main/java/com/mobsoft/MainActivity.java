package com.mobsoft;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mobsoft.library.FragmentDialogUpdate;
import com.mobsoft.library.MobSoftUpdate;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobSoftUpdate update = new MobSoftUpdate();

        update.appIsInitialized(this, new MobSoftUpdate.InitializedProvider<Boolean>() {
            @Override
            public void onInitialized(Boolean initialized, Exception error) {
                if (initialized) {
                    update.verifyUpdate(MainActivity.this, getSupportFragmentManager());
                } else {
                    Log.d("Error", String.valueOf(error));
                    Toast.makeText(MainActivity.this, "Error initializing provider: ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}