package com.fullstackoasis.sensordemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GraphView extends SurfaceView implements Runnable {
    private static String TAG = GraphView.class.getCanonicalName();
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint = new Paint();
    private boolean running;
    private Thread graphThread;

    public GraphView(Context context) {
        super(context);
        paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();
                Log.d(TAG, "Canvas? " + canvas);
                canvas.save();
                canvas.drawColor(Color.WHITE);
                Rect rect = canvas.getClipBounds();
                Log.d(TAG, "rect.bottom? " + rect.bottom + ", " + rect.top + ", " + rect.left +
                        ", " + rect.right);
                int radius = Math.min(rect.bottom, rect.right)/2;
                // draw draw draw
                Log.d(TAG, "radius " + radius + ", " + rect.right/2 + ". " + rect.bottom/2);
                canvas.drawCircle(rect.right/2, rect.bottom/2, radius, paint);
                // Restore the previously saved (default) clip and matrix state.
                canvas.restore();
                // Release the lock on the canvas and show the surface's
                // contents on the screen.
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * Called by MainActivity.onPause() to stop the thread.
     */
    public void pause() {
        running = false;
        try {
            // Stop the thread == rejoin the main thread.
            graphThread.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Called by MainActivity.onResume() to start a thread.
     */
    public void resume() {
        running = true;
        graphThread = new Thread(this);
        graphThread.start();
    }
}
