package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

/**
 * A JdbcEventListener is called by method delegations and can do any arbitrary
 * work such as logging or connection leak detection.
 * <p>
 * Must register itself in {@link JdbcEventListeners} to take effect.
 * 
 * @author panyu
 *
 */
public interface JdbcEventListener {

	/**
	 * Callback method when a connection is opened.
	 * 
	 * @param dataSource Related DataSource, or null if this connection is opened by
	 *                   {@link java.sql.DriverManager}.
	 * @param conn       The opened Connection object.
	 */
	public void onConnectionOpen(DataSource dataSource, Connection conn);

	/**
	 * Callback method when a connection open throws any exception.
	 * 
	 * @param dataSource Related DataSource, or null if the connection is opened by
	 *                   {@link java.sql.DriverManager}.
	 * @param t          The exception thrown.
	 */
	public void onConnectionOpenFail(DataSource dataSource, Throwable t);

	/**
	 * Callback method when a connection is closed.
	 * 
	 * @param conn               The closed Connection object.
	 * @param connectionOpenTime Time in milliseconds since the connection has
	 *                           opened.
	 */
	public void onConnectionClose(Connection conn, long connectionOpenTime);

	/**
	 * Callback method when a connection commits.
	 */
	public void onCommit(Connection conn);

	/**
	 * Callback method when a connection rollbacks.
	 */
	public void onRollback(Connection conn);

	/**
	 * Callback method when a Statement/PreparedStatement/CallableStatement will
	 * execute a sql.
	 * 
	 * @param sql    The sql to execute.
	 * @param params Sql parameters.
	 */
	public void beforeExecuteSql(String sql, Object[] params);

	/**
	 * Callback method when a sql is executed and either a ResultSet or a row
	 * updated count is returned.
	 * 
	 * @param sql     The sql executed.
	 * @param params  Sql parameters.
	 * @param useTime Time in milliseconds since the sql start to execute.
	 */
	public void afterExecuteSqlSuccess(String sql, Object[] params, long useTime);

	/**
	 * Callback method when a sql is executed and any exception is thrown.
	 * 
	 * @param sql     The sql executed.
	 * @param params  Sql parameters.
	 * @param useTime Time in milliseconds since the sql start to execute.
	 * @param t       The exception thrown.
	 */
	public void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t);

	/**
	 * Callback method when a ResultSet.next() is called.
	 * 
	 * @param sql    The sql executed.
	 * @param params Sql parameters.
	 * @param rowNum The current row number of this ResultSet
	 * @param rs     The ResultSet object.
	 */
	public void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs);

}
