package audio;

import android.os.Bundle;
import android.view.View;

import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;

/**
 * Created by 91982 on 2016/7/29.
 */

public class AudioplayerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音乐播放器");
        setRightButton(View.GONE);//隐藏右侧按钮
    }

    @Override
    public View setContentView() {
        return View.inflate(this,R.layout.activity_audioplayer,null);
    }

    @Override
    public void returnButtonClick() {
        finish();
    }

    @Override
    public void rightButtonClick() {}
}
