package com.pxwx.librecorder.recorder.listener;

/**
 * 音量大小回调
 */
public interface RecordSoundSizeListener {

    /**
     * 实时返回音量大小
     *
     * @param soundSize 当前音量大小
     */
    void onSoundSize(int soundSize);

}
