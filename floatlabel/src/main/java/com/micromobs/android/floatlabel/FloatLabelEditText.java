package com.micromobs.android.floatlabel;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(11)
public class FloatLabelEditText
        extends LinearLayout {

    protected int mCurrentApiVersion = android.os.Build.VERSION.SDK_INT, mFocusedColor, mUnFocusedColor, mGravity;

    protected EditText mEditTextView;
    protected TextView mFloatingLabel;

    // -----------------------------------------------------------------------
    // default constructors

    public FloatLabelEditText(Context context) {
        super(context);
        initializeView(context, null, 0);
    }

    public FloatLabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs, 0);
    }

    public FloatLabelEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context, attrs, defStyle);
    }

    // -----------------------------------------------------------------------
    // public interface

    public EditText getEditText() {
        return mEditTextView;
    }

    public String getText() {
        if (getEditTextString() != null &&
                getEditTextString().toString() != null &&
                getEditTextString().toString().length() > 0) {
            return getEditTextString().toString();
        }
        return "";
    }

    public void setHint(String hintText) {
        mEditTextView.setHint(hintText);
        mFloatingLabel.setText(hintText);
    }

    // -----------------------------------------------------------------------
    // private helpers

    protected void initializeView(Context context, AttributeSet attrs, int defStyle) {

        if (context == null) {
            return;
        }

        setBackgroundColor(Color.TRANSPARENT);
        setOrientation(VERTICAL);
        setPadding(0, 0, 0, 0);
        setFocusable(false);

        mFloatingLabel = new TextView(context, null, defStyle);
        addView(mFloatingLabel);

        mEditTextView = new EditText(context, attrs, defStyle);
        addView(mEditTextView);

        getAttributesFromXmlAndStoreLocally(context, attrs);
        setupFloatingLabel(context, attrs, defStyle);
        setupEditTextView(context, attrs, defStyle);
    }

    protected void getAttributesFromXmlAndStoreLocally(Context context, AttributeSet attrs) {
        TypedArray attributesFromXmlLayout = context.obtainStyledAttributes(attrs,
                R.styleable.FloatLabelEditText);

        if (attributesFromXmlLayout == null) {
            return;
        }

        mGravity = attributesFromXmlLayout.getInt(R.styleable.FloatLabelEditText_gravity,
                Gravity.LEFT);
        mFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintFocused,
                android.R.color.black);
        mUnFocusedColor = attributesFromXmlLayout.getColor(R.styleable.FloatLabelEditText_textColorHintUnFocused,
                android.R.color.darker_gray);
        attributesFromXmlLayout.recycle();
    }

    protected void setupEditTextView(Context context, AttributeSet attrs, int defStyle) {
        mEditTextView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mEditTextView.setId(0);
        mEditTextView.setVisibility(View.VISIBLE);

        mEditTextView.addTextChangedListener(getTextWatcher());
        if (mCurrentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mEditTextView.setOnFocusChangeListener(getFocusChangeListener());
        }
    }

    protected void setupFloatingLabel(Context context, AttributeSet attrs, int defStyle) {
        mFloatingLabel.setVisibility(View.INVISIBLE);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.bottomMargin = -15;
        mFloatingLabel.setLayoutParams(layoutParams);

        mFloatingLabel.setText(mEditTextView.getHint());
        mFloatingLabel.setTextColor(mUnFocusedColor);
        mFloatingLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, getScaledFontSize(mEditTextView.getTextSize()) / 1.3f);
        mFloatingLabel.setGravity(mGravity);
        mFloatingLabel.setPadding(mEditTextView.getPaddingLeft(), 0, 0, 0);

        if (getText().length() > 0) {
            showFloatingLabel();
        }
    }

    protected TextWatcher getTextWatcher() {
        return new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0 && mFloatingLabel.getVisibility() == INVISIBLE) {
                    showFloatingLabel();
                } else if (s.length() == 0 && mFloatingLabel.getVisibility() == VISIBLE) {
                    hideFloatingLabel();
                }
            }
        };
    }

    protected void showFloatingLabel() {
        mFloatingLabel.setVisibility(VISIBLE);
        mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(),
                R.anim.weddingparty_floatlabel_slide_from_bottom));
    }

    protected void hideFloatingLabel() {
        mFloatingLabel.setVisibility(INVISIBLE);
        mFloatingLabel.startAnimation(AnimationUtils.loadAnimation(getContext(),
                R.anim.weddingparty_floatlabel_slide_to_bottom));
    }

    protected OnFocusChangeListener getFocusChangeListener() {
        return new OnFocusChangeListener() {

            ValueAnimator mFocusToUnfocusAnimation
                    ,
                    mUnfocusToFocusAnimation;

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ValueAnimator lColorAnimation;

                if (hasFocus) {
                    lColorAnimation = getFocusToUnfocusAnimation();
                } else {
                    lColorAnimation = getUnfocusToFocusAnimation();
                }

                lColorAnimation.setDuration(700);
                lColorAnimation.start();
            }

            private ValueAnimator getFocusToUnfocusAnimation() {
                if (mFocusToUnfocusAnimation == null) {
                    mFocusToUnfocusAnimation = getFocusAnimation(mUnFocusedColor, mFocusedColor);
                }
                return mFocusToUnfocusAnimation;
            }

            private ValueAnimator getUnfocusToFocusAnimation() {
                if (mUnfocusToFocusAnimation == null) {
                    mUnfocusToFocusAnimation = getFocusAnimation(mFocusedColor, mUnFocusedColor);
                }
                return mUnfocusToFocusAnimation;
            }
        };
    }

    protected ValueAnimator getFocusAnimation(int fromColor, int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                fromColor,
                toColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mFloatingLabel.setTextColor((Integer) animator.getAnimatedValue());
            }
        });
        return colorAnimation;
    }

    protected Editable getEditTextString() {
        return mEditTextView.getText();
    }

    protected float getScaledFontSize(float fontSizeFromAttributes) {
        float scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        return fontSizeFromAttributes / scaledDensity;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), mEditTextView.onSaveInstanceState(), mFloatingLabel.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mEditTextView.onRestoreInstanceState(savedState.getEditTextViewState());
        mFloatingLabel.onRestoreInstanceState(savedState.getFloatingLabelState());
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchThawSelfOnly(container);
    }


    protected static class SavedState extends BaseSavedState {
        private Parcelable mEditTextViewState;
        private Parcelable mFloatingLabelState;

        public SavedState(Parcelable superState, Parcelable mEditTextViewState, Parcelable mFloatingLabelState) {
            super(superState);
            this.mEditTextViewState = mEditTextViewState;
            this.mFloatingLabelState = mFloatingLabelState;
        }


        private SavedState(Parcel in) {
            super(in);
            this.mEditTextViewState = in.readParcelable(TextView.SavedState.class.getClassLoader());
            this.mFloatingLabelState = in.readParcelable(TextView.SavedState.class.getClassLoader());
        }

        public Parcelable getEditTextViewState() {
            return mEditTextViewState;
        }

        public Parcelable getFloatingLabelState() {
            return mFloatingLabelState;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeParcelable(this.mEditTextViewState, flags);
            destination.writeParcelable(this.mFloatingLabelState, flags);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        };

    }
}
