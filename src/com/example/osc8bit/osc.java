package com.example.osc8bit;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.example.osc8bit.WaveFormView;
import com.example.osc8bit.ConnectThread;
import com.example.osc8bit.ConnectedThread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class osc extends Activity {
	View allScroll;// 波形显示界面

	// 衰减系数，默认值为1，衰减2，则幅值x 0.5
	// 0.5=2 1=1 2=0.5 4=0.25
	float Attenuation = 1;

	private TextView osc_info;
	private Button bt_connect;

	static boolean flag_CHAll = false;
	static boolean flag_CH1 = true;// 默认只打开ch1
	static boolean flag_CH2 = false;

	boolean flag_messure = false;// 默认测量线关闭
	Button messure;
	Button but_mes_X;
	Button but_mes_Y;
	TextView text_mes_X;// X轴向差值
	TextView text_mes_Y;// Y轴向差值
	static boolean flag_mes_X = false;
	static boolean flag_mes_Y = false;

	Button X_little;
	Button X_bigger;
	Button Y_little;
	Button Y_bigger;

	TextView timer_info;// 时间轴，横轴
	TextView Vol_info;// 电压轴，纵轴

	int mySV_X = 700;// 宽700
	static int mySV_Y = 500;// 宽500
	static int where_XL = 100;
	static int where_XR = 600;
	static int where_YT = 100;
	static int where_YD = 400;// 四条测量线初始位置

	public WaveFormView mWaveform = null;
	static int MESSAGE_READ = 3;

	Intent it;
	boolean flag_conn = false;// socket连接标志位

	// 这个Timer的作用是用来定时重复发送请求码
	private Timer mmTimer = null;
	private TimerTask mmTimerTask = null;// 这是一个计时的线程，包含任务

	ConnectThread mmConnectThread = null;// 连接线程，创建一个socket
	ConnectedThread mmConnectedThread = null;// 连接完成线程，利用socket开始传递数据
	private Handler myHandler = null;// 用来处理蓝牙连接的handler

	static int X_level = 1;
	static int Y_level = 3;

	static boolean flag_MIC = false;
	boolean flag_MIC_conn = false;

	private myMICThread MicroPhoneThread = null;
	private Handler MicHandler;
	int MIC_level = 2;

	Button but_save;// 保存波形图

	// 注册一个蓝牙状态监听事件
	private final BroadcastReceiver mmReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				osc_info.setText("info:蓝牙已连接");
				flag_conn = true;
				bt_connect.setText("断开");
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				osc_info.setText("info:蓝牙已断开");
				flag_conn = false;
				bt_connect.setText("连接");
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.osc);

		osc_info = (TextView) findViewById(R.id.osc_info);

		bt_connect = (Button) findViewById(R.id.bt_connect);

		messure = (Button) findViewById(R.id.bt_messure);// 测量开关

		X_little = (Button) findViewById(R.id.x_little);
		X_bigger = (Button) findViewById(R.id.x_bigger);

		Y_little = (Button) findViewById(R.id.y_little);
		Y_bigger = (Button) findViewById(R.id.y_bigger);
		
		timer_info = (TextView) findViewById(R.id.timer_info);// 时间轴坐标
		Vol_info = (TextView) findViewById(R.id.Vol_info);// 纵轴坐标

		but_mes_X = (Button) super.findViewById(R.id.but_mes_X);// x轴测量开启
		but_mes_Y = (Button) super.findViewById(R.id.but_mes_Y);// y轴测量开启

		//测量信息文本栏
		text_mes_X = (TextView) super.findViewById(R.id.text_mes_X);
		text_mes_X.setText("no mes");// 未开启测量，默认设置为no mes
		text_mes_Y = (TextView) super.findViewById(R.id.text_mes_Y);
		text_mes_Y.setText("no mes");// 未开启测量，默认设置为no mes

		//保存按钮
		but_save = (Button) super.findViewById(R.id.but_save);

		mWaveform = (WaveFormView) findViewById(R.id.waveformview);
		allScroll = (View) mWaveform;
		allScroll.setKeepScreenOn(true);// 使屏幕不锁屏

		bt_connect.setOnClickListener(new connectListener());
		messure.setOnClickListener(new messureListener());
		X_bigger.setOnClickListener(new X_biggerListener());
		X_little.setOnClickListener(new X_littleListener());
		Y_bigger.setOnClickListener(new Y_biggerListener());
		Y_little.setOnClickListener(new Y_littleListener());
		but_mes_X.setOnClickListener(new but_mes_XListener());
		but_mes_Y.setOnClickListener(new but_mes_YListener());
		but_save.setOnClickListener(new but_saveListener());

		// allScroll设置为可点击，响应其点击事件
		allScroll.setOnTouchListener(new OnTouchListenerImpl());

		// 四个衰减按钮和一个保存图像按钮
		Button decrease_0_5 = (Button) findViewById(R.id.decrease_0_5);
		Button decrease_1 = (Button) findViewById(R.id.decrease_1);
		Button decrease_2 = (Button) findViewById(R.id.decrease_2);
		Button decrease_4 = (Button) findViewById(R.id.decrease_4);

		Button but_save = (Button) findViewById(R.id.but_save);

		decrease_0_5.setOnClickListener(new decrease_0_5Listener());
		decrease_1.setOnClickListener(new decrease_1Listener());
		decrease_2.setOnClickListener(new decrease_2Listener());
		decrease_4.setOnClickListener(new decrease_4Listener());

		but_save.setOnClickListener(new but_saveListener());
		// 至此，初始化完毕

		// 在main.java中，打开osc界面的时候是使用startActivity(it);
		// 将意图it携带参数，参数是蓝牙地址MACADDR，一并传给osc
		// 从main界面用startActivity（it）传递过来蓝牙地址，如果地址为空，则直接连接MIC
		it = getIntent();
		String itStr = it.getStringExtra("MACADDR");

		if (itStr == null) {
			flag_MIC = true;
			flag_CH1 = false;
			flag_CH2 = false;
			flag_CHAll = false;

			//注册一个意图过滤器
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			registerReceiver(mmReceiver, intentFilter);

			messure.setText("x1");
			X_bigger.setEnabled(false);
			X_little.setEnabled(false);
			Y_bigger.setEnabled(false);
			Y_little.setEnabled(false);
			but_mes_X.setEnabled(false);
			but_mes_Y.setEnabled(false);

			RadioButton CH1 = (RadioButton) super.findViewById(R.id.rb_CH1);
			RadioButton CH2 = (RadioButton) super.findViewById(R.id.rb_CH2);
			CheckBox CH_ALL = (CheckBox) super.findViewById(R.id.cb_CHAll);
			CH1.setEnabled(false);
			CH2.setEnabled(false);
			CH_ALL.setEnabled(false);

			// 接收从MIC线程传递进来的数据
			MicHandler = new Handler() {
				int[] tempInt = new int[1000];
				int counter = 0;

				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					// int many = msg.arg1;
					byte[] temp = (byte[]) msg.obj;

					for (int i = 0; i < 700; i++) {
						tempInt[i] = temp[i * MIC_level + 1];
					}
					//获取WaveFormView对象，
					mWaveform.set_data(tempInt, tempInt);
				}
			};
		} else {
			//蓝牙地址有值
			flag_MIC = false;

			IntentFilter filter = new IntentFilter();
			filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			registerReceiver(mmReceiver, filter);

			myHandler = new Handler() {
				private int UByte(byte b) {
					if (b < 0)
						return (int) ((b & 0x7F) + 128);
					else
						return (int) b;
				}

				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					// --------------------------------------------------------------------------------
					byte[] temp = (byte[]) msg.obj;
					int[] ch_data = new int[700];
					int[] ch_data1 = new int[700];
					if (flag_CHAll) {
						// 双通道
						for (int i = 0; i < 700; i++) {
							ch_data[i] = UByte(temp[i * 2]);
							ch_data1[i] = UByte(temp[i * 2 + 1]);
						}
						mWaveform.set_data(ch_data, ch_data1);
					} else {
						//只打开一个通道
						for (int i = 0; i < 700; i++) {
							ch_data[i] = UByte(temp[i]);
						}
						//两个通道数据设为一样
						mWaveform.set_data(ch_data, ch_data);
					}
					// --------------------------------------------------------------------------------
				}
			};
		}

	}

	// 波形衰减四个按钮
	private class decrease_0_5Listener implements OnClickListener {
		String tempStr = null;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tempStr = "-B0\r\n";
			osc_info.setText("B=0.5");
			Attenuation = 2;

			ChangeText();
			if ((tempStr != null) && (mmConnectedThread != null)) {
				byte[] tempByte = tempStr.getBytes();
				mmConnectedThread.write(tempByte);
			} else {
				Toast.makeText(getBaseContext(), "未连接或者点击错误",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class decrease_1Listener implements OnClickListener {
		String tempStr = null;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tempStr = "-B1\r\n";
			osc_info.setText("B=1");
			Attenuation = 1;

			ChangeText();
			if ((tempStr != null) && (mmConnectedThread != null)) {
				byte[] tempByte = tempStr.getBytes();
				mmConnectedThread.write(tempByte);
			} else {
				Toast.makeText(getBaseContext(), "未连接或者点击错误",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class decrease_2Listener implements OnClickListener {
		String tempStr = null;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tempStr = "-B2\r\n";
			osc_info.setText("B=2");
			Attenuation = (float) 0.5;

			ChangeText();

			if ((tempStr != null) && (mmConnectedThread != null)) {
				byte[] tempByte = tempStr.getBytes();
				mmConnectedThread.write(tempByte);
			} else {
				Toast.makeText(getBaseContext(), "未连接或者点击错误",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class decrease_4Listener implements OnClickListener {
		String tempStr = null;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			tempStr = "-B3\r\n";
			osc_info.setText("B=4");
			Attenuation = (float) 0.25;

			ChangeText();

			if ((tempStr != null) && (mmConnectedThread != null)) {
				byte[] tempByte = tempStr.getBytes();
				mmConnectedThread.write(tempByte);
			} else {
				Toast.makeText(getBaseContext(), "未连接或者点击错误",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 波形保存按钮-------------------------------------------------该功能未实现
	private class but_saveListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// 获得波形图位图
			Bitmap mybm = mWaveform.savePictyre(mWaveform.getHolder()
					.lockCanvas());
			String path = "/storage/sdcard1/osc8Bit/";// 保存路径
			File myfile = new File(path + "osc8Bit.bmp");
			new File(path).mkdirs();
		}

	}

	// ------------------------------------------------------------
	// 下面的方法的作用：点击波形界面四周，设置messure四条测量线的位置
	public static int ch1_pos = mySV_Y - 2;
	public static int ch2_pos = mySV_Y - 2;
	int CH_MIN = 1;
	int CH_MAX = 499;

	private class OnTouchListenerImpl implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {

			int X = 0, Y = 0;
			int eventaction = event.getAction();

			switch (eventaction) {
			case MotionEvent.ACTION_DOWN:
				// 1.设置测量线的位置
				X = (int) event.getX();
				Y = (int) event.getY();
				if (flag_mes_X && (Y > (mySV_Y - 50))) {
					// X打开，且点击屏幕下方50宽度以内
					where_XL = X;
					ChangeText();// 每更新一次X，Y的位置，都要在文本框中更新一下
				}
				if (flag_mes_X && (Y < 50)) {
					// X打开，且点击屏幕上方50宽度以内
					where_XR = X;
					ChangeText();
				}
				if (flag_mes_Y && (X > (mySV_X - 50))) {
					// Y打开，且点击屏幕右侧50宽度以内
					where_YD = Y;
					ChangeText();
				}
				if (flag_mes_Y && (X < 50)) {
					// Y打开，且点击屏幕左侧50宽度以内
					where_YT = Y;
					ChangeText();
				}

				// 2.设置波形显示高度
				if (!flag_mes_X && !flag_mes_Y) {
					// X，Y都没有打开，点击屏幕左侧50以内，设置ch1波形显示高度
					if ((X < 50) && ((Y > CH_MIN) || (Y < CH_MAX))) {
						ch1_pos = Y;
					}
					// X，Y都没有打开，点击屏幕右侧50以内，设置ch2波形显示高度
					if ((X > 650) && ((Y > CH_MIN) || (Y < CH_MAX))) {
						ch2_pos = Y;
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				break;
			}

			return false;
		}
	}

	// 单通道开启
	public void setCHMode(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		if (!flag_CHAll) {
			switch (view.getId()) {
			case R.id.rb_CH1:
				if (checked) {
					flag_CH1 = true;
					flag_CH2 = false;

					if (mmConnectedThread != null) {
						String tempStr = "-C1\r\n";// 只获取CH1数据
						byte[] tempByte = tempStr.getBytes();
						mmConnectedThread.write(tempByte);
					}
				}
				break;
			case R.id.rb_CH2:
				if (checked) {
					flag_CH2 = true;
					flag_CH1 = false;

					if (mmConnectedThread != null) {
						String tempStr = "-C2\r\n";// 只获取CH2数据
						byte[] tempByte = tempStr.getBytes();
						mmConnectedThread.write(tempByte);
					}
				}
				break;
			}
		} else {
			Toast.makeText(getBaseContext(), "双通道已打开", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// 双通道开启
	public void setChannelAll(View view) {
		String tempStr = null;
		if (((CheckBox) view).isChecked()) {
			flag_CHAll = true;

			if (mmConnectedThread != null) {
				tempStr = "-A1\r\n";// 指令：获取两个通道数据
			}
		} else {
			flag_CHAll = false;

			if (mmConnectedThread != null) {
				tempStr = "-A0\r\n";// 指令：停止获取两个通道数据
			}
		}
		if (tempStr != null) {
			byte[] tempByte = tempStr.getBytes();
			mmConnectedThread.write(tempByte);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		String itStr = it.getStringExtra("MACADDR");
		if (itStr == null) {
			// 关闭麦克风线程
			if (MicroPhoneThread != null) {
				MicroPhoneThread.pause();
			}
		} else {
			// 关闭计时线程
			if (mmTimerTask != null) {
				mmTimerTask.cancel();
			}
			if (mmTimer != null) {
				mmTimer.cancel();
			}
			// 关闭数据传递线程
			if (mmConnectedThread != null) {
				mmConnectedThread.cancel();
			}
			// 关闭蓝牙线程
			if (mmConnectThread != null) {
				mmConnectThread.cancel();
			}
		}
		this.unregisterReceiver(mmReceiver);
		super.onDestroy();
	}

	// 连接 按钮，点击事件
	// 开启线程，传递数据
	// 关闭线程，停止传递数据
	private class connectListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (flag_MIC) {
				// 如果MIC标志位未true，则操作MIC
				if (flag_MIC_conn) {
					try {
						// 停止MIC线程
						MicroPhoneThread.pause();
						flag_MIC_conn = false;
						bt_connect.setText("连接");
						
						//断开连接之后，让波形不再显示
						//how？
						mWaveform.ch_disappear();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getBaseContext(), "MIC未断开", 1).show();
					}
				} else {
					try {
						// 开启MIC线程
						MicroPhoneThread = new myMICThread();
						MicroPhoneThread.start();
						flag_MIC_conn = true;
						mWaveform.ch_appear();
						bt_connect.setText("断开");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getBaseContext(), "MIC未连接", 1).show();
					}
				}
			} else {
				// 如果MIC标志位未false，则执行蓝牙连接操作
				if (flag_conn) {
					mmConnectThread.cancel();
				} else {
					// 断开的情况下，连接
					// it是main界面打开是传递进来的意图，随之传递进来的还有蓝牙地址
					String MACADDR = it.getStringExtra("MACADDR");
					// 根据MACADDR蓝牙地址，获取蓝牙对象
					BluetoothDevice remoteDevice = BluetoothAdapter
							.getDefaultAdapter().getRemoteDevice(MACADDR);
					// 开始建立蓝牙连接
					mmConnectThread = new ConnectThread(remoteDevice);
					mmConnectThread.start();
					// 开始建立数据连接
					mmConnectedThread = new ConnectedThread(
							mmConnectThread.mmSocket, myHandler);
					mmConnectedThread.start();
					// 下面开始写蓝牙连接的两个线程,Main.java中
				}
			}
		}

	}

	// 测量按钮点击事件
	private class messureListener implements OnClickListener {
 		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (flag_MIC) {
				// 当MIC打开的时候，测量按钮文本为x1和x2
				// x1代表2倍放大，x2代表4倍放大
				if (MIC_level == 2) {
					MIC_level = 4;
					messure.setText("x2");
				} else {
					MIC_level = 2;
					messure.setText("x1");
				}
			} else {
				if (flag_messure) {
					flag_messure = false;
					messure.setText("测量");

					if (mmTimer != null) {
						mmTimer.cancel();
					}
				} else {
					// 开始测量
					flag_messure = true;
					messure.setText("停止");
					// 这个时候mmConnectThread已经连接好了
					if (mmConnectedThread != null) {
						// 发送请求信号
						String tempStr = "-D700\r\n";
						// 按照字节数组形式发送
						byte[] tempByte = tempStr.getBytes();
						// 发送，向socket中写，调用mmOutStream.write(bytes);
						mmConnectedThread.write(tempByte);

						mmTimer = new Timer();
						mmTimerTask = new TimerTask() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								String tempStr = "-D700\r\n";
								byte[] tempByte = tempStr.getBytes();
								mmConnectedThread.write(tempByte);
							}
						};
						// Schedule a task for repeated fixed-delay execution
						// after a specific delay.
						// 任务：mmTimerTask；距首次执行间距：300ms；每间隔：300ms再执行一次
						mmTimer.schedule(mmTimerTask, 300, 300);
					}

				}
			}

		}

	}

	// X_biggerListener按钮点击事件
	private class X_biggerListener implements OnClickListener {
		public void onClick(View view) {
			if (mmConnectedThread != null) {
				String tempStr = null;
				switch (X_level) {
				case 1:
					X_level++;
					tempStr = "-S2\r\n";
					break;
				case 2:
					X_level++;
					tempStr = "-S3\r\n";
					break;
				case 3:
					X_level++;
					tempStr = "-S4\r\n";
					break;
				case 4:
					X_level++;
					tempStr = "-S5\r\n";
					break;
				case 5:
					X_level++;
					tempStr = "-S6\r\n";
					break;
				case 6:
					X_level++;
					tempStr = "-S7\r\n";
					break;
				case 7:
					X_level++;
					tempStr = "-S8\r\n";
					break;
				case 8:
					X_level++;
					tempStr = "-S9\r\n";
					break;
				case 9:
					break;
				}
				if (tempStr != null) {
					byte[] tempByte = tempStr.getBytes();
					mmConnectedThread.write(tempByte);
				}
			}
			ChangeText();
		}
	}

	// X_littleListener按钮点击事件
	private class X_littleListener implements OnClickListener {
		public void onClick(View view) {
			if (mmConnectedThread != null) {
				String tempStr = null;
				switch (X_level) {
				case 1:
					break;
				case 2:
					X_level--;
					tempStr = "-S1\r\n";
					break;
				case 3:
					X_level--;
					tempStr = "-S2\r\n";
					break;
				case 4:
					X_level--;
					tempStr = "-S3\r\n";
					break;
				case 5:
					X_level--;
					tempStr = "-S4\r\n";
					break;
				case 6:
					X_level--;
					tempStr = "-S5\r\n";
					break;
				case 7:
					X_level--;
					tempStr = "-S6\r\n";
					break;
				case 8:
					X_level--;
					tempStr = "-S7\r\n";
					break;
				case 9:
					X_level--;
					tempStr = "-S8\r\n";
					break;
				}
				if (tempStr != null) {
					byte[] tempByte = tempStr.getBytes();
					mmConnectedThread.write(tempByte);
				}
			}
			ChangeText();
		}
	}

	// Y_littleListener点击事件
	private class Y_littleListener implements OnClickListener {
		public void onClick(View view) {
			switch (Y_level) {
			case 1:
				break;
			case 2:
				Y_level--;
				break;
			case 3:
				Y_level--;
				break;
			case 4:
				Y_level--;
				break;
			case 5:
				Y_level--;
				break;
			}
			ChangeText();
		}
	}

	// Y_biggerListener点击事件
	private class Y_biggerListener implements OnClickListener {
		public void onClick(View view) {
			switch (Y_level) {
			case 1:
				Y_level++;
				break;
			case 2:
				Y_level++;
				break;
			case 3:
				Y_level++;
				break;
			case 4:
				Y_level++;
				break;
			case 5:
				break;
			}
			ChangeText();
		}
	}

	// but_mes_XListener点击事件
	private class but_mes_XListener implements OnClickListener {
		public void onClick(View view) {
			if (flag_mes_X) {
				flag_mes_X = false;
				but_mes_X.setText("X-on");
				// 将文本显示清零
				text_mes_X.setText(String.valueOf(0));
			} else {
				flag_mes_X = true;
				but_mes_X.setText("X-off");
			}
		}
	}

	// but_mes_YListener点击事件
	private class but_mes_YListener implements OnClickListener {
		public void onClick(View view) {
			if (flag_mes_Y) {
				flag_mes_Y = false;
				but_mes_Y.setText("Y-on");
				text_mes_Y.setText(String.valueOf(0));
			} else {
				flag_mes_Y = true;
				but_mes_Y.setText("Y-off");
			}
		}
	}

	public class myMICThread extends Thread {
		private AudioRecord ar;
		private int bs;
		private int SAMPLE_RATE_IN_HZ = 44100;
		private boolean isRun = false;

		public myMICThread() {
			super();
			// 一次从MIC中获取16位数据
			bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			// 录音机实例化对象
			ar = new AudioRecord(MediaRecorder.AudioSource.MIC,
					SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, bs);
		}

		public void run() {
			super.run();
			ar.startRecording();
			byte[] buffer = new byte[bs];
			isRun = true;
			while (isRun) {
				// 从ar中读取数据到buffer中，从第0位读到bs位
				int bytes = ar.read(buffer, 0, bs);
				// 将采集到的数据发送给主线程
				MicHandler.obtainMessage(osc.MESSAGE_READ, bytes, -1, buffer)
						.sendToTarget();
			}
			// 发送完毕数据信号之后，关闭资源
			ar.stop();
			ar.release();
		}

		// 对外提供一个停止线程的方法
		public void pause() {
			isRun = false;
		}

		// 对外提供一个开启的方法
		public void start() {
			if (!isRun) {
				super.start();
			}
		}
	}

	public void ChangeText() {
		// 1.改变时间轴量度
		switch (X_level) {
		// 设置时间轴实际代表时间
		case 1:
			timer_info.setText("100000");
			break;// 100s
		case 2:
			timer_info.setText("50000");
			break;// 50s
		case 3:
			timer_info.setText("25000");
			break;// 25s
		case 4:
			timer_info.setText("20000");
			break;// 20s
		case 5:
			timer_info.setText("10000");
			break;// 10s
		case 6:
			timer_info.setText("5000");
			break;// 5s
		case 7:
			timer_info.setText("2500");
			break;// 2.5s
		case 8:
			timer_info.setText("2000");
			break;// 2s
		case 9:
			timer_info.setText("1000");
			break;// 1s
		}
		// 2.改变时间轴测量值
		if (flag_mes_X) {
			int Value = where_XR - where_XL;
			switch (X_level) {

			case 1:
				Value = (int) ((float) Value * 100000 / 100);
				break;
			case 2:
				Value = (int) ((float) Value * 50000 / 100);
				break;
			case 3:
				Value = (int) ((float) Value * 25000 / 100);
				break;
			case 4:
				Value = (int) ((float) Value * 20000 / 100);
				break;
			case 5:
				Value = (int) ((float) Value * 10000 / 100);
				break;
			case 6:
				Value = (int) ((float) Value * 5000 / 100);
				break;
			case 7:
				Value = (int) ((float) Value * 2500 / 100);
				break;
			case 8:
				Value = (int) ((float) Value * 2000 / 100);
				break;
			case 9:
				Value = (int) ((float) Value * 1000 / 100);
				break;
			}
			text_mes_X.setText(String.valueOf(Value));
		}

		// Y_level五个级别：1，2，3，4，5
		// 分别对应五个值
		float Y_Level1 = (float) 161.8;
		float Y_Level2 = (float) 323.5;
		float Y_Level3 = (float) 647.0;
		float Y_Level4 = (float) 1294.0;
		float Y_Level5 = (float) 2588.0;

		// 3.根据衰减系数设置Y轴差值Vol_info
		switch (Y_level) {
		case 1:
			Vol_info.setText(String.valueOf(Y_Level1 * Attenuation));
			break;
		case 2:
			Vol_info.setText(String.valueOf(Y_Level2 * Attenuation));
			break;
		case 3:
			Vol_info.setText(String.valueOf(Y_Level3 * Attenuation));
			break;
		case 4:
			Vol_info.setText(String.valueOf(Y_Level4 * Attenuation));
			break;
		case 5:
			Vol_info.setText(String.valueOf(Y_Level5 * Attenuation));
			break;
		}

		// 4.根据衰减系数和Y_Level1设置Y轴差值Vol_info
		if (flag_mes_Y) {
			int Value = where_YD - where_YT;
			switch (Y_level) {
			case 1:
				Value = (int) ((float) Value * Y_Level1 * Attenuation / 50);
				break;
			case 2:
				Value = (int) ((float) Value * Y_Level2 * Attenuation / 50);
				break;
			case 3:
				Value = (int) ((float) Value * Y_Level3 * Attenuation / 50);
				break;
			case 4:
				Value = (int) ((float) Value * Y_Level4 * Attenuation / 50);
				break;
			case 5:
				Value = (int) ((float) Value * Y_Level5 * Attenuation / 50);
				break;
			}
			text_mes_Y.setText(String.valueOf(Value));
		}

	}
}
