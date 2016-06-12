package micherin.gp.onemore;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class DoActivity extends AppCompatActivity {

    private Button mBtnStart;
    private Button mBtnStop;

    private TextView mTitle;
    private TextView mTxtCycles;
    private TextView mTxtTime;
    private TextView mTxtRestTime;

    private long mTrainingTime;
    private long mRestTime;
    private int mCycles;
    private  String mProgramName;

    private TimeCounterClass mTimeCounter;
    private RestTimeCounterClass mRestTImeCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do);

        mBtnStart = (Button)findViewById(R.id.btn_start);
        mBtnStop = (Button)findViewById(R.id.btn_stop);
        mTitle = (TextView)findViewById(R.id.txtTitle);
        mTxtCycles = (TextView)findViewById(R.id.txtCycles);
        mTxtTime = (TextView)findViewById(R.id.txtTime);
        mTxtRestTime = (TextView)findViewById(R.id.txtRestTime);

        // argument receive
        final Intent i = getIntent();
        mTrainingTime = Long.parseLong(i.getStringExtra("time")) * 1000;
        mRestTime = Long.parseLong(i.getStringExtra("rest_time")) * 1000;
        mCycles = Integer.parseInt(i.getStringExtra("cycle"));
        mProgramName = i.getStringExtra("name");

        mTitle.setText(mProgramName);
        mTxtCycles.setText("반복 횟수 : " + String.valueOf(mCycles));
        mBtnStop.setEnabled(false);

        mTimeCounter = new TimeCounterClass(mTrainingTime, 1000);
        mRestTImeCounter =new RestTimeCounterClass(mRestTime, 1000);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnStop.setEnabled(true);
                mBtnStart.setEnabled(false);
                mTimeCounter.start();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCycles = Integer.parseInt(i.getStringExtra("cycle"));
                mBtnStart.setEnabled(true);
                mBtnStop.setEnabled(false);
                mTimeCounter.cancel();
                mRestTImeCounter.cancel();

            }
        });


    }

    public class TimeCounterClass extends CountDownTimer {

        public TimeCounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mTxtTime.setText("Completed.");

            mRestTImeCounter.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            mTxtTime.setText(hms);
        }
    }

    public class RestTimeCounterClass extends CountDownTimer {

        public RestTimeCounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mTxtRestTime.setText("Completed.");

            if (mCycles > 0) {
                mCycles = mCycles -1;
                mTxtCycles.setText("반복 횟수 : " + String.valueOf(mCycles));
                mTimeCounter.start();
            } else {
                mTxtCycles.setText("훈련 종료");
                mBtnStart.setEnabled(true);
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {

            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

            mTxtRestTime.setText(hms);
        }
    }
}
