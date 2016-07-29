package domain;

import java.io.Serializable;

//代表具体的一个视频
//alt+insert 快速生成以下函数
public class VideoItem implements Serializable{ //序列化
    //标题
    private String title;
    //时长
    private String duration;
    //大小
    private  Long size;
    //地址
    private  String data;

    @Override
    public String toString() {
        return "VideoItem{" +
                "title='" + title + '\'' +
                ", duration='" + duration + '\'' +
                ", size='" + size + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
