package micherin.gp.onemore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProgramListActivity extends AppCompatActivity {

    // Debuggin TAG
    private static final String TAG = ProgramListActivity.class.getSimpleName();

    private ProgressDialog mProgressDialog;

    private SessionManager mSession;
    private SQLiteHandler mDB;

    private String mUID;

    private JSONArray mProgramsArray = null;
    private ArrayList<HashMap<String,String>> mProgramsList;
    private ListView mListView;

//    private ProgramListAdapter mProgramListAdapter;
//    private ArrayList<ProgramItem> mProgramArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_list);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mDB = new SQLiteHandler(getApplicationContext());

        mSession = new SessionManager(getApplicationContext());
        if ( !mSession.isLoggedIn() ) {
            Toast.makeText(
                    getApplicationContext(),
                    "로그인 정보가 없어 로그인 화면으로 이동합니다",
                    Toast.LENGTH_LONG).show();
            logoutUser();
        }

        HashMap<String, String> user = mDB.getUserDetails();
        mUID = user.get("uid");

        mListView = (ListView)findViewById(R.id.program_list);
        mProgramsList = new ArrayList<HashMap<String, String>>();

        getProgramlist(mUID);

        mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> _data = (HashMap) parent.getAdapter().getItem(position);

                Intent i = new Intent (ProgramListActivity.this, DoActivity.class);
                i.putExtra("name", _data.get("name"));
                i.putExtra("ptime", _data.get("ptime"));
                i.putExtra("cycle", _data.get("cycle"));
                i.putExtra("time", _data.get("time"));
                i.putExtra("rest_time", _data.get("rest_time"));
                startActivity(i);
            }
        });
    }

    private void logoutUser() {
        mSession.setLogin(false);
        mDB.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(ProgramListActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void getProgramlist(String uid) {
        // TAG used to cancel volley request
        String tag_string_req = "req_add_program";

        mProgressDialog.setMessage("프로그램 목록을 다운로드 하는 중...");
        showDialog();

        StringRequest strRequest = new StringRequest(Request.Method.POST,
                URLStrings.URL_GETPROGRAMLIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "GetProgramList: "+response);
                hideDialog();

                try {
                    JSONObject _jObj = new JSONObject(response);
                    boolean error = _jObj.getBoolean("error");

                    if ( !error ) {
                        mProgramsArray = _jObj.getJSONArray("programs");

                        for ( int i=0; i<mProgramsArray.length(); i++) {
                            JSONObject _jObjTemp = mProgramsArray.getJSONObject(i);

                            String _Name = _jObjTemp.getString("name");
                            String _CreateAt = _jObjTemp.getString("cdate");
                            String _TotalTime = _jObjTemp.getString("ptime");
                            String _Cycle = _jObjTemp.getString("cycle");
                            String _Time = _jObjTemp.getString("c1time");
                            String _RestTime = _jObjTemp.getString("c2time");
                            String _Description = _jObjTemp.getString("info");

                            HashMap<String, String> _program = new HashMap<String, String>();

                            _program.put("name",_Name);
                            _program.put("createAt", _CreateAt);
                            _program.put("ptime", _TotalTime);
                            _program.put("cycle", _Cycle);
                            _program.put("time", _Time);
                            _program.put("rest_time", _RestTime);
                            _program.put("desc", _Description);
                            String _title = "프로그램 명: " + _Name;
                            String _timeline = "전체 수행시간: " + _TotalTime
                                    + " 반복횟수: " + _Cycle
                                    + " 운동시간: " + _Time
                                    + " 사잇시간: " + _RestTime;
                            String _date = "생성일: " + _CreateAt;
                            String _desc = "프로그램 설명: " + _Description;

                            _program.put("title", _title);
                            _program.put("timeline", _timeline);
                            _program.put("date", _date);
                            _program.put("info", _desc);

                            mProgramsList.add(_program);
                        }

                        ListAdapter _listAdapter = new SimpleAdapter(
                                ProgramListActivity.this,
                                mProgramsList,
                                R.layout.programs,
//                                new String[]{"name", "createAt", "ptime", "cycle", "time", "rest_time","desc"},
//                                new int[]{R.id.txtProgramName, R.id.txtCreateAt, R.id.txtTotalTime, R.id.txtCycle, R.id.txtTime, R.id.txtRestTime, R.id.txtDescription}
                                new String[]{"title", "timeline", "date", "info"},
                                new int[]{R.id.txtTitle, R.id.txtTimeline, R.id.txtCreateAt, R.id.txtDescription}
                        );

                        mListView.setAdapter(_listAdapter);


                    } else {
                        String errorMsg = _jObj.getString("error_message");
                        Toast.makeText(
                                getApplicationContext(),
                                errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(
                            getApplicationContext(),
                            "JSON error: "+e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "get program list error: " + error.getMessage());
                Toast.makeText(
                        getApplicationContext(),
                        error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid",mUID);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strRequest, tag_string_req);
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
