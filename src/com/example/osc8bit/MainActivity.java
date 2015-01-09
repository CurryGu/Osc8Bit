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
	
	
	TextView BTinfo;//״̬��Ϣ��
	Button BTopen;
	Button BTclose;
	Button BTpaired;
	Button BTscan;
	Button BTshut;
	ListView listOfDevice;
	
	boolean flag_state = false;//�����򿪹رձ�־λ
	ArrayAdapter<String> myArrayAdapter;//���������ַ��һ������ArrayList
	BluetoothAdapter myAdapter;
	
	//����һ���㲥�������ڲ��࣬�������������Ķ���	
	private final BroadcastReceiver myReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			//arg1�Ǽ���������ͼ
			// TODO Auto-generated method stub
			String action = arg1.getAction();
			//���������豸
			if(BluetoothDevice.ACTION_FOUND.equals(action))
			{
				//��ȡ�����豸����
				BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//�������豸��Ϣ��ӵ�ListView��
				//ǰ17λ��������ַ����17λ����������
				myArrayAdapter.add(device.getAddress()+"\n"+device.getName());
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				//ע�⣺ACTION_DISCOVERY_FINISHED��BluetoothAdapter�����еĲ���
				if(myArrayAdapter.getCount()==0)
				{
					Toast.makeText(getBaseContext(), "û�з��������豸", 0).show();
				}
			}else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
				BTinfo.setText("info:�����豸�Ѹı�");
			}else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				BTinfo.setText("info:�������������豸...");
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
		 
		 //�ֱ�ʵ�ֵ���¼�
		 BTopen.setOnClickListener(new BTopenListener());
		 BTclose.setOnClickListener(new BTcloseListener());
		 BTscan.setOnClickListener(new BTscanListener());
		 BTpaired.setOnClickListener(new BTpairedListener());
		 BTshut.setOnClickListener(new BTshutListener());
		 
		 listOfDevice.setOnItemClickListener(new myOnItemClickListener());
		 
		 //ʹ��ArrayAdapter���listview
		 myArrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1);
		 //ÿ��list���ǰ���simple_list_item_1������ʾ���м��һ���ı�
		 listOfDevice.setAdapter(myArrayAdapter);
		 
		 
		 myAdapter = BluetoothAdapter.getDefaultAdapter();
		 if(myAdapter==null)
		 {
			 //����û������
			 BTinfo.setText("info:δ���������豸");
			 //���������򿪰�ťʧЧ
			 BTopen.setEnabled(false);
		 }else{
			 if(!myAdapter.isEnabled())
			 {
				 BTinfo.setText("info:�����豸������");
			 }else{
				 BTinfo.setText("info:�����豸�Ѵ�");
				 flag_state =true;//��������־λ��Ϊtrue
			 }
		 }
		 
		 //ע��һ����ͼ���������ṩ���㲥������ʹ�ã���������״̬
		 IntentFilter intentFilter = new IntentFilter();
		 intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		 intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		 intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		 intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		 registerReceiver(myReceiver,intentFilter);
		 
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, 1, 1, "����ʾ����ģʽ");
		menu.add(0, 2, 2, "������˷�ģʽ");
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
		
	}
	
	
	//���menu��Ŀ��Ӧ���¼�
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		//���	menu.add(0, 1, 1, "����ʾ����ģʽ");
		//�����ʾ����ģʽ
		//���	menu.add(0, 2, 2, "������˷�ģʽ");
		//�������˷�ģʽ
		
		
		//����oscҳ��
		Intent it = new Intent(this,osc.class);
		//itStr����������ַ
		String itStr = null;
		
		switch(item.getItemId()){
		case 1:
			//����ʾ��������������ַMACStr����osc.class
			if(MACStr != null)
			{
				itStr = MACStr;
				it.putExtra("MACADDR", itStr);
				startActivity(it);
			}
			break;
		case 2:
			//��Я����ַ��ֱ�ӽ���MIC
			it.putExtra("MACADDR", itStr);
			startActivity(it);
			break;
		default:
			Toast.makeText(getBaseContext(), "��������", 0).show();
			break;
		}
		return true;
	}
	
	//���listview��Ŀ������ö�Ӧ�����豸�ĵ�ַ
	String MACStr = null;//��ʼ��������ַ
	private class myOnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			//��ȡ��Ŀ���ı�����
			MACStr = (String) listOfDevice.getItemAtPosition(position);
			//��ȡǰ17λ��������ַ
			MACStr =MACStr.substring(0, 17);
			Toast.makeText(getBaseContext(), "������ַ��"+MACStr, 0).show();
		}
	}
	
	
	//�����Ͽ���ť����¼�
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
					BTinfo.setText("info:�ر�����ģ��ɹ�");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					BTinfo.setText("info:�ر�����ģ��ʧ��");
				}
			}else{
				BTinfo.setText("info:�����Ѿ��ر�");
			}
		}
		
	}
	//ɨ�������豸����¼�
	private class BTscanListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			myAdapter.startDiscovery();
		}
		
	}
	//������Ե���¼�
	private class BTpairedListener implements OnClickListener{
		//��ν����ԣ����ǰ��Ѿ���Ժõ������豸��ʾ��ListView��
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!flag_state)
			{
				BTinfo.setText("info:�����豸δ����");
			}else{
				Set<BluetoothDevice> bondedDevices = myAdapter.getBondedDevices();
				if(bondedDevices.size()>0)
				{
					for(BluetoothDevice device : bondedDevices)
					{
						myArrayAdapter.add(device.getAddress()+"\n"+device.getName());
					}
				}else{
					BTinfo.setText("info:û����Ե������豸");
				}
			}
		}
		
	}
	//�����豸�Ͽ�����¼�
	private class BTshutListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//�Ͽ��豸���Ӽ���յ�ǰ������ַ���ر��������ӵ��߳�
			//���������߳�����osc�д򿪵ģ����ԣ�����Ҫ��������йر�
			//���������ʱ������ֻ�Ǵ��ڴ�״̬�����ǲ��Ǵ�������״̬
			//������Բ���Ӧ����¼�
		}
		
	}
 	
	
	
	public void onDestroy(){
		//ҳ������ʱ��Ҫ���Ĳ���
		super.onDestroy();
		
		if (myAdapter!=null) {
			//ֹͣ����
			myAdapter.cancelDiscovery();
			if (myAdapter.isEnabled()) {
				//�ر�����
				myAdapter.disable();
				flag_state=false;
			}
		}
	}
	
	//�������豸��ť�¼�
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
				BTinfo.setText("info:�����Ѵ�");
			}
			
		}
		
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data){ 
    	super.onActivityResult(requestCode, resultCode, data); 
    	if(requestCode == 0 && resultCode == RESULT_OK){
    		BTinfo.setText("info:�����Ѵ�");
    		flag_state = true;
    	}else{
    		BTinfo.setText("info:������ʧ��");
    	}
	}
	 
	
	
}

//���������߳�
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
		//��ʼ��ConnectThread�߳�֮�󣬻��һ��BluetoothSocketʵ������mmSocket
		//ͨ�����socket���󣬽������ݴ���
		mmSocket=tmp;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		//1.ֹͣ��Ѱ
		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
		try {
			//2.��ʼ����
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
	//�����ṩһ�������Ͽ�socket�ķ����������Ϳ����ȹر�socket�ٹر���������
	public void cancel(){
		try{
			mmSocket.close();
		}catch(IOException e){

		}
	}
	
}

//���ݲɼ��̣߳������Ѿ�����
class ConnectedThread extends Thread{
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	
	Handler mmHandler;
	
	int counter = 0;
	//����ͨ����ÿ���ɼ�700������
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
		//��ʼ������
		mmInStream = tmpIn;
		mmOutStream = tmpOut;
		mmHandler = handler;
	}
	
	
	public void run(){
		while(true)
		{
			try{
				//һ��ȫ����ȡ�����ĸ���
				bytes = mmInStream.read(tempBuffer);
				//ת�浽buffer��
				for(int i=0;i<bytes;i++)
					buffer[counter++]=tempBuffer[i];
				//˫ͨ��ȫ�����Ҳɼ���1400���ֽ�����
				//����˫ͨ��û���Ҳɼ���700���ֽ�����
				//��ʼ��osc���������ɼ���������buffer��ʹ��handler
				if(((counter>=1400)&&(com.example.osc8bit.osc.flag_CHAll)) || ((counter>=700)&&(!com.example.osc8bit.osc.flag_CHAll))){
					mmHandler.obtainMessage(osc.MESSAGE_READ, counter, -1, buffer).sendToTarget();
					//������ϣ���������counter����
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


