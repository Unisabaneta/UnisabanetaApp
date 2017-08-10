package com.apps.unisabanetaapp;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Clase que permite generar un identificador para cada instalación del programa
 *
 * Created by diezc on 1/06/2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Noticias", "Token: " + token);
    }
}
