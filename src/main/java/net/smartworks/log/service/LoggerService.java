package net.smartworks.log.service;

import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.nosql.NoSqlAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.MessageLayout;
import org.apache.logging.log4j.mongodb3.MongoDbProvider;
import org.springframework.stereotype.Service;

@Service
public class LoggerService {
	static {
		isAliveMongoDb = connectToMongoDb();

		if (!LoggerService.isAliveMongoDb) {
			System.out.println("db can not connect!!!");
		}

		loggerTransactions = LogManager.getLogger(LoggerService.loggerNameTransactions);
		loggerResults = LogManager.getLogger(LoggerService.loggerNameResults);
		loggerLogs = LogManager.getLogger(LoggerService.loggerNameLogs);
	}

	static boolean isAliveMongoDb;

	static String loggerNameTransactions;
	static String loggerNameResults;
	static String loggerNameLogs;

	static Logger logger;
	static Logger loggerTransactions;
	static Logger loggerResults;
	static Logger loggerLogs;

	public static final String MONGODB_SERVER = "mongodb_server";
	public static final String MONGODB_PORT = "mongodb_port";
	public static final String MONGODB_USERNAME = "mongodb_username";
	public static final String MONGODB_PASSWORD = "mongodb_password";
	public static final String MONGODB_NAME = "mongodb_name";

	public static final String COLLECTION_NAME_TRANSACTIONS = "collection_transactions";
	public static final String COLLECTION_NAME_RESULTS = "collection_results";
	public static final String COLLECTION_NAME_LOGS = "collection_logs";

	public static final String NOSQL_APPENDER_NAME_TRANSACTIONS = "nosqlTransactionsAppender";
	public static final String NOSQL_APPENDER_NAME_RESULTS = "nosqlTransactionResultsAppender";
	public static final String NOSQL_APPENDER_NAME_LOGS = "nosqlLogsAppender";

	public static final String ASYNC_APPENDER_NAME_TRANSACTION = "asyncTransactionsAppender";
	public static final String ASYNC_APPENDER_NAME_RESULTS = "asyncTransactionResultsAppender";
	public static final String ASYNC_APPENDER_NAME_LOGS = "asyncLogsAppender";

	public static final String LOGGER_NAME_TRANSACTIONS = "logger_name_transactions";
	public static final String LOGGER_NAME_RESULTS = "logger_name_results";
	public static final String LOGGER_NAME_LOGS = "logger_name_logs";

	public LoggerService() {
		super();
		System.out.println("LoggerService():" + isAliveMongoDb);
	}

	public void printLogHelloWorld() {
		System.out.println("printLogHelloWorld()");
		/*
		 * logger.debug("this is logger debug"); logger.error("this is logger error");
		 */

		if (isAliveMongoDb) {
			loggerTransactions.debug("this is mongodb logger debug");
			loggerTransactions.warn("this is mongodb logger warn");
			loggerTransactions.error("this is mongodb logger error");

			loggerResults.debug("this is mongodb logger debug");
			loggerResults.warn("this is mongodb logger warn");
			loggerResults.error("this is mongodb logger error");

			loggerLogs.debug("this is mongodb logger debug");
			loggerLogs.warn("this is mongodb logger warn");
			loggerLogs.error("this is mongodb logger error");
		}
	}

	public static boolean connectToMongoDb() {

		try {
			LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			Configuration config = ctx.getConfiguration();

			String server = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_SERVER);
			String port = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_SERVER);

			boolean canConnect = serverListening(server, Integer.parseInt(port));

			if (!canConnect) {
				return false;
			}

			String username = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_USERNAME);
			String password = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_PASSWORD);
			String dbName = config.getStrSubstitutor().getVariableResolver().lookup(MONGODB_NAME);

			String collectionTransactions = config.getStrSubstitutor().getVariableResolver()
					.lookup(COLLECTION_NAME_TRANSACTIONS);
			String collectionResults = config.getStrSubstitutor().getVariableResolver().lookup(COLLECTION_NAME_RESULTS);
			String collectionLogs = config.getStrSubstitutor().getVariableResolver().lookup(COLLECTION_NAME_LOGS);

			loggerNameTransactions = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_TRANSACTIONS);
			loggerNameResults = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_RESULTS);
			loggerNameLogs = config.getStrSubstitutor().getVariableResolver().lookup(LOGGER_NAME_LOGS);

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

			AsyncAppender asyncAppenderTransactions = setToAsyncLogger(NOSQL_APPENDER_NAME_TRANSACTIONS,
					ASYNC_APPENDER_NAME_TRANSACTION, config);
			AsyncAppender asyncAppenderResults = setToAsyncLogger(NOSQL_APPENDER_NAME_RESULTS,
					ASYNC_APPENDER_NAME_RESULTS, config);
			AsyncAppender asyncAppenderLogs = setToAsyncLogger(NOSQL_APPENDER_NAME_LOGS, ASYNC_APPENDER_NAME_LOGS,
					config);

			startUpdatLogger(noSqlAppenderTransactions, asyncAppenderTransactions, loggerNameTransactions, config);
			startUpdatLogger(noSqlAppenderResults, asyncAppenderResults, loggerNameResults, config);
			startUpdatLogger(noSqlAppenderLogs, asyncAppenderLogs, loggerNameLogs, config);

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

	private static void startUpdatLogger(NoSqlAppender noSqlAppender, AsyncAppender asyncAppender, String loggerName,
			Configuration config) {
		config.addAppender(noSqlAppender);
		config.addAppender(asyncAppender);

		noSqlAppender.start();
		asyncAppender.start();

		updateLoggers(asyncAppender, config, loggerName);
	}

	private static void updateLoggers(final Appender appender, final Configuration config, final String loggerName) {

		LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
		loggerConfig.addAppender(appender, null, null);
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
}
