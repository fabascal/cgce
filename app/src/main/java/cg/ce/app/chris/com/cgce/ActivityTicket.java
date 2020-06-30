package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.epos2.Epos2Exception;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

import cg.ce.app.chris.com.cgce.Printing.TicketPrint;

public class ActivityTicket extends AppCompatActivity implements View.OnClickListener {
    TextView nrotrn,prd,cant,precio,monto;
    ImageButton print;
    Sensores sensores = new Sensores();
    ValidateTablet tablet = new ValidateTablet();
    JSONObject ticket = new JSONObject();
    cgticket cg = new cgticket();
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");
    DecimalFormat formateador4 = new DecimalFormat("###,###.####");
    String event_ticket,evento_sorteo, tur;
    int tiptrn;
    Spinner spn_metodo,spn_metodo_den;
    List tpv ;



    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sensores.bluetooth();
        sensores.wifi(this, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        if (tablet.esTablet(getApplicationContext())) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        nrotrn = findViewById(R.id.nrotrn);
        prd = findViewById(R.id.prd);
        cant = findViewById(R.id.cant);
        precio = findViewById(R.id.precio);
        monto = findViewById(R.id.monto);
        print = findViewById(R.id.print_ticket);
        spn_metodo = findViewById(R.id.spn_metedo);
        spn_metodo_den = findViewById(R.id.spn_metodo_den);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mPagos_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_tiptrn);
// Apply the adapter to the spinner
        spn_metodo.setAdapter(adapter);

        print.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle.getString("bomba") != null) {
            try {
                ticket = cg.consulta_servicio(this, bundle.getString("bomba"));
                ticket.put("nip", cg.nip_desp(getApplicationContext()));
                nrotrn.setText(ticket.getString("nrotrn") + "0");
                prd.setText(ticket.getString("producto"));
                cant.setText("LTS " + String.valueOf(formateador4.format(ticket.getDouble("cantidad"))));
                precio.setText("$ " + String.format("%.2f", Double.valueOf(String.valueOf(formateador2.format(ticket.getDouble("precio"))))));
                monto.setText("$ " + String.valueOf(formateador2.format(ticket.getDouble("total"))));

            } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException | JSONException e) {
                new AlertDialog.Builder(ActivityTicket.this)
                        .setTitle(R.string.error)
                        .setMessage(String.valueOf(e))
                        .setPositiveButton(R.string.btn_ok,null).show();
                e.printStackTrace();
            }
        }

        spn_metodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                view_spn_metodo_den();
                try {
                    fill_spn_metodo_den();
                } catch (ClassNotFoundException | SQLException | InstantiationException | JSONException | IllegalAccessException e) {
                    new AlertDialog.Builder(ActivityTicket.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

    }


    public void fill_spn_metodo_den() throws ClassNotFoundException, SQLException, InstantiationException, JSONException, IllegalAccessException {
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito")){
            tpv = cg.getTPVs(this, "1");
            ArrayAdapter MonederoAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        }else if(spn_metodo.getSelectedItem().toString().equals("Monederos")){
            tpv = cg.getTPVs(this, "0");
            ArrayAdapter MonederoAdapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, tpv);
            MonederoAdapter.setDropDownViewResource(R.layout.spinner_tiptrn);
            spn_metodo_den.setAdapter(MonederoAdapter);
        }

    }
    public void view_spn_metodo_den(){
        if (spn_metodo.getSelectedItem().toString().equals("T. Credito") || spn_metodo.
                getSelectedItem().toString().equals("T. Debito") || spn_metodo.
                getSelectedItem().toString().equals("Monederos") ){
            spn_metodo_den.setVisibility(View.VISIBLE);
        }else{
            spn_metodo_den.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print_ticket:

                print.setEnabled(false);
                if (spn_metodo.getSelectedItem().toString().equals("Efectivo")){
                    tur="1|Efectivo";
                    tiptrn=49;
                }else if(spn_metodo.getSelectedItem().toString().equals("T. Credito")){
                    tur="2|T. Credito" + "|" + spn_metodo_den.getSelectedItem().toString();
                    tiptrn=51;
                }else if(spn_metodo.getSelectedItem().toString().equals("T. Debito")){
                    tur="3|T. Debito" + "|" + spn_metodo_den.getSelectedItem().toString();
                    tiptrn=51;
                }else if(spn_metodo.getSelectedItem().toString().equals("Anticipos")){
                    tur="4|Anticipos";
                    tiptrn=50;
                }else if(spn_metodo.getSelectedItem().toString().equals("Combu-Vale")){
                    tur="5|Combu-Vale";
                    tiptrn=50;
                }else if(spn_metodo.getSelectedItem().toString().equals("Monederos")){
                    tur="6|Monederos" + "|" + spn_metodo_den.getSelectedItem().toString();
                    tiptrn=51;
                }
                try {
                    ticket.put("rut",tur);
                    ticket.put("tiptrn",tiptrn);
                    int impreso = cg.cant_impreso(getApplicationContext(),ticket.getString("nrotrn"));
                    if (impreso == 10) {
                        Log.w("ticket con tiptrn",ticket.toString());
                        cg.guardarnrotrn(getApplicationContext(), ticket, 1);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new Impresion().execute();
                        }
                    });
                    Intent intent = new Intent(ActivityTicket.this,VentaActivity.class);
                    startActivity(intent);

                } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException | JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ActivityTicket.this)
                            .setTitle(R.string.error)
                            .setMessage(String.valueOf(e))
                            .setPositiveButton(R.string.btn_ok,null).show();

                }




        }

    }



    public class Impresion extends AsyncTask<JSONObject,String,Boolean>{

        ProgressDialog pdLoading ;

        public Impresion(){
            pdLoading = new ProgressDialog(ActivityTicket.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread

            pdLoading.setIndeterminate(true);
            pdLoading.setCancelable(false);
            pdLoading.setTitle("Combu-Express");
            pdLoading.setMessage("Imprimiendo ...");
            pdLoading.show();

        }

        @Override
        protected Boolean doInBackground(JSONObject... jsonObjects) {
            TicketPrint print = null;
            try {
                print = new TicketPrint(ActivityTicket.this, ticket);
                event_ticket = print.Print();
                ActivityTicket.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(ActivityTicket.this)
                                .setTitle(R.string.error)
                                .setMessage(event_ticket)
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                });
            } catch (JSONException | SQLException | IllegalAccessException | ClassNotFoundException | InstantiationException | Epos2Exception | WriterException e) {
                ActivityTicket.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(ActivityTicket.this)
                                .setTitle(R.string.error)
                                .setMessage(String.valueOf(e))
                                .setPositiveButton(R.string.btn_ok,null).show();
                    }
                });
            }
           /* Log.w("evento ticket",event_ticket);*/
            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            SystemClock.sleep(2500);
            if (pdLoading != null) {
                pdLoading.dismiss();
            }

        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

        }
    };

    public class Sorteo extends AsyncTask<JSONObject, String, String> {

        ProgressDialog pdLoading ;
        HttpURLConnection conn;
        public static final int CONNECTION_TIMEOUT = 5000;
        public static final int READ_TIMEOUT = 7000;
        URL url = null;
        public SorteoListener delegate=null;

        public Sorteo(){
            pdLoading = new ProgressDialog(ActivityTicket.this);
        }

        @Override
        protected void onPreExecute() {
            Log.w("inicia Sorteo","oK");
            super.onPreExecute();

            //this method will be running on UI thread

            pdLoading.setIndeterminate(true);
            pdLoading.setCancelable(false);
            pdLoading.setTitle("Repsol");
            pdLoading.setMessage("Generando ticket de sorteo!!!");
            pdLoading.show();

        }

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            try {
                url = new URL("http://factura.combuexpress.mx/repsolsorteo/combugosorteows.php");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("cveest", ticket.getString("cveest"))
                        .appendQueryParameter("ticket",ticket.getString("nrotrn"))
                        .appendQueryParameter("fecha_ticket",ticket.getString("fecha"))
                        .appendQueryParameter("id_producto",ticket.getString("id_producto"))
                        .appendQueryParameter("bomba",ticket.getString("bomba"))
                        .appendQueryParameter("preunitario",ticket.getString("precio"))
                        .appendQueryParameter("importe",ticket.getString("total"))
                        .appendQueryParameter("nip",ticket.getString("nip"));
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                conn.disconnect();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            Log.w("sorteo result",result);
            try {
                JSONObject js = new JSONObject(result);
                //SorteoPrint sorteoPrint = new SorteoPrint(ActivityTicket.this,js);
                //evento_sorteo=sorteoPrint.Print();
                //new ClassImpresionSorteo(ActivityTicket.this,getApplicationContext(),js).execute();
                //Log.w("evento sorteo",evento_sorteo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (pdLoading != null) {
                pdLoading.dismiss();
            }
        }


    }


}
