package work.seenow.seenow.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import work.seenow.seenow.R;
import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.User;


public class CameraFragment extends Fragment {
    // LogCat tag
    private static final String TAG = CameraFragment.class.getSimpleName();
    // Camera activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int STORAGE_IMAGE_CHOOSE_REQUEST_CODE = 200;
    private Uri fileUri; // file url to store image
    private Bitmap forUpload; // Bitmap for Upload
    private Button btnCapturePicture, btnExistingPicture, uploadButton;
    private AutoCompleteTextView predictLabel;
    private TextView textView1;
    private ImageView imagePreview;
    private ExifInterface exifObject;
    private User user;
    private ArrayList<String> listNames;
    private ArrayList<Integer> listNames_uids;
    ArrayAdapter<String> adapter;
    private EditText description, visibility;


    public static CameraFragment newInstance(User user) {
        CameraFragment fragment = new CameraFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",user);
        fragment.setArguments(bundle);

        return fragment;
    }

    private void getSugestions(final String s) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ACTIONS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                try {
                    listNames.clear();
                    listNames_uids.clear();
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        /** Ok */
                        JSONArray sArray = jObj.getJSONArray("suggests");
                        Log.d(TAG, "AICI: "+response.toString());
                        for (int i = 0; i < sArray.length(); i++) {

                            JSONObject sObj = (JSONObject) sArray.get(i);
                            listNames.add(sObj.getString("fullname"));
                            listNames_uids.add(sObj.getInt("id"));
                        }

                    } else {
                        listNames.add("unknown");
                        listNames_uids.add(-1);
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                    }
                    adapter.clear();
                    String[] stringArray = listNames.toArray(new String[0]);
                    Log.d(TAG, "Sunt aici si am string: "+ Arrays.toString(stringArray));
                    adapter.addAll(stringArray);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                listNames.add("unknown");
                listNames_uids.add(-1);
                adapter.clear();
                String[] stringArray = listNames.toArray(new String[0]);
                adapter.addAll(stringArray);
                adapter.notifyDataSetChanged();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("action_type", "getSuggestions");
                params.put("suggest",s);

                return params;
            }

        };


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "get_feed");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_camera, container, false);
        listNames = new ArrayList<String>();
        listNames_uids = new ArrayList<Integer>();
        user = (User) getArguments().getSerializable("user");
        btnCapturePicture = (Button) view.findViewById(R.id.NewPictureButton);
        btnExistingPicture = (Button) view.findViewById(R.id.ExistingPictureButton);
        uploadButton = (Button) view.findViewById(R.id.buttonUpload);

        visibility = (EditText) view.findViewById(R.id.visibility);
        textView1 = (TextView) view.findViewById(R.id.predictLabel);
        predictLabel = (AutoCompleteTextView) view.findViewById(R.id.editText1);
        String[] stringArray = listNames.toArray(new String[0]);
        adapter = new ArrayAdapter<String> (getActivity().getApplicationContext(),
                R.layout.simple_dropdown_item_1line, stringArray);
        predictLabel.setAdapter(adapter);
        description = (EditText) view.findViewById(R.id.description);
        predictLabel.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getSugestions(predictLabel.getText().toString());
            }
        });

        imagePreview = (ImageView) view.findViewById(R.id.ImageView);

        /** open image chooser */
        btnExistingPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, STORAGE_IMAGE_CHOOSE_REQUEST_CODE);
            }
        });

        /**         * Capture image button click event */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                if (isStoragePermissionGranted()) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    fileUri = getOutputMediaFileUri();

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    // start the image capture Intent
                   startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String local_description = description.getText().toString();
                if(local_description.isEmpty())
                {
                    local_description = "";
                }

                int local_foundUser;
                if(listNames.contains(predictLabel.getText().toString()))
                {
                    local_foundUser=listNames_uids.get(listNames.indexOf(predictLabel.getText().toString()));
                }
                else
                {
                    local_foundUser = -1;
                }

                String visibility_local = visibility.getText().toString();
                if(!visibility_local.equals("v") || (!visibility_local.equals("i"))){
                    visibility_local = "v";
                }
                Log.d(TAG,"Am datele: "+local_description+ ".si."+local_foundUser+".si."+visibility_local+".si."+user.getId());
                uploadBitmap(local_description, local_foundUser, visibility_local);

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
                getView().findViewById(R.id.visibility).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.description).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.editText1).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.predictLabel).setVisibility(View.VISIBLE);
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
        if (requestCode == STORAGE_IMAGE_CHOOSE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            //getting the image Uri
            fileUri = data.getData();
            try {
                forUpload = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fileUri);
                Log.d(TAG, "AICI si uri: " + fileUri.getPath());
                imagePreview.setImageBitmap(forUpload);
                imagePreview.setVisibility(View.VISIBLE);
                /** Make the upload button visible */
                getView().findViewById(R.id.buttonUpload).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.visibility).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.description).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.editText1).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.predictLabel).setVisibility(View.VISIBLE);

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


    private void modifyTables(final String description, final int predicted, final String visibility, final String fileName) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"Raspuns"+response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Raspuns"+error.toString());

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("visibility",visibility);
                params.put("id",((Integer)user.getId()).toString());
                params.put("useRecognizer",user.getUseRecognizer());
                if(!(description.equals(""))){
                    params.put("description",description);
                }
                if(!(predicted == -1)){
                    params.put("predicted",((Integer)predicted).toString());
                }
                params.put("filename",fileName);
                return params;
            }

        };

        /** After 3 minutes is timeout */
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(180000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(stringRequest, "upload_image");

    }

    private void uploadBitmap(final String description, final int predicted, final String visibility) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_UPLOAD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"Raspuns"+response);
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

//                     Check for error node in json
                    if (!error) {
                        Log.d(TAG,"Successful uploaded");
                        String filename = jObj.getString("filename");
                        modifyTables(description, predicted, visibility, filename);
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Raspuns"+error.toString());

            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                String imageData = imageToString(forUpload);
                params.put("image",imageData);
                params.put("upload_only","yes");
                return params;
            }

        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(stringRequest, "upload_image");

    }

    private String imageToString(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes,Base64.DEFAULT);
        return encodedImage;
    }

}