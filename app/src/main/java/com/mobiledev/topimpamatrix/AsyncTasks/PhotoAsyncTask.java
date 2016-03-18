package com.mobiledev.topimpamatrix.AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

/**
 * Created by maiaphoebedylansamerjan on 3/17/16.
 */
public class PhotoAsyncTask extends AsyncTask<Object, String, String> {
    public static final String TAG=PhotoAsyncTask.class.getString();

    interface OnServerRequestCompleteListener {
        void onServerRequestComplete(String response);
        void onErrorOccurred(String errorMessage);
    }


    private final String apikey = "YOUR API KEY HERE";
    Ht httpClient = null;
    Object httpGet = null;
    Object httpPost = null;
    public enum HTTP_METHOD  { GET, POST };
    public HTTP_METHOD httpMethod = HTTP_METHOD.GET;

    OnServerRequestCompleteListener mListener;

@Override
protected void onPreExecute(){
    super.onPreExecute();
    Log.d(TAG, "UserASyncTask started");
}

    @Override
    protected String doInBackground(Object... params)
    {
        String url = "";
        URI uri;
        if (httpMethod == HTTP_METHOD.GET) {
            url = params[0] + "apikey=" +  apikey;
            url += (String) params[1];
            try {
                uri = new URI(url);
                httpGet.setURI(uri);
            } catch (URISyntaxException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                mListener.onErrorOccurred(e1.getMessage());
            }
            try {
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entity.writeTo(out);
                    out.close();
                    String responseStr = out.toString();
                    return responseStr;
                } else {
                    mListener.onErrorOccurred("Bad response");
                }
            } catch (IOException e) {
                mListener.onErrorOccurred(e.getMessage());
            }
        }
        else if (httpMethod == HTTP_METHOD.POST) {
            try {
                url = (String) params[0];
                uri = new URI(url);
                httpPost.setURI(uri);
                MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();

                reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                Map<String,String> map = (Map) params[2];
                reqEntity.addPart("apikey", new StringBody(apikey, ContentType.TEXT_PLAIN));
                for (Map.Entry<String, String> e : map.entrySet()) {
                    String key = e.getKey();
                    String value = e.getValue();
                    if (key.equals("file")) {
                        ContentType type = ContentType.create((String) params[1], Consts.ISO_8859_1);
                        reqEntity.addBinaryBody("file", new File(value), type, "");
                    } else
                        reqEntity.addPart(key, new StringBody(value, ContentType.TEXT_PLAIN));
                }
                httpPost.setEntity(reqEntity.build());
            } catch (URISyntaxException e) {
                mListener.onErrorOccurred(e.getMessage());
            }
            try {
                HttpResponse response = httpClient.execute(httpPost);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    HttpEntity entities = response.getEntity();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entities.writeTo(out);
                    out.close();
                    String responseStr = out.toString();
                    if (responseStr != null)
                        return responseStr;
                    else
                        mListener.onErrorOccurred("Unknown error");

                } else {
                    mListener.onErrorOccurred("Bad response");
                }
            } catch (IOException e) {
                mListener.onErrorOccurred(e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... unsued) {

    }

    @Override
    protected void onPostExecute(String sResponse) {
        ParseResponse(sResponse);
    }
}
