package cg.ce.app.chris.com.cgce;

import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AutoUpdate extends AppCompatActivity implements View.OnClickListener{
    private VersionChecker mVC = new VersionChecker();
    ImageButton btnactualizar;
    TextView etcurrentversioncode,etlatestversioncode,tvactualizacion;
    String msj;
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_update);
        webview = (WebView)findViewById(R.id.web_descarga);
        new update().execute();
        etcurrentversioncode=(TextView)findViewById(R.id.etcurrentversioncode);

        etlatestversioncode=(TextView)findViewById(R.id.etlatestversioncode);

        tvactualizacion=(TextView)findViewById(R.id.tvactualizacion);

        btnactualizar=(ImageButton)findViewById(R.id.btnactualizar);
        btnactualizar.setOnClickListener(AutoUpdate.this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnactualizar:
                final Activity activity = this;
                webview.setWebChromeClient(new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        // Activities and WebViews measure progress with different scales.
                        // The progress meter will automatically disappear when we reach 100%
                        activity.setProgress(progress * 1000);
                    }
                });
                webview.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Toast.makeText(activity, "Problems with server " + description, Toast.LENGTH_SHORT).show();
                    }
                });

                webview.loadUrl(mVC.getDownloadURL());

                //descargar archivo
                webview.setDownloadListener(new DownloadListener()
                {
                    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                        //for downloading directly through download manager
                        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.allowScanningByMediaScanner();

                        request.setMimeType(mimetype);
                        //------------------------COOKIE------------------------
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        //------------------------COOKIE------------------------
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading file...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                        final DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                        new Thread("Browser download") {
                            public void run() {
                                dm.enqueue(request);
                            }
                        }.start();
                    }
                });
                //startActivity(new Intent("android.intent.action.VIEW", Uri.parse(mVC.getDownloadURL())));
                break;
        }
    }
    public class update extends AsyncTask <String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            mVC.getData(AutoUpdate.this);
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.w("current", String.valueOf(mVC.getCurrentVersionCode()));
            etcurrentversioncode.setText(mVC.getCurrentVersionName());
            Log.w("lates", String.valueOf(mVC.getLatestVersionCode()));
            etlatestversioncode.setText(mVC.getLatestVersionName());
            Log.w("newversion", String.valueOf(mVC.isNewVersionAvailable()));
            if (mVC.isNewVersionAvailable()){
                btnactualizar.setVisibility(View.VISIBLE);
                msj="Actualizacion disponible";
            }else{
                msj="Sistema actualizado";
            }
            tvactualizacion.setText(msj);
        }
    }
}
