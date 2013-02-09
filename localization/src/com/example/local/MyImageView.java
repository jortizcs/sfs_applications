package com.example.local;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.local.*;

public class MyImageView extends View {

private static final int INVALID_POINTER_ID = -1;

	private LinearLayout container = (LinearLayout)findViewById(R.id.ll2);
    private Drawable mImage = getResources().getDrawable(R.drawable.soda_f4);;
    private float mPosX;
    private float mPosY;

    private float imageStartX = 0;
    private float imageStartY = 0;
    
    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public MyImageView(Context context) {
        this(context, null, 0);
    //mImage = getResources().getDrawable(R.drawable.soda_f4);

        mImage.setBounds(0, 0, mImage.getIntrinsicWidth(), mImage.getIntrinsicHeight());
    }

    public MyImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mImage = getResources().getDrawable(R.drawable.soda_f4);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        LinearLayout container = (LinearLayout) View.inflate(getContext(), R.layout.floorplan, null);
        EditText Xcoord = (EditText)container.findViewById(R.id.Xlabel);
        EditText Ycoord = (EditText)container.findViewById(R.id.Ylabel);

        if (Xcoord!=null)
        {
        	
        	Log.d("XTEXTBOX FROM ONTOUCHEVENT","NOT NULL . " + Xcoord.getText());
        }
        else
        	Log.d("XTEXTBOX","NULL");
        if (Ycoord!=null)
        {
        	Log.d("YTEXTBOX FROM ONTOUCHEVENT","NOT NULL. " + Ycoord.getText());
        }
        else
        	Log.d("YTEXTBOX","NULL");
        
        
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN: {
            final float x = ev.getX();
            final float y = ev.getY();

            mLastTouchX = x;
            mLastTouchY = y;
            Log.d("ACTION_DOWN", "X: "+mPosX+" Y: "+mPosY);
            
            mActivePointerId = ev.getPointerId(0);
            break;
        }

        case MotionEvent.ACTION_MOVE: {
            final int pointerIndex = ev.findPointerIndex(mActivePointerId);
            final float x = ev.getX(pointerIndex);
            final float y = ev.getY(pointerIndex);

            // Only move if the ScaleGestureDetector isn't processing a gesture.
            if (!mScaleDetector.isInProgress()) {
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;

                mPosX += dx;
                mPosY += dy;
                Log.d("ACTION_MOVE", "X: "+mPosX+" Y: "+mPosY);
                
                
                invalidate();
            }
            

            mLastTouchX = x;
            mLastTouchY = y;

            break;
        }

        case MotionEvent.ACTION_UP: {
            Log.d("ACTION_UP", "X: "+mPosX+" Y: "+mPosY);

            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_CANCEL: {
            Log.d("ACTION_CANCEL", "X: "+mPosX+" Y: "+mPosY);

            mActivePointerId = INVALID_POINTER_ID;
            break;
        }

        case MotionEvent.ACTION_POINTER_UP: {
            final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            final int pointerId = ev.getPointerId(pointerIndex);
            if (pointerId == mActivePointerId) {
                // This was our active pointer going up. Choose a new
                // active pointer and adjust accordingly.
                final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                mLastTouchX = ev.getX(newPointerIndex);
                mLastTouchY = ev.getY(newPointerIndex);
                Log.d("ACTION_POINTER_UP", "X: "+mPosX+" Y: "+mPosY);

                mActivePointerId = ev.getPointerId(newPointerIndex);
            }
            break;
        }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        
        canvas.save();
        imageStartX = mPosX;
        imageStartY = mPosY;
        Log.d("DEBUG", "X: "+mPosX+" Y: "+mPosY);
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        mImage.draw(canvas);
        canvas.restore();
        
    }

    public float getPosX()
    {
    	return (mLastTouchX-imageStartX)/mScaleFactor;
    }
    public float getPosY()
    {
    	return (mLastTouchY-imageStartY)/mScaleFactor;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            invalidate();
            return true;
        }
    }

}