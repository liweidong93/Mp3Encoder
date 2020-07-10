package com.pxwx.librecorder.recorder.listener;

/**
 * 录音数据回调
 */
public interface RecordDataListener {

    /**
     * 当前的录音状态发生变化
     *
     * @param data 当前音频数据
     */
    void onData(byte[] data);

}
