package work.seenow.seenow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import work.seenow.seenow.Utils.AppConfig;
import work.seenow.seenow.Utils.AppController;
import work.seenow.seenow.Utils.SQLiteHandler;
import work.seenow.seenow.Utils.SessionManager;

public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText confirmPassword;
    private EditText inputDate;
    private CountryCodePicker countryCodePicker;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private RadioGroup selectedGender;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    private static final Pattern VALID_PASSWORD_REGEX = Pattern.compile("^(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9]).{8,}$");


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = (EditText) findViewById(R.id.registerName);
        inputEmail = (EditText) findViewById(R.id.loginEmail);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnRegister = (Button) findViewById(R.id.registerButton);
        btnLinkToLogin = (Button) findViewById(R.id.registerAlreadyMember);
        inputDate = (EditText) findViewById(R.id.registerBirthday);
        confirmPassword = (EditText) findViewById(R.id.registerPassword);
        countryCodePicker = (CountryCodePicker) findViewById(R.id.CountryPicker);
        selectedGender = (RadioGroup) findViewById(R.id.radioGroup);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String conf_password = confirmPassword.getText().toString().trim();
                String date = inputDate.getText().toString().trim();
                String country = countryCodePicker.getSelectedCountryName();
                RadioButton genderSelector = (RadioButton) findViewById(selectedGender.getCheckedRadioButtonId());
                String gender = genderSelector.getText().toString();
                if(gender.contains("Male"))
                {
                    gender = "m";
                }
                else
                {
                    gender = "f";
                }
                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !conf_password.isEmpty() && !date.isEmpty() && !country.isEmpty()) {
                    if (!isEmailValid(email)) {
                        Toast.makeText(getApplicationContext(),
                                "Wrong email adress!", Toast.LENGTH_LONG)
                                .show();

                    } else if (!password.equals(conf_password)) {
                        Toast.makeText(getApplicationContext(),
                                "The passwords doesn't match!", Toast.LENGTH_LONG)
                                .show();

                    }else if(!isPasswordStrong(password)) {
                        Toast.makeText(getApplicationContext(),
                                "The password should be at least 8 characters, 1 uppercase letter, 1 special character", Toast.LENGTH_LONG)
                                .show();
                    }else if (!isValidDate(date)) {
                        Toast.makeText(getApplicationContext(),
                                "Date must respect pattern: yyyy.MM.dd", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        registerUser(name, email, password, date, gender, country);

                    }
                }else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }


    private void registerUser(final String name, final String email,
                              final String password, final String birthday, final String gender, final String country) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = "Something went wrong! Please try again!";
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("fullname", name);
                params.put("email", email);
                params.put("password", password);
                params.put("country", country);
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("profilePicId", "1");
                params.put("useRecognizer", "n");
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

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    private static boolean isEmailValid(String email) {
        return VALID_EMAIL_ADDRESS_REGEX .matcher(email).matches();
    }

    /**
     * method is used for checking if password is strong enough
     *
     * @param password
     * @return boolean true for strong enough
     * Regex explanation:
     *      ^                         Start anchor
     *      (?=.*[A-Z])               Ensure string at least 1 uppercase letter.
     *      (?=.*[!@#$&*])            Ensure string has one special case letter.
     *      (?=.*[0-9])               Ensure string has at least 1 digit.
     *      .{8}                      Ensure string is of length 8.
     $                                End anchor.
     */
    private static boolean isPasswordStrong(String password) {
        return VALID_PASSWORD_REGEX .matcher(password).matches();
    }

    /**
     * method is used for checking valid date format.
     *
     * @param inDate
     * @return boolean true for valid false for invalid
     */
    private static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

}