package lu.example.module.DragHelper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

/**
 * @Author: luqihua
 * @Time: 2018/5/29
 * @Description: ActivityUtil
 */

public class ActivityStackUtil implements Application.ActivityLifecycleCallbacks {

    private Stack<Activity> mActivityStack = new Stack<>();

    private static class Holder {
        private static ActivityStackUtil sInstance = new ActivityStackUtil();
    }

    public static ActivityStackUtil getInstance() {
        return Holder.sInstance;
    }


    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivityStack.push(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mActivityStack.remove(activity);
    }


    /**
     * 获取相对于当前activity前一个activity
     *
     * @param mCurrentActivity
     * @return
     */
    public Activity getPreActivity(Activity mCurrentActivity) {
        Activity preActivity = null;
        if (mActivityStack.size() > 1) {
            int index = mActivityStack.lastIndexOf(mCurrentActivity);
            if (index > 0) {
                preActivity = mActivityStack.get(index - 1);
            } else {
                preActivity = mActivityStack.lastElement();
            }
        }
        return preActivity;
    }
}
