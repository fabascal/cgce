package cg.ce.app.chris.com.cgce;

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
 * Created by chris on 26/01/17.
 */

public class AdapterClienteDomicilio extends RecyclerView.Adapter {


    private Context context;
    private LayoutInflater inflater;
    List<DataClienteDomicilio> data= Collections.emptyList();
    DataCliente current;
    int currentPos=0;
    LogCE logCE = new LogCE();
    // create constructor to initialize context and data sent from MainActivity
    public AdapterClienteDomicilio(Context context, List<DataClienteDomicilio> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.container_cliente_domicilio, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in RecyclerView to bind data and assign values from list
        AdapterClienteDomicilio.MyHolder myHolder= (AdapterClienteDomicilio.MyHolder) holder;
        DataClienteDomicilio current=data.get(position);
        myHolder.calle.setText(current.calle+" "+current.exterior+" "+current.interior);
        myHolder.colonia.setText("Colonia :" + current.colonia);
        myHolder.estado.setText("Estado :"+current.estado );
        myHolder.municipio.setText("Municipio :" + current.municipio);
        myHolder.cp.setText("CP :" + current.cp);

    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView calle;
        TextView colonia;
        TextView estado;
        TextView municipio;
        TextView cp;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            calle= (TextView) itemView.findViewById(R.id.calle);
            colonia = (TextView) itemView.findViewById(R.id.colonia);
            estado = (TextView) itemView.findViewById(R.id.estado);
            municipio = (TextView) itemView.findViewById(R.id.municipio);
            cp = (TextView) itemView.findViewById(R.id.cp);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v ) {

            String id_cliente = DomicilioBusqueda.mMyAppsBundle.getString("key");

            TextView rfc = (TextView) ((DomicilioBusqueda)context).findViewById(R.id.tv_rfc_cfdi);
            TextView razon = (TextView) ((DomicilioBusqueda)context).findViewById(R.id.tv_cliente_cfdi);
            TextView correo = (TextView) ((DomicilioBusqueda)context).findViewById(R.id.tv_correo_cfdi);
            JSONObject cfdi_domicilio_data=new JSONObject();
            try {
                cfdi_domicilio_data.put("id_cliente",id_cliente);
                cfdi_domicilio_data.put("RFC",rfc.getText());
                cfdi_domicilio_data.put("razon_social",razon.getText());
                cfdi_domicilio_data.put("correo",correo.getText());
                cfdi_domicilio_data.put("id_domicilio",data.get(getAdapterPosition()).id_domicilio);
                cfdi_domicilio_data.put("calle",data.get(getAdapterPosition()).calle);
                cfdi_domicilio_data.put("colonia",data.get(getAdapterPosition()).colonia);
                cfdi_domicilio_data.put("localidad",data.get(getAdapterPosition()).localidad);
                cfdi_domicilio_data.put("id_estado",data.get(getAdapterPosition()).id_estado);
                cfdi_domicilio_data.put("estado",data.get(getAdapterPosition()).estado);
                cfdi_domicilio_data.put("municipio",data.get(getAdapterPosition()).municipio);
                cfdi_domicilio_data.put("pais",data.get(getAdapterPosition()).pais);
                cfdi_domicilio_data.put("cp",data.get(getAdapterPosition()).cp);
                cfdi_domicilio_data.put("exterior",data.get(getAdapterPosition()).exterior);
                cfdi_domicilio_data.put("interior",data.get(getAdapterPosition()).interior);
                cfdi_domicilio_data.put("bomba",data.get(getAdapterPosition()).bomba);

            } catch (JSONException e) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(context,"AdapterCliente_onClick - " + e);
                e.printStackTrace();
            }
            Intent i = new Intent(context, MetodoPagoBusqueda.class);
            i.putExtra("cliente", cfdi_domicilio_data.toString());
            try {
                i.putExtra("id_domicilio",cfdi_domicilio_data.getString("id_domicilio"));
            } catch (JSONException e) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                logCE.EscirbirLog2(context,"AdapterCliente_onClick - " + e);
                e.printStackTrace();
            }
            context.startActivity(i);
            ((DomicilioBusqueda) context).finish();
        }
    }
}
