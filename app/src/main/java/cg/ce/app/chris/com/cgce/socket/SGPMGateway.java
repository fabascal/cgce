package cg.ce.app.chris.com.cgce.socket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import cg.ce.app.chris.com.cgce.listeners.StringListener;


public class SGPMGateway extends AsyncTask<JSONObject, String, String> {
    String SERVER_IP;
    int SERVER_PORT;
    String MESSAGE;
    String response="false";
    public StringListener delegate=null;
    private ProgressDialog mProgressDialog;
    private WeakReference<Activity> mActivity;
    private Context mContext;


    public SGPMGateway (Activity activity, Context context, JSONObject jsonObject) throws JSONException {
        mActivity = new WeakReference<Activity>(activity);
        this.mContext = context;
        SERVER_IP = jsonObject.getString("ip");
        SERVER_PORT = jsonObject.getInt("port");
        MESSAGE = jsonObject.getString("message");
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(false);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("CombuGo");
        // Progress dialog message
        mProgressDialog.setMessage("Registrando venta...");
    }
    @Override
    protected void onPreExecute() {
        mProgressDialog.show();
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
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mProgressDialog!= null){
            mProgressDialog.dismiss();
        }
        super.onPostExecute(result);
        delegate.processFinish(result);
    }

}
