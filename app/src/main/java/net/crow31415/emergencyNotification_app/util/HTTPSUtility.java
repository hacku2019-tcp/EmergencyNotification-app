package net.crow31415.emergencyNotification_app.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HTTPSUtility extends AsyncTask<String, Void, String> {

    private String response;

    @Override
    protected String doInBackground(String... strings) {

        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        HttpsURLConnection connection = null;

        try{
            URL url = new URL(strings[0]);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); //Timeout: 5sec
            connection.setReadTimeout(5000);

            // // POST リクエスト実行
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(strings[1]);
            writer.close();
            connection.connect();

            Log.d("HTTPSUtility", "Connect to: " + strings[0] + " post: " + strings[1]);

            // レスポンスコード確認
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP responseCode: " + responseCode);
            }

            //レスポンス取得
            inputStream = connection.getInputStream();
            if(inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(connection != null) {
                connection.disconnect();
            }
        }

        return sb.toString();
    }



    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("HTTPSUtility", "response: " + s);
        response = s;
    }

    public String getResponse() {
        return response;
    }
}
