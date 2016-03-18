package com.mobiledev.topimpamatrix;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

/**
 * Created by maiaphoebedylansamerjan on 3/17/16.
 */
public class CameraReader {
    private final String apiKey="3d81dbc-90b2-48a1-ae42-73b1b5531aee";

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private String mImageFullPathAndName = "";

    // function called from a load image button click event
    public void DoShowImagePicker (View v) {
        Intent intent = new Intent (Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult (intent, SELECT_PICTURE);
    }

    // function called from a launch camera button click event
    public void DoTakePhoto(View view) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE ||
                requestCode == SELECT_PICTURE) {
            if (resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
// get the selected image full path and name
                mImageFullPathAndName = cursor.getString(columnIndex);
                cursor.close();
            }
        }
    }
}
