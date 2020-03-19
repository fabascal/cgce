package cg.ce.app.chris.com.cgce.socket;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SGPMGateway extends AsyncTask<JSONObject, Integer, String> {
    String SERVER_IP;
    int SERVER_PORT;
    String MESSAGE;
    String response="false";
    //ikolj


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

            /*while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
                System.out.println(response);
            }*/

            do{
                response="";
                bytesRead = inputStream.read(buffer);
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }while (inputStream.available()>0);
            System.out.println(response);

            os.close();
            osw.close();
            bw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
