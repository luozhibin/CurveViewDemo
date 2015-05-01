package com.locke.curveview.widget;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CurveAnimation extends Animation{
	private CurveView mWaveView;
	private float mInterpolatedTime;
	
	public CurveAnimation(CurveView waveView) {
		this.mWaveView = waveView;
	}
	
	public float getInterpolatedTime() {
		return mInterpolatedTime;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		mInterpolatedTime = interpolatedTime;
		mWaveView.postInvalidate();
	}
	
}
