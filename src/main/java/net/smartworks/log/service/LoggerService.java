package net.smartworks.log.service;

import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.nosql.NoSqlAppender;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.jmx.RingBufferAdmin;
import org.apache.logging.log4j.core.layout.MessageLayout;
import org.apache.logging.log4j.message.MapMessage;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.mongodb3.MongoDbProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mongodb.MongoException;

@Service
public class LoggerService {
	static {
		isConfiguredMongoDbAppender = configureLog4jAppender();

		if (!LoggerService.isAliveMongoDb) {
			System.out.println("db can not connect!!!");
		}
		System.out.println("STATICCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
	}

	private static boolean isAliveMongoDb;
	private static boolean isConfiguredMongoDbAppender;

	private static boolean okMongoDb = false;

	private static String loggerNameMongoDbTransactions;
	private static String loggerNameMongoDbResults;
	private static String loggerNameMongoDbLogs;

	private static String loggerNameFileTransactions;
	private static String loggerNameFileResults;
	private static String loggerNameFileLogs;

	private static Logger logger;
	private static Logger loggerMongoDbTransactions;
	private static Logger loggerMongoDbResults;
	private static Logger loggerMongoDbLogs;

	private static Logger loggerFileTransactions;
	private static Logger loggerFileResults;
	private static Logger loggerFileLogs;

	private static final boolean USE_ASYNC_APPENDER = false;

	private static final String MONGODB_SERVER = "mongodb_server";
	private static final String MONGODB_PORT = "mongodb_port";
	private static final String MONGODB_USERNAME = "mongodb_username";
	private static final String MONGODB_PASSWORD = "mongodb_password";
	private static final String MONGODB_NAME = "mongodb_name";

	private static final String COLLECTION_NAME_TRANSACTIONS = "collection_transactions";
	private static final String COLLECTION_NAME_RESULTS = "collection_results";
	private static final String COLLECTION_NAME_LOGS = "collection_logs";

	private static final String NOSQL_APPENDER_NAME_TRANSACTIONS = "nosqlTransactionsAppender";
	private static final String NOSQL_APPENDER_NAME_RESULTS = "nosqlTransactionResultsAppender";
	private static final String NOSQL_APPENDER_NAME_LOGS = "nosqlLogsAppender";

	private static final String ASYNC_APPENDER_NAME_TRANSACTION = "asyncTransactionsAppender";
	private static final String ASYNC_APPENDER_NAME_RESULTS = "asyncTransactionResultsAppender";
	private static final String ASYNC_APPENDER_NAME_LOGS = "asyncLogsAppender";

	private static final String LOGGER_NAME_TRANSACTIONS = "logger_name_transactions";
	private static final String LOGGER_NAME_RESULTS = "logger_name_results";
	private static final String LOGGER_NAME_LOGS = "logger_name_logs";

	private static final String LOGGER_NAME_FILE_TRANSACTIONS = "logger_name_file_transactions";
	private static final String LOGGER_NAME_FILE_RESULTS = "logger_name_file_results";
	private static final String LOGGER_NAME_FILE_LOGS = "logger_name_file_logs";

	private static RingBufferAdmin ringBufferAdminTransactions;
	private static RingBufferAdmin ringBufferAdminResults;
	private static RingBufferAdmin ringBufferAdminLogs;

	private static Thread threadMongoDbAutoConnect;

	public LoggerService() {
		super();
		System.out.println("LoggerService():" + isAliveMongoDb);
	}

	public void printLogHelloWorld() {
		System.out.println("printLogHelloWorld()");

		long firstTime = System.currentTimeMillis();
		long logsBufferMaxCapa = 10;// Math.round(ringBufferAdminLogs.getBufferSize() * 0.5);
		long logsBufferMaxRemCapa = ringBufferAdminLogs.getBufferSize() - logsBufferMaxCapa;

		for (int i = 0; i < 15; i++) {

			MapMessage mapMessage = new MapMessage();
			mapMessage.put("aaaa", "AAAA");
			mapMessage.put("bbb", "BBB");
			mapMessage.put("cccc", "CCC");
			mapMessage.put("dddd", "DDD");

			okMongoDb = false;

			if (isConfiguredMongoDbAppender && isAliveMongoDb) {
				long remainingCapacity = ringBufferAdminLogs.getRemainingCapacity();

				System.out.println("logsBufferCapa!!!!():" + remainingCapacity + " max Capa:"
						+ ringBufferAdminLogs.getBufferSize());

				if (remainingCapacity > logsBufferMaxRemCapa) {
					okMongoDb = true;
				}
			}

			if (okMongoDb) {
				try {
					System.out.println("MONGO DB Appender");
					loggerMongoDbLogs.info(mapMessage);
				} catch (Exception ex) {
					System.out.println("exception!!!!():");
				}
			} else {
				Map<String, String> map = new TreeMap();

				map.put("name", "terry");

				map.put("email", "terry@mycompany.com");

				ObjectMessage msg = new ObjectMessage(map);

				loggerFileTransactions.info(msg);

				// loggerFileTransactions.info(mapMessage);
				System.out.println("File Appender");
			}
		}

		long diffTime = System.currentTimeMillis() - firstTime;
		System.out.println("diff Time!!!!!!!!!!!!!!::" + diffTime);
	}

	public static boolean configureLog4jAppender() {

		try {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

			Configuration config = ctx.getConfiguration();

			String server = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_SERVER);
			String port = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_PORT);

			isAliveMongoDb = serverListening(server, Integer.parseInt(port));

			if (!isAliveMongoDb) {
				if (threadMongoDbAutoConnect == null
						|| threadMongoDbAutoConnect != null && !threadMongoDbAutoConnect.isAlive()) {
					AutoConnectMongoDbRunnable autoConnectMongoDbRunnable = new AutoConnectMongoDbRunnable();
					threadMongoDbAutoConnect = new Thread(autoConnectMongoDbRunnable);
					threadMongoDbAutoConnect.start();
				}
				return false;
			}

			String username = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_USERNAME);
			String password = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_PASSWORD);
			String dbName = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_NAME);

			String collectionTransactions = config.getStrSubstitutor().getVariableResolver()
					.lookup(COLLECTION_NAME_TRANSACTIONS);
			String collectionResults = config.getStrSubstitutor().getVariableResolver().lookup(COLLECTION_NAME_RESULTS);
			String collectionLogs = config.getStrSubstitutor().getVariableResolver().lookup(COLLECTION_NAME_LOGS);

			loggerNameMongoDbTransactions = config.getStrSubstitutor().getVariableResolver()
					.lookup(LOGGER_NAME_TRANSACTIONS);
			loggerNameMongoDbResults = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_RESULTS);
			loggerNameMongoDbLogs = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_LOGS);

			loggerMongoDbTransactions = LogManager.getLogger(loggerNameMongoDbTransactions);
			loggerMongoDbResults = LogManager.getLogger(loggerNameMongoDbResults);
			loggerMongoDbLogs = LogManager.getLogger(loggerNameMongoDbLogs);

			loggerNameFileTransactions = config.getStrSubstitutor().getVariableResolver()
					.lookup(LOGGER_NAME_FILE_TRANSACTIONS);
			loggerNameFileResults = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_FILE_RESULTS);
			loggerNameFileLogs = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_FILE_LOGS);

			loggerFileTransactions = LogManager.getLogger(loggerNameFileTransactions);
			loggerFileResults = LogManager.getLogger(loggerNameFileResults);
			loggerFileLogs = LogManager.getLogger(loggerNameFileLogs);

			MongoDbProvider mongoDbProviderTransactions = addMongoDbProvider(server, dbName, username, password,
					collectionTransactions);
			MongoDbProvider mongoDbProviderResults = addMongoDbProvider(server, dbName, username, password,
					collectionResults);
			MongoDbProvider mongoDbProviderLogs = addMongoDbProvider(server, dbName, username, password,
					collectionLogs);

			NoSqlAppender noSqlAppenderTransactions = addNoSqlAppender(NOSQL_APPENDER_NAME_TRANSACTIONS,
					mongoDbProviderTransactions);
			NoSqlAppender noSqlAppenderResults = addNoSqlAppender(NOSQL_APPENDER_NAME_RESULTS, mongoDbProviderResults);
			NoSqlAppender noSqlAppenderLogs = addNoSqlAppender(NOSQL_APPENDER_NAME_LOGS, mongoDbProviderLogs);

			if (USE_ASYNC_APPENDER) {
				AsyncAppender asyncAppenderTransactions = setToAsyncLogger(NOSQL_APPENDER_NAME_TRANSACTIONS,
						ASYNC_APPENDER_NAME_TRANSACTION, config);
				AsyncAppender asyncAppenderResults = setToAsyncLogger(NOSQL_APPENDER_NAME_RESULTS,
						ASYNC_APPENDER_NAME_RESULTS, config);
				AsyncAppender asyncAppenderLogs = setToAsyncLogger(NOSQL_APPENDER_NAME_LOGS, ASYNC_APPENDER_NAME_LOGS,
						config);

				startUpdatLoggerWithAsyncAppender(noSqlAppenderTransactions, asyncAppenderTransactions,
						loggerNameMongoDbTransactions, config);
				startUpdatLoggerWithAsyncAppender(noSqlAppenderResults, asyncAppenderResults, loggerNameMongoDbResults,
						config);
				startUpdatLoggerWithAsyncAppender(noSqlAppenderLogs, asyncAppenderLogs, loggerNameMongoDbLogs, config);
			} else {
				startUpdatLogger(noSqlAppenderTransactions, loggerNameMongoDbTransactions, config);
				startUpdatLogger(noSqlAppenderResults, loggerNameMongoDbResults, config);
				startUpdatLogger(noSqlAppenderLogs, loggerNameMongoDbLogs, config);
			}

			ringBufferAdminTransactions = getRingBufferAdminFromAsyncLogger(loggerNameMongoDbTransactions, config);
			ringBufferAdminResults = getRingBufferAdminFromAsyncLogger(loggerNameMongoDbResults, config);
			ringBufferAdminLogs = getRingBufferAdminFromAsyncLogger(loggerNameMongoDbLogs, config);

			return true;
		} catch (Exception ex) {

		}
		return false;
	}

	private static MongoDbProvider addMongoDbProvider(String ip, String db, String username, String password,
			String collection) {
		MongoDbProvider mongoDbProvider = MongoDbProvider.newBuilder().setCollectionName(collection).setDatabaseName(db)
				.setServer(ip).setUserName(username).setPassword(password).build();

		return mongoDbProvider;
	}

	private static NoSqlAppender addNoSqlAppender(String name, MongoDbProvider mongoDbProvider) {
		MessageLayout messageLayout = (MessageLayout) MessageLayout.createLayout();

		final NoSqlAppender noSqlAppender = NoSqlAppender.newBuilder().setName(name).setProvider(mongoDbProvider)
				.setLayout(messageLayout).build();

		return noSqlAppender;
	}

	private static AsyncAppender setToAsyncLogger(String nosqlAppenderName, String asyncAppenderName,
			Configuration config) {
		AppenderRef appenderRef = AppenderRef.createAppenderRef(nosqlAppenderName, null, null);

		AppenderRef[] appenderRefs = new AppenderRef[] { appenderRef };

		final AsyncAppender asyncAppender = AsyncAppender.newBuilder().setAppenderRefs(appenderRefs)
				.setName(asyncAppenderName).setConfiguration(config).build();

		return asyncAppender;

	}

	private static void startUpdatLoggerWithAsyncAppender(NoSqlAppender noSqlAppender, AsyncAppender asyncAppender,
			String loggerName, Configuration config) {
		config.addAppender(noSqlAppender);
		config.addAppender(asyncAppender);

		noSqlAppender.start();
		asyncAppender.start();

		updateLoggers(asyncAppender, config, loggerName);
	}

	private static void startUpdatLogger(NoSqlAppender noSqlAppender, String loggerName, Configuration config) {
		config.addAppender(noSqlAppender);
		noSqlAppender.start();
		updateLoggers(noSqlAppender, config, loggerName);
	}

	private static void updateLoggers(final Appender appender, final Configuration config, final String loggerName) {
		LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
		loggerConfig.addAppender(appender, null, null);
	}

	private static RingBufferAdmin getRingBufferAdminFromAsyncLogger(String asyncLoggerName, Configuration config) {
		String contextName = config.getLoggerContext().getName();
		AsyncLoggerConfig asyncLoggerConfig = (AsyncLoggerConfig) config.getLoggerConfig(asyncLoggerName);
		return asyncLoggerConfig.createRingBufferAdmin(contextName);
	}

	public static boolean serverListening(String host, int port) {
		Socket s = null;
		try {
			s = new Socket(host, port);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception e) {
				}
		}
	}

	@ExceptionHandler(value = MongoException.class)
	public ResponseEntity<String> handle(Exception ex) {

		System.out.println("exception!!!!@ExceptionHandler");
		return null;
	}
}
