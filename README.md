# Note

实现通过手机sco录音，保存在/sdcard/Music/m1.3gp



#### 问题：

1）sco的创建流程

2）codec：为什么通常是3gp格式的sample code，用其他格式录音怎么实现。以下面code入手了解MediaRecorder

```java
mediaRecorder = new MediaRecorder();
mediaRecorder.reset();
mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
```



#### bug

1）stop sco可能还有问题

2）申请权限后，没有开始start