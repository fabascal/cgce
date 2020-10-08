package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import java.util.Objects;


public class ActivityPrePagos extends AppCompatActivity implements View.OnClickListener {
    CardView CardViewVale,CardViewAnticipo,CardViewCine;
    ImageView imageviewCine;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        CardViewVale = findViewById(R.id.CardViewVale);
        CardViewAnticipo =  findViewById(R.id.CardViewAnticipo);
        CardViewCine =  findViewById(R.id.CardViewCine);
        CardViewVale.setOnClickListener(this);
        CardViewAnticipo.setOnClickListener(this);
        CardViewCine.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent=null;
        switch (view.getId()) {
            case R.id.CardViewVale:
                intent = new Intent(this,ActivityPrePagoVale.class);
                break;
            case R.id.CardViewCine:
                intent = new Intent(this,ServiciosActivity.class);
                break;
        }
        if (intent!=null){
            startActivity(intent);
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (Objects.requireNonNull(sharedPreferences.getString(getResources().getString(R.string.BrandName), "Combu-Express"))){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_pre_pagos);
                imageviewCine = findViewById(R.id.imageviewCine);
                imageviewCine.setImageResource(R.drawable.ticketcinecombu);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_pre_pagos);
                imageviewCine = findViewById(R.id.imageviewCine);
                imageviewCine.setImageResource(R.drawable.ticketcinerepsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_pre_pagos);
                imageviewCine = findViewById(R.id.imageviewCine);
                imageviewCine.setImageResource(R.drawable.ticketcineener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_pre_pagos);
                imageviewCine = findViewById(R.id.imageviewCine);
                imageviewCine.setImageResource(R.drawable.ticketcinetotal);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }
}
