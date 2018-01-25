package company.sunjunjie.come.sjjlxymqtt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import company.sunjunjie.come.sjjlxymqtt.Adapter.WeatherAdapter;
import company.sunjunjie.come.sjjlxymqtt.moder.Weather;
import company.sunjunjie.come.sjjlxymqtt.util.HttpCallbackListener;
import company.sunjunjie.come.sjjlxymqtt.util.HttpUtil;

public class SJJLXYWeatherActivity extends AppCompatActivity {

    private EditText ecCity;
    private ImageButton btnQuery;
    private ListView lvFutureWeather;
    public static final int SHOW_RESPONSE=1;
    private List<Weather> data;
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response=(String )msg.obj;
                    if(response!=null){
                        parseWithJSON(response);
                        WeatherAdapter weatherAdapter=new WeatherAdapter
                                (SJJLXYWeatherActivity.this,
                                        R.layout.activity_weather_listitem, data);
                        lvFutureWeather.setAdapter(weatherAdapter);
                        ScaleAnimation scaleAnimation=new ScaleAnimation(0,1,0,1);
                        scaleAnimation.setDuration(1000);
                        LayoutAnimationController animationController  =  new
                                LayoutAnimationController(
                                scaleAnimation, 0.6f);
                        lvFutureWeather.setLayoutAnimation(animationController);


                    }
                    break;

                default:
                    break;
            }
        }

        private void parseWithJSON(String response) {
            // TODO Auto-generated method stub
            data=new ArrayList<Weather>();
            JsonParser parser=new JsonParser();//json解析器
            JsonObject obj=(JsonObject) parser.parse(response);
            //获取返回状态吗
            String resultcode=obj.get("resultcode").getAsString();
            //状态码如果是200说明数据返回成功
            if(resultcode!=null&&resultcode.equals("200")){
                JsonObject resultObj=obj.get("result").getAsJsonObject();
                JsonArray futureWeatherArray=resultObj.get("future").getAsJsonArray();
                for(int i=0;i<futureWeatherArray.size();i++){
                    Weather  weather=new Weather();
                    JsonObject weatherObject=futureWeatherArray.get(i).getAsJsonObject();
                    weather.setDayOfWeek(weatherObject.get("week").getAsString());
                    weather.setDate(weatherObject.get("date").getAsString());
                    weather.setTemperature(weatherObject.get("temperature")
                            .getAsString());
                    weather.setWeather(weatherObject.get("weather")
                            .getAsString());
                    data.add(weather);
                }
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sjjlxyweather);
        initViews();
        setListeners();
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.littleback);
        }
    }
    public void initViews(){
        ecCity=(EditText) findViewById(R.id.etCity);
        btnQuery=(ImageButton)findViewById(R.id.btnQuery);
        lvFutureWeather=(ListView) findViewById(R.id.lvFutureWeather);
        data=new ArrayList<Weather>();
    }
    private void setListeners(){
        btnQuery.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String cityName=ecCity.getText().toString();
                try {
                    cityName = URLEncoder.encode(cityName, "utf-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                System.out.println("lvFutureWeather="+lvFutureWeather);
                Toast.makeText(SJJLXYWeatherActivity.this, "success",
                        Toast.LENGTH_LONG).show();
                String weatherUrl = "http://v.juhe.cn/weather/index?format=2&cityname="+cityName+"&key=d1b3955ff2778daa200c85aaa21059be";
                Toast.makeText(SJJLXYWeatherActivity.this, "success"+weatherUrl,
                        Toast.LENGTH_LONG).show();
                HttpUtil.sendHttpRequest(weatherUrl, new HttpCallbackListener() {

                    public void onFinish(String response) {
                        // TODO Auto-generated method stub
                        Message message=new Message();
                        message.what=SHOW_RESPONSE;
                        //将服务器返回的结果存放到Message中
                        message.obj=response.toString();
                        handler.sendMessage(message);
                    }

                    public void onError(Exception e) {
                        // TODO Auto-generated method stub
                        System.out.println("访问失败");
                    }
                });
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar2,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                SJJLXYWeatherActivity.this.finish();
                break;
            default:
                break;
        }
        return true;
    }
}
