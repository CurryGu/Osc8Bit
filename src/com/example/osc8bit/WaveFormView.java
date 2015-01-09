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

//自定义一个View
public class WaveFormView extends SurfaceView implements SurfaceHolder.Callback {

	private final int width = 700;// 宽700
	private final int height = 500;// 高500
	private static int[] ch1_data = new int[1000];// ch1采集到的数据
	private static int[] ch2_data = new int[1000];// ch2采集到的数据

	int temp_pos1 = 0;// ch1波形初始位置
	int temp_pos2 = 0;// ch2波形初始位置

	// 定义画笔
	private Paint ch1_paint = new Paint();// 画ch1波形
	private Paint ch2_paint = new Paint();// 画ch2波形
	private Paint back_paint = new Paint();// 画背景
	private Paint cross_paint = new Paint();// 画十字
	private Paint arrow_paint = new Paint();// 画箭头
	private Paint messure_paint = new Paint();// 画测量的线
	private Paint outline_paint = new Paint();// 画轮廓线

	// 定义作图的线程
	private WaveFormPlotThread plot_thread;

	// 构造函数
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

		// 初始化画图线程
		plot_thread = new WaveFormPlotThread(this.getHolder(), this);

		// 初始化画笔颜色
		// 设置6个画笔的颜色
		ch1_paint.setColor(Color.YELLOW);
		ch2_paint.setColor(Color.BLUE);
		back_paint.setColor(Color.rgb(100, 100, 100));
		cross_paint.setColor(Color.GREEN);
		outline_paint.setColor(Color.GREEN);
		arrow_paint.setColor(Color.RED);
		messure_paint.setColor(Color.WHITE);
	}

	// 当surface创建的时候调用
	public void surfaceCreated(SurfaceHolder holder) {
		// 创建即开启画图线程
		plot_thread.setRunning(true);
		plot_thread.start();
	}

	public WaveFormView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void onDraw(Canvas canvas) {
		plotPoints(canvas);// 开始在canvas上画点
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// holder销毁的时候，让绘图线程停止
		// 如果没有停止下来，则一直重试，知道线程执行结束，join方法等待线程结束
		boolean retry = true;
		plot_thread.setRunning(false);

		while (retry) {
			try {
				plot_thread.join();
				retry = false;
			} catch (InterruptedException e) {
				Toast.makeText(getContext(), "等待绘图结束", 1);
			}
		}
	}

	// 画点方法
	public void plotPoints(Canvas canvas) {
		
		// 1.清空canvas
		canvas.drawColor(Color.rgb(20, 20, 20));
		// 2.画背景网格
		for (int vertical = 1; vertical < 7; vertical++) {
			canvas.drawLine(vertical * (width / 7) + 1, 1, vertical
					* (width / 7) + 1, height + 1, back_paint);
		}
		for (int horizotal = 1; horizotal < 10; horizotal++) {
			canvas.drawLine(0, horizotal * (height / 10) + 1, width + 1,
					horizotal * (height / 10) + 1, back_paint);
		}
		// 3.绘制ch1的波形
		if (com.example.osc8bit.osc.flag_CH1
				|| com.example.osc8bit.osc.flag_CHAll) {
			for (int x = 0; x < (width - 1); x++) {
				canvas.drawLine(x, ch1_data[x], x + 1, ch1_data[x + 1],
						ch1_paint);
			}
			// 在位置直线上画箭头
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
		// 4.绘制ch2的波形
		if (com.example.osc8bit.osc.flag_CH2
				|| com.example.osc8bit.osc.flag_CHAll) {
			for (int x = 0; x < (width - 1); x++) {
				canvas.drawLine(x, ch2_data[x], x + 1, ch2_data[x + 1],
						ch2_paint);
			}
			// 在位置直线上画箭头
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
		// 5.画坐标中的十字
		canvas.drawLine(0, (height / 2) + 1, width + 1, (height / 2) + 1,
				cross_paint);
		canvas.drawLine((width / 2) + 1, 0, (width / 2) + 1, height + 1,
				cross_paint);

		// 6.画坐标轮廓
		canvas.drawLine(0, 0, width - 1, 0, outline_paint); // top
		canvas.drawLine(width - 1, 0, width - 1, height - 1, outline_paint); // right
		canvas.drawLine(0, height - 1, width - 1, height - 1, outline_paint); // bottom
		canvas.drawLine(0, 0, 0, height - 1, outline_paint); // left

		// 7.画X轴向的测量线
		if (com.example.osc8bit.osc.flag_mes_X) {
			int where = com.example.osc8bit.osc.where_XL;

			canvas.drawLine(where, 1, where, height - 2, messure_paint);
			// 在底部画箭头
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
			// 在顶部画箭头
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
		// 8.画Y轴向的测量线
		if (com.example.osc8bit.osc.flag_mes_Y) {
			int where = com.example.osc8bit.osc.where_YT;
			canvas.drawLine(1, where, width - 2, where, messure_paint);
			// 在屏幕最左侧画箭头
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
			// 在屏幕最右侧画箭头
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
		// 9.画MIC采集到的信号
		if (com.example.osc8bit.osc.flag_MIC) {
			for (int i = 0; i < (width - 2); i++) {
				// MIC默认存到ch1的数据数组当中
				canvas.drawLine(i, ch1_data[i], i + 1, ch1_data[i + 1],
						ch1_paint);
				// canvas.drawPoint(i, ch1_data[i], ch1_paint);
				// drawLine(x, ch1_data[x], x+1, ch1_data[x+1], ch1_paint);
			}
		}
	}

	// 为什么要把set_data方法定义在WaveFormView中呢？
	// 在osc中通过handler获取到返回的数据，需要将数据画到surface上，
	// 画的数据的形参在View中，所以，要将osc采集到的数据传给形参，所以要把set_data方法定义到View中
	public void set_data(int[] data1, int[] data2) {
		int x;
		//plot_thread.setRunning(false);

		// 1.设置ch1数据
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

		// 2.设置ch2数据
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

		// 3.MIC打开的时候，采集到的数据放入ch1
		if (com.example.osc8bit.osc.flag_MIC) {
			x = 0;
			temp_pos1 = height / 2;
			while (x < width) {
				ch1_data[x] = temp_pos1 + (data1[x]);
				x++;
			}
		}
	}

	// 关闭ch1和ch2两个波形的方法
	public void ch_disappear(){
		ch1_paint.setColor(Color.TRANSPARENT);
		ch2_paint.setColor(Color.TRANSPARENT);
	}
	// 关闭ch1和ch2两个波形的方法
	public void ch_appear(){
		ch1_paint.setColor(Color.YELLOW);
		ch2_paint.setColor(Color.BLUE);
	}
	
	
	// 对外暴露的一个保存波形的方法
	public Bitmap savePictyre(Canvas canvas) {
		// 设置大小和编码方式
		Bitmap mybm = Bitmap.createBitmap(700, 500, Config.ARGB_8888);
		// 创建一块画布，并将画布上放上bitmap图像
		Canvas mycv = new Canvas(mybm);
		mycv = canvas;
		// 保存也是保存整个canvas
		mycv.save(Canvas.ALL_SAVE_FLAG);
		mycv.restore();
		return mybm;
	}
}
