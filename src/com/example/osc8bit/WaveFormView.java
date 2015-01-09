package com.example.osc8bit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

//�Զ���һ��View
public class WaveFormView extends SurfaceView implements SurfaceHolder.Callback {

	private final int width = 700;// ��700
	private final int height = 500;// ��500
	private static int[] ch1_data = new int[1000];// ch1�ɼ���������
	private static int[] ch2_data = new int[1000];// ch2�ɼ���������

	int temp_pos1 = 0;// ch1���γ�ʼλ��
	int temp_pos2 = 0;// ch2���γ�ʼλ��

	// ���廭��
	private Paint ch1_paint = new Paint();// ��ch1����
	private Paint ch2_paint = new Paint();// ��ch2����
	private Paint back_paint = new Paint();// ������
	private Paint cross_paint = new Paint();// ��ʮ��
	private Paint arrow_paint = new Paint();// ����ͷ
	private Paint messure_paint = new Paint();// ����������
	private Paint outline_paint = new Paint();// ��������

	// ������ͼ���߳�
	private WaveFormPlotThread plot_thread;

	// ���캯��
	public WaveFormView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.getHolder().addCallback(this);

		int loop;
		temp_pos1 = com.example.osc8bit.osc.ch1_pos;
		temp_pos2 = com.example.osc8bit.osc.ch2_pos;

		for (loop = 0; loop < width; loop++) {
			ch1_data[loop] = temp_pos1;
			ch2_data[loop] = temp_pos2;
		}

		// ��ʼ����ͼ�߳�
		plot_thread = new WaveFormPlotThread(this.getHolder(), this);

		// ��ʼ��������ɫ
		// ����6�����ʵ���ɫ
		ch1_paint.setColor(Color.YELLOW);
		ch2_paint.setColor(Color.BLUE);
		back_paint.setColor(Color.rgb(100, 100, 100));
		cross_paint.setColor(Color.GREEN);
		outline_paint.setColor(Color.GREEN);
		arrow_paint.setColor(Color.RED);
		messure_paint.setColor(Color.WHITE);
	}

	// ��surface������ʱ�����
	public void surfaceCreated(SurfaceHolder holder) {
		// ������������ͼ�߳�
		plot_thread.setRunning(true);
		plot_thread.start();
	}

	public WaveFormView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void onDraw(Canvas canvas) {
		plotPoints(canvas);// ��ʼ��canvas�ϻ���
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// holder���ٵ�ʱ���û�ͼ�߳�ֹͣ
		// ���û��ֹͣ��������һֱ���ԣ�֪���߳�ִ�н�����join�����ȴ��߳̽���
		boolean retry = true;
		plot_thread.setRunning(false);

		while (retry) {
			try {
				plot_thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Toast.makeText(getContext(), "�ȴ���ͼ����", 1);
			}
		}
	}

	// ���㷽��
	public void plotPoints(Canvas canvas) {
		
		// 1.���canvas
		canvas.drawColor(Color.rgb(20, 20, 20));
		// 2.����������
		for (int vertical = 1; vertical < 7; vertical++) {
			canvas.drawLine(vertical * (width / 7) + 1, 1, vertical
					* (width / 7) + 1, height + 1, back_paint);
		}
		for (int horizotal = 1; horizotal < 10; horizotal++) {
			canvas.drawLine(0, horizotal * (height / 10) + 1, width + 1,
					horizotal * (height / 10) + 1, back_paint);
		}
		// 3.����ch1�Ĳ���
		if (com.example.osc8bit.osc.flag_CH1
				|| com.example.osc8bit.osc.flag_CHAll) {
			for (int x = 0; x < (width - 1); x++) {
				canvas.drawLine(x, ch1_data[x], x + 1, ch1_data[x + 1],
						ch1_paint);
			}
			// ��λ��ֱ���ϻ���ͷ
			temp_pos1 = com.example.osc8bit.osc.ch1_pos;
			canvas.drawLine(0, temp_pos1 - 9, 0, temp_pos1 + 9, arrow_paint);
			canvas.drawLine(1, temp_pos1 - 8, 1, temp_pos1 + 8, arrow_paint);
			canvas.drawLine(2, temp_pos1 - 7, 2, temp_pos1 + 7, arrow_paint);
			canvas.drawLine(3, temp_pos1 - 6, 3, temp_pos1 + 6, arrow_paint);
			canvas.drawLine(4, temp_pos1 - 5, 4, temp_pos1 + 5, arrow_paint);
			canvas.drawLine(5, temp_pos1 - 4, 5, temp_pos1 + 4, arrow_paint);
			canvas.drawLine(6, temp_pos1 - 3, 6, temp_pos1 + 3, arrow_paint);
			canvas.drawLine(7, temp_pos1 - 2, 7, temp_pos1 + 2, arrow_paint);
			canvas.drawLine(8, temp_pos1 - 1, 8, temp_pos1 + 1, arrow_paint);
			canvas.drawLine(9, temp_pos1 - 0, 9, temp_pos1 + 0, arrow_paint);
		}
		// 4.����ch2�Ĳ���
		if (com.example.osc8bit.osc.flag_CH2
				|| com.example.osc8bit.osc.flag_CHAll) {
			for (int x = 0; x < (width - 1); x++) {
				canvas.drawLine(x, ch2_data[x], x + 1, ch2_data[x + 1],
						ch2_paint);
			}
			// ��λ��ֱ���ϻ���ͷ
			temp_pos1 = com.example.osc8bit.osc.ch1_pos;
			canvas.drawLine(0, temp_pos1 - 9, 0, temp_pos1 + 9, arrow_paint);
			canvas.drawLine(1, temp_pos1 - 8, 1, temp_pos1 + 8, arrow_paint);
			canvas.drawLine(2, temp_pos1 - 7, 2, temp_pos1 + 7, arrow_paint);
			canvas.drawLine(3, temp_pos1 - 6, 3, temp_pos1 + 6, arrow_paint);
			canvas.drawLine(4, temp_pos1 - 5, 4, temp_pos1 + 5, arrow_paint);
			canvas.drawLine(5, temp_pos1 - 4, 5, temp_pos1 + 4, arrow_paint);
			canvas.drawLine(6, temp_pos1 - 3, 6, temp_pos1 + 3, arrow_paint);
			canvas.drawLine(7, temp_pos1 - 2, 7, temp_pos1 + 2, arrow_paint);
			canvas.drawLine(8, temp_pos1 - 1, 8, temp_pos1 + 1, arrow_paint);
			canvas.drawLine(9, temp_pos1 - 0, 9, temp_pos1 + 0, arrow_paint);
		}
		// 5.�������е�ʮ��
		canvas.drawLine(0, (height / 2) + 1, width + 1, (height / 2) + 1,
				cross_paint);
		canvas.drawLine((width / 2) + 1, 0, (width / 2) + 1, height + 1,
				cross_paint);

		// 6.����������
		canvas.drawLine(0, 0, width - 1, 0, outline_paint); // top
		canvas.drawLine(width - 1, 0, width - 1, height - 1, outline_paint); // right
		canvas.drawLine(0, height - 1, width - 1, height - 1, outline_paint); // bottom
		canvas.drawLine(0, 0, 0, height - 1, outline_paint); // left

		// 7.��X����Ĳ�����
		if (com.example.osc8bit.osc.flag_mes_X) {
			int where = com.example.osc8bit.osc.where_XL;

			canvas.drawLine(where, 1, where, height - 2, messure_paint);
			// �ڵײ�����ͷ
			canvas.drawLine(where - 9, height - 0, where + 9, height - 0,
					messure_paint);
			canvas.drawLine(where - 8, height - 1, where + 8, height - 1,
					messure_paint);
			canvas.drawLine(where - 7, height - 2, where + 7, height - 2,
					messure_paint);
			canvas.drawLine(where - 6, height - 3, where + 6, height - 3,
					messure_paint);
			canvas.drawLine(where - 5, height - 4, where + 5, height - 4,
					messure_paint);
			canvas.drawLine(where - 4, height - 5, where + 4, height - 5,
					messure_paint);
			canvas.drawLine(where - 3, height - 6, where + 3, height - 6,
					messure_paint);
			canvas.drawLine(where - 2, height - 7, where + 2, height - 7,
					messure_paint);
			canvas.drawLine(where - 1, height - 8, where + 1, height - 8,
					messure_paint);
			canvas.drawLine(where - 0, height - 9, where + 0, height - 9,
					messure_paint);

			where = com.example.osc8bit.osc.where_XR;

			canvas.drawLine(where, 1, where, height - 2, messure_paint);
			// �ڶ�������ͷ
			canvas.drawLine(where - 9, 0, where + 9, 0, messure_paint);
			canvas.drawLine(where - 8, 1, where + 8, 1, messure_paint);
			canvas.drawLine(where - 7, 2, where + 7, 2, messure_paint);
			canvas.drawLine(where - 6, 3, where + 6, 3, messure_paint);
			canvas.drawLine(where - 5, 4, where + 5, 4, messure_paint);
			canvas.drawLine(where - 4, 5, where + 4, 5, messure_paint);
			canvas.drawLine(where - 3, 6, where + 3, 6, messure_paint);
			canvas.drawLine(where - 2, 7, where + 2, 7, messure_paint);
			canvas.drawLine(where - 1, 8, where + 1, 8, messure_paint);
			canvas.drawLine(where - 0, 9, where + 0, 9, messure_paint);

		}
		// 8.��Y����Ĳ�����
		if (com.example.osc8bit.osc.flag_mes_Y) {
			int where = com.example.osc8bit.osc.where_YT;
			canvas.drawLine(1, where, width - 2, where, messure_paint);
			// ����Ļ����໭��ͷ
			canvas.drawLine(0, where - 9, 0, where + 9, messure_paint);
			canvas.drawLine(1, where - 8, 1, where + 8, messure_paint);
			canvas.drawLine(2, where - 7, 2, where + 7, messure_paint);
			canvas.drawLine(3, where - 6, 3, where + 6, messure_paint);
			canvas.drawLine(4, where - 5, 4, where + 5, messure_paint);
			canvas.drawLine(5, where - 4, 5, where + 4, messure_paint);
			canvas.drawLine(6, where - 3, 6, where + 3, messure_paint);
			canvas.drawLine(7, where - 2, 7, where + 2, messure_paint);
			canvas.drawLine(8, where - 1, 8, where + 1, messure_paint);
			canvas.drawLine(9, where - 0, 9, where + 0, messure_paint);

			where = com.example.osc8bit.osc.where_YD;

			canvas.drawLine(1, where, width - 2, where, messure_paint);
			// ����Ļ���Ҳ໭��ͷ
			canvas.drawLine(width - 9, where - 0, width - 9, where + 0,
					messure_paint);
			canvas.drawLine(width - 8, where - 1, width - 8, where + 1,
					messure_paint);
			canvas.drawLine(width - 7, where - 2, width - 7, where + 2,
					messure_paint);
			canvas.drawLine(width - 6, where - 3, width - 6, where + 3,
					messure_paint);
			canvas.drawLine(width - 5, where - 4, width - 5, where + 4,
					messure_paint);
			canvas.drawLine(width - 4, where - 5, width - 4, where + 5,
					messure_paint);
			canvas.drawLine(width - 3, where - 6, width - 3, where + 6,
					messure_paint);
			canvas.drawLine(width - 2, where - 7, width - 2, where + 7,
					messure_paint);
			canvas.drawLine(width - 1, where - 8, width - 1, where + 8,
					messure_paint);
			canvas.drawLine(width - 0, where - 9, width - 0, where + 9,
					messure_paint);
		}
		// 9.��MIC�ɼ������ź�
		if (com.example.osc8bit.osc.flag_MIC) {
			for (int i = 0; i < (width - 2); i++) {
				// MICĬ�ϴ浽ch1���������鵱��
				canvas.drawLine(i, ch1_data[i], i + 1, ch1_data[i + 1],
						ch1_paint);
				// canvas.drawPoint(i, ch1_data[i], ch1_paint);
				// drawLine(x, ch1_data[x], x+1, ch1_data[x+1], ch1_paint);
			}
		}
	}

	// ΪʲôҪ��set_data����������WaveFormView���أ�
	// ��osc��ͨ��handler��ȡ�����ص����ݣ���Ҫ�����ݻ���surface�ϣ�
	// �������ݵ��β���View�У����ԣ�Ҫ��osc�ɼ��������ݴ����βΣ�����Ҫ��set_data�������嵽View��
	public void set_data(int[] data1, int[] data2) {
		int x;
		//plot_thread.setRunning(false);

		// 1.����ch1����
		if (com.example.osc8bit.osc.flag_CH1
				|| com.example.osc8bit.osc.flag_CHAll) {
			x = 0;
			temp_pos1 = com.example.osc8bit.osc.ch1_pos;
			while (x < width) {
				switch (com.example.osc8bit.osc.Y_level) {
				case 1:
					ch1_data[x] = temp_pos1 - (data1[x] * 4);
					break;
				case 2:
					ch1_data[x] = temp_pos1 - (data1[x] * 2);
					break;
				case 3:
					ch1_data[x] = temp_pos1 - data1[x];
					break;
				case 4:
					ch1_data[x] = temp_pos1 - (data1[x] / 2);
					break;
				case 5:
					ch1_data[x] = temp_pos1 - (data1[x] / 4);
					break;
				}
				x++;
			}
		}

		// 2.����ch2����
		if (com.example.osc8bit.osc.flag_CH2
				|| com.example.osc8bit.osc.flag_CHAll) {
			x = 0;
			temp_pos2 = com.example.osc8bit.osc.ch2_pos;
			while (x < width) {
				switch (com.example.osc8bit.osc.Y_level) {
				case 1:
					ch2_data[x] = temp_pos2 - (data2[x] * 4);
					break;
				case 2:
					ch2_data[x] = temp_pos2 - (data2[x] * 2);
					break;
				case 3:
					ch2_data[x] = temp_pos2 - data2[x];
					break;
				case 4:
					ch2_data[x] = temp_pos2 - (data2[x] / 2);
					break;
				case 5:
					ch2_data[x] = temp_pos2 - (data2[x] / 4);
					break;
				}
				x++;
			}
		}

		// 3.MIC�򿪵�ʱ�򣬲ɼ��������ݷ���ch1
		if (com.example.osc8bit.osc.flag_MIC) {
			x = 0;
			temp_pos1 = height / 2;
			while (x < width) {
				ch1_data[x] = temp_pos1 + (data1[x]);
				x++;
			}
		}
	}

	// �ر�ch1��ch2�������εķ���
	public void ch_disappear(){
		ch1_paint.setColor(Color.TRANSPARENT);
		ch2_paint.setColor(Color.TRANSPARENT);
	}
	// �ر�ch1��ch2�������εķ���
	public void ch_appear(){
		ch1_paint.setColor(Color.YELLOW);
		ch2_paint.setColor(Color.BLUE);
	}
	
	
	// ���Ⱪ¶��һ�����沨�εķ���
	public Bitmap savePictyre(Canvas canvas) {
		// ���ô�С�ͱ��뷽ʽ
		Bitmap mybm = Bitmap.createBitmap(700, 500, Config.ARGB_8888);
		// ����һ�黭�������������Ϸ���bitmapͼ��
		Canvas mycv = new Canvas(mybm);
		mycv = canvas;
		// ����Ҳ�Ǳ�������canvas
		mycv.save(Canvas.ALL_SAVE_FLAG);
		mycv.restore();
		return mybm;
	}
}
