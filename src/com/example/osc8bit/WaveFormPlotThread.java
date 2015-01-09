package com.example.osc8bit;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class WaveFormPlotThread extends Thread {
	
	private SurfaceHolder holder;
	private WaveFormView plot_area;
	
	//绘图线程开启、关闭标志位
	private boolean _run = false;
	private WaveFormView view;
	//修改标志位的方法
	public void setRunning(boolean run){
		_run = run;
	}
	
	//构造函数
	public WaveFormPlotThread(SurfaceHolder surfaceHolder, WaveFormView view){
		holder=surfaceHolder;
		plot_area=view;
	}

	
	@Override
	public void run() {
		Canvas canvas;
		while(_run){
			canvas = null;
			try {
				//锁是canvas
				canvas=holder.lockCanvas(null);
				synchronized(canvas){
					//开始在canvas上画点
					plot_area.plotPoints(canvas);
				}
			} catch (Exception e) {
				if(canvas!=null){
					holder.unlockCanvasAndPost(canvas);
				}
				e.printStackTrace();
			}finally{
				if(canvas!=null){
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

}
