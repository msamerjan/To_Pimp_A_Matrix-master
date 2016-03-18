package com.mobiledev.topimpamatrix.Acitivities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maiaphoebedylansamerjan on 3/17/16.
 */
public class CameraMainAcitivity extends Activity {
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private String mImageFullPathAndName = "";
    private String localImagePath = "";
    private static final int OPTIMIZED_LENGTH = 1024;

    private  final  String idol_ocr_service = "https://api.idolondemand.com/1/api/async/ocrdocument/v1?";
    private  final  String idol_ocr_job_result = "https://api.idolondemand.com/1/job/result/";
    private String jobID = "";

    ImageView ivSelectedImg;
    EditText edTextResult;
    LinearLayout llResultContainer;
    LinearLayout llCameraButtons;
    LinearLayout llOperations;
    ProgressBar pbOCRReconizing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivSelectedImg = (ImageView) findViewById(R.id.imageView);
        llCameraButtons = (LinearLayout) findViewById(R.id.llcamerabuttons);
        llOperations = (LinearLayout) findViewById(R.id.lloptions);

        edTextResult = (EditText) findViewById(R.id.etresult);
        llResultContainer = (LinearLayout) findViewById(R.id.llresultcontainer);
        pbOCRReconizing = (ProgressBar) findViewById(R.id.pbocrrecognizing);

        CreateLocalImageFolder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (commsEngine == null)
            commsEngine = new CommsEngine();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }
    @Override public void onBackPressed() {
        if (llResultContainer.getVisibility() == View.VISIBLE) {
            llResultContainer.setVisibility(View.GONE);
            ivSelectedImg.setVisibility(View.VISIBLE);
            return;
        } else
            finish();
    }
    public void DoStartOCR(View v) {
        pbOCRReconizing.setVisibility(View.VISIBLE);
        if (jobID.length() > 0)
            getResultByJobId();
        else if (!mImageFullPathAndName.isEmpty()){
            Map<String,String> map =  new HashMap<String,String>();
            map.put("file", mImageFullPathAndName);
            String fileType = "image/jpeg";
            map.put("mode", "document_photo");
            commsEngine.ServicePostRequest(idol_ocr_service, fileType, map, new OnServerRequestCompleteListener() {
                @Override
                public void onServerRequestComplete(String response) {
                    try {
                        JSONObject mainObject = new JSONObject(response);
                        if (!mainObject.isNull("jobID")) {
                            jobID = mainObject.getString("jobID");
                            getResultByJobId();
                        } else
                            ParseSyncResponse(response);
                    } catch (Exception ex) {}
                }
                @Override
                public void onErrorOccurred(String error) {
                    // handle error
                }
            });
        } else
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_LONG).show();
    }
    private void getResultByJobId() {
        String param = idol_ocr_job_result + jobID + "?";
        commsEngine.ServiceGetRequest(param, "", new
                OnServerRequestCompleteListener() {
                    @Override
                    public void onServerRequestComplete(String response) {
                        ParseAsyncResponse(response);
                    }
                    @Override
                    public void onErrorOccurred(String error) {
                        // handle error
                    }
                });
    }

    public void DoCloseResult(View v) {
        ivSelectedImg.setVisibility(View.VISIBLE);
        llResultContainer.setVisibility(View.GONE);
    }
    private void ParseSyncResponse(String response) {
        pbOCRReconizing.setVisibility(View.GONE);
        if (response == null) {
            Toast.makeText(this, "Unknown error occurred. Try again", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray textBlockArray = mainObject.getJSONArray("text_block");
            int count = textBlockArray.length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    JSONObject texts = textBlockArray.getJSONObject(i);
                    String text = texts.getString("text");
                    ivSelectedImg.setVisibility(View.GONE);
                    llResultContainer.setVisibility(View.VISIBLE);
                    edTextResult.setText(text);
                }
            }
            else
                Toast.makeText(this, "Not available", Toast.LENGTH_LONG).show();
        } catch (Exception ex){}
    }
    private void ParseAsyncResponse(String response) {
        pbOCRReconizing.setVisibility(View.GONE);
        if (response == null) {
            Toast.makeText(this, "Unknown error occurred. Try again", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            JSONObject mainObject = new JSONObject(response);
            JSONArray textBlockArray = mainObject.getJSONArray("actions");
            int count = textBlockArray.length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    JSONObject actions = textBlockArray.getJSONObject(i);
                    String action = actions.getString("action");
                    String status = actions.getString("status");
                    JSONObject result = actions.getJSONObject("result");
                    JSONArray textArray = result.getJSONArray("text_block");
                    count = textArray.length();
                    if (count > 0) {
                        for (int n = 0; n < count; n++) {
                            JSONObject texts = textArray.getJSONObject(n);
                            String text = texts.getString("text");
                            ivSelectedImg.setVisibility(View.GONE);
                            llResultContainer.setVisibility(View.VISIBLE);
                            edTextResult.setText(text);
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Not available", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
        }
    }
    public void CreateLocalImageFolder()
    {
        if (localImagePath.length() == 0)
        {
            localImagePath = getFilesDir().getAbsolutePath() + "/orc/";
            File folder = new File(localImagePath);
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (!success)
                Toast.makeText(this, "Cannot create local folder", Toast.LENGTH_LONG).show();
        }
    }
    public Bitmap decodeFile(File file) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        int mImageRealWidth = options.outWidth;
        int mImageRealHeight = options.outHeight;
        Bitmap pic = null;
        try {
            pic = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception ex) {
            Log.e("MainActivity", ex.getMessage());
        }
        return pic;
    }
    public Bitmap rescaleBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
    private Bitmap rotateBitmap(Bitmap pic, int deg) {
        Matrix rotate90DegAntiClock = new Matrix();
        rotate90DegAntiClock.preRotate(deg);
        Bitmap newPic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), rotate90DegAntiClock, true);
        return newPic;
    }

}
