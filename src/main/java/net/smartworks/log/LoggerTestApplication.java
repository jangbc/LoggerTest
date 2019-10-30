package net.smartworks.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.apache.logging.log4j.core.config.Configuration;

//@SpringBootApplication
@SpringBootApplication(exclude = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class })
public class LoggerTestApplication {

	public static void main(String[] args) {
		
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getRootLogger().removeAppender("nosqlLogTestAppender");
		ctx.updateLoggers();
		
		SpringApplication.run(LoggerTestApplication.class, args);
	}

}
