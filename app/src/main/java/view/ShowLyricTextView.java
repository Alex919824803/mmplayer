package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;
import domain.Lyric;

import java.util.ArrayList;

//显示歌词
public class ShowLyricTextView extends TextView{
    private Paint paint;
    private ArrayList<Lyric> lyrics;
    //当前句歌词，可以代表某一句歌词在列表中的位置
    private int index;

    //在布局文件中使用，实例化的时候用到它
    public ShowLyricTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        paint=new Paint();
        //设置画笔颜色为绿色
        paint.setColor(Color.GREEN);
        //设置抗锯齿
        paint.setAntiAlias(true);
        //设置文字水平方向对齐
        paint.setTextAlign(Paint.Align.CENTER);
        //设置文字大小
        paint.setTextSize(16);

        //添加假设歌词
        lyrics=new ArrayList<Lyric>();
        for (int i=0;i<200;i++){
            Lyric lyric=new Lyric();
            lyric.setContent(i+"aaaaaaaaaaaaaaaa"+i);
            lyric.setTimePoint(1000*i);
            lyric.setSleepTime(2000);
            //把歌词添加到歌词列表中
            lyrics.add(lyric);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if (lyrics!=null&&lyrics.size()>0) {
            //1.画当前语句
            String currentContent = lyrics.get(index).getContent();
            //2.画当前语句前面歌词
            //3.画当前语句后面歌词

        }
        else {
            canvas.drawText("暂无歌词", getWidth() / 2, getHeight() / 2, paint);
        }
    }
}
