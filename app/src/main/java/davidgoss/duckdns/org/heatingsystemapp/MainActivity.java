package davidgoss.duckdns.org.heatingsystemapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.VelocityTracker;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {
    private TextView avaTempOutputTextView;
    private TextView hallwayTempOutputTextView;
    private EditText desTempTextView;
    private URL avaUrl;
    private URL hallwayUrl;
    private URL desUrl;
    private URL onUrl;
    private Button desiredTempSubmitBtn;
    private URL emptyUrl;
    private ImageView progressImageView;
    private float angle = 0;
    WebView fireGif;
    ImageView arrowImageView;
    ImageView arrowRedImageView;
    private int arrowSendYesCount = 0;
    private tempLoop myThreadr;
    float dX;
    float dY;
    private VelocityTracker velocityTracker = null;
    float maxXVelocity;
    float maxYVelocity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        avaTempOutputTextView = findViewById(R.id.avaTempOutputTextView);
        hallwayTempOutputTextView = findViewById(R.id.hallwayTempOutputTextView);
        desTempTextView = findViewById(R.id.desTempTextView);
        desiredTempSubmitBtn = findViewById(R.id.desTempSubBtn);
        progressImageView = findViewById(R.id.workingImageView);
        fireGif = findViewById(R.id.gifWebView);
        fireGif.loadUrl("file:///android_asset/fire.html");
        fireGif.setVisibility(View.VISIBLE);
        arrowImageView = findViewById(R.id.arriwImageView);
        arrowImageView.setVisibility(View.INVISIBLE);
        arrowRedImageView = findViewById(R.id.arrowDownImageView);
        try {
            avaUrl = new URL("http://www.davidgoss.duckdns.org/text/ava_temp.txt");
            hallwayUrl = new URL("http://www.davidgoss.duckdns.org/text/hallway_temp.txt");
            desUrl = new URL("http://www.davidgoss.duckdns.org/text/temp.txt");
            onUrl = new URL("http://www.davidgoss.duckdns.org/text/on_off.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getTemps get = new getTemps();
        get.execute(avaUrl, hallwayUrl, desUrl, onUrl);

        myThreadr = new tempLoop();
        Thread myThread = new Thread(myThreadr);
        myThread.start();



        desiredTempSubmitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String des = desTempTextView.getText().toString();
                CallAPI sub = new CallAPI();
                sub.execute("http://www.davidgoss.duckdns.org/php/form.php", des);


            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        myThreadr.onPause();

    }
    @Override
    protected void onResume() {
        super.onResume();
        myThreadr.onResume();


    }


    private class getTemps extends AsyncTask<URL, String, String> {

        private String avaTemp;
        private String hallwaytemp;
        private String desTemp;
        private String on_off;


        @Override
        protected String doInBackground(URL... urls) {


            try {
                Scanner scanner1 = new Scanner(urls[0].openStream());
                Scanner scanner2 = new Scanner(urls[1].openStream());
                Scanner scanner4 = new Scanner(urls[3].openStream());
                if (!(urls[2] == emptyUrl)) {
                    Scanner scanner3 = new Scanner(urls[2].openStream());
                    desTemp = scanner3.next();

                } else {
                    desTemp = "";
                }
                avaTemp = scanner1.next();
                hallwaytemp = scanner2.next();
                on_off = scanner4.next();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            publishProgress(avaTemp, hallwaytemp, desTemp, on_off);

            return avaTemp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {
            avaTempOutputTextView.setText(text[0]);
            hallwayTempOutputTextView.setText(text[1]);
            if (!(text[2] == "")) {
                desTempTextView.setText(text[2]);
            }
            if (new String(text[3]).equals("run")) {

                fireGif.loadUrl("file:///android_asset/fire.html");
            } else {
                fireGif.loadUrl("file:///android_asset/right.html");
            }

        }
    }

    public class CallAPI extends AsyncTask<String, String, String> {

        public CallAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0]; // URL to call

            String data = "addition=" + params[1]; //data to post

            OutputStream out = null;
            try {

                URL obj = new URL(urlString);


                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                // For POST only - START
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();
                // For POST only - END

                int responseCode = con.getResponseCode();
                System.out.println("POST Response Code :: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // print result
                    System.out.println(response.toString());
                    publishProgress("sent");


                } else {
                    System.out.println("POST request not worked");

                }


            } catch (Exception e) {

                System.out.println(e.getMessage());


            }

            return "";
        }

        @Override
        protected void onProgressUpdate(String... text) {
            if (text[0].equals("sent")) {
                arrowImageView.setVisibility(View.VISIBLE);
                arrowSendYesCount = 1;
            } else {
                arrowRedImageView.setVisibility(View.VISIBLE);
            }
        }

    }

    class tempLoop implements Runnable {
        private Object mPauseLock;
        private boolean mPaused;
        private boolean mFinished;

        public tempLoop() {
            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;
        }

        public void run() {
            while (!mFinished) {
                if (arrowSendYesCount > 0) {
                    arrowSendYesCount += 1;
                }
                if (arrowSendYesCount >= 3) {
                    arrowSendYesCount = 0;
                    arrowImageView.setVisibility(View.INVISIBLE);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                angle += 30;
                getTemps get = new getTemps();
                get.execute(avaUrl, hallwayUrl, emptyUrl, onUrl);
                progressImageView.setRotation(angle);

                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
        /**
         * Call this on pause.
         */
        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        /**
         * Call this on resume.
         */
        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }


    }

}