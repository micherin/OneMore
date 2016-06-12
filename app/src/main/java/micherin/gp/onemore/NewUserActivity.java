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

public class NewUserActivity extends AppCompatActivity {

    // Debuggin TAG
    private static final String TAG = NewUserActivity.class.getSimpleName();

    // Back button click twice in short time for app closing
    private DoubleBackPressHandler doubleBackPressHandler;

    // UI Element
    private Button mBtnRegister;
    private TextView mBtnLinkLogin;
    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;

    // Progress dialog
    private ProgressDialog mProgressDialog;

    // Utility Class
    private  SessionManager mSession;
    private SQLiteHandler mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        // Already logined
        mSession = new SessionManager(getApplicationContext());
        if (mSession.isLoggedIn()) {
            Intent intent = new Intent(NewUserActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // UI Element Binding
        mName = (EditText)findViewById(R.id.input_name);
        mEmail = (EditText)findViewById(R.id.input_email);
        mPassword = (EditText)findViewById(R.id.input_password);
        mBtnRegister = (Button)findViewById(R.id.btn_signup);
        mBtnLinkLogin = (TextView) findViewById(R.id.link_login);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mSession = new SessionManager(getApplicationContext());

        mDB = new SQLiteHandler(getApplicationContext());

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString().trim();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                registerUser(name, email, password);
            }
        });

        mBtnLinkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewUserActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password) {
        // Reset error
        mName.setError(null);
        mEmail.setError(null);
        mPassword.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for name
        if (TextUtils.isEmpty(name)) {
            mName.setError("이름을 입력해주세요");
            focusView = mName;
            cancel = true;
        } else if (name.length() < 1) {
            mName.setError("이름이 너무 짧습니다");
            focusView = mName;
            cancel = true;
        } else if (name.length() > 40) {
            mName.setError("이름이 너무 깁니다");
            focusView = mName;
            cancel = true;
        } else if (!isNameValid(name)) {
            mName.setError("올바른 이름형식이 아닙니다");
            focusView = mName;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "name field error");
            focusView.requestFocus();
            return;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("이메일 주소를 입력해 주세요");
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError("올바른 이메일 주소가 아닙니다");
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "e-mail fiend error");
            focusView.requestFocus();
            return;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("패스워드를 입력하세요");
            focusView = mPassword;
            cancel = true;
        } else if (password.length() < 8) {
            mPassword.setError("패스워드가 너무 짧습니다");
            focusView = mPassword;
            cancel = true;
        } else if (password.length() > 20) {
            mPassword.setError("패스워드가 너무 깁니다");
            focusView = mPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPassword.setError("올바른 비밀번호를 입력하세요");
            focusView = mPassword;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "passsword fiend error");
            focusView.requestFocus();
            return;
        }

//        mBtnRegister.setEnabled(false);

        // Tag used to cancel the request in volley
        String tag_string_req = "req_register";

        mProgressDialog.setMessage("사용자 등록중입니다...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URLStrings.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user.getString("cdate");

                        // Inserting row in users table
                        mDB.addUser(name, email, uid, created_at);

                        Toast.makeText(getApplicationContext(), "사용자 등록 성공, 로그인 하세요", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                NewUserActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_message");
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
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

    private boolean isNameValid(String name) {
        // Verify name by regular expression
        // special character or number is not allowed
        Pattern p = Pattern.compile("^[가-힣]{2,8}|[a-zA-Z\\s]{2,40}$");
        Matcher m = p.matcher(name);

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
