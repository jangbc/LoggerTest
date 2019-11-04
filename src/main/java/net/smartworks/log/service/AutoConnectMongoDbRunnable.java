package net.smartworks.log.service;

public class AutoConnectMongoDbRunnable implements Runnable {

	private boolean done = false;

	public AutoConnectMongoDbRunnable() {

	}

	@Override
	public void run() {

		while (!done) {
			done = LoggerService.configureLog4jAppender();

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
