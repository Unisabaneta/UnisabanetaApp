/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apps.unisabanetaapp;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ParseException;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provee una interfaz para el usuario cuando este da clic en una de las noticias.
 */
public class ActividadDetalleNoticia extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_noticia);

        Toolbar toolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);

        // Set Collapsing Toolbar layout to the screen
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        // Set title of Detail page

        Intent data = getIntent();
        //collapsingToolbar.setTitle(data.getStringExtra("Titulo"));

        TextView descripcion = (TextView) findViewById(R.id.descripcion);
        descripcion.setText(data.getStringExtra("Descripcion").trim()+"\n");

        TextView titulo =  (TextView) findViewById(R.id.titulo);
        titulo.setText(minusculas(data.getStringExtra("Titulo")));

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd"); // here set the pattern as you date in string was containing like date/month/year
            Date d = sdf.parse(data.getStringExtra("Fecha"));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        TextView fecha =  (TextView) findViewById(R.id.fechaPublicacion);
        fecha.setText(data.getStringExtra("Fecha").substring(0,10));

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(FragmentoNoticias.ImageHolder.getInstance().getBitmap());

        TextView autor = (TextView) findViewById(R.id.autor);
        autor.setText(minusculas(data.getStringExtra("Autor")));

        TextView url = (TextView) findViewById(R.id.url);
        url.setText(data.getStringExtra("Url"));
    }

    /**
     * Permite mostrar texto en letra minuscula
     * @param string
     * @return
     */

    public String minusculas (String string){

        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);

    }
}
