package com.pxwx.librecorder.recorder.listener;

/**
 * 傅里叶数据回调
 */
public interface RecordFftDataListener {

    /**
     * @param data 录音可视化数据，即傅里叶转换后的数据：fftData
     */
    void onFftData(byte[] data);

}
