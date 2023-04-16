package com.example.concretemixcalculator.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.List;

public abstract class Utils {
    public static final double DEG2RAD = 0.017453292519943295d;
    public static final double DOUBLE_EPSILON = Double.longBitsToDouble(1);
    public static final float FDEG2RAD = 0.017453292f;
    public static final float FLOAT_EPSILON = Float.intBitsToFloat(1);
    private static final int[] POW_10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    private static Rect mCalcTextHeightRect = new Rect();
    private static Rect mCalcTextSizeRect = new Rect();
    private static ValueFormatter mDefaultValueFormatter = generateDefaultValueFormatter();
    private static Rect mDrawTextRectBuffer = new Rect();
    private static Rect mDrawableBoundsCache = new Rect();
    private static Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();
    private static Paint.FontMetrics mFontMetricsBuffer = new Paint.FontMetrics();
    private static int mMaximumFlingVelocity = 8000;
    private static DisplayMetrics mMetrics = null;
    private static int mMinimumFlingVelocity = 50;

    public static float getNormalizedAngle(float f) {
        while (f < 0.0f) {
            f += 360.0f;
        }
        return f % 360.0f;
    }

    public static void init(Context context) {
        if (context == null) {
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
            Log.e("MPChartLib-Utils", "Utils.init(...) PROVIDED CONTEXT OBJECT IS NULL");
            return;
        }
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        mMetrics = context.getResources().getDisplayMetrics();
    }

    @Deprecated
    public static void init(Resources resources) {
        mMetrics = resources.getDisplayMetrics();
        mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
    }

    public static float convertDpToPixel(float f) {
        DisplayMetrics displayMetrics = mMetrics;
        if (displayMetrics != null) {
            return f * displayMetrics.density;
        }
        Log.e("MPChartLib-Utils", "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before calling Utils.convertDpToPixel(...). Otherwise conversion does not take place.");
        return f;
    }

    public static float convertPixelsToDp(float f) {
        DisplayMetrics displayMetrics = mMetrics;
        if (displayMetrics != null) {
            return f / displayMetrics.density;
        }
        Log.e("MPChartLib-Utils", "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before calling Utils.convertPixelsToDp(...). Otherwise conversion does not take place.");
        return f;
    }

    public static int calcTextWidth(Paint paint, String str) {
        return (int) paint.measureText(str);
    }

    public static int calcTextHeight(Paint paint, String str) {
        Rect rect = mCalcTextHeightRect;
        rect.set(0, 0, 0, 0);
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();
    }

    public static float getLineHeight(Paint paint) {
        return getLineHeight(paint, mFontMetrics);
    }

    public static float getLineHeight(Paint paint, Paint.FontMetrics fontMetrics) {
        paint.getFontMetrics(fontMetrics);
        return fontMetrics.descent - fontMetrics.ascent;
    }

    public static float getLineSpacing(Paint paint) {
        return getLineSpacing(paint, mFontMetrics);
    }

    public static float getLineSpacing(Paint paint, Paint.FontMetrics fontMetrics) {
        paint.getFontMetrics(fontMetrics);
        return (fontMetrics.ascent - fontMetrics.top) + fontMetrics.bottom;
    }

    public static FSize calcTextSize(Paint paint, String str) {
        FSize instance = FSize.getInstance(0.0f, 0.0f);
        calcTextSize(paint, str, instance);
        return instance;
    }

    public static void calcTextSize(Paint paint, String str, FSize fSize) {
        Rect rect = mCalcTextSizeRect;
        rect.set(0, 0, 0, 0);
        paint.getTextBounds(str, 0, str.length(), rect);
        fSize.width = (float) rect.width();
        fSize.height = (float) rect.height();
    }

    private static ValueFormatter generateDefaultValueFormatter() {
        return new DefaultValueFormatter(1);
    }

    public static ValueFormatter getDefaultValueFormatter() {
        return mDefaultValueFormatter;
    }

    public static String formatNumber(float f, int i, boolean z) {
        return formatNumber(f, i, z, '.');
    }

    public static String formatNumber(float f, int i, boolean z, char c) {
        boolean z2;
        float f2 = f;
        char[] cArr = new char[35];
        if (f2 == 0.0f) {
            return "0";
        }
        int i2 = 0;
        boolean z3 = f2 < 1.0f && f2 > -1.0f;
        if (f2 < 0.0f) {
            f2 = -f2;
            z2 = true;
        } else {
            z2 = false;
        }
        int[] iArr = POW_10;
        int i3 = i;
        int length = i3 > iArr.length ? iArr.length - 1 : i3;
        long round = (long) Math.round(f2 * ((float) POW_10[length]));
        int i4 = 34;
        long j = round;
        boolean z4 = false;
        while (true) {
            if (j == 0 && i2 >= length + 1) {
                break;
            }
            int i5 = (int) (j % 10);
            j /= 10;
            int i6 = i4 - 1;
            cArr[i4] = (char) (i5 + 48);
            i2++;
            if (i2 == length) {
                i4 = i6 - 1;
                cArr[i6] = ',';
                i2++;
                z4 = true;
            } else {
                if (z && j != 0 && i2 > length) {
                    if (z4) {
                        if ((i2 - length) % 4 == 0) {
                            i4 = i6 - 1;
                            cArr[i6] = c;
                        }
                    } else if ((i2 - length) % 4 == 3) {
                        i4 = i6 - 1;
                        cArr[i6] = c;
                    }
                    i2++;
                }
                i4 = i6;
            }
        }
        if (z3) {
            cArr[i4] = '0';
            i2++;
            i4--;
        }
        if (z2) {
            cArr[i4] = '-';
            i2++;
        }
        int i7 = 35 - i2;
        return String.valueOf(cArr, i7, 35 - i7);
    }

    public static float roundToNextSignificant(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d) || d == DOUBLE_EPSILON) {
            return 0.0f;
        }
        float pow = (float) Math.pow(10.0d, (double) (1 - ((int) ((float) Math.ceil((double) ((float) Math.log10(d < DOUBLE_EPSILON ? -d : d)))))));
        return ((float) Math.round(d * ((double) pow))) / pow;
    }

    public static int getDecimals(float f) {
        float roundToNextSignificant = roundToNextSignificant((double) f);
        if (Float.isInfinite(roundToNextSignificant)) {
            return 0;
        }
        return ((int) Math.ceil(-Math.log10((double) roundToNextSignificant))) + 2;
    }

    public static int[] convertIntegers(List<Integer> list) {
        int[] iArr = new int[list.size()];
        copyIntegers(list, iArr);
        return iArr;
    }

    public static void copyIntegers(List<Integer> list, int[] iArr) {
        int length = iArr.length < list.size() ? iArr.length : list.size();
        for (int i = 0; i < length; i++) {
            iArr[i] = list.get(i).intValue();
        }
    }

    public static String[] convertStrings(List<String> list) {
        int size = list.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = list.get(i);
        }
        return strArr;
    }

    public static void copyStrings(List<String> list, String[] strArr) {
        int length = strArr.length < list.size() ? strArr.length : list.size();
        for (int i = 0; i < length; i++) {
            strArr[i] = list.get(i);
        }
    }

    public static double nextUp(double d) {
        if (d == Double.POSITIVE_INFINITY) {
            return d;
        }
        double d2 = d + DOUBLE_EPSILON;
        return Double.longBitsToDouble(Double.doubleToRawLongBits(d2) + (d2 >= DOUBLE_EPSILON ? 1 : -1));
    }

    public static MPPointF getPosition(MPPointF mPPointF, float f, float f2) {
        MPPointF instance = MPPointF.getInstance(0.0f, 0.0f);
        getPosition(mPPointF, f, f2, instance);
        return instance;
    }

    public static void getPosition(MPPointF mPPointF, float f, float f2, MPPointF mPPointF2) {
        double d = (double) f;
        double d2 = (double) f2;
        mPPointF2.f127x = (float) (((double) mPPointF.f127x) + (Math.cos(Math.toRadians(d2)) * d));
        mPPointF2.f128y = (float) (((double) mPPointF.f128y) + (d * Math.sin(Math.toRadians(d2))));
    }

    public static void velocityTrackerPointerUpCleanUpIfNecessary(MotionEvent motionEvent, VelocityTracker velocityTracker) {
        velocityTracker.computeCurrentVelocity(1000, (float) mMaximumFlingVelocity);
        int actionIndex = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(actionIndex);
        float xVelocity = velocityTracker.getXVelocity(pointerId);
        float yVelocity = velocityTracker.getYVelocity(pointerId);
        int pointerCount = motionEvent.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            if (i != actionIndex) {
                int pointerId2 = motionEvent.getPointerId(i);
                if ((velocityTracker.getXVelocity(pointerId2) * xVelocity) + (velocityTracker.getYVelocity(pointerId2) * yVelocity) < 0.0f) {
                    velocityTracker.clear();
                    return;
                }
            }
        }
    }

    public static void postInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.postInvalidateOnAnimation();
        } else {
            view.postInvalidateDelayed(10);
        }
    }

    public static int getMinimumFlingVelocity() {
        return mMinimumFlingVelocity;
    }

    public static int getMaximumFlingVelocity() {
        return mMaximumFlingVelocity;
    }

    public static void drawImage(Canvas canvas, Drawable drawable, int i, int i2, int i3, int i4) {
        MPPointF instance = MPPointF.getInstance();
        instance.f127x = (float) (i - (i3 / 2));
        instance.f128y = (float) (i2 - (i4 / 2));
        drawable.copyBounds(mDrawableBoundsCache);
        drawable.setBounds(mDrawableBoundsCache.left, mDrawableBoundsCache.top, mDrawableBoundsCache.left + i3, mDrawableBoundsCache.top + i3);
        int save = canvas.save();
        canvas.translate(instance.f127x, instance.f128y);
        drawable.draw(canvas);
        canvas.restoreToCount(save);
    }

    public static void drawXAxisValue(Canvas canvas, String str, float f, float f2, Paint paint, MPPointF mPPointF, float f3) {
        float fontMetrics = paint.getFontMetrics(mFontMetricsBuffer);
        paint.getTextBounds(str, 0, str.length(), mDrawTextRectBuffer);
        float f4 = 0.0f - ((float) mDrawTextRectBuffer.left);
        float f5 = (-mFontMetricsBuffer.ascent) + 0.0f;
        Paint.Align textAlign = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.LEFT);
        if (f3 != 0.0f) {
            float width = f4 - (((float) mDrawTextRectBuffer.width()) * 0.5f);
            float f6 = f5 - (fontMetrics * 0.5f);
            if (!(mPPointF.f127x == 0.5f && mPPointF.f128y == 0.5f)) {
                FSize sizeOfRotatedRectangleByDegrees = getSizeOfRotatedRectangleByDegrees((float) mDrawTextRectBuffer.width(), fontMetrics, f3);
                f -= sizeOfRotatedRectangleByDegrees.width * (mPPointF.f127x - 0.5f);
                f2 -= sizeOfRotatedRectangleByDegrees.height * (mPPointF.f128y - 0.5f);
                FSize.recycleInstance(sizeOfRotatedRectangleByDegrees);
            }
            canvas.save();
            canvas.translate(f, f2);
            canvas.rotate(f3);
            canvas.drawText(str, width, f6, paint);
            canvas.restore();
        } else {
            if (!(mPPointF.f127x == 0.0f && mPPointF.f128y == 0.0f)) {
                f4 -= ((float) mDrawTextRectBuffer.width()) * mPPointF.f127x;
                f5 -= fontMetrics * mPPointF.f128y;
            }
            canvas.drawText(str, f4 + f, f5 + f2, paint);
        }
        paint.setTextAlign(textAlign);
    }

    public static void drawMultilineText(Canvas canvas, StaticLayout staticLayout, float f, float f2, TextPaint textPaint, MPPointF mPPointF, float f3) {
        float fontMetrics = textPaint.getFontMetrics(mFontMetricsBuffer);
        float width = (float) staticLayout.getWidth();
        float lineCount = ((float) staticLayout.getLineCount()) * fontMetrics;
        float f4 = 0.0f - ((float) mDrawTextRectBuffer.left);
        float f5 = lineCount + 0.0f;
        Paint.Align textAlign = textPaint.getTextAlign();
        textPaint.setTextAlign(Paint.Align.LEFT);
        if (f3 != 0.0f) {
            float f6 = f4 - (width * 0.5f);
            float f7 = f5 - (lineCount * 0.5f);
            if (!(mPPointF.f127x == 0.5f && mPPointF.f128y == 0.5f)) {
                FSize sizeOfRotatedRectangleByDegrees = getSizeOfRotatedRectangleByDegrees(width, lineCount, f3);
                f -= sizeOfRotatedRectangleByDegrees.width * (mPPointF.f127x - 0.5f);
                f2 -= sizeOfRotatedRectangleByDegrees.height * (mPPointF.f128y - 0.5f);
                FSize.recycleInstance(sizeOfRotatedRectangleByDegrees);
            }
            canvas.save();
            canvas.translate(f, f2);
            canvas.rotate(f3);
            canvas.translate(f6, f7);
            staticLayout.draw(canvas);
            canvas.restore();
        } else {
            if (!(mPPointF.f127x == 0.0f && mPPointF.f128y == 0.0f)) {
                f4 -= width * mPPointF.f127x;
                f5 -= lineCount * mPPointF.f128y;
            }
            canvas.save();
            canvas.translate(f4 + f, f5 + f2);
            staticLayout.draw(canvas);
            canvas.restore();
        }
        textPaint.setTextAlign(textAlign);
    }

    public static void drawMultilineText(Canvas canvas, String str, float f, float f2, TextPaint textPaint, FSize fSize, MPPointF mPPointF, float f3) {
        TextPaint textPaint2 = textPaint;
        drawMultilineText(canvas, new StaticLayout(str, 0, str.length(), textPaint2, (int) Math.max(Math.ceil((double) fSize.width), 1.0d), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false), f, f2, textPaint2, mPPointF, f3);
    }

    public static FSize getSizeOfRotatedRectangleByDegrees(FSize fSize, float f) {
        return getSizeOfRotatedRectangleByRadians(fSize.width, fSize.height, f * 0.017453292f);
    }

    public static FSize getSizeOfRotatedRectangleByRadians(FSize fSize, float f) {
        return getSizeOfRotatedRectangleByRadians(fSize.width, fSize.height, f);
    }

    public static FSize getSizeOfRotatedRectangleByDegrees(float f, float f2, float f3) {
        return getSizeOfRotatedRectangleByRadians(f, f2, f3 * 0.017453292f);
    }

    public static FSize getSizeOfRotatedRectangleByRadians(float f, float f2, float f3) {
        double d = (double) f3;
        return FSize.getInstance(Math.abs(((float) Math.cos(d)) * f) + Math.abs(((float) Math.sin(d)) * f2), Math.abs(f * ((float) Math.sin(d))) + Math.abs(f2 * ((float) Math.cos(d))));
    }

    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }
}
