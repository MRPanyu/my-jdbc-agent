package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JdbcEventListeners manages all {@link JdbcEventListener} and calls them in
 * method delegation events.
 * 
 * @author panyu
 *
 */
public class JdbcEventListeners {

	private static List<JdbcEventListener> listeners = new ArrayList<JdbcEventListener>();

	/**
	 * Current initialization process is just call every known JdbcEventListener
	 * implementation classes and let them config themselves, during which they may
	 * (or may not) register themselves into the JdbcEventListeners class.
	 */
	public static void init() {
		LoggingJdbcEventListener.init();
	}

	public synchronized static void addListener(JdbcEventListener listener) {
		listeners.add(listener);
	}

	public static void onConnectionOpen(Connection conn) {
		for (JdbcEventListener listener : listeners) {
			listener.onConnectionOpen(conn);
		}
	}

	public static void onConnectionOpenFail(Throwable t) {
		for (JdbcEventListener listener : listeners) {
			listener.onConnectionOpenFail(t);
		}
	}

	public static void onConnectionClose(Connection conn, long connectionOpenTime) {
		for (JdbcEventListener listener : listeners) {
			listener.onConnectionClose(conn, connectionOpenTime);
		}
	}

	public static void onCommit(Connection conn) {
		for (JdbcEventListener listener : listeners) {
			listener.onCommit(conn);
		}
	}

	public static void onRollback(Connection conn) {
		for (JdbcEventListener listener : listeners) {
			listener.onRollback(conn);
		}
	}

	public static void beforeExecuteSql(String sql, Object[] params) {
		for (JdbcEventListener listener : listeners) {
			listener.beforeExecuteSql(sql, params);
		}
	}

	public static void afterExecuteSqlSuccess(String sql, Object[] params, long useTime) {
		for (JdbcEventListener listener : listeners) {
			listener.afterExecuteSqlSuccess(sql, params, useTime);
		}
	}

	public static void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t) {
		for (JdbcEventListener listener : listeners) {
			listener.afterExecuteSqlFail(sql, params, useTime, t);
		}
	}

	public static void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs) {
		for (JdbcEventListener listener : listeners) {
			listener.onResultSetNext(sql, params, rowNum, rs);
		}
	}

}
