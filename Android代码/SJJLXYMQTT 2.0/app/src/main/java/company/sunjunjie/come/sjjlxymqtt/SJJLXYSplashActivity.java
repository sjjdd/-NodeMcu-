package company.sunjunjie.come.sjjlxymqtt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SJJLXYSplashActivity extends AppCompatActivity {
    private TextView welcome_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sjjlxysplash);
        //获取组件
        LinearLayout r1_splash=(LinearLayout) findViewById(R.id.r1_splash);
        welcome_title=(TextView) findViewById(R.id.welcome);
        //背景透明度变化5秒内从0.5变到1.0
        AlphaAnimation aa=new AlphaAnimation(0.5f,1.0f);
        aa.setDuration(5000);
        r1_splash.startAnimation(aa);
        //创建Timer对象
      /*  Connector.getDatabase();
        initMusic();*/
        Timer timer=new Timer();
        //创建TimerTask对象
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
              /*  Connector.getDatabase();
                initMusic();*/
                Intent intent=new Intent(SJJLXYSplashActivity.this,SJJLXYMainActivity.class);
                startActivity(intent);
                finish();
            }

        };
        //使用timer.schedule()方法调用timerTak,定时5秒后执行run

        timer.schedule(timerTask,5000);

    }
}
