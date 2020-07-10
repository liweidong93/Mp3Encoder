package com.pxwx.librecorder.recorder.listener;


import com.pxwx.librecorder.recorder.RecordHelper;

/**
 * 录音状态回调
 */
public interface RecordStateListener {

    /**
     * 当前的录音状态发生变化
     *
     * @param state 当前状态
     */
    void onStateChange(RecordHelper.RecordState state);

    /**
     * 录音错误
     *
     * @param error 错误
     */
    void onError(String error);

    /**
     * 录音开始回调
     */
    void onStart();

    /**
     * 录音结束回调
     */
    void onStop();

}
