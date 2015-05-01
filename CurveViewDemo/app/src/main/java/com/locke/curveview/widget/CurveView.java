package com.locke.curveview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

public class CurveView extends View {
    private static final int STEPS = 12;

    private CurveAnimation mWaveAnim;

    private Paint mPaintCurve;
    private Paint mPaintPoint;
    private Path mPathCurve;

    private int[] mDegreeTop;
    private int[] mDegreeLow;
    private int mBase;
    private int mDPT;

    public CurveView(Context context) {
        super(context);
        init();
    }

    public CurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPathCurve = new Path();

        mPaintPoint = new Paint();
        mPaintPoint.setAntiAlias(true);
        mPaintPoint.setColor(0xc0ffffff);

        mPaintCurve = new Paint();
        mPaintCurve.setAntiAlias(true);
        mPaintCurve.setColor(0xa0ffffff);
        mPaintCurve.setStyle(Paint.Style.STROKE);
        mPaintCurve.setStrokeWidth(2);

        mDegreeTop = new int[] {28, 31, 27, 23, 24, 27};
        mDegreeLow = new int[] {22, 23, 19, 19, 20, 21};
    }

    private Cubic[] calculate(float[] x) {
        int n = x.length - 1;
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] d = new float[n + 1];
        int i;

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x[1] - x[0]) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
        }
        delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

        d[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            d[i] = delta[i] - gamma[i] * d[i + 1];
        }

        Cubic[] cubics = new Cubic[n];
        for (i = 0; i < n; i++) {
            cubics[i] = new Cubic(x[i], d[i], 3 * (x[i + 1] - x[i])
                    - 2 * d[i] - d[i + 1], 2 * (x[i] - x[i + 1]) + d[i] + d[i + 1]);
        }

        return cubics;
    }

    private void drawCurve(Canvas canvas, float[] x, float[] y) {
        Cubic[] calculateX = calculate(x);
        Cubic[] calculateY = calculate(y);

        mPathCurve.reset();
        mPathCurve.moveTo(calculateX[0].eval(0), calculateY[0].eval(0));
        for (int i = 0; i < calculateX.length; i++) {
            for (int j = 1; j <= STEPS; j++) {
                float u = j / (float) STEPS;
                mPathCurve.lineTo(calculateX[i].eval(u), calculateY[i].eval(u));
            }
            drawPoints(canvas, x, y);
        }
        canvas.drawPath(mPathCurve, mPaintCurve);
    }

    private void drawPoints(Canvas canvas, float[] x, float[] y) {
        for (int i = 0; i < x.length; i++) {
            canvas.drawCircle(x[i], y[i], 4, mPaintPoint);
        }
    }

    private void readyDraw() {
        int maxOfTop = 0;
        if (mDegreeTop != null) {
            maxOfTop = mDegreeTop[0];
            for (int i = 1; i < mDegreeTop.length; i++) {
                int cur = mDegreeTop[i];
                if (cur > maxOfTop) {
                    maxOfTop = cur;
                }
            }
        }

        int minOfLow = 0;
        if (mDegreeLow != null) {
            minOfLow = mDegreeLow[0];
            for (int i = 1; i < mDegreeLow.length; i++) {
                int cur = mDegreeLow[i];
                if (cur < minOfLow) {
                    minOfLow = cur;
                }
            }
        }

        mBase = (maxOfTop + minOfLow) / 2;
        mDPT = getHeight() / (Math.abs(maxOfTop - minOfLow) + 10);
    }

    private float[] getRealX() {
        float averageX = getWidth() / 12f;
        return new float[] {averageX, averageX * 3, averageX * 5, averageX * 7, averageX * 9, averageX * 11};
    }

    private float[] getRealY(int[] degressY, float time) {
        float h = getHeight() / 2f;
        float[] y = new float[degressY.length];
        for (int i = 0; i < degressY.length; i++) {
            float space = (mBase - degressY[i]) * mDPT;
            float c = i / (float) degressY.length;
            if (time >= c) {
                float diff = (time - c) * (degressY.length / (degressY.length - i));
                y[i] = h + space * diff;
            } else {
                y[i] = h;
            }
        }
        return y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        readyDraw();

        float[] x = getRealX();
        float[] y1 = getRealY(mDegreeTop, getInterpolatedTime());
        float[] y2 = getRealY(mDegreeLow, getInterpolatedTime());

        drawCurve(canvas, x, y1);
        drawCurve(canvas, x, y2);
    }

    public boolean hasAnimEnded() {
        Animation anim = getAnimation();
        if (anim == null || anim.hasEnded()) {
            return true;
        }
        return false;
    }

    private float getInterpolatedTime() {
        if (!hasAnimEnded()) {
            return mWaveAnim.getInterpolatedTime();
        }
        return 1;
    }

    public void start() {
        mWaveAnim = new CurveAnimation(this);
        mWaveAnim.setDuration(800);
        startAnimation(mWaveAnim);
    }
}