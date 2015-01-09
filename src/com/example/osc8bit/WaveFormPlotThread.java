package com.example.osc8bit;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class WaveFormPlotThread extends Thread {
	
	private SurfaceHolder holder;
	private WaveFormView plot_area;
	
	//��ͼ�߳̿������رձ�־λ
	private boolean _run = false;
	private WaveFormView view;
	//�޸ı�־λ�ķ���
	public void setRunning(boolean run){
		_run = run;
	}
	
	//���캯��
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
				//����canvas
				canvas=holder.lockCanvas(null);
				synchronized(canvas){
					//��ʼ��canvas�ϻ���
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
