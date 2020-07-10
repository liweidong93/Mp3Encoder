package com.pxwx.stududent.mp3recorder.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.pxwx.stududent.mp3recorder.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述： 音频文件波形view
 * Created By liweidong on 2020/7/6
 */
public class AudioWaveView extends View {


    /**
     * 频谱数量
     */
//    private static final int LUMP_COUNT = 128 * 2;
    //频谱的个数
    private static final int LUMP_COUNT = 64 * 2;
    private static int LUMP_WIDTH;
    //频谱间距
    private static final int LUMP_SPACE = 20;
    private static int LUMP_MIN_HEIGHT;
    //最高
    private static int LUMP_MAX_HEIGHT;//TODO: HEIGHT
    private static int LUMP_SIZE;
    //圆角
    private int raduis;
    private static final int LUMP_COLOR = Color.parseColor("#00a4ff");

    private static final int WAVE_SAMPLING_INTERVAL = 5;

    //    private static final float SCALE = LUMP_MAX_HEIGHT / 64;
    private static float SCALE;

    private AudioWaveView.ShowStyle upShowStyle = AudioWaveView.ShowStyle.STYLE_HOLLOW_LUMP;
    private AudioWaveView.ShowStyle downShowStyle = AudioWaveView.ShowStyle.STYLE_HOLLOW_LUMP;

    private byte[] waveData;
    List<Point> pointList;

    private Paint lumpUpPaint, lumpDownPaint;
    Path wavePathUp = new Path();
    Path wavePathDown = new Path();


    public AudioWaveView(Context context) {
        super(context);
        init(context);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LUMP_MAX_HEIGHT = DisplayUtil.dp2px(context, 30);
        SCALE = LUMP_MAX_HEIGHT / 128f;
        LUMP_WIDTH = DisplayUtil.dp2px(context, 3);
        LUMP_MIN_HEIGHT = LUMP_WIDTH;
        LUMP_SIZE = LUMP_WIDTH + LUMP_SPACE;
        raduis = LUMP_WIDTH / 2;

        lumpUpPaint = new Paint();
        lumpUpPaint.setAntiAlias(true);
        lumpUpPaint.setColor(LUMP_COLOR);
        lumpUpPaint.setStrokeWidth(3);
        lumpUpPaint.setStyle(Paint.Style.FILL);

        lumpDownPaint = new Paint();
        lumpDownPaint.setAntiAlias(true);
        lumpDownPaint.setColor(LUMP_COLOR);
        lumpDownPaint.setStrokeWidth(3);
        lumpDownPaint.setStyle(Paint.Style.STROKE);
    }

    public void setWaveData(byte[] data) {
        this.waveData = readyData(data);
        genSamplingPoint(data);
        invalidate();
    }

    public void setStyle(AudioWaveView.ShowStyle upShowStyle, AudioWaveView.ShowStyle downShowStyle) {
        this.upShowStyle = upShowStyle;
        this.downShowStyle = downShowStyle;
        if (upShowStyle == AudioWaveView.ShowStyle.STYLE_HOLLOW_LUMP || upShowStyle == AudioWaveView.ShowStyle.STYLE_ALL) {
            lumpUpPaint.setColor(Color.parseColor("#A4D3EE"));
        }
        if (downShowStyle == AudioWaveView.ShowStyle.STYLE_HOLLOW_LUMP || downShowStyle == AudioWaveView.ShowStyle.STYLE_ALL) {
            lumpDownPaint.setColor(Color.parseColor("#A4D3EE"));
        }
    }

    public AudioWaveView.ShowStyle getUpStyle() {
        return upShowStyle;
    }

    public AudioWaveView.ShowStyle getDownStyle() {
        return downShowStyle;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wavePathUp.reset();
        wavePathDown.reset();

        for (int i = 0; i < LUMP_COUNT; i++) {
            if (waveData == null) {
                canvas.drawRect((LUMP_WIDTH + LUMP_SPACE) * i,
                        LUMP_MAX_HEIGHT - LUMP_MIN_HEIGHT,
                        (LUMP_WIDTH + LUMP_SPACE) * i + LUMP_WIDTH,
                        LUMP_MAX_HEIGHT,
                        lumpUpPaint);
                continue;
            }

            if (upShowStyle != null) {
                switch (upShowStyle) {
                    case STYLE_HOLLOW_LUMP:
                        drawLump(canvas, i, true);
                        break;
                    case STYLE_WAVE:
                        drawWave(canvas, i, true);
                        break;
                    case STYLE_ALL:
                        drawLump(canvas, i, true);
                        drawWave(canvas, i, true);
                    default:
                        break;
                }
            }
            if (downShowStyle != null) {
                switch (downShowStyle) {
                    case STYLE_HOLLOW_LUMP:
                        drawLump(canvas, i, false);
                        break;
                    case STYLE_WAVE:
                        drawWave(canvas, i, false);
                        break;
                    case STYLE_ALL:
                        drawLump(canvas, i, false);
                        drawWave(canvas, i, false);
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 预处理数据
     *
     * @return
     */
    private static byte[] readyData(byte[] fft) {
        byte[] newData = new byte[LUMP_COUNT];
        for (int i = 0; i < Math.min(fft.length, LUMP_COUNT); i++) {
            newData[i] = (byte) Math.abs(fft[i]);
        }
        return newData;
    }

    /**
     * 绘制曲线
     *
     * @param canvas
     * @param i
     * @param reversal
     */
    private void drawWave(Canvas canvas, int i, boolean reversal) {
        if (pointList == null || pointList.size() < 2) {
            return;
        }
        float ratio = SCALE * (reversal ? 1 : -1);
        if (i < pointList.size() - 2) {
            Point point = pointList.get(i);
            Point nextPoint = pointList.get(i + 1);
            int midX = (point.x + nextPoint.x) >> 1;
            if (reversal) {
                if (i == 0) {
                    wavePathUp.moveTo(point.x, LUMP_MAX_HEIGHT - point.y * ratio);
                }
                wavePathUp.cubicTo(midX, LUMP_MAX_HEIGHT - point.y * ratio,
                        midX, LUMP_MAX_HEIGHT - nextPoint.y * ratio,
                        nextPoint.x, LUMP_MAX_HEIGHT - nextPoint.y * ratio);
                canvas.drawPath(wavePathUp, lumpDownPaint);
            } else {
                if (i == 0) {
                    wavePathDown.moveTo(point.x, LUMP_MAX_HEIGHT - point.y * ratio);
                }
                wavePathDown.cubicTo(midX, LUMP_MAX_HEIGHT - point.y * ratio,
                        midX, LUMP_MAX_HEIGHT - nextPoint.y * ratio,
                        nextPoint.x, LUMP_MAX_HEIGHT - nextPoint.y * ratio);
                canvas.drawPath(wavePathDown, lumpDownPaint);
            }

        }
    }

    /**
     * 绘制矩形条
     * reversal： true: 上
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawLump(Canvas canvas, int i, boolean reversal) {
        int minus = reversal ? 1 : -1;
        float top;

        if ((reversal && upShowStyle == AudioWaveView.ShowStyle.STYLE_ALL) || (!reversal && downShowStyle == AudioWaveView.ShowStyle.STYLE_ALL)) {
            top = (LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + waveData[i] / 4 * SCALE) * minus);
        } else {
            top = (LUMP_MAX_HEIGHT - (LUMP_MIN_HEIGHT + waveData[i] * SCALE) * minus);
        }
        if (top > LUMP_MAX_HEIGHT && reversal){
            top = LUMP_MAX_HEIGHT;
        }
        canvas.drawRect(LUMP_SIZE * i,
                top,
                LUMP_SIZE * i + LUMP_WIDTH,
                LUMP_MAX_HEIGHT,
                lumpUpPaint);

        //绘制圆角
        if (reversal){
            //上面部分绘制重新绘制
            canvas.drawArc(LUMP_SIZE * i, top - raduis, LUMP_SIZE * i + LUMP_WIDTH, top + raduis, 0, -180, false, lumpUpPaint);
        }else{
            //下面部分绘制
            canvas.drawArc(LUMP_SIZE * i, top - raduis, LUMP_SIZE * i + LUMP_WIDTH, top + raduis, 0, 180, false, lumpUpPaint);
        }


    }

    /**
     * 生成波形图的采样数据，减少计算量
     *
     * @param data
     */
    private void genSamplingPoint(byte[] data) {
        if (upShowStyle != AudioWaveView.ShowStyle.STYLE_WAVE && downShowStyle != AudioWaveView.ShowStyle.STYLE_WAVE && upShowStyle != AudioWaveView.ShowStyle.STYLE_ALL && downShowStyle != AudioWaveView.ShowStyle.STYLE_ALL) {
            return;
        }
        if (pointList == null) {
            pointList = new ArrayList<>();
        } else {
            pointList.clear();
        }
        pointList.add(new Point(0, 0));
        for (int i = WAVE_SAMPLING_INTERVAL; i < LUMP_COUNT; i += WAVE_SAMPLING_INTERVAL) {
            pointList.add(new Point(LUMP_SIZE * i, waveData[i]));
        }
        pointList.add(new Point(LUMP_SIZE * LUMP_COUNT, 0));
    }


    /**
     * 可视化样式
     */
    public enum ShowStyle {
        /**
         * 空心的矩形小块
         */
        STYLE_HOLLOW_LUMP,

        /**
         * 曲线
         */
        STYLE_WAVE,

        /**
         * 不显示
         */
        STYLE_NOTHING,
        /**
         * 都显示
         */
        STYLE_ALL;

        public static AudioWaveView.ShowStyle getStyle(String name) {
            for (AudioWaveView.ShowStyle style : AudioWaveView.ShowStyle.values()) {
                if (style.name().equals(name)) {
                    return style;
                }
            }

            return STYLE_NOTHING;
        }
    }


}
