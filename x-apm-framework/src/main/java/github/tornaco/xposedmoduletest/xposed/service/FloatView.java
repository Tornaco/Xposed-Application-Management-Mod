package github.tornaco.xposedmoduletest.xposed.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class FloatView extends LinearLayout {

    private Rect mRect = new Rect();
    private WindowManager mWm;
    private WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();

    private int mTouchSlop, mSwipeSlop;
    private int mLargeSlop = 50;
    private int mScreenHeight, mScreenWidth;
    private int mTapDelay = 0;
    private float density = getResources().getDisplayMetrics().density;

    private boolean mDoubleTapEnabled, mEdgeEnabled = false, mFeedbackAnimEnabled = true, mLocked = false;

    private GestureDetectorCompat mDetectorCompat;
    private Callback mCallback;

    private ViewGroup mContainerView;
    private TextView mTextView;
    private String mText;
    private String mTips;

    private Handler mHandler = new Handler();

    private Runnable mSingleTapNotifier = new Runnable() {
        @Override
        public void run() {
            mCallback.onSingleTap(getText());
            performTapFeedbackIfNeed();
        }
    };

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (screenState == SCREEN_STATE_OFF) {
            cleanAnim();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public FloatView(final Context context, Callback callback) {
        super(context);

        mTips = new AppResource(context)
                .loadStringFromAPMApp("tips_current_activity_view");

        mCallback = callback;

        mDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mHandler.removeCallbacks(mSingleTapNotifier);
                mCallback.onDoubleTap();
                performTapFeedbackIfNeed();
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mDoubleTapEnabled) {
                    mHandler.removeCallbacks(mSingleTapNotifier);
                    mHandler.postDelayed(mSingleTapNotifier, mTapDelay);
                } else {
                    mSingleTapNotifier.run();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                inDragMode = true;
                super.onLongPress(e);
                mCallback.onLongPress();
                performTapFeedbackIfNeed();
            }

            @Override
            public boolean onFling(MotionEvent me, MotionEvent me2, float velocityX, float velocityY) {

                SwipeDirection swipeDirection = null;

                float y = me.getY() - me2.getY();

                float x = me.getX() - me2.getX();

                float absX = Math.abs(x);
                float absY = Math.abs(y);

                boolean large = false;

                if (absX > absY) {
                    // Check slot.
                    if (absX > mSwipeSlop) {
                        // Check direction.
                        swipeDirection = x > 0 ? SwipeDirection.L : SwipeDirection.R;
                        int largeThs = (int) ((float) mLargeSlop / (float) 100 * (float) mScreenWidth);
                        large = absX >= largeThs;
                    }
                } else if (absX < absY) {
                    // Check slot.
                    if (absY > mSwipeSlop) {
                        // Check direction.
                        swipeDirection = y > 0 ? SwipeDirection.U : SwipeDirection.D;
                        int largeThs = (int) ((float) mLargeSlop / (float) 100 * (float) mScreenHeight);
                        large = absY >= largeThs;
                    }
                }

                if (swipeDirection != null) {
                    if (large) {
                        mCallback.onSwipeDirectionLargeDistance(swipeDirection);
                    } else {
                        mCallback.onSwipeDirection(swipeDirection);
                    }
                    performTapFeedbackIfNeed();
                }

                return true;
            }
        });


        mContainerView = new RelativeLayout(context);

        RelativeLayout.LayoutParams layoutParams
                = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        mContainerView.setLayoutParams(layoutParams);
        mContainerView.setPadding(18, 18, 18, 18);
        mContainerView.setBackgroundColor(Color.WHITE);
        mContainerView.setAlpha(0.6f);
        setAlpha(0.6f);
        mContainerView.setFocusable(true);
        mContainerView.setClickable(true);

        mTextView = new TextView(context);
        mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        mTextView.setFocusable(true);
        mTextView.setClickable(true);
        mTextView.setTextColor(Color.BLACK);

        mContainerView.addView(mTextView, layoutParams);

        addView(mContainerView);

        getWindowVisibleDisplayFrame(mRect);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTouchSlop = mTouchSlop * mTouchSlop;
        mSwipeSlop = 50; // FIXME Read from Settings.

        mWm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        mScreenHeight = mWm.getDefaultDisplay().getHeight();
        mScreenWidth = mWm.getDefaultDisplay().getWidth();

        mLp.gravity = Gravity.START | Gravity.TOP;
        mLp.format = PixelFormat.RGBA_8888;
        mLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLp.type = Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        OnTouchListener touchListener = new OnTouchListener() {
            private float touchX;
            private float touchY;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchX = event.getX() + getLeft();
                        touchY = event.getY() + getTop();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isDragging = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!mLocked && inDragMode) {
                            int dx = (int) (event.getRawX() - startX);
                            int dy = (int) (event.getRawY() - startY);
                            if ((dx * dx + dy * dy) > mTouchSlop) {
                                isDragging = true;
                                mLp.x = (int) (event.getRawX() - touchX);
                                mLp.y = (int) (event.getRawY() - touchY);
                                mWm.updateViewLayout(FloatView.this, mLp);
                                return true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touchX = touchY = 0.0F;
                        if (!mLocked && isDragging) {
                            if (mEdgeEnabled) {
                                reposition();
                            }
                            isDragging = false;
                            inDragMode = false;
                            return true;
                        }
                }
                return mDetectorCompat.onTouchEvent(event);
            }
        };

        setOnTouchListener(touchListener);
        mContainerView.setOnTouchListener(touchListener);
        mTextView.setOnTouchListener(touchListener);

        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                cleanAnim();
            }

            @Override
            public void onViewDetachedFromWindow(View view) {

            }
        });
    }

    public void setText(String text) {
        String textToShow = mTips + "\n" + text;
        if (isShowing()) {
            mTextView.setText(textToShow);
        }
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public void attach() {
        if (getParent() == null) {
            mWm.addView(this, mLp);
        }
        mWm.updateViewLayout(this, mLp);
        getWindowVisibleDisplayFrame(mRect);
        mRect.top += dp2px(50);
        mLp.y = dp2px(150);
        mLp.x = mRect.width() - dp2px(55);
        reposition();
        if (BuildConfig.DEBUG) {
            XposedLog.verbose("FloatView show");
        }
    }

    private void performTapFeedbackIfNeed() {
        if (!mFeedbackAnimEnabled) return;
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator alphaAnimatorX = ObjectAnimator.ofFloat(mContainerView, "scaleX", 1f, 0.8f);
        final ObjectAnimator alphaAnimatorY = ObjectAnimator.ofFloat(mContainerView, "scaleY", 1f, 0.8f);
        set.setDuration(120);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                AnimatorSet set = new AnimatorSet();
                final ObjectAnimator alphaAnimatorX = ObjectAnimator.ofFloat(mContainerView, "scaleX", 0.8f, 1f);
                final ObjectAnimator alphaAnimatorY = ObjectAnimator.ofFloat(mContainerView, "scaleY", 0.8f, 1f);
                set.setDuration(120);
                set.setInterpolator(new LinearInterpolator());
                set.playTogether(alphaAnimatorX, alphaAnimatorY);
                set.start();
            }
        });
        set.playTogether(alphaAnimatorX, alphaAnimatorY);
        set.start();
    }

    private void cleanAnim() {
        mContainerView.clearAnimation();
    }

    public void detach() {
        try {
            XposedLog.verbose("detach");
            mWm.removeViewImmediate(FloatView.this);
        } catch (Exception ignored) {
            XposedLog.wtf("Fail detach float view: " + Log.getStackTraceString(ignored));
        } finally {
        }
    }

    public boolean isShowing() {
        return getVisibility() == VISIBLE;
    }

    public void hideAndDetach() {
        if (!isShowing()) return;
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator alphaAnimatorX = ObjectAnimator.ofFloat(mContainerView, "scaleX", 1f, 0f);
        final ObjectAnimator alphaAnimatorY = ObjectAnimator.ofFloat(mContainerView, "scaleY", 1f, 0f);
        set.setDuration(200);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(INVISIBLE);
                detach();
            }
        });
        set.playTogether(alphaAnimatorX, alphaAnimatorY);
        set.start();
    }

    public void show() {
        if (isShowing()) return;
        setVisibility(VISIBLE);
        AnimatorSet set = new AnimatorSet();
        final ObjectAnimator alphaAnimatorX = ObjectAnimator.ofFloat(mContainerView, "scaleX", 0f, 1f);
        final ObjectAnimator alphaAnimatorY = ObjectAnimator.ofFloat(mContainerView, "scaleY", 0f, 1f);
        set.setDuration(300);
        set.setInterpolator(new LinearInterpolator());
        set.playTogether(alphaAnimatorX, alphaAnimatorY);
        set.start();
        if (BuildConfig.DEBUG) {
            XposedLog.verbose("FloatView show");
        }
    }

    private boolean isDragging, inDragMode;

    private int dp2px(int dp) {
        return (int) (dp * density);
    }

    public void reposition() {
        if (mLp.x < (mRect.width() - getWidth()) / 2) {
            mLp.x = 0;
        } else {
            mLp.x = mRect.width() - dp2px(getWidth());
        }
        if (mLp.y < mRect.top) {
            mLp.y = mRect.top;
        }

        mWm.updateViewLayout(this, mLp);
    }

    private int previousX, previousY;
    private boolean needRestoreOnImeHidden;

    public enum SwipeDirection {
        L, R, U, D
    }

    public interface Callback {
        void onSingleTap(String text);

        void onDoubleTap();

        void onSwipeDirection(@NonNull SwipeDirection direction);

        void onSwipeDirectionLargeDistance(@NonNull SwipeDirection direction);

        void onLongPress();
    }
}