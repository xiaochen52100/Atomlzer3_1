package com.example.atomlzer30;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.liys.view.LineProView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public  SerialPortThread serialPortThread;
    private long countdown1=0,countdown2=0;//设备倒计时时长
    private int taskTime1=0,taskTime2=0;//设备定时时长
    private Timer timerTask;//计时器
    private CountDownTimer counttimer1;
    private CountDownTimer counttimer2;
    private boolean state1=false,state2=false;//设备状态
    private byte sendData=0x20;
    private int temperature=25;
    private int humidity =60;
    private int level=90;
    private int gear1=1;
    private int gear2=1;
    private boolean delay1=false;
    private boolean delay2=false;
    private int motor1=0;
    private int motor2=0;
    private int direction1=1;
    private int direction2=1;
    /***********控件初始化*************/
    protected DashboardView tempDashboardView,humDashboardView,levelDashboard;
    protected Button device1Button,device2Button,gearLow1Button,gearHigh1Button,gearLow2Button,gearHigh2Button;
    protected TextView lastTime1,lastTime2;
    protected CircleProgress mCpLoading;
    protected MyNumberPicker np1,np2;
    protected LineProView lineProView1,lineProView2;
    protected TextView temperatureTextView,humidityTextView,debugTextView;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        hideBottomUIMenu();
        //serialPortThread=new SerialPortThread(mHandler);
        //serialPortThread.openSerialPort();
        device1Button=findViewById(R.id.device1Button);
        device2Button=findViewById(R.id.device2Button);
        gearLow1Button=findViewById(R.id.gearLow1);
        gearHigh1Button=findViewById(R.id.gearHigh1);
        gearLow2Button=findViewById(R.id.gearLow2);
        gearHigh2Button=findViewById(R.id.gearHigh2);
        debugTextView=findViewById(R.id.debugTextView);
        lastTime1=findViewById(R.id.lastTime1);
        lastTime2=findViewById(R.id.lastTime2);

        np1 = findViewById(R.id.np1);
        np2 = findViewById(R.id.np2);

        lineProView1=findViewById(R.id.lineProView1);
        lineProView2=findViewById(R.id.lineProView2);

        temperatureTextView=findViewById(R.id.temperature);
        humidityTextView=findViewById(R.id.humidityTextView);

        device1Button.setOnClickListener(this);
        device2Button.setOnClickListener(this);
        gearLow1Button.setOnClickListener(this);
        gearHigh1Button.setOnClickListener(this);
        gearLow2Button.setOnClickListener(this);
        gearHigh2Button.setOnClickListener(this);
        temperatureTextView.setOnClickListener(this);

        np1.setMinValue(0);
        np1.setMaxValue(15);
        np1.setValue(5);

        taskTime1=np1.getValue();
        //Log.d("TAG","taskTime1：" + taskTime1);
        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime1=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        np2.setMinValue(0);
        np2.setMaxValue(15);
        np2.setValue(5);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        taskTime2=np2.getValue();
        //Log.d("TAG","taskTime2：" + taskTime2);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //当NunberPicker的值发生改变时，将会激发该方法
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                taskTime2=newVal;
                //Log.d("TAG","oldVal：" + oldVal + "   newVal：" + newVal);
            }
        });

        mCpLoading = findViewById(R.id.cp_loading);
        //mCpLoading.setProgress(100,5000);
        mCpLoading.setProgress(90);
        mCpLoading.setOnCircleProgressListener(new CircleProgress.OnCircleProgressListener() {
            @Override
            public boolean OnCircleProgress(int progress) {
                return false;
            }
        });
        mCpLoading.setOnClickListener(this);
        if (timerTask==null){
            timerTask = new Timer(true);
            timerTask.schedule(countTask, 500, 1000);
        }
        new Udp.udpReceiveBroadCast("232.11.12.13",6000,mHandler).start();
        new Udp.udpReceiveBroadCast("232.11.12.13",7000,mHandler).start();
    }
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {

            Window _window = getWindow();
            WindowManager.LayoutParams params = _window.getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
            _window.setAttributes(params);
        }
    }
    public TimerTask countTask = new TimerTask() {
        public void run() {
            long currentTime = System.currentTimeMillis();
            TaskData taskData1=new TaskData();
            if (currentTime>=countdown1){//结束
                sendData=(byte)(sendData&(~0x01));
                if (state1){
                    sendHandler(10,null);
                }
                state1=false;
                sendHandler(1,taskData1);
            }else{//进行中
                sendData=(byte)(sendData|0x01);
                double progess=((double) (countdown1-currentTime))/(double)(taskTime1*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown1-currentTime)+"  "+(taskTime1*60*1000));
                taskData1.setProgess(progess);
                int time= (int) ((countdown1-currentTime)/1000);
                taskData1.setLastTime(time);
                sendHandler(1,taskData1);

            }
            TaskData taskData2=new TaskData();
            if (currentTime>=countdown2){//结束
                sendData=(byte)(sendData&(~0x02));
                if (state2){
                    sendHandler(11,null);
                }
                state2=false;
                sendHandler(2,taskData2);
            }else{//进行中
                sendData=(byte)(sendData|0x02);
                double progess=((double) (countdown2-currentTime))/(double)(taskTime2*60*1000);
                //Log.v("tag","progess:"+progess+"  "+(countdown2-currentTime)+"  "+(taskTime2*60*1000));
                taskData2.setProgess(progess);
                int time= (int) ((countdown2-currentTime)/1000);
                taskData2.setLastTime(time);
                sendHandler(2,taskData2);
                byte[] sendBuf={0x25};
                //serialPortThread.sendSerialPort(sendBuf);
            }

            byte[] sendBuf={0};
            sendBuf[0]=sendData;
            //Log.d("TAG","sendData:"+sendData);
            //serialPortThread.sendSerialPort(sendBuf);
            //发送udp数据格式
            byte[] udpSendBuf=new byte[80];
            System.arraycopy(DateForm.intToBytesArray(temperature),0,udpSendBuf,0,4);
            System.arraycopy(DateForm.intToBytesArray(humidity),0,udpSendBuf,4,4);
            System.arraycopy(DateForm.intToBytesArray(level),0,udpSendBuf,8,4);

            System.arraycopy(DateForm.intToBytesArray(taskTime1),0,udpSendBuf,12,4);
            System.arraycopy(DateForm.intToBytesArray(taskData1.getLastTime()),0,udpSendBuf,16,4);
            System.arraycopy(DateForm.doubleToByteArray(taskData1.getProgess()),0,udpSendBuf,20,8);
            if (state1){
                System.arraycopy(DateForm.intToBytesArray(1),0,udpSendBuf,28,4);
            }else {
                System.arraycopy(DateForm.intToBytesArray(0),0,udpSendBuf,28,4);
            }
            System.arraycopy(DateForm.intToBytesArray(gear1),0,udpSendBuf,32,4);

            System.arraycopy(DateForm.intToBytesArray(taskTime2),0,udpSendBuf,36,4);
            System.arraycopy(DateForm.intToBytesArray(taskData2.getLastTime()),0,udpSendBuf,40,4);
            System.arraycopy(DateForm.doubleToByteArray(taskData2.getProgess()),0,udpSendBuf,44,8);
            if (state2){
                System.arraycopy(DateForm.intToBytesArray(1),0,udpSendBuf,52,4);
            }else {
                System.arraycopy(DateForm.intToBytesArray(0),0,udpSendBuf,52,4);
            }
            System.arraycopy(DateForm.intToBytesArray(gear2),0,udpSendBuf,56,4);

            new Udp.udpSendBroadCast("232.11.12.13",6000,udpSendBuf).start();
            //byte[] LastTime1Byte=new byte[4];
            //System.arraycopy(udpSendBuf,20,LastTime1Byte,0,4);
            //Log.d("TAG",DateForm.byteArrayToInt(LastTime1Byte)+"");
            //Log.d("TAG", Arrays.toString(udpSendBuf));
            byte[] udpSendBufBoard=new byte[8];
            udpSendBufBoard[0]=0x7f;
            if (state1){
                udpSendBufBoard[1]=1;
                if (gear1==1){
                    udpSendBufBoard[2]=1;
                }else if (gear1==2){
                    udpSendBufBoard[2]=2;
                }
            }else {
                if (delay1){
                    udpSendBufBoard[1]=1;
                }else {
                    udpSendBufBoard[1]=0;
                }
                udpSendBufBoard[2]=(byte)motor1;
            }
            if (state2){
                udpSendBufBoard[3]=1;
                if (gear2==1){
                    udpSendBufBoard[4]=1;
                }else if (gear2==2){
                    udpSendBufBoard[4]=2;
                }
            }else {
                if (delay2){
                    udpSendBufBoard[3]=1;
                }else {
                    udpSendBufBoard[3]=0;
                }
                udpSendBufBoard[4]=(byte)motor2;
            }
            udpSendBufBoard[5]=(byte)(180-128);//低
            udpSendBufBoard[6]=(byte)(210-128);//中
            udpSendBufBoard[7]=(byte)(250-128);//高
            udpSendBufBoard[8]=(byte)direction1;//电机1方向
            udpSendBufBoard[9]=(byte)direction2;//电机2方向
            //Log.d("TAG","state2:"+state2);
            //Log.d("TAG","send: " + Arrays.toString(udpSendBufBoard) + "\n");
            new Udp.udpSendBroadCast("232.11.12.13",7000,udpSendBufBoard).start();

        }
    };
    private int bytesToInt(byte[] b, int offset) {
        int res = (b[offset]&0xff)+((b[offset+1]&0xff)<<8)+((b[offset+2]&0xff)<<16)+((b[offset+1]&0xff)<<24);
        return res&0xffffff;
    }
    private Handler mHandler  = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (state1){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView1.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime1.setText(minutes+":"+second);
                    }else {
                        lineProView1.setProgress(0);
                        lastTime1.setText("00:00");
                        device1Button.setText("开始");
                    }
                    break;
                case 2:
                    if (state2){
                        TaskData taskData=new TaskData();
                        taskData=(TaskData)msg.obj;
                        lineProView2.setProgress(100-taskData.getProgess()*100);
                        String minutes=String.format("%0" + 2 + "d", taskData.getLastTime()/60);
                        String second=String.format("%0" + 2 + "d", ((int)taskData.getLastTime()%60));
                        lastTime2.setText(minutes+":"+second);
                    }else {
                        lineProView2.setProgress(0);
                        lastTime2.setText("00:00");
                        device2Button.setText("开始");
                    }
                    break;
                case 3:
                    break;
                case 4:
                    break;

                case 5://设备1 开始
                    device1Button.setText("停止");
                    taskTime1=(int)msg.obj;
                    np1.setValue(taskTime1);
                    countdown1=System.currentTimeMillis()+taskTime1*60*1000;
                    state1=true;
                    break;
                case 6://设备1 停止
                    device1Button.setText("开始");
                    countdown1=System.currentTimeMillis();
                    //state1=false;
                    //motor1=0;
                    counttimer1 = new CountDownTimer(20000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                            if (((millisUntilFinished-1) / 1000)>5&&((millisUntilFinished-1) / 1000)<15){
                                direction1=2;
                                motor1=2;
                            }else {
                                direction1=1;
                                motor1=0;
                            }
                            delay1=true;
                        }
                        @Override
                        public void onFinish() {
                            device1Button.setText("停止");
                            delay1=false;
                        }
                    };
                    counttimer1.start();
                    break;
                case 7://设备2 开始
                    device2Button.setText("停止");
                    taskTime2=(int)msg.obj;
                    np2.setValue(taskTime2);
                    countdown2=System.currentTimeMillis()+taskTime2*60*1000;
                    state2=true;
                    break;
                case 8://设备2 停止
                    device2Button.setText("开始");
                    countdown2=System.currentTimeMillis();
                    state2=false;
                    break;
                case 9://设备1挡位
                    if ((int)msg.obj==31){
                        gear1=1;
                        gearHigh1Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                        gearLow1Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                    }else if ((int)msg.obj==32){
                        gear1=2;
                        gearLow1Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                        gearHigh1Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                    }
                    break;
                case 10://
                    counttimer1 = new CountDownTimer(20000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                            if (((millisUntilFinished-1) / 1000)>5&&((millisUntilFinished-1) / 1000)<15){
                                direction1=2;
                                motor1=2;
                            }else {
                                direction1=1;
                                motor1=0;
                            }
                            delay1=true;
                        }
                        @Override
                        public void onFinish() {
                            device1Button.setText("停止");
                            delay1=false;
                        }
                    };
                    counttimer1.start();
                    break;
                case 11://
                    counttimer2 = new CountDownTimer(20000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                            if (((millisUntilFinished-1) / 1000)>5&&((millisUntilFinished-1) / 1000)<15){
                                direction2=2;
                                motor2=2;
                            }else {
                                direction2=1;
                                motor2=0;
                            }
                            delay2=true;
                        }
                        @Override
                        public void onFinish() {
                            device2Button.setText("停止");
                            delay2=false;
                        }
                    };
                    counttimer2.start();
                    break;
                case 12://
                    break;

                case 13:
                    byte[] rcvByte=(byte[])msg.obj;
                    if (rcvByte[0]==(byte)0x7f){
                        int temperature=bytesToInt(rcvByte,3);
                        int humidity=bytesToInt(rcvByte,7);
                        int levels=bytesToInt(rcvByte,11);
                        Log.d("TAG","temperature: " + temperature);
                        Log.d("TAG","humidity: " + humidity);
                        Log.d("TAG","levels: " + levels);
                        //Log.d("TAG","levels: " + ((rcvByte[11]&0xff)+((rcvByte[12]&0xff)*256)));
                        debugTextView.setText(levels+"");
                        //if (levels>100) break;
                        //if (levels<0) break;
                        //level=levels;//(int)((levels-7)/34.00*100);//用于液位校准 7-41
                        if (level<=0) level=0;
                        if (level>=100) level=100;
                        temperatureTextView.setText(temperature/10.0+"℃");
                        humidityTextView.setText(humidity/10.0+"％");
                        //mCpLoading.setProgress(level);
                    }
                    break;
            }
        }
    };

    private void sendHandler(int what,Object obj){
        Message msg = new Message();
        msg.what=what;
        msg.obj=obj;
        mHandler.sendMessage(msg);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.device1Button:
                if (!state1){
                    device1Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown1=currentTime+taskTime1*60*1000;
                    state1=true;

                }else {
                    //device1Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown1=System.currentTimeMillis();
                    sendHandler(10,null);
                }
                CountDownTimer counttimer3 = new CountDownTimer(5000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                        device1Button.setEnabled(false);
                    }
                    @Override
                    public void onFinish() {
                        device1Button.setEnabled(true);
                    }
                };
                counttimer3.start();
                break;
            case R.id.device2Button:
                if (!state2){
                    device2Button.setText("停止");
                    long currentTime = System.currentTimeMillis();
                    countdown2=currentTime+taskTime2*60*1000;
                    state2=true;
                }else {
                    //device2Button.setText("开始");
                    long currentTime = System.currentTimeMillis();
                    countdown2=System.currentTimeMillis();
                    sendHandler(11,null);

                }
                CountDownTimer counttimer4 = new CountDownTimer(5000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //device1Button.setText(((millisUntilFinished-1) / 1000)+"秒后停止");
                        device2Button.setEnabled(false);
                    }
                    @Override
                    public void onFinish() {
                        device2Button.setEnabled(true);
                    }
                };
                counttimer4.start();
                break;

            case R.id.cp_loading:
                //Log.d("TAG","hello");
                //new Udp.udpSendBroadCast("hello").start();
//                new Udp.udpReceiveBroadCast(mHandler).start();
                break;
            case R.id.temperature:
                //new Udp.udpReceiveBroadCast().start();
                break;
            case R.id.gearLow1:
                gearHigh1Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                gearLow1Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                gear1=1;
                break;
            case R.id.gearHigh1:
                gearLow1Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                gearHigh1Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                gear1=2;
                break;
            case R.id.gearLow2:
                gearHigh2Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                gearLow2Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                gear2=1;
                break;
            case R.id.gearHigh2:
                gearLow2Button.setBackgroundColor(Color.parseColor("#CDD1D1"));
                gearHigh2Button.setBackgroundColor(Color.parseColor("#03DAC5"));
                gear2=2;
                break;
        }
    }
}
