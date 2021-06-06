package com.example.atomlzer30;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.x6.serialportlib.SerialPort;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialPortThread {
    private SerialPort serialPort;
    private boolean openFlag = false;
    private String selectPort = "S0";
    private int selectSpeed = 115200;
    private String recvStr = "";
    private Handler mHandler;
    private AtomicBoolean alive = new AtomicBoolean(true);
    public SerialPortThread(Handler handler){
        mHandler=handler;
    }
    /**
     * 打开串口
     */
    public void openSerialPort() {
        if (serialPort == null) {
            openFlag = true;
            alive.set(true);
            serialPort = new SerialPort(selectPort, selectSpeed, 8, 1, (int)'n', true, 0, 0);

            // 创建接收数据线程
            Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "RecvThread");
                }
            }).execute(new Runnable() {
                @Override
                public void run() {
                    while (alive.get()) {
                        if (serialPort != null) {
                            byte[] recvBytes = serialPort.receiveData(true);
                            if (recvBytes != null && recvBytes.length > 0) {
                                //Log.i("tag", "recvBytes === " + SerialPort.bytesToHexString(recvBytes, recvBytes.length));
                                Message msg = new Message();
                                msg.what = 13;
                                msg.obj=recvBytes;
                                mHandler.sendMessage(msg);
//                                if (quickHandler!=null){
//                                    quickHandler.sendMessage(msg);
//
//                                }else if(broadHandler!=null){
//                                    broadHandler.sendMessage(msg);
//
//                                }else if(professionHandler!=null){
//                                    professionHandler.sendMessage(msg);
//                                }MainActivity.nowFragmentId
//                                if (MainActivity.nowFragmentId== R.id.quick_disinfection_fragment){
//                                    if (quickHandler!=null) {
//                                        quickHandler.sendMessage(msg);
//                                    }
//
//                                }else if(MainActivity.nowFragmentId==R.id.broad_disinfection_fragment){
//                                    broadHandler.sendMessage(msg);
//
//                                }else if(MainActivity.nowFragmentId==R.id.profession_disinfection_fragment){
//                                    professionHandler.sendMessage(msg);
//                                }

                            }
                        }
                        SystemClock.sleep(20);
                    }
                }
            });
        }
    }
    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        openFlag = false;
        alive.set(false);
        // 添加延时等待读取线程停止
        SystemClock.sleep(100);

        if (serialPort != null && serialPort.isOpen) {
            serialPort.closeSerial();
        }

        serialPort = null;
    }
    /**
     * 发送数据
     */
    public void sendSerialPort(byte[] sendBuff){
        if (serialPort != null && serialPort.isOpen) {
            serialPort.sendData(sendBuff);
            //Log.i("tag",sendBuff+"");
        }
    }
}
