package myjdbcagent.listener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;

public class LoggingJdbcEventListener implements JdbcEventListener {

	@Override
	public void onConnectionOpen(Connection conn) {
		System.out.println("onConnectionOpen: " + conn);
	}

	@Override
	public void onConnectionOpenFail(Throwable t) {
		System.out.println("onConnectionOpenFail: " + t.getClass().getName());
	}

	@Override
	public void onConnectionClose(Connection conn, long connectionOpenTime) {
		System.out.println("onConnectionClose: " + conn);
	}

	@Override
	public void onCommit(Connection conn) {
		System.out.println("onCommit: " + conn);
	}

	@Override
	public void onRollback(Connection conn) {
		System.out.println("onRollback: " + conn);
	}

	@Override
	public void beforeExecuteSql(String sql, Object[] params) {
		System.out.println("beforeExecuteSql: {" + sql + "}" + Arrays.toString(params));
	}

	@Override
	public void afterExecuteSqlSuccess(String sql, Object[] params, long useTime) {
		System.out.println("afterExecuteSqlSuccess: (" + useTime + "){" + sql + "}" + Arrays.toString(params));
	}

	@Override
	public void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t) {
		System.out.println("afterExecuteSqlFail: (" + useTime + "){" + sql + "}" + Arrays.toString(params) + "("
				+ t.getClass().getName() + ": " + t.getMessage() + ")");
	}

	@Override
	public void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs) {
		System.out.println("onResultSetNext: {" + sql + "}" + Arrays.toString(params) + "(" + rowNum + ") of " + rs);
	}

}
