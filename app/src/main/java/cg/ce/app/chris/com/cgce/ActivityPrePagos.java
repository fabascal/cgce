package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;


public class ActivityPrePagos extends AppCompatActivity implements View.OnClickListener {
    CardView CardViewVale,CardViewAnticipo,CardViewCine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_pagos);
        CardViewVale = (CardView)findViewById(R.id.CardViewVale);
        CardViewAnticipo = (CardView) findViewById(R.id.CardViewAnticipo);
        CardViewCine = (CardView) findViewById(R.id.CardViewCine);
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
}
