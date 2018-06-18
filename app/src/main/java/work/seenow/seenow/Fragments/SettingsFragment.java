package work.seenow.seenow.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import work.seenow.seenow.MainActivity;
import work.seenow.seenow.R;
import work.seenow.seenow.RegisterActivity;
import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.CircleTransform;
import work.seenow.seenow.Utils.User;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private ImageView imageView;
    private EditText about_ET, new_password_ET, current_password_ET, new_password_confirm_ET, new_email_ET;
    private Switch useRecognizer;
    private Button changeEmail, changePassword, submitChanges;
    private User user;
    private boolean isChangeEmail, isChangePassword;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance(User user) {
        SettingsFragment fragmentFirst = new SettingsFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
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

        View view = inflater.inflate(R.layout.activity_settings, container, false);
        user = (User) getArguments().getSerializable("user");
        imageView = (ImageView) view.findViewById(R.id.ImageView);
        about_ET = (EditText) view.findViewById(R.id.about);
        new_password_confirm_ET = (EditText) view.findViewById(R.id.password_confirmed);
        new_password_ET = (EditText) view.findViewById(R.id.password);
        current_password_ET = (EditText) view.findViewById(R.id.currentPassword);
        new_email_ET = (EditText) view.findViewById(R.id.new_email);
        useRecognizer = (Switch) view.findViewById(R.id.useRecognizer);
        changePassword = (Button) view.findViewById(R.id.changePassword);
        changeEmail = (Button) view.findViewById(R.id.changemail);
        submitChanges = (Button) view.findViewById(R.id.submit_changes);
        isChangePassword = false;
        isChangeEmail = false;
        Glide.with(this).load(user.getProfileImage())
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(getActivity().getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);

        if (user.getUseRecognizer().equals('d')) {
            useRecognizer.setChecked(true);
        } else {
            useRecognizer.setChecked(false);
        }

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChangeEmail = true;
                current_password_ET.setVisibility(View.VISIBLE);
                new_email_ET.setVisibility(View.VISIBLE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChangePassword = true;
                current_password_ET.setVisibility(View.VISIBLE);
                new_password_confirm_ET.setVisibility(View.VISIBLE);
                new_password_ET.setVisibility(View.VISIBLE);
            }
        });

        submitChanges.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                    String new_mail = new_email_ET.getText().toString().trim();
                    String new_pass = new_password_ET.getText().toString().trim();
                    String new_pass_conf = new_password_confirm_ET.getText().toString().trim();
                    String current_password = current_password_ET.getText().toString().trim();
                    String about = about_ET.getText().toString().trim();
                    boolean useRecognizer_b = useRecognizer.isChecked();
                    if(     ((isChangeEmail) && ((new_mail.isEmpty()) || (current_password.isEmpty()))) ||
                            ((isChangePassword) && ((new_pass.isEmpty()) || (new_pass_conf.isEmpty()) || (current_password.isEmpty()) || (!new_pass_conf.equals(new_pass)) || (!RegisterActivity.isPasswordStrong(current_password)))) ||
                            (about.isEmpty()) ||
                            (about.length() > 250)){
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Please enter all the data!", Toast.LENGTH_LONG)
                                .show();
                    }
                    else {
                        Log.d(TAG, "Si am inputuri: "+new_mail+"."+new_pass+"."+new_pass_conf+"."+current_password+"."+about+"."+useRecognizer_b);
                        updateRequired(new_mail, new_pass, new_pass_conf, current_password, about, useRecognizer_b);
                    }
            }
        });
        return view;
    }

    private void updateRequired(final String new_mail, final String new_pass, final String new_pass_conf, final String current_password, final String about, final boolean useRecognizer_b){
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ACTIONS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        /** Ok */
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Update performed successfully", Toast.LENGTH_LONG)
                                .show();
                            user.setAbout(about);
                            if(useRecognizer_b){
                                user.setUseRecognizer("d");
                            }else {
                                user.setUseRecognizer("n");
                            }
                        MainActivity.modifyUser(user);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "There was an error updating your information", Toast.LENGTH_LONG)
                                .show();
                    }

                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "There was an error updating your information", Toast.LENGTH_LONG)
                            .show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());

                Toast.makeText(getActivity().getApplicationContext(),
                        "There was an error updating your information", Toast.LENGTH_LONG)
                        .show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("action_type", "update");
                if(isChangeEmail){
                    params.put("isChangeEmail","yes");
                    params.put("current_password",current_password);
                    params.put("new_email",new_mail);
                }
                if(isChangePassword){
                    if(!isChangeEmail) {
                        params.put("current_password",current_password);
                    }
                    params.put("new_password",new_pass);
                }

                params.put("about",about);
                params.put("user_id",((Integer)user.getId()).toString());
                if(useRecognizer_b){
                    params.put("useRecognizer","d");
                }
                else{
                    params.put("useRecognizer","n");
                }
                for (String key : params.keySet()) {
                   Log.d(TAG, (key + "=" + params.get(key)));
                }
                return params;
            }

        };


        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, "get_feed");

    }

}
