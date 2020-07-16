package com.pxwx.stududent.mp3recorder.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import java.util.Arrays;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.pxwx.stududent.mp3recorder.utils.DisplayUtil;

/**
 * 描述： 音频条形view，波形居中显示
 * Created By liweidong on 2020/7/16
 */
public class AudioLumpView extends View {

    //控件宽高
    private int width;
    private int height;

    //条形宽度
    private int lumpWidth;
    //条形间距
    private int lumpSpace;
    //条形最小高度
    private int lumpMinHeight;
    //条形最大高度
    private int lumpMaxHeight;
    private int middleLeft;
    private int middleTop;

    //画笔
    private Paint paint;
    //条形数一半
    private int lumpSize;
    private int lumpCount;

    private byte[] waveData;
    private int middleBottom;
    private int minus;
    private static final float SCALE = 1.5f;
    //圆角半径
    private int roundRadius;

    public AudioLumpView(Context context) {
        super(context);
        init(context);
    }

    public AudioLumpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioLumpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private void init(Context context){
        width = DisplayUtil.dp2px(context, 260);
        height = DisplayUtil.dp2px(context, 60);
        lumpWidth = DisplayUtil.dp2px(context, 3);
        lumpMinHeight = DisplayUtil.dp2px(context, 6);
        lumpMaxHeight = DisplayUtil.dp2px(context, 55);
        lumpSpace = DisplayUtil.dp2px(context, 5);
        roundRadius = DisplayUtil.dp2px(context, 1.5f);
        middleLeft = (width - lumpWidth) / 2;
        middleTop = (height - lumpMinHeight) / 2;
        middleBottom = height / 2 + lumpMinHeight;
        lumpSize = middleLeft / (lumpWidth + lumpSpace);
        lumpCount = lumpSize * 2 + 1;


        paint = new Paint();
        paint.setColor(Color.parseColor("#00a4ff"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(lumpWidth);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        if (waveData == null){
            return;
        }
        drawLump(canvas, true);
        drawLump(canvas, false);
    }

    public void setWaveData(byte[] data) {
        this.waveData = readyData(data);
        invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void drawLump(Canvas canvas, boolean reverse){
        minus = reverse ? 1 : -1;
        middleTop = (int) (lumpMaxHeight / 2 - (lumpWidth + waveData[waveData.length - 1] / 4 * SCALE) * minus);
        //最中间的条形
        canvas.drawRect(middleLeft, middleTop, middleLeft + lumpWidth, height / 2, paint);
        //绘制圆角
        drawRound(canvas, middleLeft, middleTop - roundRadius,middleLeft + lumpWidth,middleTop + roundRadius , 180 * (-minus));
        //向左画
        for (int i = lumpSize; i > 0; i--){
            middleTop = (int) (lumpMaxHeight / 2 - (lumpWidth + waveData[i] / 4 * SCALE) * minus);
            canvas.drawRect(middleLeft - (lumpSpace + lumpWidth) * (lumpSize + 1 - i), middleTop, middleLeft + lumpWidth - (lumpSpace + lumpWidth) * (lumpSize + 1 - i), height / 2, paint);
            //绘制圆角
            drawRound(canvas, middleLeft - (lumpSpace + lumpWidth) * (lumpSize + 1 - i), middleTop - roundRadius,middleLeft + lumpWidth - (lumpSpace + lumpWidth) * (lumpSize + 1 - i),middleTop + roundRadius , 180 * (-minus));
        }

        //向右画
        for (int i = lumpSize; i < lumpCount; i++){
            middleTop = (int) (lumpMaxHeight / 2 + (lumpWidth + waveData[lumpCount - i - 1] / 4 * SCALE) * minus);
            canvas.drawRect(middleLeft + (lumpSpace + lumpWidth) * (i + 1 - lumpSize), middleTop, middleLeft + lumpWidth + (lumpSpace + lumpWidth) * (i + 1 - lumpSize), height / 2, paint);
            //绘制圆角
            drawRound(canvas, middleLeft + (lumpSpace + lumpWidth) * (i + 1 - lumpSize), middleTop - roundRadius,middleLeft + lumpWidth + (lumpSpace + lumpWidth) * (i + 1 - lumpSize),middleTop + roundRadius , 180 * minus);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawRound(Canvas canvas, int left, int top, int right, int bottom, float sweepAngle){
        //绘制圆角
        canvas.drawArc(left, top, right, bottom, 0, sweepAngle, false, paint);
    }

    /**
     * 预处理数据
     *
     * @return
     */
    private byte[] readyData(byte[] fft) {
        byte[] newData = new byte[lumpSize + 2];
        for (int i = 0; i < Math.min(fft.length, lumpSize + 2); i++) {
            newData[i] = (byte) Math.abs(fft[i]);
        }
        Arrays.sort(newData);
        return newData;
    }
}
