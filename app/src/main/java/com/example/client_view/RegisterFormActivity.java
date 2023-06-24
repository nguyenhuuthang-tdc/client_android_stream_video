package com.example.client_view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;

public class RegisterFormActivity extends AppCompatActivity {
    //
    public static String server_ip = "192.168.1.7";
    EditText edtDeviceName;
    Button submitBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);
        edtDeviceName = findViewById(R.id.edtDeviceName);
        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setEnabled(true);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                submitBtn.setEnabled(false);
                btnSubmitClick();
            }
        });
    }
    //
    public void btnSubmitClick() {
        String device_name = edtDeviceName.getText().toString();
        if (!device_name.matches("")) {
            new registerToken().execute(device_name);
            return;
        }
       submitBtn.setEnabled(true);
    }
    //
    class registerToken extends AsyncTask<String, Void, ArrayList<String>> {
        public ArrayList<String> list = new ArrayList<>();
        //
        public ArrayList<String> doInBackground(String... strings) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://" + server_ip + "/admin/check_device.php").openConnection();
                httpURLConnection.setConnectTimeout(1000);
                httpURLConnection.setReadTimeout(1000);
                httpURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputStreamWriter.write(URLEncoder.encode("device_name", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8"));
                outputStreamWriter.flush();
                list.add(new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream())).readLine());
                list.add(strings[0]);
                return list;
            } catch (Exception e) {
                e.printStackTrace();
                return list;
            }
        }
        //
        public void onPostExecute(ArrayList<String> arrayList) {
            super.onPostExecute(arrayList);
            list = arrayList;
            if (arrayList.isEmpty()) {
                edtDeviceName.setText("");
                submitBtn.setEnabled(true);
                Toast.makeText(RegisterFormActivity.this, "Server Error", Toast.LENGTH_LONG).show();
                return;
            }
            OkHttpClient okHttpClient = new OkHttpClient();
            Request build = new Request.Builder().url("ws://" + server_ip + ":8080").build();
            RegisterFormActivity registerFormActivity = RegisterFormActivity.this;
            new SocketListener(registerFormActivity);
            okHttpClient.newCall(build).enqueue(new Callback() {
                public void onFailure(Call call, IOException iOException) {
                    RegisterFormActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            edtDeviceName.setText("");
                            submitBtn.setEnabled(true);
                            Toast.makeText(RegisterFormActivity.this, "Server Socket not responding", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                //
                public void onResponse(Call call, Response response) throws IOException {
                    String str = list.get(0);
                    String str2 = list.get(1);
                    try {
                        final JSONObject jSONObject = new JSONObject(str);
                        if (jSONObject.getString("response").equals("success")) {
                            try {
                                File file = new File(new File(Environment.getDataDirectory().getPath() + "/data/com.example.client_view/"), "data.txt");
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                                byte[] bytes = str2.getBytes();
                                fileOutputStream.write(bytes, 0, bytes.length);
                                fileOutputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RegisterFormActivity.this.startActivity(new Intent(RegisterFormActivity.this, MainActivity.class));
                            return;
                        }
                        RegisterFormActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                edtDeviceName.setText("");
                                submitBtn.setEnabled(true);
                                try {
                                    Toast.makeText(RegisterFormActivity.this, jSONObject.getString("message"), Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            });
        }
    }
    //
    public class SocketListener extends WebSocketListener {
        public RegisterFormActivity activity;

        public SocketListener(RegisterFormActivity registerFormActivity) {
            this.activity = registerFormActivity;
        }
    }
}