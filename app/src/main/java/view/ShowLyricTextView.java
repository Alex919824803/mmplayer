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
public class ShowLyricTextView extends TextView {
    private Paint currentPaint;
    private Paint nocurrentPaint;
    private ArrayList<Lyric> lyrics;
    //当前句歌词，可以代表某一句歌词在列表中的位置
    private int index;

    //在布局文件中使用，实例化的时候用到它
    public ShowLyricTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        currentPaint = new Paint();
        //设置画笔颜色为绿色
        currentPaint.setColor(Color.GREEN);
        //设置抗锯齿
        currentPaint.setAntiAlias(true);
        //设置文字水平方向对齐
        currentPaint.setTextAlign(Paint.Align.CENTER);
        //设置文字大小
        currentPaint.setTextSize(16);

        nocurrentPaint = new Paint();
        //设置画笔颜色为白色
        nocurrentPaint.setColor(Color.WHITE);
        //设置抗锯齿
        nocurrentPaint.setAntiAlias(true);
        //设置文字水平方向对齐
        nocurrentPaint.setTextAlign(Paint.Align.CENTER);
        //设置文字大小
        nocurrentPaint.setTextSize(32);

        //添加假设歌词
        lyrics = new ArrayList<Lyric>();
        for (int i = 0; i < 200; i++) {
            Lyric lyric = new Lyric();
            lyric.setContent(i + "aaaaaaaaaaaaaaaa" + i);
            lyric.setTimePoint(1000 * i);
            lyric.setSleepTime(2000);
            //把歌词添加到歌词列表中
            lyrics.add(lyric);
        }

    }

    //当前控件的宽
    private int width;
    //当前控件的高
    private int heigh;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        heigh = h;
    }

    //每行的高度
    private float textHeight = 20;

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        if (lyrics != null && lyrics.size() > 0) {
            //1.画当前语句
            String currentContent = lyrics.get(index).getContent();
            canvas.drawText(currentContent, width / 2, heigh / 2, currentPaint);

            //2.画当前语句前面歌词
            float tempY = heigh / 2;
            for (int i = index - 1; i > 0; i--) {
                String nextContent = lyrics.get(i).getContent();
                tempY = tempY - textHeight;
                canvas.drawText(nextContent, width / 2, tempY, nocurrentPaint);
                if (tempY < 0) {
                    break;
                }
            }

            //3.画当前语句后面歌词
            tempY = heigh / 2;
            for (int i = index + 1; i < lyrics.size(); i++) {
                String preContent = lyrics.get(i).getContent();
                tempY = tempY + textHeight;
                canvas.drawText(preContent, width / 2, tempY, nocurrentPaint);
                if (tempY > heigh) {
                    break;
                }
            }

        } else {
            canvas.drawText("暂无歌词", width / 2, heigh / 2, currentPaint);
        }
    }
}
