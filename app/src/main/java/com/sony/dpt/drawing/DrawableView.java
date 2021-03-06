package com.sony.dpt.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sony.dpt.override.UpdateMode;
import com.sony.dpt.override.ViewOverride;
import com.sony.infras.dp_libraries.systemutil.SystemUtil;

public class DrawableView extends SurfaceView implements SurfaceHolder.Callback2 {

    private final DrawingManager drawingManager;

    private static final int BASE_STROKE_SIZE = 5;
    private static final boolean HANDLE_PRESSURE_CHANGE = false;

    public DrawableView(Context context) {
        super(context);
        this.getHolder().addCallback(this);
        drawingManager = new DrawingManager(this, BASE_STROKE_SIZE, HANDLE_PRESSURE_CHANGE);
    }

    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
        drawingManager = new DrawingManager(this, BASE_STROKE_SIZE, HANDLE_PRESSURE_CHANGE);
    }

    public DrawableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.getHolder().addCallback(this);
        drawingManager = new DrawingManager(this, BASE_STROKE_SIZE, HANDLE_PRESSURE_CHANGE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return drawingManager.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        SystemUtil.getEpdUtilInstance().removeAllDhwArea();
        SystemUtil.getEpdUtilInstance().addDhwArea(
                new Rect(
                        0,
                        0,
                        width,
                        height
                ),
                drawingManager.penWidth(),
                width < height ? 0 : 1
        );
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SystemUtil.getEpdUtilInstance().removeAllDhwArea();
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        Canvas canvas = ViewOverride.getInstance().lockCanvas(holder, UpdateMode.UPDATE_MODE_CONVERT_A2_PARTIAL);
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable drawingFinished) {

    }

}
