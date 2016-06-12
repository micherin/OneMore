package micherin.gp.onemore;

public class ProgramItem {
    private String mName;
    private String mCreateAt;
    private String mTotalTime;
    private String mCycle;
    private String mTime;
    private String mRestTime;
    private String mDescription;

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmCreateAt() {
        return mCreateAt;
    }

    public void setmCreateAt(String mCreateAt) {
        this.mCreateAt = mCreateAt;
    }

    public ProgramItem(String mName, String mCreateAt, String mTotalTime, String mCycle, String mTime, String mRestTime, String mDescription) {
        this.mName = mName;
        this.mCreateAt = mCreateAt;
        this.mTotalTime = mTotalTime;
        this.mCycle = mCycle;
        this.mTime = mTime;
        this.mRestTime = mRestTime;
        this.mDescription = mDescription;
    }

    public String getmTotalTime() {
        return mTotalTime;
    }

    public void setmTotalTime(String mTotalTime) {
        this.mTotalTime = mTotalTime;
    }

    public String getmCycle() {
        return mCycle;
    }

    public void setmCycle(String mCycle) {
        this.mCycle = mCycle;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmRestTime() {
        return mRestTime;
    }

    public void setmRestTime(String mRestTime) {
        this.mRestTime = mRestTime;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
