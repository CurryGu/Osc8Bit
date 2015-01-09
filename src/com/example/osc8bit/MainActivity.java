package com.example.osc8bit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	
	TextView BTinfo;//状态信息栏
	Button BTopen;
	Button BTclose;
	Button BTpaired;
	Button BTscan;
	Button BTshut;
	ListView listOfDevice;
	
	boolean flag_state = false;//蓝牙打开关闭标志位
	ArrayAdapter<String> myArrayAdapter;//存放蓝牙地址的一个容器ArrayList
	BluetoothAdapter myAdapter;
	
	//设置一个广播接收者内部类，用来监听蓝牙的动作	
	private final BroadcastReceiver myReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			//arg1是监听到的意图
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			//发现蓝牙设备
			if(BluetoothDevice.ACTION_FOUND.equals(action))
			{
				//获取蓝牙设备对象
				BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//将蓝牙设备信息添加到ListView上
				//前17位是蓝牙地址，后17位是蓝牙名称
				myArrayAdapter.add(device.getAddress()+"\n"+device.getName());
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				//注意：ACTION_DISCOVERY_FINISHED是BluetoothAdapter集合中的参数
				if(myArrayAdapter.getCount()==0)
				{
					Toast.makeText(getBaseContext(), "没有发现蓝牙设备", 0).show();
				}
			}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
				BTinfo.setText("info:蓝牙设备已改变");
			}else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				BTinfo.setText("info:正在搜索蓝牙设备...");
			}
		}
		
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 BTinfo = (TextView) findViewById(R.id.info);
		 BTopen = (Button) findViewById(R.id.open_bt);
		 BTclose = (Button) findViewById(R.id.close_bt);
		 BTscan = (Button) findViewById(R.id.scan_bt);
		 BTpaired = (Button) findViewById(R.id.pair_bt);
		 BTshut = (Button) findViewById(R.id.shut_bt);
		 listOfDevice = (ListView) findViewById(R.id.listOfDevice);
		 
		 //分别实现点击事件
		 BTopen.setOnClickListener(new BTopenListener());
		 BTclose.setOnClickListener(new BTcloseListener());
		 BTscan.setOnClickListener(new BTscanListener());
		 BTpaired.setOnClickListener(new BTpairedListener());
		 BTshut.setOnClickListener(new BTshutListener());
		 
		 listOfDevice.setOnItemClickListener(new myOnItemClickListener());
		 
		 //使用ArrayAdapter填充listview
		 myArrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1);
		 //每个list都是按照simple_list_item_1进行显示，中间就一个文本
		 listOfDevice.setAdapter(myArrayAdapter);
		 
		 
		 myAdapter = BluetoothAdapter.getDefaultAdapter();
		 if(myAdapter==null)
		 {
			 //本机没有蓝牙
			 BTinfo.setText("info:未发现蓝牙设备");
			 //就让蓝牙打开按钮失效
			 BTopen.setEnabled(false);
		 }else{
			 if(!myAdapter.isEnabled())
			 {
				 BTinfo.setText("info:蓝牙设备不可用");
			 }else{
				 BTinfo.setText("info:蓝牙设备已打开");
				 flag_state =true;//将蓝牙标志位设为true
			 }
		 }
		 
		 //注册一个意图过滤器，提供给广播接受者使用，监听蓝牙状态
		 IntentFilter intentFilter = new IntentFilter();
		 intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		 intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		 intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		 intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		 registerReceiver(myReceiver,intentFilter);
		 
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, 1, 1, "进入示波器模式");
		menu.add(0, 2, 2, "进入麦克风模式");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		
	}
	
	
	//点击menu条目对应的事件
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		//点击	menu.add(0, 1, 1, "进入示波器模式");
		//则进入示波器模式
		//点击	menu.add(0, 2, 2, "进入麦克风模式");
		//则进入麦克风模式
		
		
		//进入osc页面
		Intent it = new Intent(this,osc.class);
		//itStr保存蓝牙地址
		String itStr = null;
		
		switch(item.getItemId()){
		case 1:
			//进入示波器，将蓝牙地址MACStr传给osc.class
			if(MACStr != null)
			{
				itStr = MACStr;
				it.putExtra("MACADDR", itStr);
				startActivity(it);
			}
			break;
		case 2:
			//不携带地址，直接进入MIC
			it.putExtra("MACADDR", itStr);
			startActivity(it);
			break;
		default:
			Toast.makeText(getBaseContext(), "无设置项", 0).show();
			break;
		}
		return true;
	}
	
	//点击listview条目，即获得对应蓝牙设备的地址
	String MACStr = null;//初始化蓝牙地址
	private class myOnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			//获取条目的文本内容
			MACStr = (String) listOfDevice.getItemAtPosition(position);
			//截取前17位，蓝牙地址
			MACStr =MACStr.substring(0, 17);
			Toast.makeText(getBaseContext(), "蓝牙地址："+MACStr, 0).show();
		}
	}
	
	
	//蓝牙断开按钮点击事件
	private class BTcloseListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(flag_state){
				try {
					if(myAdapter.isDiscovering()){
						myAdapter.cancelDiscovery();
					}
					if(myAdapter.isEnabled()){
						myAdapter.disable();
						flag_state=false;
					}
					BTinfo.setText("info:关闭蓝牙模块成功");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					BTinfo.setText("info:关闭蓝牙模块失败");
				}
			}else{
				BTinfo.setText("info:蓝牙已经关闭");
			}
		}
		
	}
	//扫描蓝牙设备点击事件
	private class BTscanListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			myAdapter.startDiscovery();
		}
		
	}
	//蓝牙配对点击事件
	private class BTpairedListener implements OnClickListener{
		//所谓的配对，就是把已经配对好的蓝牙设备显示到ListView上
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!flag_state)
			{
				BTinfo.setText("info:蓝牙设备未连接");
			}else{
				Set<BluetoothDevice> bondedDevices = myAdapter.getBondedDevices();
				if(bondedDevices.size()>0)
				{
					for(BluetoothDevice device : bondedDevices)
					{
						myArrayAdapter.add(device.getAddress()+"\n"+device.getName());
					}
				}else{
					BTinfo.setText("info:没有配对的蓝牙设备");
				}
			}
		}
		
	}
	//蓝牙设备断开点击事件
	private class BTshutListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//断开设备连接即清空当前蓝牙地址，关闭蓝牙连接的线程
			//由于蓝牙线程是在osc中打开的，所以，不需要在这里进行关闭
			//在主界面的时候，蓝牙只是处于打开状态，但是不是处在连接状态
			//这里可以不响应点击事件
		}
		
	}
 	
	
	
	public void onDestroy(){
		//页面销毁时，要做的操作
		super.onDestroy();
		
		if (myAdapter!=null) {
			//停止搜索
			myAdapter.cancelDiscovery();
			if (myAdapter.isEnabled()) {
				//关闭蓝牙
				myAdapter.disable();
				flag_state=false;
			}
		}
	}
	
	//打开蓝牙设备按钮事件
	private class BTopenListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!flag_state)
			{
				try{
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    	    startActivityForResult(enableBtIntent,0);
				}catch(Exception e)
				{
					
				}
			}else{
				BTinfo.setText("info:蓝牙已打开");
			}
			
		}
		
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ 
    	super.onActivityResult(requestCode, resultCode, data); 
    	if(requestCode == 0 && resultCode == RESULT_OK){
    		BTinfo.setText("info:蓝牙已打开");
    		flag_state = true;
    	}else{
    		BTinfo.setText("info:蓝牙打开失败");
    	}
	}
	 
	
	
}

//蓝牙连接线程
class ConnectThread extends Thread{
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	
	public ConnectThread(BluetoothDevice device) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp =null;
		this.mmDevice = device;
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try{
			tmp=device.createRfcommSocketToServiceRecord(MY_UUID);
		}catch(IOException e){
			
		}
		//初始化ConnectThread线程之后，获得一个BluetoothSocket实例对象mmSocket
		//通过这个socket对象，进行数据传递
		mmSocket=tmp;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		//1.停止搜寻
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			//2.开始连接
			mmSocket.connect();
			
		} catch (IOException e) {
			
			try {
				mmSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			e.printStackTrace();
			
			return;
		}
	}
	/** Will cancel an in-progress connection, and close the socket */
	//对外提供一个主动断开socket的方法，这样就可以先关闭socket再关闭蓝牙连接
	public void cancel(){
		try{
			mmSocket.close();
		}catch(IOException e){

		}
	}
	
}

//数据采集线程，蓝牙已经连接
class ConnectedThread extends Thread{
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	
	Handler mmHandler;
	
	int counter = 0;
	//两个通道，每个采集700个数据
	byte[] buffer = new byte[1400];
	int bytes;
	byte[] tempBuffer = new byte[1400];
	
	
	
	public ConnectedThread(BluetoothSocket socket,Handler handler)
	{
		mmSocket =  socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		
		try {
			tmpIn= socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//初始化参数
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
		mmHandler = handler;
	}
	
	
	public void run(){
		while(true)
		{
			try{
				//一次全部读取进来的个数
				bytes = mmInStream.read(tempBuffer);
				//转存到buffer里
				for(int i=0;i<bytes;i++)
					buffer[counter++]=tempBuffer[i];
				//双通道全部打开且采集了1400个字节数据
				//或者双通道没打开且采集了700个字节数据
				//开始向osc发送蓝牙采集到的数据buffer，使用handler
				if(((counter>=1400)&&(com.example.osc8bit.osc.flag_CHAll)) || ((counter>=700)&&(!com.example.osc8bit.osc.flag_CHAll))){
					mmHandler.obtainMessage(osc.MESSAGE_READ, counter, -1, buffer).sendToTarget();
					//发送完毕，将计数的counter清零
					counter = 0;
				}
			}catch(IOException e){
				break;
			}
		}
	}
	/* Call this from the main activity to send data to the remote device */
	public void write(byte[] bytes) {
	    try {
	        mmOutStream.write(bytes);
	    } catch (IOException e) { }
	}

	/* Call this from the main activity to shutdown the connection */
	public void cancel() {
	    try {
	        mmSocket.close();
	    } catch (IOException e) { }
	  }
} 


