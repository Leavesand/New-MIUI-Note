package com.example.donxing.starnote.ui;



import android.app.Activity;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Bitmap.Config;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.graphics.PorterDuff;
        import android.graphics.PorterDuffXfermode;
        import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.view.ViewGroup.LayoutParams;
        import android.widget.Button;
        import android.widget.LinearLayout;

 public class MyView extends View {
     private int SCREEN_W;
     private int SCREEN_H;
     private int Pen = 1;
     private int Eraser = 2;
        private int mMode = 1;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mEraserPaint;
        private Paint mPaint;
        private Path mPath;
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setFocusable(true);
            setScreenWH();
            initPaint();
        }

     public MyView(Context context, AttributeSet attrs) {
         super(context, attrs);
         setFocusable(true);
         setScreenWH();
         initPaint();
     }

     public MyView(Context context) {
         super(context);
         setFocusable(true);
         setScreenWH();
         initPaint();
     }
        private void setScreenWH() {
            DisplayMetrics dm = new DisplayMetrics();
            dm = this.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            SCREEN_W = screenWidth;
            SCREEN_H = screenHeight;
        }

        //设置绘制模式是“画笔”还是“橡皮擦”
        public void setMode(int mode){
            this.mMode = mode;
        }

        private void initPaint() {
            //画笔
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(10);
            //橡皮擦
            mEraserPaint = new Paint();
            mEraserPaint.setAlpha(0);
            mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mEraserPaint.setAntiAlias(true);
            mEraserPaint.setDither(true);
            mEraserPaint.setStyle(Paint.Style.STROKE);
            mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
            mEraserPaint.setStrokeWidth(30);

            mPath = new Path();

            mBitmap = Bitmap.createBitmap(SCREEN_W, SCREEN_H, Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            }
            super.onDraw(canvas);
        }

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            //如果是“画笔”模式就用mPaint画笔进行绘制
            if (mMode == Pen) {
                mCanvas.drawPath(mPath, mPaint);
            }
            //如果是“橡皮擦”模式就用mEraserPaint画笔进行绘制
            if (mMode == Eraser) {
                mCanvas.drawPath(mPath, mEraserPaint);
            }

        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                if (mMode == Pen) {
                    mCanvas.drawPath(mPath, mPaint);
                }
                if (mMode == Eraser) {
                    mCanvas.drawPath(mPath, mEraserPaint);
                }
            }
        }


        private void touch_up() {
            mPath.lineTo(mX, mY);
            if (mMode == Pen) {
                mCanvas.drawPath(mPath, mPaint);
            }
            if (mMode == Eraser) {
                mCanvas.drawPath(mPath, mEraserPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
