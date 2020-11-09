package myjdbcagent.delegation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

public class PreparedStatementDelegation extends StatementDelegation {

	public static void setNull(@Argument(0) int parameterIndex, @Argument(1) int sqlType,
			@SuperCall Callable<Void> superCall, @This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		if (s != null) {
			ArrayList<Object> params = s.getParams();
			while (params.size() < parameterIndex) {
				params.add(null);
			}
		}
		try {
			superCall.call();
		} catch (Throwable e) {
			CommonFunctions.rethrowException(e);
		}
	}

	public static void setAnyParam(@Argument(0) int parameterIndex, @Argument(1) Object parameterValue,
			@SuperCall Callable<Void> superCall, @This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		if (s != null) {
			ArrayList<Object> params = s.getParams();
			while (params.size() < parameterIndex) {
				params.add(null);
			}
			params.set(parameterIndex - 1, parameterValue);
		}
		try {
			superCall.call();
		} catch (Throwable e) {
			CommonFunctions.rethrowException(e);
		}
	}

	public static ResultSet executeQuery(@SuperCall Callable<ResultSet> superCall, @This Object thisObj)
			throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		String sql = null;
		Object[] paramsArr = null;
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				sql = s.getSql();
				paramsArr = s.getParamsArray();
				JdbcEventListeners.beforeExecuteSql(sql, paramsArr);
				startTime = System.currentTimeMillis();
			}
			ResultSet rs = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, paramsArr, useTime);
				SupportObject rss = SupportObject.createSupportObject(rs);
				rss.copyProperties(s);
			}
			return rs;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, paramsArr, useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}

	public static int executeUpdate(@SuperCall Callable<Integer> superCall, @This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		String sql = null;
		Object[] paramsArr = null;
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				sql = s.getSql();
				paramsArr = s.getParamsArray();
				JdbcEventListeners.beforeExecuteSql(sql, paramsArr);
				startTime = System.currentTimeMillis();
			}
			Integer returnVal = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, paramsArr, useTime);
			}
			return returnVal;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, paramsArr, useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}

	public static boolean execute(@SuperCall Callable<Boolean> superCall, @This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		String sql = null;
		Object[] paramsArr = null;
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				sql = s.getSql();
				paramsArr = s.getParamsArray();
				JdbcEventListeners.beforeExecuteSql(sql, paramsArr);
				startTime = System.currentTimeMillis();
			}
			Boolean returnVal = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, paramsArr, useTime);
				if (returnVal) {
					ResultSet rs = ((PreparedStatement) thisObj).getResultSet();
					SupportObject rss = SupportObject.createSupportObject(rs);
					rss.copyProperties(s);
				}
			}
			return returnVal;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, paramsArr, useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}
	
	public static ResultSet executeQuery(@Argument(0) String sql, @SuperCall Callable<ResultSet> superCall,
			@This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				JdbcEventListeners.beforeExecuteSql(sql, new Object[0]);
				startTime = System.currentTimeMillis();
			}
			ResultSet rs = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, new Object[0], useTime);
				SupportObject rss = SupportObject.createSupportObject(rs);
				rss.setSql(sql);
			}
			return rs;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, new Object[0], useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}

	public static int executeUpdate(@Argument(0) String sql, @SuperCall Callable<Integer> superCall,
			@This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				JdbcEventListeners.beforeExecuteSql(sql, new Object[0]);
				startTime = System.currentTimeMillis();
			}
			Integer returnVal = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, new Object[0], useTime);
			}
			return returnVal;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, new Object[0], useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}

	public static boolean execute(@Argument(0) String sql, @SuperCall Callable<Boolean> superCall, @This Object thisObj)
			throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		long startTime = 0;
		long useTime = 0;
		try {
			if (s != null) {
				JdbcEventListeners.beforeExecuteSql(sql, new Object[0]);
				startTime = System.currentTimeMillis();
			}
			Boolean returnVal = superCall.call();
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlSuccess(sql, new Object[0], useTime);
				if (returnVal) {
					ResultSet rs = ((Statement) thisObj).getResultSet();
					SupportObject rss = SupportObject.createSupportObject(rs);
					rss.setSql(sql);
				}
			}
			return returnVal;
		} catch (Throwable e) {
			if (s != null) {
				useTime = System.currentTimeMillis() - startTime;
				JdbcEventListeners.afterExecuteSqlFail(sql, new Object[0], useTime, e);
			}
			return CommonFunctions.rethrowException(e);
		}
	}

}
