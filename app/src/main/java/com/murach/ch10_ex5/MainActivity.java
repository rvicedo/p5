package com.murach.ch10_ex5;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView messageTextView;

    Timer timer = new Timer(true);

    private int stopped = 0;

    private SharedPreferences savedValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        messageTextView = (TextView) findViewById(R.id.messageTextView);

        startTimer(0);


        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);

    }
    
    private void startTimer(long elapsed) {
        final long startMillis = System.currentTimeMillis();
        TimerTask task;
        final long stoppedMillis = elapsed * 1000;
        task = new TimerTask() {
            @Override
            public void run() {
                final String FILENAME = "news_feed.xml";
                try {
                    URL url = new URL("http://rss.cnn.com/rss/cnn_tech.rss");
                    InputStream in = url.openStream();

                    FileOutputStream out = openFileOutput(FILENAME, Context.MODE_PRIVATE);

                    byte[] buffer = new byte[1024];
                    int bytesRead = in.read(buffer);
                    while (bytesRead != -1) {
                        out.write(buffer, 0, bytesRead);
                        bytesRead = in.read(buffer);
                    }
                    out.close();
                    in.close();
                }
                catch (IOException e) {
                    Log.e("News reader", e.toString());
                }


                long elapsedMillis = System.currentTimeMillis() - startMillis + stoppedMillis;
                updateView(elapsedMillis);
            }
        };
        timer.schedule(task, 0, 1000); //every second
    }

    private void updateView(final long elapsedMillis) {
        // UI changes need to be run on the UI thread
        messageTextView.post(new Runnable() {

            int elapsedSeconds = (int) elapsedMillis/1000;

            @Override
            public void run() {
                messageTextView.setText("File downloaded " + elapsedSeconds + " time(s)");
            }
        });
    }


    public void startButtonClick(View view) {
        stopped = 0;
        timer = new Timer(true);
        startTimer(getElapsedTime());
    }

    public void stopButtonClick(View view) {
        stopped = 1;
        timer.cancel();
    }

    public int getElapsedTime() {
        String seconds = messageTextView.getText().toString();
        if ("Hello world!".equalsIgnoreCase(seconds)) {
            return 0;
        }
        else {
            seconds = seconds.substring(16, seconds.length()-8);
            int time = Integer.parseInt(seconds);
            return time;
        }
    }


    @Override
    protected void onPause() {
        Editor editor = savedValues.edit();
        String seconds = messageTextView.getText().toString();
        editor.putString("seconds", seconds);
        editor.putInt("stopped", stopped);
        editor.commit();

        //timer.cancel();

        super.onPause();
    }

    @Override
    protected void onResume() {
        String seconds = savedValues.getString("seconds", "0");
        if (!("Hello world!".equalsIgnoreCase(seconds) || "0".equalsIgnoreCase(seconds))) {
            seconds = seconds.substring(16, seconds.length()-8); //removes the "Seconds: " from it

            int stopped = savedValues.getInt("stopped", 0);

            long elapsed = Long.parseLong(seconds);

            timer.cancel();
            timer = new Timer(true);
            startTimer(elapsed);

            if (stopped == 1) {
                //String elapsedString = Long.toString(elapsed);
                updateView(elapsed * 1000);
                timer.cancel();
            }
        }


        super.onResume();
    }

}