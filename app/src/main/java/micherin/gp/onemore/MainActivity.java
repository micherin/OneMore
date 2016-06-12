package micherin.gp.onemore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnProgramList;
    private Button btnAddProgram;

    private SQLiteHandler mDb;
    private SessionManager mSession;

    // Back button click twice in short time for app closing
    private DoubleBackPressHandler doubleBackPressHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView)findViewById(R.id.name);
        txtEmail = (TextView)findViewById(R.id.email);
        btnLogout = (Button)findViewById(R.id.btnLogout);
        btnProgramList = (Button)findViewById(R.id.btnProgramlist);
        btnAddProgram = (Button)findViewById(R.id.btnAddProgram);

        doubleBackPressHandler = new DoubleBackPressHandler(this);

        mDb = new SQLiteHandler(getApplicationContext());

        mSession = new SessionManager(getApplicationContext());
        if( !mSession.isLoggedIn() ) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = mDb.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Program list button click
        btnProgramList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProgramListActivity.class);
                startActivity(i);
            }
        });

        // Add program button click
        btnAddProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddProgramActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Close app when back button pressed twice in short time
        doubleBackPressHandler.onBackPressed();
    }

    private void logoutUser() {
        mSession.setLogin(false);

        mDb.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
