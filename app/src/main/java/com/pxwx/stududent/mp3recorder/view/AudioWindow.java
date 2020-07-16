package com.pxwx.stududent.mp3recorder.view;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pxwx.librecorder.RecordManager;
import com.pxwx.librecorder.recorder.RecordHelper;
import com.pxwx.librecorder.recorder.listener.RecordFftDataListener;
import com.pxwx.librecorder.recorder.listener.RecordResultListener;
import com.pxwx.librecorder.recorder.listener.RecordSoundSizeListener;
import com.pxwx.librecorder.recorder.listener.RecordStateListener;
import com.pxwx.stududent.mp3recorder.R;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * 描述： 录音弹窗管理
 * Created By liweidong on 2020/7/6
 */
public class AudioWindow {

    private static AudioWindow instance = null;
    private PopupWindow popupWindow;
    //波形控件
    private AudioLumpView audioWaveView;
    //计时控件
    private TextView textViewTime;
    //计时
    private Disposable disposable;
    //录制按钮
    private ImageView imageViewRecorder;
    //录音结束监听
    private RecorderFinishListener recorderFinishListener;

    private AudioWindow(){

    }

    public static AudioWindow getInstance() {
        if (instance == null) {
            synchronized (AudioWindow.class) {
                if (instance == null) {
                    instance = new AudioWindow();
                }
            }
        }
        return instance;
    }

    /**
     * 创建window
     * @param context
     */
    private void createPopWindow(Context context){
        View view = LayoutInflater.from(context).inflate(R.layout.homework_audio_window_layout, null);
        audioWaveView = view.findViewById(R.id.awv_audiowindow_wave);
        textViewTime = view.findViewById(R.id.tv_audiowindow_time);
        imageViewRecorder = view.findViewById(R.id.iv_audiowindow_recorder);
        //录制按钮
        imageViewRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageViewRecorder.getTag().equals("stop")){
                    startRecorder();
                }else{
                    stopRecorder();
                    imageViewRecorder.setTag("stop");
                    dismiss();
                }
            }
        });
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击空白不消失
        popupWindow.setFocusable(false);
        RelativeLayout relativeLayoutRoot = view.findViewById(R.id.rl_audiowindow_root);
        relativeLayoutRoot.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_bottom_in));
        //设置弹窗消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                stopRecorder();
                dismiss();
            }
        });
    }

    /**
     * 显示弹窗
     * @param context
     */
    public void show(Context context, View rootView, RecorderFinishListener listener){
        //判断权限，存储是否大于500M，录音、存储权限获取
        dismiss();
        //每次都重新初始化一次界面，避免初始化一些控件的状态及显示
        createPopWindow(context);
        this.recorderFinishListener = listener;
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        //显示弹窗就开始录制
        startRecorder();
    }

    /**
     * 隐藏弹簧
     */
    private void dismiss(){
        if (popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private int soundsize;
    /**
     * 开始录制
     */
    private void startRecorder(){
        //开始录制将tag设置为开始
        imageViewRecorder.setTag("start");
        //根据波形设置到波形控件
        RecordManager.getInstance().setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                audioWaveView.setWaveData(data);
            }
        });
        RecordManager.getInstance().setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                if (soundSize > soundsize){
                    soundsize = soundSize;
                }
                Log.e("lwd","soundSize:" + soundSize + ",最大音量：" + soundsize);
            }
        });
        //设置录音状态
        RecordManager.getInstance().setRecordStateListener(new RecordStateListener() {

            @Override
            public void onStateChange(RecordHelper.RecordState state) {
                //根据状态回调改变imageview的图片
                if (state == RecordHelper.RecordState.RECORDING){
                    imageViewRecorder.setImageResource(R.mipmap.recorder_start);
                }else{
                    imageViewRecorder.setImageResource(R.mipmap.recorder_stop);
                }
            }

            @Override
            public void onError(String error) {
                dismiss();
            }

            @Override
            public void onStart() {
                //存储时间拼接
                final StringBuilder sb = new StringBuilder();
                //开始录音，计时
                disposable = Flowable.intervalRange(0, 301, 0, 1, TimeUnit.SECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                sb.delete(0, sb.toString().length());
                                int minute = (int) (aLong / 60);
                                int second = (int) (aLong % 60);
                                textViewTime.setText(sb.append(minute < 10 ? "0" + minute : minute).append(":").append(second < 10 ? "0" + second : second));
                            }
                        })
                        .doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                stopRecorder();
                                dismiss();
                            }
                        })
                        .subscribe();
            }

            @Override
            public void onStop() {
                stopRecorder();
                dismiss();
            }
        });
        //设置录音结果文件监听
        RecordManager.getInstance().setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                if (result != null && result.exists()){
                    //设置回调监听
                    recorderFinishListener.fileCallBack(result, RecordManager.getInstance().getDuring(result.getAbsolutePath()));
                }
            }
        });
        //开始录制
        RecordManager.getInstance().start();
    }

    /**
     * 停止录制
     */
    private void stopRecorder(){
        RecordManager.getInstance().stop();
        //停止掉计时线程
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    /**
     * 录制结束监听，用于上传音频文件
     */
    public interface RecorderFinishListener{

        /**
         * 文件结果回调，用于上传七牛
         * @param file
         * @param time  录音时长（秒）
         */
        void fileCallBack(File file, long time);

    }

}
