package lu.example.module.DragHelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import lu.example.R;


/**
 * @Author: luqihua
 * @Time: 2018/5/28
 * @Description: 用于实现activity的滑动返回
 */

public class SlideBackLayout extends FrameLayout {

    private static final float BACK_THRESHOLD_RATIO = 0.4f;//当滑动宽度达到界面0.4的时候  执行滑动返回
    private static final int SHADOW_WIDTH = 50; //阴影部分面积

    private int mWidth, mHeight;

    private ViewDragHelper mViewDragHelper;
    /**
     * 标记刚开始滑动时当前视图左侧和顶部的值(一般一开始是0,0)
     */
    private int mDragLeftX, mDragTopY;

    /**
     * 包装上一个activity的contentView  用于实现上一个页面跟随当前页面滑动而滑动
     */
    private PreContentViewWrapper mPreContentViewWrapper;

    private Activity mCurrentActivity;
    private View mCurrentContentView;

    private Drawable mShawDrawable;//页面滑动时左边缘的阴影图片

    private ISlideListener mSlideListener;

    public SlideBackLayout(Context context) {
        this(context, null);
    }

    public SlideBackLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mShawDrawable = ContextCompat.getDrawable(getContext(), R.drawable.slide_shadow);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mDragCallback);
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    /**
     * 与需要滑动返回的activity绑定
     *
     * @param activity
     * @param listener
     */
    public void attach2Activity(Activity activity, ISlideListener listener) {
        this.mCurrentActivity = activity;
        this.mSlideListener = listener;
        //创建一个viewGroup 用于添加上一个界面的contentView
        mPreContentViewWrapper = new PreContentViewWrapper(getContext());
        mPreContentViewWrapper.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mPreContentViewWrapper);

        /*当前activity的contentView*/
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        mCurrentContentView = decorView.getChildAt(0);
        decorView.addView(this, mCurrentContentView.getLayoutParams());

        decorView.removeView(mCurrentContentView);
        addView(mCurrentContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mViewDragHelper.cancel();
            return false;
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mViewDragHelper != null && mViewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mShawDrawable.draw(canvas);
    }


    /*=========================处理滑动事件================================*/

    private ViewDragHelper.Callback mDragCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return false;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
            mViewDragHelper.captureChildView(mCurrentContentView, pointerId);
            if (!mPreContentViewWrapper.isBindPreActivity())
                mPreContentViewWrapper.bindPreActivity(mCurrentActivity);
            if (mSlideListener != null)
                mSlideListener.onSlideStart();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE) {
                //返回上个界面
                if (mCurrentContentView.getLeft() >= mWidth) {
                    if (mSlideListener != null) {
                        mSlideListener.onSlideComplete();
                    }
                    mCurrentActivity.finish();
                    mCurrentActivity.overridePendingTransition(0, 0);
                    mCurrentActivity.getWindow().getDecorView().setVisibility(GONE);
                    removeView(mPreContentViewWrapper);
                    mPreContentViewWrapper.unBindPreActivity();
                } else {
                    //返回当前界面
                    if (mSlideListener != null)
                        mSlideListener.onSlideCancel();
                }
            }
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            mDragLeftX = capturedChild.getLeft();
            mDragTopY = capturedChild.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left < 0 ? 0 : left;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (releasedChild.getLeft() > mWidth * BACK_THRESHOLD_RATIO) {
                mViewDragHelper.settleCapturedViewAt(mWidth, mDragTopY);
            } else {
                mViewDragHelper.settleCapturedViewAt(mDragLeftX, mDragTopY);
            }
            invalidate();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (mPreContentViewWrapper != null && mPreContentViewWrapper.isBindPreActivity()) {
                float ratio = left * 1.0f / mWidth;
                mPreContentViewWrapper.onSlideChange(ratio);
                mShawDrawable.setBounds(left - SHADOW_WIDTH, 0, left, mHeight);
                if (mSlideListener != null)
                    mSlideListener.onSliding(ratio);
                invalidate();
            }
        }
    };


    /*====================滑动的监听=====================*/
    public interface ISlideListener {
        void onSlideStart();

        void onSlideComplete();

        void onSliding(float ratio);

        void onSlideCancel();
    }


    /*============================上一个界面的容器=============================*/
    public static class PreContentViewWrapper extends FrameLayout {

        private static final float TRANSLATE_X_RATIO = 0.3f;//当前页面在滑动的时候，前一个界面初始被隐藏的宽度为0.3*width

        private WeakReference<Activity> mPreActivityRef;
        private ViewGroup mPreDecorView;
        private ViewGroup mPreContentView;

        private ViewGroup.LayoutParams mPreLayoutParams;

        private boolean isBindPreActivity;
        private int mHideWidth;

        public PreContentViewWrapper(Context context) {
            this(context, null);
        }

        public PreContentViewWrapper(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mHideWidth = (int) (TRANSLATE_X_RATIO * w);
        }

        /**
         * 绑定上一个activity的ContenView
         * @param currentActivity
         */
        public void bindPreActivity(Activity currentActivity) {
            Activity preActivity = ActivityStackUtil.getInstance().getPreActivity(currentActivity);
            if (!preActivity.isDestroyed() && !preActivity.isFinishing()) {
                //创建一个软连接指向上个activity
                mPreActivityRef = new WeakReference<Activity>(preActivity);

                mPreDecorView = (ViewGroup) preActivity.getWindow().getDecorView();
                mPreContentView = (ViewGroup) mPreDecorView.getChildAt(0);
                mPreLayoutParams = mPreContentView.getLayoutParams();
                mPreDecorView.removeView(mPreContentView);
                addView(mPreContentView, 0, mPreLayoutParams);
                this.isBindPreActivity = true;
            }
        }


        /**
         * 解除绑定，将preContentView归还给上个activity
         */
        public void unBindPreActivity() {
            if (!isBindPreActivity) return;
            if (mPreActivityRef == null || mPreActivityRef.get() == null) return;
            if (mPreContentView != null && mPreDecorView != null) {
                this.removeView(mPreContentView);
                mPreDecorView.addView(mPreContentView, 0, mPreLayoutParams);
                mPreContentView = null;
                mPreActivityRef.clear();
                mPreActivityRef = null;
            }
            this.isBindPreActivity = false;
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (mPreDecorView != null && mPreContentView == null) {
                mPreDecorView.draw(canvas);
            }
        }

        /**
         * 前一个界面跟随当前页面滑动而滑动
         *
         * @param ratio
         */
        public void onSlideChange(float ratio) {
            this.setTranslationX(mHideWidth * (ratio - 1));
        }


        public boolean isBindPreActivity() {
            return isBindPreActivity;
        }
    }
}
