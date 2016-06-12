package micherin.gp.onemore;

import android.app.Activity;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Micherin on 2016-05-15.
 *
 * Class name : DoubleBackPressHandler
 * Close app handler when back button pressed twice within 2000 milli second
 */
public class DoubleBackPressHandler {

    private long firstPressedTime = 0;
    private Toast toast;

    private Activity activity;

    public DoubleBackPressHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if ( System.currentTimeMillis() > firstPressedTime + 2000 ) {
            firstPressedTime = System.currentTimeMillis();
            toastInfoMsg();
            return;
        } else {
            activity.finish();
            toast.cancel();
        }
    }

    public void toastInfoMsg() {
        toast = Toast.makeText(activity,
                "[뒤로] 버튼을 한번더 누르시면 종료됩니다.",
                Toast.LENGTH_SHORT);

        // toast message text resize
        ViewGroup viewGroup = (ViewGroup)toast.getView();
        TextView messageTextView = (TextView)viewGroup.getChildAt(0);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

        toast.show();
    }
}
