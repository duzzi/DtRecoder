### Android屏幕录制（视频+音频）

录屏同时内录音频的话，有三种方案：
1. 音频源选择AudioSource.MIC，但是会同时环境杂音也录制进去了
2. 音频源选择AudioSource.REMOTE_SUBMIX，但是AudioSource.REMOTE_SUBMIX仅系统级应用才可以使用，网上有方案说是利用系统签名打包，略麻烦。
3. 音频源使用AudioSource.MIC，加上某宝黑科技**保真内录插头**，可将音频信号直接输入到麦克风，这样就不会有环境杂音了

本项目采用第三种方案