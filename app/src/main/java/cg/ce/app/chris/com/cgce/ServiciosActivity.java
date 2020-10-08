package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import cg.ce.app.chris.com.cgce.Gmail.GMailSender;

public class ServiciosActivity extends AppCompatActivity implements CinepolisAsyncResponse, View.OnClickListener {
    Button ventaboleto;
    EditText subject;
    String correo = null;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BrandSharedPreferences();
        subject = findViewById(R.id.subject);
        ventaboleto.setOnClickListener(ServiciosActivity.this);
    }




    public void sendMessage(final String subject, final String folios, final String precio, final String mensaje, final String boletos, final String estacion) {
        //final ProgressDialog dialog = new ProgressDialog(ServiciosActivity.this);
        //dialog.setTitle("Enviando boletos");
        //dialog.setMessage("Favor de esperar");
        //dialog.show();

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("cinepolis@combu-express.com.mx", "qwerty888");
                    sender.sendMail("Boletos Combu-Express-Cinepolis",
                            buildhtml(folios,precio,mensaje,boletos,estacion),
                            "cinepolis@combu-express.com.mx",
                            subject);
                    //dialog.dismiss();
                } catch (Exception e) {
                    Log.e("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }

    public String buildhtml(String folios, String precio, String mensaje, String boletos, String estacion){
        String r1 ="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "    <head>\n" +
                "        <meta charset=\"utf-8\">\n" +
                "        <title></title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "    <div class=\"_17Jc6QO1QjTKmo_3K8hSZn YFtQ2HcVd7qxiOXuKcekC allowTextSelection\"><div><div class=\"rps_3ab1\">\n" +
                "<div>\n" +
                "<table width=\"652\" height=\"558\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table width=\"650\" height=\"558\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr height=\"24\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td width=\"25\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:16px; font-weight:bold; color:#056dae\" width=\"300\" height=\"17\" align=\"left\">\n" +
                "Compra Boletos </td>\n" +
                "</tr>\n" +
                "<tr height=\"19\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\">\n" +
                "<tbody>\n" +
                "<tr height=\"20\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td width=\"299.8\" align=\"right\">\n" +
                "    <img class=\"fix\" src=\"http://combuexpress.com.mx/imgs/varias/logo1.jpg\" width=\"100%\" border=\"0\" alt=\"\" />\n" +
                "<td width=\"20.2\"></td>\n" +
                "</tr>\n" +
                "<tr height=\"15\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"5\">\n" +
                "<td colspan=\"2\" bgcolor=\"#056dae\"></td>\n" +
                "</tr>\n" +
                "<tr height=\"10\">\n" +
                "<td colspan=\"2\" bgcolor=\"#fafafa\"></td>\n" +
                "</tr>\n" +
                "<tr bgcolor=\"#fafafa\">\n" +
                "<td colspan=\"2\">\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"25\"></td>\n" +
                "<td>\n" +
                "<table width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"#ffffff\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr height=\"10\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"265\"></td>\n" +
                "<td><img data-imagetype=\"External\" src=\"http://189.206.183.110:1390/cecg_app/icon_success.gif\" originalsrc=\"http://189.206.183.110:1390/cecg_app/icon_success.gif\" data-connectorsauthtoken=\"1\" data-imageproxyendpoint=\"/actions/ei\" data-imageproxyid=\"\" width=\"71\" height=\"71\"></td>\n" +
                "<td width=\"34\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"4\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr height=\"22\">\n" +
                "<td colspan=\"2\">\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"55\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; line-height:1.54; text-align:left; color:#666666; font-size:14px\" width=\"300\">\n" +
                "<b>Estimado cliente</b></td>\n" +
                "<td width=\"15\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"16\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr height=\"18\">\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"55\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; line-height:1.13; font-size:16px; text-align:left; color:#666666\" width=\"278\">\n" +
                "Se ha realizado la siguiente <b>operación</b></td>\n" +
                "<td width=\"37\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"8\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr height=\"20\">\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"55\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:18px; text-align:left; color:#666666\" width=\"289\">\n" +
                "<b>Compra de boletos</b></td>\n" +
                "<td width=\"26\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"2\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr height=\"25\">\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"60\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:22px; text-align:left; color:#666666\" width=\"298\">\n" +
                "<b>"+boletos+"</b> Boleto(s)</td>\n" +
                "<td width=\"42\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"1\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr height=\"16\">\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"55\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:14px; text-align:left; color:#666666\" width=\"301\">\n" +
                "<b>Combu-Express "+estacion+"</b></td>\n" +
                "<td width=\"14\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"7\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:10px; line-height:2; text-align:right; color:#8c8c8c\" width=\"210\" height=\"20\">\n" +
                "Abr 03, 2019 13 :18:57 PM</td>\n" +
                "<td width=\"20\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"80\">\n" +
                "<td></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td><img data-imagetype=\"External\" src=\"" +
                "\" originalsrc=\"http://189.206.183.110:1390/cecg_app/promo-02-.jpg\" data-connectorsauthtoken=\"1\" data-imageproxyendpoint=\"/actions/ei\" data-imageproxyid=\"\" width=\"175\" height=\"110\"></td>\n" +
                "<td width=\"55\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"20\">\n" +
                "<td colspan=\"5\"></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td colspan=\"2\">\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td width=\"75\"></td>\n" +
                "<td>\n" +
                "<b>"+mensaje+"</b></td>\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" bgcolor=\"#f7f7f7\">\n" +
                "<tbody>\n" +
                "<tr style=\"font:ArialMT; font-family:Arial; font-size:14px; line-height:2; color:#666666\" height=\"29\">\n" +
                "<td style=\"border-top:1px solid #e2e2e2; border-bottom:1px solid #e2e2e2; border-left:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"right\">\n" +
                "Total </td>\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2\" width=\"22\">\n" +
                "</td>\n" +
                "<td style=\"border-top:1px solid #e2e2e2; border-bottom:1px solid #e2e2e2; border-right:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"left\">\n" +
                "<b>"+precio+"</b></td>\n" +
                "</tr>\n" +
                "<tr style=\"font:ArialMT; font-family:Arial; font-size:14px; line-height:2; color:#666666\" height=\"29\">\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2; border-left:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"right\">\n" +
                "Estatus </td>\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2\" width=\"22\">\n" +
                "</td>\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2; border-right:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"left\">\n" +
                "<b>Exitoso </b></td>\n" +
                "</tr>\n" +
                "<tr style=\"font:ArialMT; font-family:Arial; font-size:14px; line-height:2; color:#666666\" height=\"29\">\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2; border-left:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"right\">\n" +
                "Folios Cinepolis </td>\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2\" width=\"22\">\n" +
                "</td>\n" +
                "<td style=\"border-bottom:1px solid #e2e2e2; border-top:1px solid #e2e2e2; border-right:1px solid #e2e2e2; vertical-align:middle\" width=\"215\" valign=\"middle\" align=\"left\">\n" +
                "<b>"+folios+"</b></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "<td width=\"73\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"20\">\n" +
                "<td colspan=\"5\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "<td width=\"25\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"10\" bgcolor=\"#fafafa\">\n" +
                "<td colspan=\"2\"></td>\n" +
                "</tr>\n" +
                "<tr bgcolor=\"#fafafa\">\n" +
                "<td colspan=\"2\">\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr height=\"10\">\n" +
                "<td colspan=\"3\"></td>\n" +
                "</tr>\n" +
                "<tr height=\"16\">\n" +
                "<td width=\"30\"></td>\n" +
                "<td style=\"font:ArialMT; font-family:Arial; font-size:14px; line-height:1.14; text-align:center; color:#4d4d4d\" width=\"590\">\n" +
                "<b>Gracias por tu preferencia!!!</b></td>\n" +
                "<td width=\"30\"></td>\n" +
                "</tr>\n" +
                "<tr height=\"15\">\n" +
                "<td colspan=\"3\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr bgcolor=\"#fafafa\">\n" +
                "<td colspan=\"2\">\n" +
                "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "<tbody>\n" +
                "<tr>\n" +
                "<td colspan=\"2\">\n" +
                "\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr height=\"15\" bgcolor=\"#fafafa\">\n" +
                "<td colspan=\"2\"></td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<td colspan=\"2\"><img data-imagetype=\"External\" src=\"http://189.206.183.110:1390/cecg_app/footer.png\" originalsrc=\"http://189.206.183.110:1390/cecg_app/footer.png\" data-connectorsauthtoken=\"1\" data-imageproxyendpoint=\"/actions/ei\" data-imageproxyid=\"\" width=\"650\" height=\"78\"></td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</div>\n" +
                "</div>\n" +
                "</div></div>\n" +
                "\n" +
                "\n" +
                "    </body>\n" +
                "</html>\n";
        return r1;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ventaboleto:
                if (subject.getText().length()>0){
                    correo=subject.getText().toString();
                }else {
                    correo="cinepolis@combu-express.com.mx";
                }
                CinepolisBoleto cinepolisBoleto =  CinepolisBoleto.newInstance(this);
                Bundle bundle = new Bundle();
                bundle.putString("subject",correo);
                bundle.putString("boletos","0");
                bundle.putString("precio","0");
                bundle.putString("folio","");
                cinepolisBoleto.setArguments(bundle);
                cinepolisBoleto.show(getFragmentManager(), "dialog");
                break;
                /*
                if (subject.getText().length()>0){
                    correo=subject.getText().toString();
                }else {
                    correo="fabascal@combu-express.com.mx";
                }

                Cinepolis cinepolis = new Cinepolis(ServiciosActivity.this,correo);
                cinepolis.delegate = this;
                cinepolis.execute();
                //sendMessage()*/
        }
    }
    @Override
    public void processFinish(String output){
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
        Log.w("res1",output);

    }
    @SuppressLint("SourceLockedOrientationActivity")
    public void BrandSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("Brand", Context.MODE_PRIVATE);
        switch (sharedPreferences.getString(getResources().getString(R.string.BrandName),"Combu-Express")){
            case "Combu-Express":
                setTheme(R.style.AppTheme);
                setContentView(R.layout.activity_servicios);
                ventaboleto =  findViewById(R.id.ventaboleto);
                ventaboleto.setBackgroundResource(R.drawable.ticketcinecombu);
                break;
            case "Repsol":
                setTheme(R.style.ContentMainRepsol);
                setContentView(R.layout.activity_servicios);
                ventaboleto =  findViewById(R.id.ventaboleto);
                ventaboleto.setBackgroundResource(R.drawable.ticketcinerepsol);
                break;
            case "Ener":
                setTheme(R.style.ContentMainEner);
                setContentView(R.layout.activity_servicios);
                ventaboleto =  findViewById(R.id.ventaboleto);
                ventaboleto.setBackgroundResource(R.drawable.ticketcineener);
                break;
            case "Total":
                setTheme(R.style.ContentMainTotal);
                setContentView(R.layout.activity_servicios);
                ventaboleto =    findViewById(R.id.ventaboleto);
                ventaboleto.setBackgroundResource(R.drawable.ticketcinetotal);
                break;
        }
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }
}
