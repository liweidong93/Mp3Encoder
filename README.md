# recorder_lib（录音库）
### 介绍
	recorder库是用来录制音频文件，目前支持三种格式MP3、PCM、WAV格式，默认为MP3格式。
###### 录制MP3过程介绍：
	通过android自带的AudioRecord进行录制，将录制的原始数据通过C++库lame库进行编码成MP3格式进行保存。

###### 项目结构介绍：
	RecordManager：对外提供的录音管理类，包含开启录制、停止录制、设置录制参数、设置一些回调监听。
    Mp3Encoder：lame库注册，native方法注册。
    Mp3EncodeThread：MP3编码线程，实时取录制数据进行编码。
    RecordConfig：录音配置实体类，包括采样率、比特率、声道、录制格式。
        
        
### 使用说明
##### 基本使用
	RecordManager recordManager = RecordManager.getInstance();
    
    
    //设置录制配置信息，可以不进行设置，采用默认配置
    RecordConfig recordConfig = recordManager.getRecordConfig();//注意：先获取到默认的配置类，不要直接new
    //采样率
    recordConfig.setSampleRate(16000);
    //采样位宽
    recordConfig.setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT);
    //声道
    recordConfig.setChannelConfig(AudioFormat.CHANNEL_IN_MONO);
    recordManager.changeRecordConfig(recordConfig);
    
    //开始录制
    recordManager.start();
    //停止录制
    recordManager.stop();
    
   
   
   ##### 设置一些监听
   	
    //设置声音分贝大小监听回调
    recordManager.setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
                
            }
        });
        
   	
    
    //录制状态监听
    recordManager.setRecordStateListener(new RecordStateListener() {
            
            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "录音异常，请重新录制！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStart() {
                //开始录制
                
            }

            @Override
            public void onStop() {
                //停止录制
               
            }
        });
        
        
        
     
     //获取录音文件，录音结束回调
     recordManager.setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                
            }
        });
        
        
     
     
     //获取录音时长，参数为录音文件全路径
     recordManager.getDuring(file.getAbsolutePath());
     
     //设置文件缓存目录(默认目录结构为sd卡/Recorder)
     recordManager.changeRecordDir(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
     
     //设置文件名
     recordManager.setFileName("");
        
    
   
### 参数设置对比
采样率(KHz)  | 采样位宽（bit） | 时长(分) | 文件大小 | 声音质量
----|------|----|----|------|
8  |  8 | 5 | 300k |  电话
8  |  16 | 5 | 600k |  电话
16  |  8 | 5 | 300k |  无线电广播
16  |  16 | 5 | 600k |  无线电广播
44.1  |  8 | 5 | 1.2M |  CD音质
44.1  |  16 | 5 | 1.2M |  CD音质

结论：最优为采样率16KHz，采样位宽8bit，1秒1KB数据。
