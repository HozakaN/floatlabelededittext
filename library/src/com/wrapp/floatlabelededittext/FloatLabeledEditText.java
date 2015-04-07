package com.wrapp.floatlabelededittext;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class FloatLabeledEditText extends FrameLayout {

    private static final int DEFAULT_PADDING_LEFT= 2;

    private static final int INVALID_TEXT_COLOR = -1;

    private TextView mExplicativeTextView;
    private TextView mHintTextView;

    private EditText mEditText;
    private Context mContext;

    private String mPassiveMessage;
    private String mValidMessage;
    private String mErrorMessage;

    private int mPassiveTextColor;
    private int mValidTextColor;
    private int mErrorTextColor;

    private int mPassiveTextAppearance;
    private int mValidTextAppearance;
    private int mErrorTextAppearance;

    private ColorStateList mDefaultColorStateList;
    private ColorStateList mDefaultHintTextColor;

    public FloatLabeledEditText(Context context) {
        super(context);
        mContext = context;
    }

    public FloatLabeledEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public FloatLabeledEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        setAttributes(attrs);
    }

    @SuppressLint("NewApi")
    private void setAttributes(AttributeSet attrs) {
        mHintTextView = new TextView(mContext);
        mDefaultHintTextColor = mHintTextView.getTextColors();

        mExplicativeTextView = new TextView(mContext);

        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.FloatLabeledEditText);

        final int padding = a.getDimensionPixelSize(R.styleable.FloatLabeledEditText_fletPadding, 0);
        final int defaultPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PADDING_LEFT, getResources().getDisplayMetrics());
        final int paddingLeft = a.getDimensionPixelSize(R.styleable.FloatLabeledEditText_fletPaddingLeft, defaultPadding);
        final int paddingTop = a.getDimensionPixelSize(R.styleable.FloatLabeledEditText_fletPaddingTop, 0);
        final int paddingRight = a.getDimensionPixelSize(R.styleable.FloatLabeledEditText_fletPaddingRight, 0);
        final int paddingBottom = a.getDimensionPixelSize(R.styleable.FloatLabeledEditText_fletPaddingBottom, 0);
        Drawable background = a.getDrawable(R.styleable.FloatLabeledEditText_fletBackground);

        mPassiveMessage = a.getString(R.styleable.FloatLabeledEditText_fletPassiveMessage);
        mValidMessage = a.getString(R.styleable.FloatLabeledEditText_fletValidMessage);
        mErrorMessage = a.getString(R.styleable.FloatLabeledEditText_fletErrorMessage);

        mPassiveTextAppearance = a.getResourceId(R.styleable.FloatLabeledEditText_fletExplicativeMessageTextAppearance, R.style.ExplicativeMessageDefaultStyle);
        mPassiveTextColor = a.getColor(R.styleable.FloatLabeledEditText_fletPassiveTextColor, INVALID_TEXT_COLOR);

        mValidTextAppearance = a.getResourceId(R.styleable.FloatLabeledEditText_fletValidTextAppearance, R.style.ExplicativeMessageDefaultStyle);
        mValidTextColor = a.getColor(R.styleable.FloatLabeledEditText_fletValidTextColor, INVALID_TEXT_COLOR);

        mErrorTextAppearance = a.getResourceId(R.styleable.FloatLabeledEditText_fletErrorTextAppearance, R.style.ExplicativeMessageDefaultStyle);
        mErrorTextColor = a.getColor(R.styleable.FloatLabeledEditText_fletErrorTextColor, INVALID_TEXT_COLOR);

        if (padding != 0) {
            mHintTextView.setPadding(padding, padding, padding, padding);
            mExplicativeTextView.setPadding(padding, padding, padding, padding);
        } else {
            mHintTextView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            mExplicativeTextView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }

        if (background != null) {
            setHintBackground(background);
            setExplicativeBackground(background);
        }

        mHintTextView.setTextAppearance(mContext, a.getResourceId(R.styleable.FloatLabeledEditText_fletHintTextAppearance, android.R.style.TextAppearance_Small));
        mExplicativeTextView.setTextAppearance(mContext, a.getResourceId(R.styleable.FloatLabeledEditText_fletExplicativeMessageTextAppearance, R.style.ExplicativeMessageDefaultStyle));

        //Start hidden
        mHintTextView.setVisibility(INVISIBLE);
        AnimatorProxy.wrap(mHintTextView).setAlpha(0);

        setPassiveMessageTextAndUpdateDisplay(mPassiveMessage);

        addView(mHintTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(mExplicativeTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        a.recycle();
    }

    @SuppressLint("NewApi")
    private void setHintBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mHintTextView.setBackground(background);
        } else {
            mHintTextView.setBackgroundDrawable(background);
        }
    }

    @SuppressLint("NewApi")
    private void setExplicativeBackground(Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mExplicativeTextView.setBackground(background);
        } else {
            mExplicativeTextView.setBackgroundDrawable(background);
        }
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            if (mEditText != null) {
                throw new IllegalArgumentException("Can only have one Edittext subview");
            }

            final LayoutParams lp = new LayoutParams(params);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            lp.topMargin = (int) (mHintTextView.getTextSize() + mHintTextView.getPaddingBottom() + mHintTextView.getPaddingTop());
            params = lp;

            setEditText((EditText) child);
        }

        super.addView(child, index, params);
    }

    private void positionExplicativeMessageTextView(View child) {
        ViewGroup.LayoutParams currentParams = child.getLayoutParams();
        final LayoutParams lp = new LayoutParams(currentParams);
        lp.gravity = Gravity.BOTTOM;
        lp.topMargin = (int) (mHintTextView.getTextSize() + mHintTextView.getPaddingTop() + mHintTextView.getPaddingBottom()
                                + mEditText.getTextSize() + mEditText.getPaddingTop() + mEditText.getPaddingBottom()
                                + mExplicativeTextView.getTextSize() / 8);
        lp.bottomMargin = (int) mExplicativeTextView.getTextSize() / 8;
        child.setLayoutParams(lp);
    }

    private void setEditText(EditText editText) {
        mEditText = editText;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mDefaultColorFilter = mEditText.getBackground().getColorFilter();
            mDefaultColorStateList = mEditText.getBackgroundTintList();
        }

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                setShowHint(!TextUtils.isEmpty(s));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean gotFocus) {
                onFocusChanged(gotFocus);
            }
        });

        mHintTextView.setText(mEditText.getHint());

        if(!TextUtils.isEmpty(mEditText.getText())){
            mHintTextView.setVisibility(VISIBLE);
        }

        positionExplicativeMessageTextView(mExplicativeTextView);
    }

    private void onFocusChanged(boolean gotFocus) {
        if (gotFocus && mHintTextView.getVisibility() == VISIBLE) {
            ObjectAnimator.ofFloat(mHintTextView, "alpha", 0.33f, 1f).start();
        } else if (mHintTextView.getVisibility() == VISIBLE) {
            AnimatorProxy.wrap(mHintTextView).setAlpha(1f);  //Need this for compat reasons
            ObjectAnimator.ofFloat(mHintTextView, "alpha", 1f, 0.33f).start();
        }

        if (gotFocus && mExplicativeTextView.getVisibility() == VISIBLE) {
            ObjectAnimator.ofFloat(mExplicativeTextView, "alpha", 0.33f, 1f).start();
        } else if (mExplicativeTextView.getVisibility() == VISIBLE) {
            AnimatorProxy.wrap(mExplicativeTextView).setAlpha(1f);  //Need this for compat reasons
            ObjectAnimator.ofFloat(mExplicativeTextView, "alpha", 1f, 0.33f).start();
        }
    }

    private void setShowHint(final boolean show) {
        AnimatorSet animation = null;
        if ((mHintTextView.getVisibility() == VISIBLE) && !show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mHintTextView, "translationY", 0, mHintTextView.getHeight() / 8);
            ObjectAnimator fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 1, 0);
            animation.playTogether(move, fade);
        } else if ((mHintTextView.getVisibility() != VISIBLE) && show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mHintTextView, "translationY", mHintTextView.getHeight() / 8, 0);
            ObjectAnimator fade;
            if (mEditText.isFocused()) {
                fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 0, 1);
            } else {
                fade = ObjectAnimator.ofFloat(mHintTextView, "alpha", 0, 0.33f);
            }
            animation.playTogether(move, fade);
        }

        if (animation != null) {
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mHintTextView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mHintTextView.setVisibility(show ? VISIBLE : INVISIBLE);
                    AnimatorProxy.wrap(mHintTextView).setAlpha(show ? 1 : 0);
                }
            });
            animation.start();
        }
    }

    private void setShowExplicativeMessage(final boolean show) {
        AnimatorSet animation = null;
        if ((mExplicativeTextView.getVisibility() == VISIBLE) && !show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mExplicativeTextView, "translationY", mExplicativeTextView.getHeight() / 8, 0);
            ObjectAnimator fade = ObjectAnimator.ofFloat(mExplicativeTextView, "alpha", 1, 0);
            animation.playTogether(move, fade);
        } else if ((mExplicativeTextView.getVisibility() != VISIBLE) && show) {
            animation = new AnimatorSet();
            ObjectAnimator move = ObjectAnimator.ofFloat(mExplicativeTextView, "translationY", 0, mExplicativeTextView.getHeight() / 8);
            ObjectAnimator fade;
            if (mEditText.isFocused()) {
                fade = ObjectAnimator.ofFloat(mExplicativeTextView, "alpha", 0, 1);
            } else {
                fade = ObjectAnimator.ofFloat(mExplicativeTextView, "alpha", 0, 0.33f);
            }
            animation.playTogether(move, fade);
        }

        if (animation != null) {
            animation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mExplicativeTextView.setVisibility(VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mExplicativeTextView.setVisibility(show ? VISIBLE : INVISIBLE);
                    AnimatorProxy.wrap(mExplicativeTextView).setAlpha(show ? 1 : 0);
                }
            });
            animation.start();
        }
    }

    public EditText getEditText() {
        return mEditText;
    }

    public void setHint(String hint) {
        mEditText.setHint(hint);
        mHintTextView.setText(hint);
    }

    public CharSequence getHint() {
        return mHintTextView.getHint();
    }

    public void setPassiveMessageTextAndUpdateDisplay(String passiveMessage) {
        setPassiveMessageText(passiveMessage);
        mExplicativeTextView.setText(mPassiveMessage);
        if (TextUtils.isEmpty(mPassiveMessage)) {
            mExplicativeTextView.setVisibility(INVISIBLE);
            AnimatorProxy.wrap(mExplicativeTextView).setAlpha(0);
        } else {
            mExplicativeTextView.setVisibility(VISIBLE);
            AnimatorProxy.wrap(mExplicativeTextView).setAlpha(1);
        }
    }

    public void setPassiveMessageText(String passiveMessage) {
        mPassiveMessage = passiveMessage;
    }

    public void setValidMessageText(String validMessage) {
        mValidMessage = validMessage;
    }

    public void setErrorMessageText(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public String getPassiveMessage() {
        return mPassiveMessage;
    }

    public void setValid(String validMessage) {
        setValidMessageText(validMessage);
        setValid();
    }

    public void setError(String errorMessage) {
        setErrorMessageText(errorMessage);
        setError();
    }

    public void setValid() {
        mExplicativeTextView.setTextAppearance(mContext, mValidTextAppearance);

        if (!TextUtils.isEmpty(mValidMessage)) {
            mExplicativeTextView.setText(mValidMessage);
        }

        if (mValidTextColor != INVALID_TEXT_COLOR) {
            mExplicativeTextView.setTextColor(mValidTextColor);
            mHintTextView.setTextColor(mValidTextColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getEditText().setBackgroundTintList(ColorStateList.valueOf(mValidTextColor));
            } else {
                getEditText().getBackground().setColorFilter(mValidTextColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public void setError() {
        mExplicativeTextView.setTextAppearance(mContext, mErrorTextAppearance);

        if (!TextUtils.isEmpty(mErrorMessage)) {
            mExplicativeTextView.setText(mErrorMessage);
        }

        if (mErrorTextColor != INVALID_TEXT_COLOR) {
            mExplicativeTextView.setTextColor(mErrorTextColor);
            mHintTextView.setTextColor(mErrorTextColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getEditText().setBackgroundTintList(ColorStateList.valueOf(mErrorTextColor));
            } else {
                getEditText().getBackground().setColorFilter(mErrorTextColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    public void setPassive() {
        mExplicativeTextView.setTextAppearance(mContext, mPassiveTextAppearance);
        if (mPassiveTextColor != INVALID_TEXT_COLOR) {
            mExplicativeTextView.setTextColor(mPassiveTextColor);
        }
        mHintTextView.setTextColor(mDefaultHintTextColor);
        mExplicativeTextView.setText(mPassiveMessage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getEditText() != null) {
                getEditText().setBackgroundTintList(mDefaultColorStateList);
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (getEditText() != null) {
//                if (mDefaultColorFilter != null) {
//                    getEditText().getBackground().setColorFilter(mDefaultColorFilter);
//                } else {
                    getEditText().getBackground().clearColorFilter();
//                }
            }
        }
    }

}
