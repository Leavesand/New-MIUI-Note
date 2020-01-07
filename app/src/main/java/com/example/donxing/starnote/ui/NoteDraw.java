package com.example.donxing.starnote.ui;

        import java.util.ArrayList;
        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.graphics.PorterDuff;
        import android.graphics.PorterDuffXfermode;
        import android.util.AttributeSet;
        import android.view.MotionEvent;
        import android.view.View;

public class NoteDraw extends View {

    private int view_width=0;//屏幕的宽度
    private int view_height=0;//屏幕的高度
    private float preX;//起始点的x坐标
    private float preY;//起始点的y坐标
    private Path path;//路径
    public Paint paint;//画笔
    Bitmap cacheBitmap=null;//定义一个内存中的图片，该图片将作为缓冲区
    Canvas cacheCanvas=null;//定义cacheBitmap上的Canvas对象

    private Paint mPaint;
    private float currentX;
    private float currentY;
    private Path mPath;
    private ArrayList<Path> mPaths = new ArrayList<Path>();//保存已绘制的信息

    public NoteDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        view_width = context.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
        view_height = context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        //创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();//创建一个新的画布
        //在cacheCanvas上绘制cacheBitmap
        cacheCanvas.setBitmap(cacheBitmap);
    }

    public NoteDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        view_width = context.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
        view_height = context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        //创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();//创建一个新的画布
        //在cacheCanvas上绘制cacheBitmap
        cacheCanvas.setBitmap(cacheBitmap);
    }

    public NoteDraw(Context context) {
        super(context);
        view_width = context.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
        view_height = context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        //创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();//创建一个新的画布
        //在cacheCanvas上绘制cacheBitmap
        cacheCanvas.setBitmap(cacheBitmap);
    }


    public ArrayList<Path> getmPaths() { return mPaths; }

    public void setmPaths(ArrayList<Path> newmPaths) { this.mPaths = newmPaths;}
    /*
     * 功能：构造方法
     * */
    public void SetPaint() {
        mPaint = new Paint(Paint.DITHER_FLAG);//Paint.DITHER_FLAG防抖动的
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);//设置填充方式为描边
        mPaint.setStrokeJoin(Paint.Join.ROUND);//设置笔刷转弯处的连接风格
        mPaint.setStrokeCap(Paint.Cap.ROUND);//设置笔刷的图形样式(体现在线的端点上)
        mPaint.setStrokeWidth(2);//设置默认笔触的宽度像素
        mPaint.setAntiAlias(true);//设置抗锯齿效果
        mPaint.setDither(true);//使用抖动效果
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint bmpPaint = new Paint();//采用默认设置创建一个画笔
        canvas.drawBitmap(cacheBitmap, 0, 0,bmpPaint);//绘制cacheBitmap
        if (mPath != null)
        {
            canvas.drawPath(mPath, mPaint);
        }
        canvas.save();//保存canvas的状态
        canvas.restore();//恢复canvas之前保存的状态，防止保存后对canvas执行的操作对后续的绘制有影响

    //    if (mPaths != null && mPaths.size() > 0) {// 显示已画过的图形
    //        for (int i = 0; i < mPaths.size(); i++) {
    //            canvas.drawPath(mPaths.get(i), mPaint);
    //        }
    //    }
    //    if (mPath != null) {// 显示当前画的路径
    //       canvas.drawPath(mPath, mPaint);
    //    }
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
                cacheCanvas.drawPath(mPath, mPaint);
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

    public void clear(){
        //设置图形重叠时的处理方式
        mPaint.setAlpha(0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //设置擦除的宽度
     //   mPaint.setStrokeWidth(50);
    }

}