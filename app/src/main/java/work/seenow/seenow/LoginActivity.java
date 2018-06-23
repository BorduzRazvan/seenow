package work.seenow.seenow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.SQLiteHandler;
import work.seenow.seenow.Utils.SessionManager;
import work.seenow.seenow.Utils.User;

public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private Button btnForgotPassword;
    private Button btnSubmit;
    private EditText inputEmail;
    private EditText inputPassword, inputPassword2, inputActivationCode;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private CallbackManager callbackManager;
    private LoginButton mButtonLogin;

    private int reset_password_state;

    private User user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mButtonLogin = (LoginButton) findViewById(R.id.login_button);
        btnForgotPassword = (Button) findViewById(R.id.forgotPassword);
        mButtonLogin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday","user_photos","user_location","user_gender"));
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        mButtonLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());
                                try {
                                    // Application code
                                    String email = object.getString("email");
                                    int id = object.getInt("id");
                                    String fullname = object.getString("name");
                                    String birthday = object.getString("birthday");
                                    String loc = object.getString("location");
                                    String profileImage = object.getString("profile_pic");
                                    String gender = object.getString("gender");
                                    user = new User(id, fullname, email, profileImage, birthday, loc, gender, 0, "d","14-12-1995","none");
                                    checkLogin_Social();
                                } catch (JSONException e) {

                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,location,photos{images}");
                request.setParameters(parameters);
                request.executeAsync();


            }

            @Override
            public void onCancel() {
                // App code
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.v("LoginActivity", exception.getCause().toString());
            }
        });


        inputEmail = (EditText) findViewById(R.id.loginEmail);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        inputPassword2 = (EditText) findViewById(R.id.loginPassword2);
        btnLogin = (Button) findViewById(R.id.loginButton);
        btnLinkToRegister = (Button) findViewById(R.id.loginNoAccountButton);
        inputActivationCode = (EditText) findViewById(R.id.activation_code);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        btnSubmit = (Button) findViewById(R.id.submit);
        btnForgotPassword.setOnClickListener(new View.OnClickListener(){
                                                 @Override
                                                 public void onClick(View v) {
                                                     inputPassword.setVisibility(View.INVISIBLE);
                                                     btnLogin.setVisibility(View.INVISIBLE);
                                                     btnLinkToRegister.setVisibility(View.INVISIBLE);
                                                     btnSubmit.setVisibility(View.VISIBLE);
                                                 }
                                             });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String activation_code = inputActivationCode.getText().toString().trim();
                String password1 = inputPassword.getText().toString().trim();
                String password2 = inputPassword2.getText().toString().trim();
                if (reset_password_state == 0) {
                    if ((email.isEmpty()) || (!RegisterActivity.isEmailValid(email))) {
                        // Prompt user to enter credentials
                        Toast.makeText(getApplicationContext(),
                                "Please enter the email email address!", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        setPassowrdResetCode(email);
                    }
                } else {
                    if(email.isEmpty() || activation_code.isEmpty() || password1.isEmpty() || password2.isEmpty() || (!password1.equals(password2)) ||
                            (!RegisterActivity.isEmailValid(email)) || (!RegisterActivity.isPasswordStrong(password1))){
                        // Prompt user to enter credentials
                        Toast.makeText(getApplicationContext(),
                                "Please enter the email email address!", Toast.LENGTH_LONG)
                                .show();
                        Log.d(TAG, "Email: "+email+".Activation_Code:"+activation_code+".password1"+password1);
                    }else {
                        modify_password(email,password1,activation_code);
                    }
                }
            }
        });
        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private void modify_password(final String email, final String password, final String activation_code) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                   AppConfig.URL_ACTIONS, new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.d(TAG, "Login Response: " + response.toString());
            try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Toast.makeText(getApplicationContext(),
                                "Password is changed, please login", Toast.LENGTH_LONG)
                                .show();
                        reset_password_state = 0;
                        inputPassword.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                        btnLinkToRegister.setVisibility(View.VISIBLE);
                        btnForgotPassword.setVisibility(View.VISIBLE);
                        btnSubmit.setVisibility(View.INVISIBLE);
                        inputActivationCode.setVisibility(View.INVISIBLE);
                        inputPassword2.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(getApplicationContext(),
                        "There was an error updating your information", Toast.LENGTH_LONG)
                        .show();
                    }
                } catch (JSONException e) {
                        // JSON error
                     e.printStackTrace();
                     Toast.makeText(getApplicationContext(),
                        "There was an error updating your information", Toast.LENGTH_LONG)
                        .show();
            }

        }
        }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());

                    Toast.makeText(getApplicationContext(),
                            "There was an error updating your information", Toast.LENGTH_LONG)
                            .show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("action_type", "set_password");
                    params.put("email", email);
                    params.put("password", password);
                    params.put("activation_code", activation_code);
                    return params;
                }
            };
            // Adding request to request queue

        strReq.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(strReq, "get_feed");
        }


    private void setPassowrdResetCode(final String email){
                StringRequest strReq = new StringRequest(Request.Method.POST,
                        AppConfig.URL_ACTIONS, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Login Response: " + response.toString());
                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                reset_password_state = 1;
                                inputPassword.setVisibility(View.VISIBLE);
                                inputPassword2.setVisibility(View.VISIBLE);
                                inputActivationCode.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "There was an error updating your information", Toast.LENGTH_LONG)
                                        .show();
                            }

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "There was an error updating your information", Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login Error: " + error.getMessage());

                        Toast.makeText(getApplicationContext(),
                                "There was an error updating your information", Toast.LENGTH_LONG)
                                .show();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        // Posting parameters to login url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("action_type", "forgot_password");
                        params.put("email", email);
                        return params;
                    }
                };
                // Adding request to request queue
        strReq.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(strReq, "reset_pwd");
            }


    private void checkLogin_Social(){
        {
            // Tag used to cancel the request
            String tag_string_req = "req_login";

            pDialog.setMessage("Logging in ...");
            showDialog();

            StringRequest strReq = new StringRequest(Method.POST,
                    AppConfig.URL_LOGIN, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Login Response: " + response.toString());
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

//                     Check for error node in json
                        if (!error) {
                        session.setLogin(true);
                        JSONObject jSUser = jObj.getJSONObject("user");
                            user = new User(jSUser.getInt("id"),jSUser.getString("fullname"),
                                    jSUser.getString("email"), jSUser.getString("profilePicture"),
                                    jSUser.getString("birthday"), jSUser.getString("country"),jSUser.getString("gender"),jSUser.getInt("points"), jSUser.getString("use_Recognizer"),jSUser.getString("created_at"),jSUser.getString("about"));
                            // Inserting row in users table
                            user.numberofFriends.set(jSUser.getLong("nr_friends"));
                            user.numberofAppereances.set(jSUser.getLong("nr_foundIn"));
                            user.numberofPhotosTaken.set(jSUser.getLong("nr_pictures"));

                        db.addUser(user);

//                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to login url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("social_id", ((Integer)user.getId()).toString());
                    params.put("email", user.getEmail());
                    params.put("fullname", user.getName());
                    params.put("birthday", user.getBirthday());
                    params.put("country", user.getCountry());
                    params.put("profilePicture", user.getProfileImage());
                    params.put("gender", user.getGender());
                    params.put("useRecognizer", user.getUseRecognizer());
                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }

    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

//                     Check for error node in json
                    if (!error) {
                        session.setLogin(true);
                        JSONObject jSUser = jObj.getJSONObject("user");
                        user = new User(jSUser.getInt("id"),jSUser.getString("fullname"),
                                jSUser.getString("email"), jSUser.getString("profilePicture"),
                                jSUser.getString("birthday"), jSUser.getString("country"),jSUser.getString("gender"),jSUser.getInt("points"), jSUser.getString("use_Recognizer"), jSUser.getString("created_at"), jSUser.getString("about"));
                        // Inserting row in users table

                        user.numberofFriends.set(jSUser.getLong("nr_friends"));
                        user.numberofAppereances.set(jSUser.getLong("nr_foundIn"));
                        user.numberofPhotosTaken.set(jSUser.getLong("nr_pictures"));

                        db.addUser(user);

//                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}