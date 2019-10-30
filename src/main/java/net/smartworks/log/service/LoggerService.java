package net.smartworks.log.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class LoggerService {

	public static final String MONGODB_LOGGER = "net.smartworks.logger.mongodb";

	private static Logger logger = LogManager.getLogger(LoggerService.class);
	//private static Logger mongodbLogger = LogManager.getLogger(MONGODB_LOGGER);

	public LoggerService() {
		super();
		System.out.println("LoggerService()");
	}

	public void printLogHelloWorld() {
		System.out.println("printLogHelloWorld()");
		logger.debug("this is logger debug");
		logger.error("this is logger error");
		//mongodbLogger.debug("this is mongodb logger debug");
		//mongodbLogger.warn("this is mongodb logger warn");
		//mongodbLogger.error("this is mongodb logger error");
	}
}
