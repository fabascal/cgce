package cg.ce.app.chris.com.cgce;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Created by chris on 24/01/17.
 */

public class AdapterCliente extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Sensores sensores = new Sensores();
    private Context context;
    private LayoutInflater inflater;
    List<DataCliente> data= Collections.emptyList();
    LogCE logCE = new LogCE();


    // create constructor to initialize context and data sent from MainActivity
    public AdapterCliente(Context context, List<DataCliente> data){
        this.context=context;
        sensores.wifi(context,true);
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.container_cliente, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        MyHolder myHolder= (MyHolder) holder;
        DataCliente current=data.get(position);
        myHolder.razonsocial.setText(current.nombre);
        myHolder.rfc.setText("RFC: " + current.rfc);
        myHolder.mail.setText("Mail: " + current.correo);
    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView razonsocial;
        TextView rfc;
        TextView mail;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            razonsocial= (TextView) itemView.findViewById(R.id.razonsocial);
            rfc = (TextView) itemView.findViewById(R.id.rfc);
            mail = (TextView) itemView.findViewById(R.id.mail);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v ) {
            JSONObject cfdi_data=new JSONObject();
            try {
                cfdi_data.put("rfc",data.get(getAdapterPosition()).rfc);
                cfdi_data.put("nombre",data.get(getAdapterPosition()).nombre);
                cfdi_data.put("correo",data.get(getAdapterPosition()).correo);
                cfdi_data.put("id_cliente",data.get(getAdapterPosition()).id_cliente);
                cfdi_data.put("bomba",data.get(getAdapterPosition()).bomba);
            } catch (JSONException e) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(context,"AdapterCliente_onClick - " + e);
                e.printStackTrace();
            }
            Intent i = new Intent(context, DomicilioBusqueda.class);
            i.putExtra("cliente", cfdi_data.toString());
            context.startActivity(i);
        }
    }
}
