package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;

import myjdbcagent.support.AgentConfig;
import myjdbcagent.support.Logger;

/**
 * This very basic JdbcEventListener writes a log on every event.
 * <p>
 * With configuration, this can be used to log only "slow sql" or "big resultset
 * fetch" which may be useful.
 * 
 * @author panyu
 *
 */
public class LoggingJdbcEventListener implements JdbcEventListener {

	public static final String ENABLED = "listener.logging.enabled";
	public static final String LOG_CONNECTION_EVENT = "listener.logging.logConnectionEvent";
	public static final String LOG_SQL_EXECUTION = "listener.logging.logSqlExecution";
	public static final String LOG_SQL_EXECUTION_OF_MINIMAL_USE_TIME = "listener.logging.logSqlExecutionOfMinimalUseTime";
	public static final String LOG_RESULT_SET_EVENT = "listener.logging.logResultSetEvent";
	public static final String LOG_RESULT_SET_EVENT_OF_MINIMAL_ROW = "listener.logging.logResultSetEventOfMinimalRow";

	public static void init() {
		boolean enabled = AgentConfig.getBooleanProperty(ENABLED, false);
		if (enabled) {
			LoggingJdbcEventListener instance = new LoggingJdbcEventListener();
			instance.logConnectionEvent = AgentConfig.getBooleanProperty(LOG_CONNECTION_EVENT, false);
			instance.logSqlExecution = AgentConfig.getBooleanProperty(LOG_SQL_EXECUTION, false);
			instance.logResultSetEvent = AgentConfig.getBooleanProperty(LOG_RESULT_SET_EVENT, false);
			instance.logSqlExecutionOfMinimalUseTime = Long
					.parseLong(AgentConfig.getProperty(LOG_SQL_EXECUTION_OF_MINIMAL_USE_TIME, "0"));
			instance.logResultSetEventOfMinimalRow = Integer
					.parseInt(AgentConfig.getProperty(LOG_RESULT_SET_EVENT_OF_MINIMAL_ROW, "0"));
			JdbcEventListeners.addListener(instance);
		}
	}

	private boolean logConnectionEvent = true;
	private boolean logSqlExecution = true;
	private boolean logResultSetEvent = true;
	private long logSqlExecutionOfMinimalUseTime = 0;
	private int logResultSetEventOfMinimalRow = 0;

	@Override
	public void onConnectionOpen(Connection conn) {
		if (logConnectionEvent) {
			Logger.log("onConnectionOpen: (connection=" + conn + ")");
		}
	}

	@Override
	public void onConnectionOpenFail(Throwable t) {
		if (logConnectionEvent) {
			Logger.log("onConnectionOpenFail: (exception=" + t.getClass().getName() + ":" + t.getMessage() + ")");
		}
	}

	@Override
	public void onConnectionClose(Connection conn, long connectionOpenTime) {
		if (logConnectionEvent) {
			Logger.log("onConnectionClose: (connection=" + conn + ")(connectionOpenTime=" + connectionOpenTime + ")");
		}
	}

	@Override
	public void onCommit(Connection conn) {
		if (logConnectionEvent) {
			Logger.log("onCommit: (connection=" + conn + ")");
		}
	}

	@Override
	public void onRollback(Connection conn) {
		if (logConnectionEvent) {
			Logger.log("onRollback: (connection=" + conn + ")");
		}
	}

	@Override
	public void beforeExecuteSql(String sql, Object[] params) {
		if (logSqlExecution && logSqlExecutionOfMinimalUseTime <= 0L) {
			Logger.log("beforeExecuteSql: (sql=" + sql + ")(params=" + Arrays.toString(params) + ")");
		}
	}

	@Override
	public void afterExecuteSqlSuccess(String sql, Object[] params, long useTime) {
		if (logSqlExecution && useTime >= logSqlExecutionOfMinimalUseTime) {
			Logger.log("afterExecuteSqlSuccess: (sql=" + sql + ")(params=" + Arrays.toString(params) + ")(useTime="
					+ useTime + ")");
		}
	}

	@Override
	public void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t) {
		if (logSqlExecution && useTime >= logSqlExecutionOfMinimalUseTime) {
			Logger.log("afterExecuteSqlFail: (sql=" + sql + ")(params=" + Arrays.toString(params) + ")(useTime="
					+ useTime + ")(exception=" + t.getClass().getName() + ":" + t.getMessage() + ")");
		}
	}

	@Override
	public void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs) {
		if (logResultSetEvent && rowNum >= logResultSetEventOfMinimalRow) {
			Logger.log("onResultSetNext: (sql=" + sql + ")(params=" + Arrays.toString(params) + ")(rowNum=" + rowNum
					+ ")(resultSet=" + rs + ")");
		}
	}

}
