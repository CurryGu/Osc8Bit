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
	View allScroll;// ������ʾ����

	// ˥��ϵ����Ĭ��ֵΪ1��˥��2�����ֵx 0.5
	// 0.5=2 1=1 2=0.5 4=0.25
	float Attenuation = 1;

	private TextView osc_info;
	private Button bt_connect;

	static boolean flag_CHAll = false;
	static boolean flag_CH1 = true;// Ĭ��ֻ��ch1
	static boolean flag_CH2 = false;

	boolean flag_messure = false;// Ĭ�ϲ����߹ر�
	Button messure;
	Button but_mes_X;
	Button but_mes_Y;
	TextView text_mes_X;// X�����ֵ
	TextView text_mes_Y;// Y�����ֵ
	static boolean flag_mes_X = false;
	static boolean flag_mes_Y = false;

	Button X_little;
	Button X_bigger;
	Button Y_little;
	Button Y_bigger;

	TextView timer_info;// ʱ���ᣬ����
	TextView Vol_info;// ��ѹ�ᣬ����

	int mySV_X = 700;// ��700
	static int mySV_Y = 500;// ��500
	static int where_XL = 100;
	static int where_XR = 600;
	static int where_YT = 100;
	static int where_YD = 400;// ���������߳�ʼλ��

	public WaveFormView mWaveform = null;
	static int MESSAGE_READ = 3;

	Intent it;
	boolean flag_conn = false;// socket���ӱ�־λ

	// ���Timer��������������ʱ�ظ�����������
	private Timer mmTimer = null;
	private TimerTask mmTimerTask = null;// ����һ����ʱ���̣߳���������

	ConnectThread mmConnectThread = null;// �����̣߳�����һ��socket
	ConnectedThread mmConnectedThread = null;// ��������̣߳�����socket��ʼ��������
	private Handler myHandler = null;// ���������������ӵ�handler

	static int X_level = 1;
	static int Y_level = 3;

	static boolean flag_MIC = false;
	boolean flag_MIC_conn = false;

	private myMICThread MicroPhoneThread = null;
	private Handler MicHandler;
	int MIC_level = 2;

	Button but_save;// ���沨��ͼ

	// ע��һ������״̬�����¼�
	private final BroadcastReceiver mmReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
				osc_info.setText("info:����������");
				flag_conn = true;
				bt_connect.setText("�Ͽ�");
			} else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
				osc_info.setText("info:�����ѶϿ�");
				flag_conn = false;
				bt_connect.setText("����");
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

		messure = (Button) findViewById(R.id.bt_messure);// ��������

		X_little = (Button) findViewById(R.id.x_little);
		X_bigger = (Button) findViewById(R.id.x_bigger);

		Y_little = (Button) findViewById(R.id.y_little);
		Y_bigger = (Button) findViewById(R.id.y_bigger);
		
		timer_info = (TextView) findViewById(R.id.timer_info);// ʱ��������
		Vol_info = (TextView) findViewById(R.id.Vol_info);// ��������

		but_mes_X = (Button) super.findViewById(R.id.but_mes_X);// x���������
		but_mes_Y = (Button) super.findViewById(R.id.but_mes_Y);// y���������

		//������Ϣ�ı���
		text_mes_X = (TextView) super.findViewById(R.id.text_mes_X);
		text_mes_X.setText("no mes");// δ����������Ĭ������Ϊno mes
		text_mes_Y = (TextView) super.findViewById(R.id.text_mes_Y);
		text_mes_Y.setText("no mes");// δ����������Ĭ������Ϊno mes

		//���水ť
		but_save = (Button) super.findViewById(R.id.but_save);

		mWaveform = (WaveFormView) findViewById(R.id.waveformview);
		allScroll = (View) mWaveform;
		allScroll.setKeepScreenOn(true);// ʹ��Ļ������

		bt_connect.setOnClickListener(new connectListener());
		messure.setOnClickListener(new messureListener());
		X_bigger.setOnClickListener(new X_biggerListener());
		X_little.setOnClickListener(new X_littleListener());
		Y_bigger.setOnClickListener(new Y_biggerListener());
		Y_little.setOnClickListener(new Y_littleListener());
		but_mes_X.setOnClickListener(new but_mes_XListener());
		but_mes_Y.setOnClickListener(new but_mes_YListener());
		but_save.setOnClickListener(new but_saveListener());

		// allScroll����Ϊ�ɵ������Ӧ�����¼�
		allScroll.setOnTouchListener(new OnTouchListenerImpl());

		// �ĸ�˥����ť��һ������ͼ��ť
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
		// ���ˣ���ʼ�����

		// ��main.java�У���osc�����ʱ����ʹ��startActivity(it);
		// ����ͼitЯ��������������������ַMACADDR��һ������osc
		// ��main������startActivity��it�����ݹ���������ַ�������ַΪ�գ���ֱ������MIC
		it = getIntent();
		String itStr = it.getStringExtra("MACADDR");

		if (itStr == null) {
			flag_MIC = true;
			flag_CH1 = false;
			flag_CH2 = false;
			flag_CHAll = false;

			//ע��һ����ͼ������
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

			// ���մ�MIC�̴߳��ݽ���������
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
					//��ȡWaveFormView����
					mWaveform.set_data(tempInt, tempInt);
				}
			};
		} else {
			//������ַ��ֵ
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
						// ˫ͨ��
						for (int i = 0; i < 700; i++) {
							ch_data[i] = UByte(temp[i * 2]);
							ch_data1[i] = UByte(temp[i * 2 + 1]);
						}
						mWaveform.set_data(ch_data, ch_data1);
					} else {
						//ֻ��һ��ͨ��
						for (int i = 0; i < 700; i++) {
							ch_data[i] = UByte(temp[i]);
						}
						//����ͨ��������Ϊһ��
						mWaveform.set_data(ch_data, ch_data);
					}
					// --------------------------------------------------------------------------------
				}
			};
		}

	}

	// ����˥���ĸ���ť
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
				Toast.makeText(getBaseContext(), "δ���ӻ��ߵ������",
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
				Toast.makeText(getBaseContext(), "δ���ӻ��ߵ������",
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
				Toast.makeText(getBaseContext(), "δ���ӻ��ߵ������",
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
				Toast.makeText(getBaseContext(), "δ���ӻ��ߵ������",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// ���α��水ť-------------------------------------------------�ù���δʵ��
	private class but_saveListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// ��ò���ͼλͼ
			Bitmap mybm = mWaveform.savePictyre(mWaveform.getHolder()
					.lockCanvas());
			String path = "/storage/sdcard1/osc8Bit/";// ����·��
			File myfile = new File(path + "osc8Bit.bmp");
			new File(path).mkdirs();
		}

	}

	// ------------------------------------------------------------
	// ����ķ��������ã�������ν������ܣ�����messure���������ߵ�λ��
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
				// 1.���ò����ߵ�λ��
				X = (int) event.getX();
				Y = (int) event.getY();
				if (flag_mes_X && (Y > (mySV_Y - 50))) {
					// X�򿪣��ҵ����Ļ�·�50�������
					where_XL = X;
					ChangeText();// ÿ����һ��X��Y��λ�ã���Ҫ���ı����и���һ��
				}
				if (flag_mes_X && (Y < 50)) {
					// X�򿪣��ҵ����Ļ�Ϸ�50�������
					where_XR = X;
					ChangeText();
				}
				if (flag_mes_Y && (X > (mySV_X - 50))) {
					// Y�򿪣��ҵ����Ļ�Ҳ�50�������
					where_YD = Y;
					ChangeText();
				}
				if (flag_mes_Y && (X < 50)) {
					// Y�򿪣��ҵ����Ļ���50�������
					where_YT = Y;
					ChangeText();
				}

				// 2.���ò�����ʾ�߶�
				if (!flag_mes_X && !flag_mes_Y) {
					// X��Y��û�д򿪣������Ļ���50���ڣ�����ch1������ʾ�߶�
					if ((X < 50) && ((Y > CH_MIN) || (Y < CH_MAX))) {
						ch1_pos = Y;
					}
					// X��Y��û�д򿪣������Ļ�Ҳ�50���ڣ�����ch2������ʾ�߶�
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

	// ��ͨ������
	public void setCHMode(View view) {
		boolean checked = ((RadioButton) view).isChecked();
		if (!flag_CHAll) {
			switch (view.getId()) {
			case R.id.rb_CH1:
				if (checked) {
					flag_CH1 = true;
					flag_CH2 = false;

					if (mmConnectedThread != null) {
						String tempStr = "-C1\r\n";// ֻ��ȡCH1����
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
						String tempStr = "-C2\r\n";// ֻ��ȡCH2����
						byte[] tempByte = tempStr.getBytes();
						mmConnectedThread.write(tempByte);
					}
				}
				break;
			}
		} else {
			Toast.makeText(getBaseContext(), "˫ͨ���Ѵ�", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// ˫ͨ������
	public void setChannelAll(View view) {
		String tempStr = null;
		if (((CheckBox) view).isChecked()) {
			flag_CHAll = true;

			if (mmConnectedThread != null) {
				tempStr = "-A1\r\n";// ָ���ȡ����ͨ������
			}
		} else {
			flag_CHAll = false;

			if (mmConnectedThread != null) {
				tempStr = "-A0\r\n";// ָ�ֹͣ��ȡ����ͨ������
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
			// �ر���˷��߳�
			if (MicroPhoneThread != null) {
				MicroPhoneThread.pause();
			}
		} else {
			// �رռ�ʱ�߳�
			if (mmTimerTask != null) {
				mmTimerTask.cancel();
			}
			if (mmTimer != null) {
				mmTimer.cancel();
			}
			// �ر����ݴ����߳�
			if (mmConnectedThread != null) {
				mmConnectedThread.cancel();
			}
			// �ر������߳�
			if (mmConnectThread != null) {
				mmConnectThread.cancel();
			}
		}
		this.unregisterReceiver(mmReceiver);
		super.onDestroy();
	}

	// ���� ��ť������¼�
	// �����̣߳���������
	// �ر��̣߳�ֹͣ��������
	private class connectListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (flag_MIC) {
				// ���MIC��־λδtrue�������MIC
				if (flag_MIC_conn) {
					try {
						// ֹͣMIC�߳�
						MicroPhoneThread.pause();
						flag_MIC_conn = false;
						bt_connect.setText("����");
						
						//�Ͽ�����֮���ò��β�����ʾ
						//how��
						mWaveform.ch_disappear();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getBaseContext(), "MICδ�Ͽ�", 1).show();
					}
				} else {
					try {
						// ����MIC�߳�
						MicroPhoneThread = new myMICThread();
						MicroPhoneThread.start();
						flag_MIC_conn = true;
						mWaveform.ch_appear();
						bt_connect.setText("�Ͽ�");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(getBaseContext(), "MICδ����", 1).show();
					}
				}
			} else {
				// ���MIC��־λδfalse����ִ���������Ӳ���
				if (flag_conn) {
					mmConnectThread.cancel();
				} else {
					// �Ͽ�������£�����
					// it��main������Ǵ��ݽ�������ͼ����֮���ݽ����Ļ���������ַ
					String MACADDR = it.getStringExtra("MACADDR");
					// ����MACADDR������ַ����ȡ��������
					BluetoothDevice remoteDevice = BluetoothAdapter
							.getDefaultAdapter().getRemoteDevice(MACADDR);
					// ��ʼ������������
					mmConnectThread = new ConnectThread(remoteDevice);
					mmConnectThread.start();
					// ��ʼ������������
					mmConnectedThread = new ConnectedThread(
							mmConnectThread.mmSocket, myHandler);
					mmConnectedThread.start();
					// ���濪ʼд�������ӵ������߳�,Main.java��
				}
			}
		}

	}

	// ������ť����¼�
	private class messureListener implements OnClickListener {
 		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (flag_MIC) {
				// ��MIC�򿪵�ʱ�򣬲�����ť�ı�Ϊx1��x2
				// x1����2���Ŵ�x2����4���Ŵ�
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
					messure.setText("����");

					if (mmTimer != null) {
						mmTimer.cancel();
					}
				} else {
					// ��ʼ����
					flag_messure = true;
					messure.setText("ֹͣ");
					// ���ʱ��mmConnectThread�Ѿ����Ӻ���
					if (mmConnectedThread != null) {
						// ���������ź�
						String tempStr = "-D700\r\n";
						// �����ֽ�������ʽ����
						byte[] tempByte = tempStr.getBytes();
						// ���ͣ���socket��д������mmOutStream.write(bytes);
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
						// ����mmTimerTask�����״�ִ�м�ࣺ300ms��ÿ�����300ms��ִ��һ��
						mmTimer.schedule(mmTimerTask, 300, 300);
					}

				}
			}

		}

	}

	// X_biggerListener��ť����¼�
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

	// X_littleListener��ť����¼�
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

	// Y_littleListener����¼�
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

	// Y_biggerListener����¼�
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

	// but_mes_XListener����¼�
	private class but_mes_XListener implements OnClickListener {
		public void onClick(View view) {
			if (flag_mes_X) {
				flag_mes_X = false;
				but_mes_X.setText("X-on");
				// ���ı���ʾ����
				text_mes_X.setText(String.valueOf(0));
			} else {
				flag_mes_X = true;
				but_mes_X.setText("X-off");
			}
		}
	}

	// but_mes_YListener����¼�
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
			// һ�δ�MIC�л�ȡ16λ����
			bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
					AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			// ¼����ʵ��������
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
				// ��ar�ж�ȡ���ݵ�buffer�У��ӵ�0λ����bsλ
				int bytes = ar.read(buffer, 0, bs);
				// ���ɼ��������ݷ��͸����߳�
				MicHandler.obtainMessage(osc.MESSAGE_READ, bytes, -1, buffer)
						.sendToTarget();
			}
			// ������������ź�֮�󣬹ر���Դ
			ar.stop();
			ar.release();
		}

		// �����ṩһ��ֹͣ�̵߳ķ���
		public void pause() {
			isRun = false;
		}

		// �����ṩһ�������ķ���
		public void start() {
			if (!isRun) {
				super.start();
			}
		}
	}

	public void ChangeText() {
		// 1.�ı�ʱ��������
		switch (X_level) {
		// ����ʱ����ʵ�ʴ���ʱ��
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
		// 2.�ı�ʱ�������ֵ
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

		// Y_level�������1��2��3��4��5
		// �ֱ��Ӧ���ֵ
		float Y_Level1 = (float) 161.8;
		float Y_Level2 = (float) 323.5;
		float Y_Level3 = (float) 647.0;
		float Y_Level4 = (float) 1294.0;
		float Y_Level5 = (float) 2588.0;

		// 3.����˥��ϵ������Y���ֵVol_info
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

		// 4.����˥��ϵ����Y_Level1����Y���ֵVol_info
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
