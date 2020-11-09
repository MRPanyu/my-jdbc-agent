package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;

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
