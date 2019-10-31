package net.smartworks.log.exception;

public class LoggerTestExceptionHandler implements com.lmax.disruptor.ExceptionHandler {

	public LoggerTestExceptionHandler() {

	}

	@Override
	public void handleEventException(Throwable ex, long sequence, Object event) {
		System.out.println("handleEventException!!!!@ExceptionHandler");

	}

	@Override
	public void handleOnStartException(Throwable ex) {
		// TODO Auto-generated method stub
		System.out.println("handleOnStartException!!!!@ExceptionHandler");
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		// TODO Auto-generated method stub
		System.out.println("handleOnShutdownException!!!!@ExceptionHandler");
	}

}
