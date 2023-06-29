package com.example.client_view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {
    private static String server_ip = "192.168.1.6";
    private DownloadFileFromURL aST;
    Context context;
    public VideoView videoView;
    //
    public String device_name;
    //
    public int currentPosition;
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        videoView = findViewById(R.id.video_view);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });
        device_name = getDeviceName();
        if (!device_name.equals("")) {
            initialWebSocket(device_name);
            //
            String file_url = "http://" + server_ip + "/admin/get_video.php?device_name=" + device_name;
            String reload_url = "http://" + server_ip + "/admin/update_reload.php?device_name=" + device_name;
            aST = new DownloadFileFromURL();
            aST.execute(file_url, reload_url);
        }
    }
    //
    @Override
    protected void onPause() {
        super.onPause();
        currentPosition = videoView.getCurrentPosition();
        videoView.pause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(currentPosition);
        videoView.start();
    }
    //
    public String getDeviceName() {
        try {
            String root = Environment.getDataDirectory().getPath() + "/data/com.example.client_view/";
            File myDir = new File(root);
            //
            String fileName = "data" + ".txt";
            File file = new File(myDir, fileName);
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                String deviceName = "";
                int len;
                byte buff[] = new byte[1024];
                while ((len = inputStream.read(buff)) > 0 ) {
                    deviceName += new String(buff, 0, len);
                }

                inputStream.close();
                return deviceName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }
    //
    public class DownloadFileFromURL extends AsyncTask<String, String, Void> {
        @Override
        public Void doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                // error here
                HttpURLConnection http = (HttpURLConnection)url.openConnection();
                http.setConnectTimeout(2000);
                http.setReadTimeout(2000);
                //
                int statusCode = http.getResponseCode();
                if(statusCode != 200) {
                    return null;
                }
                //
                InputStream input = new BufferedInputStream(url.openStream(),8192);
                //
                String root = Environment.getDataDirectory().getPath() + "/data/com.example.client_view/";
                File myDir = new File(root);
                //
                String fname = "video" + ".mp4";
                File file = new File(myDir, fname);
                if (file.exists())
                    file.delete();

                FileOutputStream output = new FileOutputStream(file, true);
                byte data[] = new byte[1024];
                while ((count = input.read(data)) != -1) {
                    // writing data to file
                    output.write(data, 0, count);
                }
                // flushing output
                output.flush();
                // closing streams
                output.close();
                input.close();
                //set reload
                URL url_reload = new URL(f_url[1]);
                HttpURLConnection http_reload = (HttpURLConnection)url_reload.openConnection();
                int statusCode_reload = http_reload.getResponseCode();
                if(statusCode_reload != 200) {
                    return null;
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
            return null;
        }
        //
        @Override
        public void onPostExecute(Void result) {
            super.onPostExecute(result);
            String root = Environment.getDataDirectory().getPath() + "/data/com.example.client_view/video.mp4";
            File file = new File(root);
            if (file.exists()) {
                videoView.setVideoPath(root);
                videoView.start();
            }
        }
    }
    //
    private void initialWebSocket(String device_name) {
        this.webSocket = new OkHttpClient().newWebSocket(new Request.Builder().url("ws://" + server_ip + ":8080").build(), new SocketListener(this, device_name));
    }
    public class SocketListener extends WebSocketListener {
        public MainActivity activity;
        public String device_name;

        public SocketListener(MainActivity mainActivity, String device_name) {
            this.activity = mainActivity;
            this.device_name = device_name;
        }

        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("from", "device");
                jSONObject.put("data", this.device_name);
                //
                webSocket.send(jSONObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public void onMessage(WebSocket webSocket, String str) {
            super.onMessage(webSocket, str);
            String file_url = "http://" + server_ip + "/admin/get_video.php?device_name=" + device_name;
            String reload_url = "http://" + server_ip + "/admin/update_reload.php?device_name=" + device_name;
            aST = new DownloadFileFromURL();
            aST.execute(file_url, reload_url);
        }
    }
}