package com.pxwx.librecorder;


import android.annotation.SuppressLint;

import com.pxwx.librecorder.recorder.RecordUtils;
import com.pxwx.librecorder.recorder.listener.RecordDataListener;
import com.pxwx.librecorder.recorder.listener.RecordFftDataListener;
import com.pxwx.librecorder.recorder.listener.RecordResultListener;
import com.pxwx.librecorder.recorder.listener.RecordSoundSizeListener;
import com.pxwx.librecorder.recorder.listener.RecordStateListener;
import com.pxwx.librecorder.recorder.RecordConfig;
import com.pxwx.librecorder.recorder.RecordHelper;
import com.pxwx.librecorder.recorder.mp3.Mp3Utils;
import com.pxwx.librecorder.utils.Logger;

/**
 * 录音管理类
 */
public class RecordManager {
    private static final String TAG = RecordManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private volatile static RecordManager instance;

    private RecordManager() {
    }

    public static RecordManager getInstance() {
        if (instance == null) {
            synchronized (RecordManager.class) {
                if (instance == null) {
                    instance = new RecordManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     * @param showLog     是否开启日志
     */
    public void init(boolean showLog) {
        Logger.IsDebug = showLog;
    }

    public void start() {
        Logger.i(TAG, "start...");
        RecordUtils.handleRecorder(RecordUtils.ACTION_START_RECORD);
    }

    public void stop() {
        RecordUtils.handleRecorder(RecordUtils.ACTION_STOP_RECORD);
    }

    public void resume() {
        RecordUtils.handleRecorder(RecordUtils.ACTION_RESUME_RECORD);
    }

    public void pause() {
        RecordUtils.handleRecorder(RecordUtils.ACTION_PAUSE_RECORD);
    }

    /**
     * 录音状态监听回调
     */
    public void setRecordStateListener(RecordStateListener listener) {
        RecordUtils.setRecordStateListener(listener);
    }

    /**
     * 录音数据监听回调
     */
    public void setRecordDataListener(RecordDataListener listener) {
        RecordUtils.setRecordDataListener(listener);
    }

    /**
     * 录音可视化数据回调，傅里叶转换后的频域数据
     */
    public void setRecordFftDataListener(RecordFftDataListener recordFftDataListener) {
        RecordUtils.setRecordFftDataListener(recordFftDataListener);
    }

    /**
     * 录音文件转换结束回调
     */
    public void setRecordResultListener(RecordResultListener listener) {
        RecordUtils.setRecordResultListener(listener);
    }

    /**
     * 录音音量监听回调
     */
    public void setRecordSoundSizeListener(RecordSoundSizeListener listener) {
        RecordUtils.setRecordSoundSizeListener(listener);
    }


    public boolean changeFormat(RecordConfig.RecordFormat recordFormat) {
        return RecordUtils.changeFormat(recordFormat);
    }


    public boolean changeRecordConfig(RecordConfig recordConfig) {
        return RecordUtils.changeRecordConfig(recordConfig);
    }

    public RecordConfig getRecordConfig() {
        return RecordUtils.getRecordConfig();
    }

    /**
     * 修改录音文件存放路径
     */
    public void changeRecordDir(String recordDir) {
        RecordUtils.changeRecordDir(recordDir);
    }

    /**
     * 获取当前的录音状态
     *
     * @return 状态
     */
    public RecordHelper.RecordState getState() {
        return RecordUtils.getState();
    }

    /**
     * 获取时长(s)
     * @param mp3File
     */
    public long getDuring(String mp3File){
        return Mp3Utils.getDuration(mp3File) / 1000;
    }

}
