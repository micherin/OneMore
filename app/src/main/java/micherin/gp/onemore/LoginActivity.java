package micherin.gp.onemore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    // Debuging TAG
    private static final String TAG = LoginActivity.class.getSimpleName();

    // Back button click twice in short time for app closing
    private DoubleBackPressHandler doubleBackPressHandler;

    // UI elements
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private TextView mSignupLink;

    // Progress dialog
    private ProgressDialog mProgressDialog;

    // Utility Class
    private SessionManager mSession;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // SessionManager
        mSession = new SessionManager(getApplicationContext());
        if (mSession.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // SQLite
        db = new SQLiteHandler(getApplicationContext());

        // Component assign
        mEmailView = (EditText) findViewById(R.id.input_email);
        mPasswordView = (EditText) findViewById(R.id.input_password);
        mLoginButton = (Button) findViewById(R.id.btn_login);
        mSignupLink = (TextView) findViewById(R.id.link_signup);

        // Progress dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        // Handler for back button twice
        doubleBackPressHandler = new DoubleBackPressHandler(this);

        // Login button cliick listener
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailView.getText().toString().trim();
                String password = mPasswordView.getText().toString().trim();

                attemptLogin(email, password);
            }
        });

        // Signup link click listener
        mSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Close app when back button pressed twice in short time
        doubleBackPressHandler.onBackPressed();
    }

    public void attemptLogin(final String email, final String password) {
        Log.d(TAG, "attemptLogin start");

        // Tag used to cancel the request
        String tag_string_req = "req_login";

        // Reset error
        mEmailView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("이메일 주소를 입력해 주세요");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("올바른 이메일 주소가 아닙니다");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "e-mail fiend error");
            focusView.requestFocus();
            return;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("패스워드를 입력하세요");
            focusView = mPasswordView;
            cancel = true;
        } else if (password.length() < 8) {
            mPasswordView.setError("패스워드가 너무 짧습니다");
            focusView = mPasswordView;
            cancel = true;
        } else if (password.length() > 20) {
            mPasswordView.setError("패스워드가 너무 깁니다");
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("올바른 비밀번호를 입력하세요");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "passsword fiend error");
            focusView.requestFocus();
            return;
        }

//        mLoginButton.setEnabled(false);

        mProgressDialog.setMessage("로그인 중입니다...");
        showDialog();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                URLStrings.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());

                hideDialog();

                try {
                    JSONObject jObject = new JSONObject(response);
                    boolean error = jObject.getBoolean("error");

                    if (!error) {
                        mSession.setLogin(true);

                        String uid = jObject.getString("uid");

                        JSONObject jobjUser = jObject.getJSONObject("user");
                        String name = jobjUser.getString("name");
                        String email = jobjUser.getString("email");
                        String cdate = jobjUser.getString("cdate");

                        db.addUser(name, email, uid, cdate);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = jObject.getString("error_message");
                        Toast.makeText(
                                getApplicationContext(),
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            getApplicationContext(),
                            "Json error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(
                        getApplicationContext(),
                        error.getMessage(),
                        Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {

        // Verify password by regular expression
        // length : 8 - 20
        // use 3 type character
        Pattern p = Pattern.compile("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$");
        Matcher m = p.matcher(password);

        return m.matches();
    }

    private void showDialog() {
        if ( !mProgressDialog.isShowing() ) {
            mProgressDialog.show();
        }
    }

    private  void hideDialog() {
        if ( mProgressDialog.isShowing() ) {
            mProgressDialog.hide();
        }
    }
}