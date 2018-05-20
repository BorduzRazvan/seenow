package work.seenow.seenow.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import work.seenow.seenow.R;
import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.VolleyMultipartRequest;

public class CameraFragment extends Fragment {
    // LogCat tag
    private static final String TAG = CameraFragment.class.getSimpleName();
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private Uri fileUri; // file url to store image
    private Bitmap forUpload; // Bitmap for Upload
    private Button btnCapturePicture, btnUploadPicture, uploadButton;
    private EditText predictLabel;
    private ImageView imagePreview;
    private ExifInterface exifObject;

    // newInstance constructor for creating fragment with arguments
    public static CameraFragment newInstance(int page, String title) {
        CameraFragment fragmentFirst = new CameraFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    public CameraFragment() {
        // Required empty public constructor
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_camera, container, false);

        btnCapturePicture = (Button) view.findViewById(R.id.NewPictureButton);
        btnUploadPicture = (Button) view.findViewById(R.id.ExistingPictureButton);
        uploadButton = (Button) view.findViewById(R.id.buttonUpload);

        predictLabel = (EditText) view.findViewById(R.id.predictLabel);
        imagePreview = (ImageView) view.findViewById(R.id.ImageView);

        /** open image chooser */
        btnUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 100);
            }
        });
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);

        /**         * Capture image button click event */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                if (isStoragePermissionGranted()) {
                    captureImage();
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBitmap(forUpload);
            }
        });


        // Checking camera availability
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
        }
        return view;
    }



    /**
     * Checking device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        if (getActivity().getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        getActivity().startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        outState.putParcelable("file_uri", fileUri);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Show the image into imageview to preview it
        if (fileUri != null) {
            File imgFile = new File(fileUri.getPath());

            if (imgFile.isFile()) {

                try {
                    exifObject = new ExifInterface(fileUri.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                int orientation = exifObject.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                forUpload = rotateBitmap(myBitmap,orientation);
                Log.d(TAG, "Aici");
                imagePreview.setImageBitmap(forUpload);
                imagePreview.setVisibility(View.VISIBLE);
                /** Make the upload button visible */
                getView().findViewById(R.id.buttonUpload).setVisibility(View.VISIBLE);
                predictLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            fileUri = savedInstanceState.getParcelable("file_uri");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Aici");
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {

            //getting the image Uri
            fileUri = data.getData();
            try {
                forUpload = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fileUri);
                Log.d(TAG, "AICI si uri: " + fileUri.getPath());
                imagePreview.setImageBitmap(forUpload);
                imagePreview.setVisibility(View.VISIBLE);
                /** Make the upload button visible */
                getView().findViewById(R.id.buttonUpload).setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * returning image
     */
    private static File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                AppConfig.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + AppConfig.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }


    /** Check for storage permission */
    public  boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= 23) {
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }

            if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    /** Before the review of image rotate it */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (bitmap != null && !bitmap.isRecycled()) {
                //  bitmap.recycle();
            }
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private void uploadBitmap(final Bitmap bitmap) {

        //getting the tag from the edittext
        final String author="Author";

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, AppConfig.UPLOAD_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getActivity().getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("author", author);
                String predictedLabel_string = predictLabel.getText().toString();
                if(!predictedLabel_string.isEmpty()) {
                    params.put("predictedLabel", predictLabel.getText().toString());
                }

                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("pic", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(getContext()).add(volleyMultipartRequest);
    }

    /*
     * The method is taking Bitmap as an argument
     * then it will return the byte[] array for the given bitmap
     * and we will send this array to the server
     * here we are using PNG Compression with 80% quality
     * you can give quality between 0 to 100
     * 0 means worse quality
     * 100 means best quality
     * */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


}