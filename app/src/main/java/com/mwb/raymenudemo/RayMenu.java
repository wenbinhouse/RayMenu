package com.mwb.raymenudemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by wbMa on 16/5/3.
 */
public class RayMenu extends ViewGroup {

    private Context mContext;
    // 布局的宽
    private int mMeasuredWidth;
    // 布局的高
    private int mMeasuredHeight;
    // 实际布局的宽，已经减去左右的padding
    private int mActualWidth;
    private int mPaddingLeft;
    private int mPaddingRight;

    // menu item 水平间隔
    private int mHorizontalSpace;
    // menu item 垂直间隔
    private int mVerticalSpace;

    // menu开关的按钮
    private View mToggleBtn;

    // menu状态
    private boolean mIsOpen;
    // menu的行数
    private int mLine;
    // 子View的宽
    private int mCWidth;
    // 子View的高
    private int mCHeight;

    private OnMenuItemClickListener mListener;
    private View mClickView;
    private int mPos;

    public RayMenu(Context context) {
        super(context);
    }

    public RayMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RayMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 当前ViewGroup的宽度和高度
        mMeasuredWidth = getMeasuredWidth();
        mMeasuredHeight = getMeasuredHeight();

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();

        // ViewGroup实际可显示的宽，减去左右的padding
        mActualWidth = mMeasuredWidth - mPaddingLeft - mPaddingRight;

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // changed参数，当前ViewGroup的尺寸或者位置是否发生了改变
        if (!changed) {
            return;
        }

        // 第一个子View，作为menu的开关button
        View firstChildView = getChildAt(0);

        // 所有子View的宽高都一致
        mCWidth = firstChildView.getMeasuredWidth();
        mCHeight = firstChildView.getMeasuredHeight();

        // 水平间隔
        mHorizontalSpace = (mActualWidth - 3 * mCWidth) / 2;
        // 垂直间隔
        mVerticalSpace = dip2px(mContext, 40);

        // 绘制第一个子View
        layoutFirstChildView(firstChildView);

        int childCount = getChildCount();

        // 子View的行数
        mLine = (int) Math.ceil(childCount / 3.0f);

        for (int i = 1; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setVisibility(GONE);

            // 标记当前子View的所在行
            int lineTag = (i - 1) / mLine;

            // 水平偏移量
            int horizontalOffset = (i - 1 - lineTag * 3) * (mHorizontalSpace + mCWidth);
            // 垂直偏移量
            int verticalOffset = (2 - lineTag) * mVerticalSpace;

            int left = horizontalOffset + mPaddingLeft;
            int top = mMeasuredHeight - (2 - lineTag) * mCHeight - verticalOffset;
            int right = left + mCWidth;
            int bottom = mMeasuredHeight - (1 - lineTag) * mCHeight - verticalOffset;

            childView.layout(left, top, right, bottom);
        }
    }

    /**
     * 绘制第一个子View
     * 作为menu的开关button
     */
    private void layoutFirstChildView(View firstChildView) {
        int cWidth = firstChildView.getMeasuredWidth();
        int cHeight = firstChildView.getMeasuredHeight();

        int left = 2 * mHorizontalSpace + 2 * cWidth + mPaddingLeft;
        int top = mMeasuredHeight - cHeight - mVerticalSpace;
        int right = left + cWidth;
        int bottom = mMeasuredHeight - mVerticalSpace;

        firstChildView.layout(left, top, right, bottom);

        // 点击事件
        firstChildView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mToggleBtn = getChildAt(0);
                toggleMenu(300);

                changeStatus(true);
            }
        });
    }

    /**
     * 开关menu
     */
    private void toggleMenu(int durationMillis) {
        int childCount = getChildCount();

        for (int i = 1; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setVisibility(VISIBLE);

            // 标记当前子View的所在行
            int lineTag = (i - 1) / mLine;
            // 垂直偏移量
            int verticalOffset = (2 - lineTag) * mVerticalSpace;
            int top = mMeasuredHeight - (2 - lineTag) * mCHeight - verticalOffset;

            // 创建并且绑定menu动画
            createBindMenuAnim(childView, childCount, i, top, durationMillis);

            childView.setTag(i);
            // 子View点击事件
            childView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    bindMenuItemAnim(v, (Integer) v.getTag());
                }
            });
        }
    }

    /**
     * menu动画
     *
     * @param childView 子View
     * @param top fromYDelta、toYDelta
     * @param i 当前子View的位置
     * @param durationMillis 动画时间
     * @return
     */
    private void createBindMenuAnim(final View childView, int childCount, int i, int top, int durationMillis) {
        AnimationSet animset = new AnimationSet(true);
        Animation animation = null;

        if (!mIsOpen) {
            // 打开menu
            animset.setInterpolator(new OvershootInterpolator(1.5F));
            animation = new TranslateAnimation(0, 0, top, 0);
            childView.setClickable(true);
            childView.setFocusable(true);

        } else {
            // 关闭menu
            animation = new TranslateAnimation(0, 0, 0, top);
            childView.setClickable(false);
            childView.setFocusable(false);
        }

        // 当menu关闭时隐藏所有的子View
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                if (!mIsOpen) {
                    childView.setVisibility(View.GONE);
                }
            }
        });

        animation.setFillAfter(true);
        animation.setDuration(durationMillis);
        // 设置动画开始的延迟时间
        animation.setStartOffset((i * 100) / (childCount - 1));
        animset.addAnimation(animation);
        childView.startAnimation(animset);
    }

    /**
     * 绑定子View动画
     */
    private void bindMenuItemAnim(View clickView, int pos) {
        mClickView = clickView;
        mPos = pos;
        Animation animation = null;
        for (int i = 1; i < getChildCount(); i++) {
            final View childView = getChildAt(i);
            if (pos == i) {
                // 当前点击的子View
                animation = createChildViewAnim(true, 300);
            } else {
                // 其他未被点击的字View
                animation = createChildViewAnim(false, 300);
            }

            childView.startAnimation(animation);
            childView.setClickable(false);
            childView.setFocusable(false);
        }

//        changeStatus(false);

        mIsOpen = false;
        Animation anim = new ScaleAnimation(0f, 1.0f, 0, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(300);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mListener != null) {
                    mListener.onClick(mClickView, mPos);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mToggleBtn.startAnimation(anim);
    }

    /**
     * 子View动画
     * 缩小消失，透明度降低
     */
    private Animation createChildViewAnim(boolean isClick, int durationMillis) {
        Animation anim = isClick ? new AlphaAnimation(1, 0) :
                new ScaleAnimation(1.0f, 0f, 1.0f, 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(durationMillis);
        anim.setFillAfter(true);
        return anim;
    }

    /**
     * menu状态改变
     */
    private void changeStatus(boolean isOpen) {
        mIsOpen = isOpen;

        // menu开关按钮显示隐藏
        if (!mIsOpen) {
            Animation anim = new ScaleAnimation(0f, 1.0f, 0, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setDuration(300);
            anim.setFillAfter(true);
            mToggleBtn.startAnimation(anim);

        } else {
            mToggleBtn.startAnimation(createChildViewAnim(false, 300));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        // 设置touch事件，点击背景关闭menu，只有当menu已经打开才有效
        if (mIsOpen && action == MotionEvent.ACTION_DOWN) {
            toggleMenu(300);

            changeStatus(false);
        }
        return super.onTouchEvent(event);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

    public interface OnMenuItemClickListener {
        void onClick(View view, int pos);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
