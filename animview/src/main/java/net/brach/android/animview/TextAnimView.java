package net.brach.android.animview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.view.Gravity.BOTTOM;
import static android.view.Gravity.CENTER;

public class TextAnimView extends RelativeLayout {
    // text style
    private static final int SANS = 1;
    private static final int SERIF = 2;
    private static final int MONOSPACE = 3;

    // animation type
//    private static final int BOUNCE = 0;

    private TextView textView;
    private String text;
    private @ColorInt int textColor;

    private final float textSize;
    private final int typefaceIndex;
    private String fontFamily;
    private final int textStyleIndex;
    private final boolean textAllCaps;
    private final int textAnimationDuration;

    private boolean started;

    private LinearLayout container;
    private int titleCount;
    private int min, max;
    private final int bounceEffect;
    private final int bounceDuration;
    private final int bounceReloadDuration;

    private int width = -1;
    private int height = -1;

    public TextAnimView(Context context) {
        this(context, null);
    }

    public TextAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextAnimView(final Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextAnimView, 0, 0);
        text = a.getString(R.styleable.TextAnimView_tva_text);
        textColor = a.getColor(R.styleable.TextAnimView_tva_textColor, Color.BLACK);

        textSize = a.getDimensionPixelSize(R.styleable.TextAnimView_tva_textSize, 10);
        typefaceIndex = a.getInt(R.styleable.TextAnimView_tva_typeface, -1);
        fontFamily = a.getString(R.styleable.TextAnimView_tva_fontFamily);
        textStyleIndex = a.getInt(R.styleable.TextAnimView_tva_textStyle, 0);
        textAllCaps = a.getBoolean(R.styleable.TextAnimView_tva_textAllCaps, false);
        textAnimationDuration = a.getInt(R.styleable.TextAnimView_tva_text_animation_duration, 300);

//        int animationIdx = a.getInt(R.styleable.TextAnimView_tva_animation, BOUNCE);

        bounceEffect = a.getDimensionPixelSize(R.styleable.TextAnimView_tva_bounce_effect,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics));
        bounceDuration = a.getInt(R.styleable.TextAnimView_tva_bounce_duration, 100);
        bounceReloadDuration = a.getInt(R.styleable.TextAnimView_tva_bounce_reload_duration, 5 * bounceDuration);
        a.recycle();

        if (typefaceIndex != -1) {
            fontFamily = null;
        }

        if (textAllCaps) {
            text = text.toUpperCase();
        }

        if (isInEditMode()) {
            TextView textView = createTextView(context, text);
            addView(textView);
            ((MarginLayoutParams) textView.getLayoutParams()).setMargins(0, bounceEffect / 2, 0, 0);
            return;
        }

        textView = createTextView(getContext(), text);
        addView(textView);
        ((MarginLayoutParams) textView.getLayoutParams()).setMargins(0, bounceEffect / 2, 0, 0);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(CENTER);
        addView(container);
        init(false, -1, -1);
    }

    public void start() {
        if (!started && isEnabled()) {
            started = true;
            titleCount = container.getChildCount();

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    animate(0);
                }
            });
        }
    }

    public void stop() {
        started = false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            stop();
        }
    }

    public void setTextAndColor(String text, @ColorInt int color) {
        setTextAndColor(text, color, false);
    }

    public void setTextAndColor(String text, int color, boolean animate) {
        this.text = textAllCaps ? text.toUpperCase() : text;
        int prev = this.textColor;
        this.textColor = color;
        init(animate, animate ? prev : -1, color);
    }

    public void setText(String text) {
        setText(text, false);
    }

    public void setText(String text, boolean animate) {
        this.text = this.textAllCaps ? text.toUpperCase() : text;
        init(animate, -1, -1);
    }

    public void setTextColor(@ColorInt int color) {
        setTextColor(color, false);
    }

    public void setTextColor(@ColorInt int color, boolean animate) {
        int prev = this.textColor;
        this.textColor = color;
        init(false, animate ? prev : -1, color);
    }

    public void setMaxLines(int maxlines) {
        textView.setMaxLines(maxlines);
        textView.setSingleLine(maxlines == 1);
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        textView.setEllipsize(where);
    }

    public boolean getEllipsisOverflow() {
        return textView.getLayout() != null && textView.getLayout().getEllipsisCount(0) != 0;
    }

    public String getText() {
        return text;
    }

    /****************************/
    /** {@link RelativeLayout} **/
    /****************************/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (width == -1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(width, height);
        }
    }

    /*************/
    /** private **/
    /*************/

    private void init(boolean text, @ColorInt int previousColor, @ColorInt int newColor) {
        if (text) {
            if (previousColor == -1) { // text only
                animatorText().start();
            } else { // text and color
                final ValueAnimator textAnim = animatorText();

                ValueAnimator animator = animatorColor(previousColor, newColor);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        textAnim.start();
                    }
                });
                animator.start();
            }
        } else if (previousColor != -1) { // color only
            ValueAnimator animator = animatorColor(previousColor, newColor);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    init();
                }
            });
            animator.start();
        } else { // no animation
            init();
        }
    }

    private void init() {
        textView.setText(this.text);
        textView.setAlpha(1);
        ((MarginLayoutParams) textView.getLayoutParams()).setMargins(0, bounceEffect / 2, 0, 0);

        createTextBounce();

        addOnGlobalLayoutListener(textView, new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeOnGlobalLayoutListener(textView, this);

                min = 0;
                max = bounceEffect;

                width = textView.getWidth();
                height = textView.getHeight() + max;

                container.setLayoutParams(new LayoutParams(width, height));

                container.setAlpha(1);
                textView.setAlpha(0);
                invalidate();
            }
        });
    }

    private ValueAnimator animatorColor(@ColorInt int previousColor, @ColorInt int newColor) {
        ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), previousColor, newColor);
        colorAnim.setDuration(textAnimationDuration);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setTextColor((int) animation.getAnimatedValue());
            }
        });
        return colorAnim;
    }

    private ValueAnimator animatorText() {
        final ValueAnimator fadeIn = ValueAnimator.ofFloat(0, 1);
        fadeIn.setDuration(textAnimationDuration / 2);
        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setAlpha((float) animation.getAnimatedValue());
            }
        });
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.setText(TextAnimView.this.text);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                init();
            }
        });

        ValueAnimator fadeOut = ValueAnimator.ofFloat(1, 0);
        fadeOut.setDuration(textAnimationDuration / 2);
        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textView.setAlpha((float) animation.getAnimatedValue());
            }
        });
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.setAlpha(1);
                container.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fadeIn.start();
            }
        });
        return fadeOut;
    }

    private void createTextBounce() {
        if (text != null) {
            container.removeAllViews();
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (char c : text.toCharArray()) {
                container.addView(createLetter(getContext(), "" + c));
            }
        }
    }

    private void animate(final int idx) {
        if (started) {
            final View letter = ((ViewGroup) container.getChildAt(idx)).getChildAt(0);
            final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) letter.getLayoutParams();

            ValueAnimator anim = ValueAnimator.ofInt(min, max);

            anim.setInterpolator(new DecelerateInterpolator());
            anim.setDuration(bounceDuration);

            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    params.setMargins(0, 0, 0, (int) animation.getAnimatedValue());
                    letter.requestLayout();
                }
            });
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    int next = idx + 1;
                    if (next < titleCount) {
                        animate(next, idx);
                    } else {
                        shrink(idx);
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animate(0);
                            }
                        }, bounceReloadDuration);
                    }
                }
            });

            anim.start();
        }
    }

    private void animate(final int idx, int prev) {
        animate(idx);
        shrink(prev);
    }

    private void shrink(int idx) {
        final View previous = ((ViewGroup) container.getChildAt(idx)).getChildAt(0);
        final LinearLayout.LayoutParams previousParams = (LinearLayout.LayoutParams) previous.getLayoutParams();

        ValueAnimator anim = ValueAnimator.ofInt(max, min);

        anim.setInterpolator(new BounceInterpolator());
        anim.setDuration(3 * bounceDuration);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                previousParams.setMargins(0, 0, 0, (int) animation.getAnimatedValue());
                previous.requestLayout();
            }
        });

        anim.start();
    }

    @NonNull
    private LinearLayout createLetter(Context ctx, String c) {
        LinearLayout container = new LinearLayout(ctx);
        container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        container.setGravity(BOTTOM);

        TextView letter = createTextView(getContext(), c);
        letter.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        container.addView(letter);
        return container;
    }

    private TextView createTextView(Context ctx, String text) {
        TextView textView = new TextView(ctx);
        textView.setTextColor(textColor);
        textView.setTextSize(textSize);
        textView.setText(text);
        setTypeface(textView);
        return textView;
    }

    private void setTypeface(TextView textView) {
        Typeface tf = null;
        if (fontFamily != null) {
            tf = Typeface.create(fontFamily, textStyleIndex);
            if (tf != null) {
                textView.setTypeface(tf);
                return;
            }
        }
        switch (typefaceIndex) {
            case SANS:
                tf = Typeface.SANS_SERIF;
                break;

            case SERIF:
                tf = Typeface.SERIF;
                break;

            case MONOSPACE:
                tf = Typeface.MONOSPACE;
                break;
        }

        textView.setTypeface(tf, textStyleIndex);
    }

    private static void addOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    @SuppressWarnings("deprecation")
    private static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }
}
