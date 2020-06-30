package cg.ce.app.chris.com.cgce.socket;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.SorteoListener;
import cg.ce.app.chris.com.cgce.VentaActivity;
import cg.ce.app.chris.com.cgce.listeners.StringListener;
import cg.ce.app.chris.com.cgce.listeners.ValesListener;

import static java.lang.Thread.sleep;

public class SGPMGateway extends AsyncTask<JSONObject, String, String> {
    String SERVER_IP;
    int SERVER_PORT;
    String MESSAGE;
    String response="false";
    public StringListener delegate=null;


    public SGPMGateway (JSONObject jsonObject) throws JSONException {
        SERVER_IP = jsonObject.getString("ip");
        SERVER_PORT = jsonObject.getInt("port");
        MESSAGE = jsonObject.getString("message");

    }

    @Override
    protected String doInBackground(JSONObject... jsonObjects) {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(MESSAGE);
            bw.flush();
            System.out.println("Message sent to the server : "+MESSAGE);
            ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = socket.getInputStream();
            do{
                response="";
                bytesRead = inputStream.read(buffer);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }while (inputStream.available()>0);
            os.close();
            osw.close();
            bw.close();
            socket.close();
        } catch (IOException e) {
           response="Info|0|" + String.valueOf(e);
            e.printStackTrace();
        }
       /* int waited = 0;
        while (waited < 1500) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waited += 100;
        }*/
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("Gateway response");
        System.out.println(result);
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

}
