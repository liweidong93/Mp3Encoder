package com.pxwx.librecorder.recorder;

import com.pxwx.librecorder.recorder.listener.RecordDataListener;
import com.pxwx.librecorder.recorder.listener.RecordFftDataListener;
import com.pxwx.librecorder.recorder.listener.RecordResultListener;
import com.pxwx.librecorder.recorder.listener.RecordSoundSizeListener;
import com.pxwx.librecorder.recorder.listener.RecordStateListener;
import com.pxwx.librecorder.utils.ByteUtils;
import com.pxwx.librecorder.utils.FileUtils;
import com.pxwx.librecorder.utils.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author zhaolewei on 2018/7/31.
 */
public class RecordUtils {

    /**
     * 录音配置
     */
    private static RecordConfig currentConfig = new RecordConfig();

    //默认设置mp3
    static {
        currentConfig.setFormat(RecordConfig.RecordFormat.MP3);
    }

    public final static int ACTION_INVALID = 0;
    //开始录音
    public final static int ACTION_START_RECORD = 1;

    public final static int ACTION_STOP_RECORD = 2;

    public final static int ACTION_RESUME_RECORD = 3;

    public final static int ACTION_PAUSE_RECORD = 4;
    //log-tag
    private static final String TAG = RecordUtils.class.getSimpleName();

    /**
     * 获取录音的声音分贝值
     * 计算公式：dB = 20 * log(a / a0);
     * @return 声音分贝值
     */
    public static long getMaxDecibels(byte[] input) {
        short[] amplitudes = ByteUtils.toShorts(input);
        if (amplitudes == null) {
            return 0;
        }
        float maxAmplitude = 2;
        for (float amplitude : amplitudes) {
            if (Math.abs(maxAmplitude) < Math.abs(amplitude)) {
                maxAmplitude = amplitude;
            }
        }
        return Math.round(20 * Math.log10(maxAmplitude));
    }


    public static float[] byteToFloat(byte[] input) {
        if (input == null) {
            return null;
        }
        int bytesPerSample = 2;
        ByteBuffer buffer = ByteBuffer.wrap(input);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer floatBuffer = FloatBuffer.allocate(input.length / bytesPerSample);
        for (int i = 0; i < floatBuffer.capacity(); i++) {
            floatBuffer.put(buffer.getShort(i * bytesPerSample));
        }
        return floatBuffer.array();
    }

    /**
     * 处理录音
     * @param type
     */
    public static void handleRecorder(int type){
        switch (type) {
            case ACTION_START_RECORD:
                doStartRecording(getFilePath());
                break;
            case ACTION_STOP_RECORD:
                doStopRecording();
                break;
            case ACTION_RESUME_RECORD:
                doResumeRecording();
                break;
            case ACTION_PAUSE_RECORD:
                doPauseRecording();
                break;
            default:
                break;
        }
    }

    /**
     * 开始录音
     * @param path
     */
    private static void doStartRecording(String path) {
        Logger.v(TAG, "doStartRecording path: %s", path);
        RecordHelper.getInstance().start(path, currentConfig);
    }

    /**
     * 重新录制
     */
    private static void doResumeRecording() {
        Logger.v(TAG, "doResumeRecording");
        RecordHelper.getInstance().resume();
    }

    /**
     * 暂停录制
     */
    private static void doPauseRecording() {
        Logger.v(TAG, "doResumeRecording");
        RecordHelper.getInstance().pause();
    }

    /**
     * 停止录制
     */
    private static void doStopRecording() {
        Logger.v(TAG, "doStopRecording");
        RecordHelper.getInstance().stop();
    }

    /**
     * 设置录音状态
     * @param recordStateListener
     */
    public static void setRecordStateListener(RecordStateListener recordStateListener) {
        RecordHelper.getInstance().setRecordStateListener(recordStateListener);
    }

    /**
     * 设置录音数据回调
     * @param recordDataListener
     */
    public static void setRecordDataListener(RecordDataListener recordDataListener) {
        RecordHelper.getInstance().setRecordDataListener(recordDataListener);
    }

    public static void setRecordFftDataListener(RecordFftDataListener recordFftDataListener) {
        RecordHelper.getInstance().setRecordFftDataListener(recordFftDataListener);
    }

    /**
     * 录音结束回调
     * @param recordResultListener
     */
    public static void setRecordResultListener(RecordResultListener recordResultListener) {
        RecordHelper.getInstance().setRecordResultListener(recordResultListener);
    }

    /**
     * 音量监听
     * @param recordSoundSizeListener
     */
    public static void setRecordSoundSizeListener(RecordSoundSizeListener recordSoundSizeListener) {
        RecordHelper.getInstance().setRecordSoundSizeListener(recordSoundSizeListener);
    }

    /**
     * 改变录音格式
     */
    public static boolean changeFormat(RecordConfig.RecordFormat recordFormat) {
        if (getState() == RecordHelper.RecordState.IDLE) {
            currentConfig.setFormat(recordFormat);
            return true;
        }
        return false;
    }

    /**
     * 获取当前的录音状态
     */
    public static RecordHelper.RecordState getState() {
        return RecordHelper.getInstance().getState();
    }

    /**
     * 改变录音配置
     */
    public static boolean changeRecordConfig(RecordConfig recordConfig) {
        if (getState() == RecordHelper.RecordState.IDLE) {
            currentConfig = recordConfig;
            return true;
        }
        return false;
    }

    /**
     * 获取录音配置参数
     */
    public static RecordConfig getRecordConfig() {
        return currentConfig;
    }

    /**
     * 改变录音存放文件位置
     * @param recordDir
     */
    public static void changeRecordDir(String recordDir) {
        currentConfig.setRecordDir(recordDir);
    }

    /**
     * 根据当前的时间生成相应的文件名
     * 实例 record_20160101_13_15_12
     */
    private static String getFilePath() {

        String fileDir =
                currentConfig.getRecordDir();
        if (!FileUtils.createOrExistsDir(fileDir)) {
            Logger.w(TAG, "文件夹创建失败：%s", fileDir);
            return null;
        }
        String fileName = String.format(Locale.getDefault(), "record_%s", FileUtils.getNowString(new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.SIMPLIFIED_CHINESE)));
        return String.format(Locale.getDefault(), "%s%s%s", fileDir, fileName, currentConfig.getFormat().getExtension());
    }
}
