package com.example.getwifiinfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MainSocket {
	Socket socket;
	OutputStream sockerWriter;
	byte[] sendData;
	//String test = "GET";
	Toast toast;
	Context c;
	String sendOK = "NEO:OK";
	static public ProgressDialog m_pDialog;
	private static String utf8Code = "UTF-8";
	
	//NEO:&IP=xx&PORT=xx
	public MainSocket(String name, String pwd, Context context)
	{
		System.out.println("xxxxxxxxxx11111111");
		//发送格式:NEO:&SSID=""&KEY=""
		c = context;
		String sendMSG = "";
		sendMSG += "NEO:&IP=";
		sendMSG += name;
		sendMSG += "&PORT=";
		sendMSG += pwd;
		//sendMSG += "";
		System.out.println("sendMSG = " + sendMSG);
		creatSocket(sendMSG);
	}
	
	//NEO:&SSID=xx&KEY=xx&IP=xx&PORT=xx
	public MainSocket(String name, String pwd, String ip, String port, Context context)
	{
		//发送格式:NEO:&SSID=""&KEY=""
		System.out.println("xxxxxxxxxx222222222222");
		c = context;
		String sendMSG = "";
		sendMSG += "NEO:&SSID=";
		sendMSG += name;
		sendMSG += "&KEY=";
		sendMSG += pwd;
		sendMSG += "&IP=";
		sendMSG += ip;
		sendMSG += "&PORT=";
		sendMSG += port;
		//sendMSG += "";
		System.out.println("sendMSG = " + sendMSG);
		creatSocket(sendMSG);
	}
	
	/*
     * 创建SOCKET
     */
    public void creatSocket(String sendMSG)
    {
    	sendData = sendMSG.getBytes();
		if(true)
		{
			while(true){
    			new Thread(){	
    				public void run() {
    				try{
    						System.out.println("creat socket...");
    						sendBroadcastToMain(MainActivity.PROGRESSSOCKET);
							socket=new Socket("192.168.1.1",80);
							System.out.println("creat socket succeseful");
							sockerWriter = socket.getOutputStream();																	
							sockerWriter.write(sendData);
							sockerWriter.flush();	
							
							byte[] result_data = new byte[1024];
							InputStream inget = socket.getInputStream();
							int len = inget.read(result_data);
							System.out.println("result_data = "+result_data);
							sendBroadcastToMain(MainActivity.disPROGRESSSOCKET);
							inget.close();
							PopSuccesfulSendMsg(result_data, len);
							socket.close();
    					}catch (Exception e) {
    						// TODO: handle exception
    						e.printStackTrace();
    						sendBroadcastToMain(MainActivity.disPROGRESSSOCKET);
    						Intent it = new Intent(MainActivity.PopString);
    			        	it.putExtra("strSign", R.string.sendFaild);
    			        	c.sendBroadcast(it);
    						System.out.println("creat socket fiald");
    					}
    			};}.start();
    			break;
    			}
		}
    }
    
    //发送
    public void sendMsg(byte[] data)
    {
    	sendData = data;
    	System.out.println("sendMsg");
    	while(true){
    	new Thread(){public void run() {			
			try{
				System.out.println("sendMsg1");
		    	sockerWriter = socket.getOutputStream();																	
				sockerWriter.write(sendData);
				sockerWriter.flush();	
				System.out.println("sendMsg2");
				byte[] result_data = new byte[4*1024];
				InputStream inget = socket.getInputStream();
				inget.read(result_data);
				System.out.println("result_data = "+result_data);
				inget.close();
			}catch (Exception e) {
				//Intent it = new Intent();
				//it.setAction(publicMethod.POPTOASTINFILE);
				//broadcast_Context.sendBroadcast(it);
				System.out.println("sendMsg3");
				e.printStackTrace();
			}
    	}
    	
    	};
    	break;
    	}
    	System.out.println("sendMsg4");
    }
    public boolean closeSocket()
    {
 		   if(socket != null)
 		   {
 			   if(socket.isConnected())
 			   {
 				   try 
 				   {
 					   if(sockerWriter != null)
 					   {
 						   sockerWriter.close();
 					   }
 					   socket.close();
 					   System.out.println("关闭TCP灯控的SOCKET");
 				   } catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				   }
 				   return false;
 			   }
 		   }
 	   return false;
    }
    
    //成功发送指令
    private void PopSuccesfulSendMsg(byte[] data, int len)
    {
    	char resultStr[];
    	byte[] resultData = new byte[len];
    	for(int i = 0; i < len; i++)
    	{
    		resultData[i] = data[i];
    	}
    	String str="";
    	try
		{
    		str = new String(resultData, utf8Code);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    	System.out.println("str="+str);
    	if(charArrayCompare(str.toCharArray(), sendOK.toCharArray()))
    	{
    		Intent it = new Intent(MainActivity.PopString);
        	it.putExtra(MainActivity.strSign, R.string.sendOK);
        	c.sendBroadcast(it);
    	}
    	else
    	{
    		Intent it = new Intent(MainActivity.PopString);
        	it.putExtra(MainActivity.strSign, R.string.sendFaild);
        	c.sendBroadcast(it);
    	}
    }
    
    //CHAR数组比较
    private boolean charArrayCompare(char[] c1, char[] c2)
    {
    	if(c1.length != c2.length)
    	{
    		return false;
    	}
    	
    	for(int i = 0; i < c1.length; i++)
    	{
    		if(c1[i] != c2[i])
    		{
    			return false;
    		}
    	}
    	
    	return true;
    }
	
	private void sendBroadcastToMain(String str)
	{
		Intent it = new Intent(str);
		c.sendBroadcast(it);
	}
}
