package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;

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

	public void onConnectionOpen(Connection conn);

	public void onConnectionOpenFail(Throwable t);

	public void onConnectionClose(Connection conn, long connectionOpenTime);

	public void onCommit(Connection conn);

	public void onRollback(Connection conn);

	public void beforeExecuteSql(String sql, Object[] params);

	public void afterExecuteSqlSuccess(String sql, Object[] params, long useTime);

	public void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t);

	public void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs);

}
