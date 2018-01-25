package company.sunjunjie.come.sjjlxymqtt;




import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import company.sunjunjie.come.sjjlxymqtt.Fragment.MQFragment;
import company.sunjunjie.come.sjjlxymqtt.Fragment.PM25Fragment;
import company.sunjunjie.come.sjjlxymqtt.Interface.OnChangedListener;
import company.sunjunjie.come.sjjlxymqtt.moder.SlipButton;
import me.panpf.switchbutton.SwitchButton;

public class SJJLXYMainActivity extends AppCompatActivity implements PM25Fragment.OnFragmentInteractionListener,MQFragment.OnFragmentInteractionListener{
    private TextView resultTv;
    private DrawerLayout mDrawerLayout;
    private String host = "tcp://172.20.10.3:61613";
    private String userName = "admin";
    private String passWord = "password";
    private int i = 1;
    private MQFragment mqFragment;
    private PM25Fragment pm25Fragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    //用于画曲线图
    int constNum = 100;
    int constNum2=100;
    private int MQ,PM25;
    private Timer timer = new Timer();
    private GraphicalView chart1,chart2;
    private TimerTask task;
    private int addY = -1,addY2=-1;
    private long addX,addX2;
    private TimeSeries series,series2;
    private XYMultipleSeriesDataset dataset,dataset2;
    Date[] xcache = new Date[constNum];
    int[] ycache = new int[constNum];
    Date[] xcache2 = new Date[constNum];
    int[] ycache2 = new int[constNum];
    private Handler handler,handler2;
    private MqttClient client;
    private String DustTopic = "Dust";
    private String HumidityTopic="Humidity";
    private String TemperatureTopic="Temperature";
    private String PMTopic="PM";
    private String AirTopic="Air";
    private MqttConnectOptions options;
    private TextView temp,humi,PM,yanwu,air,PM25Chart,MQChart;
    private ScheduledExecutorService scheduler;
    //使用广播接收
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;
    public void showChart1(int PM25){
        //生成图表
       /* LinearLayout frameLayout=(LinearLayout) findViewById(R.id.showPM25chart);
        chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "mm:ss");
        frameLayout.addView(chart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,200));*/
        updateChart(PM25);
    }
    public void initPM25(){
        //生成图表
        LinearLayout frameLayout=(LinearLayout) findViewById(R.id.showPM25chart);
        chart1 = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "mm:ss");
        frameLayout.addView(chart1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,200));
    }
    public void initMQ(){
        LinearLayout frameLayout=(LinearLayout) findViewById(R.id.showMQchart);
        chart2 = ChartFactory.getTimeChartView(this, getMQDemoDataset(), getMQRenderer(), "mm:ss");
        frameLayout.addView(chart2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,200));
    }
    public void showChart2(int MQ){
        //生成图表
       /* LinearLayout frameLayout=(LinearLayout) findViewById(R.id.showMQchart);
        chart = ChartFactory.getTimeChartView(this, getMQDemoDataset(), getMQRenderer(), "mm:ss");
        frameLayout.addView(chart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,200));*/
        updateMQChart(MQ);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sjjlxymain);
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.daohang);
        }
        NavigationView navView=(NavigationView) findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.search_weather);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.search_weather:
                        Intent intent=new Intent(SJJLXYMainActivity.this,SJJLXYWeatherActivity.class);
                        startActivity(intent);
                         mDrawerLayout.closeDrawers();
                         break;
                    case R.id.open_camera:
                        Toast.makeText(SJJLXYMainActivity.this, "打开监控", Toast.LENGTH_SHORT).show();
                        break;
                        default:
                            break;

                }
               return true;
            }
        });
        final SlipButton heart=(SlipButton) findViewById(R.id.on);
         heart.SetOnChangedListener(new OnChangedListener() {
            @Override
            public void OnChanged(boolean CheckState) {
                if (CheckState) {
                    Message msg=new Message();
                    msg.what=9;
                    handler.sendMessage(msg);
                } else {
                    Message msg=new Message();
                    msg.what=10;
                    handler.sendMessage(msg);
                }
               /* if(heart.getText().toString().equals("关闭取暖器")){
                    Message msg=new Message();
                    msg.what=9;
                    handler.sendMessage(msg);
                  /*  heart.setText("打开取暖器");
                    MqttMessage message = new MqttMessage();
                    String open="1";
                    message.setPayload(open.getBytes());
                    if(client.isConnected()){
                        try {
                            client.publish("Heater", message);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }*/

                   /* try {
                        client.subscribe(DustTopic, 1);
                        client.subscribe(HumidityTopic,2);
                        client.subscribe(PMTopic,1);
                        client.subscribe(TemperatureTopic,2);
                        client.subscribe(AirTopic,1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
            /*    }else{
                    /*heart.setText("关闭取暖器");
                    MqttMessage message = new MqttMessage();
                    String open="0";
                    message.setPayload(open.getBytes());
                    if(client.isConnected()){
                        try {
                            client.publish("Heater", message);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }*/
                 /*   Message msg=new Message();
                    msg.what=10;
                    handler.sendMessage(msg);
                   /* try {
                        client.subscribe(DustTopic, 1);
                        client.subscribe(HumidityTopic,2);
                        client.subscribe(PMTopic,1);
                        client.subscribe(TemperatureTopic,2);
                        client.subscribe(AirTopic,1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
               // }


            }
        });
        temp=(TextView) findViewById(R.id.temp);
        humi=(TextView) findViewById(R.id.humidity);
        PM=(TextView) findViewById(R.id.PM);
        yanwu=(TextView) findViewById(R.id.yanwu);
        air=(TextView) findViewById(R.id.air);
        PM25Chart=(TextView) findViewById(R.id.PM25Chart);
        MQChart=(TextView) findViewById(R.id.MQChart);
        init();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1) {

                   // Toast.makeText(SJJLXYMainActivity.this, (String) msg.obj,
                           // Toast.LENGTH_SHORT).show();
                    System.out.println("-----------------------------");
                } else if(msg.what == 2) {
                    Toast.makeText(SJJLXYMainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    try {
                        client.subscribe(DustTopic, 1);
                        client.subscribe(HumidityTopic,2);
                        client.subscribe(PMTopic,1);
                        client.subscribe(TemperatureTopic,2);
                        client.subscribe(AirTopic,1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(msg.what == 3) {
                    Toast.makeText(SJJLXYMainActivity.this, "连接失败，系统正在重连", Toast.LENGTH_SHORT).show();
                }else if(msg.what==4){
                    humi.setText("湿度："+(String)msg.obj+"%");
                }else if(msg.what==5){
                    yanwu.setText("MQ烟雾值："+(String)msg.obj);
                    MQ=Integer.parseInt((String)msg.obj);
                    //showChart2(MQ);
                }else if(msg.what==6){
                    temp.setText("温度："+(String)msg.obj+"℃");
                }else if(msg.what==7){
                    PM.setText("PM2.5值："+(String)msg.obj);
                    //刷新图表
                    PM25=Integer.parseInt((String)msg.obj);
                   // updateChart(PM25);
                    //showChart1(PM25);
                }else if(msg.what==8){
                    air.setText("空气质量："+(String)msg.obj);
                }else if(msg.what==9){
                   // heart.setText("打开取暖器");
                    MqttMessage message = new MqttMessage();
                    String open="1";
                    message.setPayload(open.getBytes());
                    try {
                        client.publish("Heater", message);
                    }catch (MqttException e){
                        e.printStackTrace();
                    }
                }else if(msg.what==10){
                   // heart.setText("关闭取暖器");
                    MqttMessage message = new MqttMessage();
                    String open="0";
                    message.setPayload(open.getBytes());
                    try {
                        client.publish("Heater", message);
                    }catch (MqttException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        startReconnect();
        //画图
        //生成图表
       /*FrameLayout frameLayout=(FrameLayout) findViewById(R.id.painting);
        chart = ChartFactory.getTimeChartView(this, getDateDemoDataset(), getDemoRenderer(), "mm:ss");
        frameLayout.addView(chart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,200));*/
        pm25Fragment = new PM25Fragment();           //创建了刚才定义的MainFragment实例
        fragmentManager = getSupportFragmentManager();      //得到FragmentManager
        fragmentTransaction = fragmentManager.beginTransaction();   //得到fragmentTransaction,用于管理fragment的切换
        fragmentTransaction.replace(R.id.painting, pm25Fragment).commit();//将MainActivity里的布局模块fragment_layout替换为mainFrag
       /* mqFragment = new MQFragment();           //创建了刚才定义的MainFragment实例
        fragmentManager = getSupportFragmentManager();      //得到FragmentManager
        fragmentTransaction = fragmentManager.beginTransaction();   //得到fragmentTransaction,用于管理fragment的切换
        fragmentTransaction.replace(R.id.painting, mqFragment).commit();//将MainActivity里的布局模块fragment_layout替换为mainFrag*/
        PM25Chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pm25Fragment = new PM25Fragment();           //创建了刚才定义的MainFragment实例
                fragmentManager = getSupportFragmentManager();      //得到FragmentManager
                fragmentTransaction = fragmentManager.beginTransaction();   //得到fragmentTransaction,用于管理fragment的切换
                fragmentTransaction.replace(R.id.painting, pm25Fragment).commit();//将MainActivity里的布局模块fragment_layout替换为mainFrag
            }
        });
        MQChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqFragment = new MQFragment();           //创建了刚才定义的MainFragment实例
                fragmentManager = getSupportFragmentManager();      //得到FragmentManager
                fragmentTransaction = fragmentManager.beginTransaction();   //得到fragmentTransaction,用于管理fragment的切换
                fragmentTransaction.replace(R.id.painting, mqFragment).commit();//将MainActivity里的布局模块fragment_layout替换为mainFrag
            }
        });
        intentFilter=new IntentFilter();
         localBroadcastManager=LocalBroadcastManager.getInstance(this);
        intentFilter.addAction("company.sunjunjie.come.sjjlxymqtt.painting");
        localReceiver=new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if(!client.isConnected()) {
                    connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    private void init() {
        try {
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, "test",
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                   System.out.println("messageArrived----------");
                   // Toast.makeText(SJJLXYMainActivity.this, "messageArrived----------", Toast.LENGTH_SHORT).show();
                    if(topicName.equals(HumidityTopic))
                    {
                        Message msg=new Message();
                        msg.what=4;
                        msg.obj=message.toString();
                        handler.sendMessage(msg);
                    } else if (topicName.equals(DustTopic)) {
                        Message msg=new Message();
                        msg.what=5;
                        msg.obj=message.toString();
                        handler.sendMessage(msg);
                    }else if(topicName.equals(TemperatureTopic)){
                        Message msg=new Message();
                        msg.what=6;
                        msg.obj=message.toString();
                        handler.sendMessage(msg);
                    }else if(topicName.equals(PMTopic)){
                        Message msg=new Message();
                        msg.what=7;
                        msg.obj=message.toString();
                        handler.sendMessage(msg);
                    }else if(topicName.equals(AirTopic))
                    {
                        Message msg=new Message();
                        msg.what=8;
                        msg.obj=message.toString();
                        handler.sendMessage(msg);
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = topicName+"---"+message.toString();
                    handler.sendMessage(msg);
                }
            });
//			connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    client.connect(options);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
   public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
                default:
                    break;
        }
        return true;
   }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(client != null && keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                client.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
   //更新图表
   private void updateChart(int PM25) {
       //设定长度为20
       int length = series.getItemCount();
       if(length>=constNum) length = constNum;
       addY=PM25;
       addX=new Date().getTime();

       //将前面的点放入缓存
       for (int i = 0; i < length; i++) {
           xcache[i] =  new Date((long)series.getX(i));
           ycache[i] = (int) series.getY(i);
       }

       series.clear();
       //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
       series.add(new Date(addX), addY);
       for (int k = 0; k < length; k++) {
           series.add(xcache[k], ycache[k]);
       }
       //在数据集中添加新的点集
       dataset.removeSeries(series);
       dataset.addSeries(series);
       //曲线更新
       chart1.invalidate();
   }
    private XYMultipleSeriesRenderer getDemoRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitle("PM2.5值随时间的改变");//标题
        renderer.setChartTitleTextSize(20);
        renderer.setXTitle("时间");    //x轴说明
        renderer.setYTitle("PM2.5值");
        renderer.setAxisTitleTextSize(16);
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsTextSize(15);    //数轴刻度字体大小
        renderer.setLabelsColor(Color.BLACK);
        renderer.setLegendTextSize(15);    //曲线说明
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[] {15, 30, 15, 2});//上左下右{ 20, 30, 100, 0 })
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.RED);
        r.setChartValuesTextSize(15);
        r.setChartValuesSpacing(3);
        r.setPointStyle(PointStyle.POINT);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(Color.WHITE);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setPanEnabled(false,false);
        renderer.setShowGrid(true);
        renderer.setYAxisMax(1500);//纵坐标最大值
        renderer.setYAxisMin(500);//纵坐标最小值
        renderer.setInScroll(true);
        return renderer;
    }
    private XYMultipleSeriesDataset getDateDemoDataset() {//初始化的数据
        dataset = new XYMultipleSeriesDataset();
        final int nr = 10;
        long value = new Date().getTime();
      //  Random r = new Random();
        series = new TimeSeries("Demo series " +  1);
        for (int k = 0; k < nr; k++) {
            series.add(new Date(value+k*1000), 0);//初值Y轴以20为中心，X轴初值范围再次定义
        }
        dataset.addSeries(series);
        return dataset;
    }
    //更新图表
    private void updateMQChart(int MQ) {
        //设定长度为20
        int length = series2.getItemCount();
        if(length>=constNum2) length = constNum2;
        addY2=MQ;
        addX2=new Date().getTime();

        //将前面的点放入缓存
        for (int i = 0; i < length; i++) {
            xcache2[i] =  new Date((long)series2.getX(i));
            ycache2[i] = (int) series2.getY(i);
        }

        series2.clear();
        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        series2.add(new Date(addX2), addY2);
        for (int k = 0; k < length; k++) {
            series2.add(xcache2[k], ycache2[k]);
        }
        //在数据集中添加新的点集
        dataset2.removeSeries(series2);
        dataset2.addSeries(series2);
        //曲线更新
        chart2.invalidate();
    }
    private XYMultipleSeriesRenderer getMQRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setChartTitle("MQ值随时间的改变");//标题
        renderer.setChartTitleTextSize(20);
        renderer.setXTitle("时间");    //x轴说明
        renderer.setYTitle("MQ烟雾值");
        renderer.setAxisTitleTextSize(16);
        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsTextSize(15);    //数轴刻度字体大小
        renderer.setLabelsColor(Color.BLACK);
        renderer.setLegendTextSize(15);    //曲线说明
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0,Color.BLACK);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[] {15, 30, 15, 2});//上左下右{ 20, 30, 100, 0 })
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.RED);
        r.setChartValuesTextSize(15);
        r.setChartValuesSpacing(3);
        r.setPointStyle(PointStyle.POINT);
        r.setFillBelowLine(true);
        r.setFillBelowLineColor(Color.WHITE);
        r.setFillPoints(true);
        renderer.addSeriesRenderer(r);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setPanEnabled(false,false);
        renderer.setShowGrid(true);
        renderer.setYAxisMax(300);//纵坐标最大值
        renderer.setYAxisMin(100);//纵坐标最小值
        renderer.setInScroll(true);
        return renderer;

    }
    private XYMultipleSeriesDataset getMQDemoDataset() {//初始化的数据
        dataset2 = new XYMultipleSeriesDataset();
        final int nr = 10;
        long value = new Date().getTime();
       // Random r = new Random();
        series2 = new TimeSeries("Demo series2 " + 1);
        for (int k = 0; k < nr; k++) {
            series2.add(new Date(value+k*1000), 0);//初值Y轴以20为中心，X轴初值范围再次定义
        }
        dataset2.addSeries(series2);
        return dataset2;
    }
    class LocalReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int flag=intent.getIntExtra("painting",-1);
            if(flag==1){
                showChart1(PM25);
            }else if(flag==2){
                showChart2(MQ);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            scheduler.shutdown();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
