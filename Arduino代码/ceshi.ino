#include <SoftwareSerial.h>
#include <PubSubClient.h>
#include <dht11.h>
#include <ESP8266WiFi.h>
#include<stdlib.h>

#define DHT11PIN D5
dht11 DHT11;
int dustPin=A0;
float dustVal=0;
int ledPower=D2;
int delayTime=280;
int delayTime2=40;
float offTime=9680;
int jinghuaqi=D4;
int jidianqi=D6;
int heater=D8;
char msg[50],msg1[50],msg2[50],msg3[50];
const char *ssid="sjj";                    
const char *password="sjj6191055";                      
const char *host="172.20.10.3";
const int httpPort=61613;
const char* mqtt_username = "admin";
const char* mqtt_password = "password";
WiFiClient mqtt_client;
PubSubClient client(mqtt_client);


void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  pinMode(ledPower,OUTPUT);
  pinMode(dustPin,INPUT);
  pinMode(jinghuaqi,OUTPUT); 
  pinMode(jidianqi,OUTPUT); 
  pinMode(heater,OUTPUT);
  pinMode(D7,OUTPUT);   
  digitalWrite(jinghuaqi,HIGH);
  digitalWrite(heater,HIGH);
  
  Serial.println("Connecting to  ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
   
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
 
  Serial.println("");
  Serial.println("WiFi connected");  
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  client.setServer(host,httpPort);
  client.setCallback(callback);
 }
 
void reconnect(){
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect(mqtt_username, mqtt_username, mqtt_password)) {
      Serial.println("connected");
      client.subscribe("Heater");   
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(1000);
    }
  }
  }
  
 void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  
  String inchars = ""; //用于存放接收到的字符串
  for (int i = 0; i < length; i++) {
    inchars += (char)payload[i];
  }
  Serial.print(inchars);
  Serial.println();

  if (inchars=="1") {
    digitalWrite(heater, LOW);          //打开取暖器
  } else {
    digitalWrite(heater, HIGH);         //关闭取暖器
  }
}

void loop() {
   while (!client.connected())//几个非连接的异常处理
    {  
    reconnect();
    }
    client.loop();
   
  // put your main code here, to run repeatedly:
int i=0;
digitalWrite(ledPower,LOW);
delayMicroseconds(delayTime);
dustVal=analogRead(dustPin);
delayMicroseconds(delayTime2);
digitalWrite(ledPower,HIGH);
delayMicroseconds(offTime);
delay(1000);
   DHT11.read(DHT11PIN);  
   
   Serial.println("PM2.5：");
   float pm=(float(dustVal/1024)-0.0356)*120000*0.035;
   Serial.println(pm);
     if(0<pm&&pm<=150)  {
      Serial.println("空气质量非常好！");
      client.publish("Air","空气质量非常好！");
      digitalWrite(jinghuaqi, HIGH);       //关闭空气净化器
       digitalWrite(jidianqi, HIGH);      //报警结束
       client.publish("Alarm","0");
       delay(1000);
     }
     else if(150<pm&&pm<=300) {
      Serial.println("空气质量好！");
      client.publish("Air","空气质量好！");
      digitalWrite(jinghuaqi, HIGH);        //关闭空气净化器
       digitalWrite(jidianqi, HIGH);      //报警结束
       client.publish("Alarm","0");
       delay(1000);
      }
     else if(300<pm&&pm<=1100)  {
      Serial.println("空气质量一般！");
       client.publish("Air","空气质量一般！");
      digitalWrite(jinghuaqi, HIGH);        //关闭空气净化器
       digitalWrite(jidianqi, HIGH);      //报警结束
       client.publish("Alarm","0");
       delay(1000);
      }
     else if(1100<pm&&pm<=3000)  {
      Serial.println("空气质量差！");
      client.publish("Air","空气质量差！");
      digitalWrite(jinghuaqi, LOW);        //启动空气净化器
      digitalWrite(jidianqi, LOW);        //报警
      client.publish("Alarm","1");
      while(i<100){
      digitalWrite(D7,HIGH);
      delay(3);
      digitalWrite(D7,LOW);
      delay(3);
      i++;
       }
      delay(1000);
      }
     else {
      Serial.println("空气质量非常差！");
      client.publish("Air","空气质量非常差！");
       digitalWrite(jinghuaqi,LOW);        //启动空气净化器
      digitalWrite(jidianqi, LOW);        //报警
      client.publish("Alarm","1");
      while(i<100){
      digitalWrite(D7,HIGH);
      delay(3);
      digitalWrite(D7,LOW);
      delay(3);
      i++;
       }
      delay(1000);
      }
   itoa(pm,msg,10);
  client.publish("PM", msg);
   
   Serial.println("烟雾：");
   Serial.println(analogRead(D3));  //d3
    itoa(analogRead(D3),msg1,10);
    client.publish("Dust", msg1);
    
   Serial.print("Humdity(%):");
   Serial.println(DHT11.humidity);
    itoa(DHT11.humidity,msg2,10);
    client.publish("Humidity", msg2);
    
   Serial.print("Temperature(oC):");
   Serial.println(DHT11.temperature);
   itoa(DHT11.temperature,msg3,10);
   client.publish("Temperature", msg3);
   
   Serial.println("============");
 
 }

