package com.letv.shop.aladdin.client.test;

import org.junit.Test;

import com.letv.shop.aladdin.client.reporter.impl.TcpReporter;

public class TcpReporterTest {
	@Test
	public void test() throws Exception {
		TcpReporter tr = new TcpReporter();

		int thread = 10;
		int perThread = 10000;

		class SendThread extends Thread {
			private TcpReporter tr;
			private int size;
			private String key;

			public SendThread(TcpReporter tr, int size, String key) {
				this.tr = tr;
				this.size = size;
				this.key = key;
			}

			@Override
			public void run() {
				for (int i = 0; i < size; i++) {
					String info = "info" + i;
					tr.warn(key, info);
				}
			}
		}
	
		for(int i=0;i<thread;i++){
			String key="key"+i;
			new SendThread(tr, perThread, key).start();
		}
		
		Thread.sleep(20000);
	}
}
