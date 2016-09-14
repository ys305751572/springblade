package com.smallchill.common.task;

import com.smallchill.core.toolbox.kit.DateKit;

public class GlobalTask implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("任务调度执行:" + DateKit.getTime());
	}

}
