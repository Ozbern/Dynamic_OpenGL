package com.example.ozbern.gltest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Ozbern on 09.09.2016.
 */
public class CustomGLSurfaceView extends GLSurfaceView
{
    private OpenGLRenderer mRenderer;

    // Offsets for touch events
    private float mPreviousX;
    private float mPreviousY;

    private float mDensity;
    private boolean waitForDoubleClick;
    private long doubleClickTimeout;

    public CustomGLSurfaceView(Context context)
    {
        super(context);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event != null)
        {
            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (mRenderer != null)
                {
                    float deltaX = (x - mPreviousX) / mDensity / 2f;
                    float deltaY = (y - mPreviousY) / mDensity / 2f;

                    mRenderer.mTouchDeltaX += deltaX;
                    mRenderer.mTouchDeltaY += deltaY;
                }
                waitForDoubleClick=false;
                doubleClickTimeout=0;
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if (waitForDoubleClick){
                        //Двойное нажание - сбрасываем поворот матрицы вида в исходное положение (если успели в интервал ожидания второго нажатия)
                        if (SystemClock.uptimeMillis()-doubleClickTimeout<1000){
                            //Второе нажание пришло вовремя
                            if (mRenderer != null)
                            {
                                mRenderer.identityAccumulationRotation();
                            }
                        }
                        waitForDoubleClick=false;
                        doubleClickTimeout=0;
                    } else {
                        waitForDoubleClick=true;
                        doubleClickTimeout= SystemClock.uptimeMillis();
                    }
                }
            }

            mPreviousX = x;
            mPreviousY = y;

            return true;
        }
        else
        {
            return super.onTouchEvent(event);
        }
    }

    // Overload method
    public void setRenderer(OpenGLRenderer renderer, float density)
    {
        mRenderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
    }
}