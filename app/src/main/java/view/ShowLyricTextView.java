package view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

//显示歌词
public class ShowLyricTextView extends TextView{
    private Paint paint;

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

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        canvas.drawText("歌词显示",getWidth()/2,getHeight()/2,paint);
    }
}
