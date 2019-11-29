package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.Result;

import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private List<AceiteList> aceiteLists;
    private  Integer camera = 2;
    String js=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.containsKey("camera")){
                camera = extras.getInt("camera");
            }
            if(extras.containsKey("js")){
                js=extras.getString("js");
            }
        }

        /*Intent intent = getIntent();
        if (intent.getBundleExtra("data")!=null) {
            Bundle args = intent.getBundleExtra("data");
            aceiteLists = (ArrayList<AceiteList>) args.getSerializable("array");
            Log.w("Array", aceiteLists.toString());
        }*/


        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setFlash(false);
        mScannerView.setAutoFocus(true);
        mScannerView.startCamera(camera);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    @Override
    public void handleResult(Result rawResult) {
        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(BarcodeScanner.this);
        Intent intent= new Intent (BarcodeScanner.this,AceiteActivity.class);
        intent.putExtra("barcode",rawResult.getText());
        intent.putExtra("js",js);
        startActivity(intent);
    }
}
