package com.example.donxing.starnote.ui;

        import java.util.ArrayList;
        import android.content.Context;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.util.AttributeSet;
        import android.view.MotionEvent;
        import android.view.View;

public class NoteDraw extends View {
    private Paint mPaint;
    private float currentX;
    private float currentY;
    private Path mPath;
    private ArrayList<Path> mPaths = new ArrayList<Path>();//保存已绘制的信息

    public NoteDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public NoteDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public NoteDraw(Context context) {
        super(context);
        // TODO Auto-generated constructor stub+
    }


    public ArrayList<Path> getmPaths() { return mPaths; }

    public void setmPaths(ArrayList<Path> newmPaths) { this.mPaths = newmPaths;}


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(Color.RED);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(4);
            mPaint.setAntiAlias(true);// 去锯齿
        }
        if (mPaths != null && mPaths.size() > 0) {// 显示已画过的图形
            for (int i = 0; i < mPaths.size(); i++) {
                canvas.drawPath(mPaths.get(i), mPaint);
            }
        }
        if (mPath != null) {// 显示当前画的路径
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.currentX = event.getX();
        this.currentY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 按下
                mPath = new Path();
                mPath.moveTo(currentX, currentY);
                break;
            case MotionEvent.ACTION_UP://松开
                mPaths.add(mPath);
                mPath = null;//必须添加，用于重绘是判断是否绘制
                break;
            case MotionEvent.ACTION_MOVE:
                this.currentX = event.getX();
                this.currentY = event.getY();
                mPath.lineTo(currentX, currentY);
                break;
        }
        this.invalidate();
        return true;// 表明该事件处理完
    }
}