package micherin.gp.onemore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Micherin on 2016-06-12.
 */
public class ProgramListAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<ProgramItem> mItemArrayList;

    private TextView mName;
    private TextView mCreateAt;
    private TextView mTotalTime;
    private TextView mCycle;
    private TextView mTime;
    private TextView mRestTime;
    private TextView mDescription;

    public ProgramListAdapter(Context mContext, ArrayList<ProgramItem> mItemArrayList) {
        this.mContext = mContext;
        this.mItemArrayList = mItemArrayList;
    }

    @Override
    public int getCount() {
        return this.mItemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.mItemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if ( convertView == null ) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.programs, null);

//            mName = (TextView)convertView.findViewById(R.id.txtProgramName);
//            mCreateAt = (TextView)convertView.findViewById(R.id.txtCreateAt);
//            mTotalTime = (TextView)convertView.findViewById(R.id.txtTotalTime);
//            mCycle = (TextView)convertView.findViewById(R.id.txtCycle);
//            mTime = (TextView)convertView.findViewById(R.id.txtTime);
//            mRestTime = (TextView)convertView.findViewById(R.id.txtRestTime);
//            mDescription = (TextView)convertView.findViewById(R.id.txtDescription);
//
//            mName.setText(mItemArrayList.get(position).getmName());
//            mCreateAt.setText(mItemArrayList.get(position).getmCreateAt());
//            mTotalTime.setText(mItemArrayList.get(position).getmTotalTime());
//            mCycle.setText(mItemArrayList.get(position).getmCycle());
//            mTime.setText(mItemArrayList.get(position).getmTotalTime());
//            mRestTime.setText(mItemArrayList.get(position).getmRestTime());
//            mDescription.setText(mItemArrayList.get(position).getmDescription());
        }
        return convertView;
    }
}
