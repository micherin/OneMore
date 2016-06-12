package micherin.gp.onemore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddProgramActivity extends AppCompatActivity {

    // Debuggin TAG
    private static final String TAG = AddProgramActivity.class.getSimpleName();

    // UI element
    private EditText mProgramName;
    private EditText mCycle;
    private EditText mTime;
    private EditText mRestTime;
    private EditText mDescription;
    private Button mBtnAddProgram;

    private ProgressDialog mProgressDialog;

    private SessionManager mSession;
    private SQLiteHandler mDB;
    private String mUniqueID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_program);

        mSession = new SessionManager(getApplicationContext());
        if ( !mSession.isLoggedIn() ) {
            Intent i = new Intent(AddProgramActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        mDB = new SQLiteHandler(getApplicationContext());

        mProgramName = (EditText)findViewById(R.id.input_name);
        mCycle = (EditText)findViewById(R.id.input_cycle);
        mTime = (EditText)findViewById(R.id.input_time);
        mRestTime = (EditText)findViewById(R.id.input_rest_time);
        mDescription = (EditText)findViewById(R.id.input_descriptioin);
        mBtnAddProgram = (Button)findViewById(R.id.btn_add_program);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        HashMap<String, String> user = mDB.getUserDetails();
        mUniqueID = user.get("uid");

        mBtnAddProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pName = mProgramName.getText().toString().trim();
                String cycle = mCycle.getText().toString().trim();
                String time = mTime.getText().toString().trim();
                String restTime = mRestTime.getText().toString().trim();
                String desc = mDescription.getText().toString().trim();

                addProgram(pName, cycle, time, restTime, desc);
            }
        });
    }

    private void addProgram(final String name,
                            final String cycle,
                            final String time,
                            final String restTime,
                            final String desc) {
        Log.d(TAG, "addProgram start");

        // TAG used to cancel volley request
        String tag_string_req = "req_add_program";

        // Reset Error
        mProgramName.setError(null);
        mCycle.setError(null);
        mTime.setError(null);
        mRestTime.setError(null);
        mDescription.setError(null);

        int _cycle = 0;
        int _time = 0;
        int _restTime = 0;

        boolean cancel = false;
        View focusView = null;

        // check name
        if (TextUtils.isEmpty(name)) {
            mProgramName.setError("이름을 입력해주세요");
            focusView = mProgramName;
            cancel = true;
        } else if (name.length() < 3) {
            mProgramName.setError("이름이 너무 짧습니다");
            focusView = mProgramName;
            cancel = true;
        } else if (name.length() > 50) {
            mProgramName.setError("이름이 너무 길어요");
            focusView = mProgramName;
            cancel = true;
        }

        if (cancel) {
            Log.e(TAG, "name error");
            focusView.requestFocus();
            return;
        }

        // check cycle
        if (TextUtils.isEmpty(cycle)) {
            mCycle.setError("반복 횟수를 입력하세요");
            focusView = mCycle;
            cancel = true;
        } else if (!isNumber(cycle)) {
            mCycle.setError("숫자를 입력해주세요");
            focusView = mCycle;
            cancel = true;
        } else {
            _cycle = Integer.parseInt(cycle);
        }

        if (cancel) {
            Log.e(TAG, "cycle error");
            focusView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            mTime.setError("시간을 설정해주세요");
            focusView = mTime;
            cancel = true;
        } else if (!isNumber(time)) {
            mTime.setError("숫자를 입력해주세요");
            focusView = mTime;
            cancel = true;
        } else {
            _time = Integer.parseInt(time);
        }

        if (cancel) {
            Log.e(TAG, "time error");
            focusView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(restTime)) {
            mRestTime.setError("휴식 시간을 입력해주세요");
            focusView = mRestTime;
            cancel = true;
        } else if (!isNumber(restTime)) {
            mRestTime.setError("숫자를 입력해주세요");
            focusView = mRestTime;
            cancel = true;
        } else {
            _restTime = Integer.parseInt(restTime);
        }

        if (cancel) {
            Log.e(TAG, "rest time error");
            focusView.requestFocus();
            return;
        }

        if (desc.length() > 300) {
            mDescription.setError("설명이 너무 길어요 - 300자 이하");
            Log.e(TAG, "description error");
            focusView = mDescription;
            focusView.requestFocus();
            return;
        }

        final int _totalTime = (_time + _restTime) * _cycle;


        mProgressDialog.setMessage("프로그램을 추가하는 중입니다...");
        showDialog();

        StringRequest strRequest = new StringRequest(Request.Method.POST,
                URLStrings.URL_ADDPROGRAM, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Add program: " + response);

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Toast.makeText(
                                getApplicationContext(),
                                "프로그램 추가 성공",
                                Toast.LENGTH_LONG).show();

                        Intent i = new Intent(AddProgramActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_message");
                        Toast.makeText(
                                getApplicationContext(),
                                errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            getApplicationContext(),
                            "JSON error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Add program error: " + error.getMessage());
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
                params.put("uid", mUniqueID);
                params.put("name", name);
                params.put("cycle", cycle);
                params.put("time", time);
                params.put("rest_time", restTime);
                params.put("desc", desc);
                params.put("ptime", String.valueOf(_totalTime));

                return  params;
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest, tag_string_req);
    }

    private boolean isNumber(String str) {
        Pattern p= Pattern.compile("^[0-9]{1,5}$");
        Matcher m = p.matcher(str);

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
