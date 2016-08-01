package audio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import com.zhanghao.musicplayer.BaseActivity;
import com.zhanghao.musicplayer.R;


public class AudioPlayerActivity extends BaseActivity {

    //代表了服务,通过它得到服务里面的信息
    private IMusicPlayerService service;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音乐播放器");
        setRightButton(View.GONE);//隐藏右侧按钮
        getData();
        bindService();
    }

    //得到数据
    private void getData() {
        position=getIntent().getIntExtra("position",0);

    }


    private ServiceConnection conn=new ServiceConnection() {
        //绑定成功
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service=IMusicPlayerService.Stub.asInterface(iBinder);
            if (service!=null){
                try {
                    service.openAudio(position);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        //取消绑定
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service=null;
        }
    };

    //以绑定方式启动服务
    private void bindService() {
        Bundle bundle=new Bundle();
        Intent intent=new Intent();
        intent.setAction("bindservice");
        intent.setPackage(this.getPackageName());//安卓5.0之后必须要加这个否则会报错
        intent.putExtras(bundle);
        Log.e("","22222");
        bindService(intent,conn, Context.BIND_AUTO_CREATE);
        Log.e("","333333");
        startService(intent);
        Log.e("","444444");
    }

    @Override
    public View setContentView() {
        return View.inflate(this,R.layout.activity_audioplayer,null);
    }

    @Override
    public void returnButtonClick( ) {finish();}

    @Override
    public void rightButtonClick() {}

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }
}
