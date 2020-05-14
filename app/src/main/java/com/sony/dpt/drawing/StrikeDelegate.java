package com.sony.dpt.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;

import static android.content.Context.POWER_SERVICE;
import static com.sony.dpt.override.UpdateMode.UPDATE_MODE_NOWAIT_NOCONVERT_DU_SP1_IGNORE;

/**
 * This is in charge of striking the pen on a view
 */
public class StrikeDelegate extends AbstractDrawingDelegate {

    private float lastX;
    private float lastY;

    private int strokeWidth;

    private Rect converter;
    private RectF invalidationRectangle;
    private PowerManager.WakeLock wakeLock;

    private void resetInvalidation() {
        invalidationRectangle.set(lastX, lastY, lastX, lastY);
    }

    private void updatePath(final float x, final float y) {
        lastX = x;
        lastY = y;
        invalidationRectangle.union(lastX, lastY);
    }

    private void handleMotion(final MotionEvent event) {
        int historySize = event.getHistorySize();
        for (int i = 0; i < historySize; i++) {
            updatePath(event.getHistoricalX(i), event.getHistoricalY(i));
        }
        updatePath(event.getX(), event.getY());

        // We inset by the stroke width so that the invalidation also encompass the full width of the line
        invalidationRectangle.inset(-strokeWidth, -strokeWidth);
        invalidate(invalidationRectangle);
    }

    public StrikeDelegate(final int strokeWidth, final View view, final Bitmap cachedLayer, final Canvas drawCanvas) {
        super(view, cachedLayer, drawCanvas);
        this.strokeWidth = strokeWidth;
        this.converter = new Rect();
        this.invalidationRectangle = new RectF();
        PowerManager powerManager = (PowerManager) view.getContext().getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DPT::DHWLock");
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                epdUtil.setDhwState(true);
                lastX = event.getX();
                lastY = event.getY();
                resetInvalidation();
                break;
            case MotionEvent.ACTION_MOVE:
                handleMotion(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleMotion(event);
                resetInvalidation();
                wakeLock.release();
                break;
        }
        return true;
    }

    @Override
    public boolean pressureSensitive() {
        return false;
    }

    @Override
    public void setPenWidth(int penWidth) {
        this.strokeWidth = penWidth;
    }

    @Override
    public int penWidth() {
        return strokeWidth;
    }

    @Override
    public int maxPenWidth() {
        return strokeWidth;
    }

    @Override
    public Paint getPaint() {
        return null;
    }

    @Override
    public boolean nativeDhw() {
        return true;
    }

    @Override
    public void invalidate(RectF dirty) {
        converter.set((int) dirty.left, (int) dirty.top, (int) dirty.right, (int) dirty.bottom);
        viewOverride.invalidate(view, converter, UPDATE_MODE_NOWAIT_NOCONVERT_DU_SP1_IGNORE);
    }


}
