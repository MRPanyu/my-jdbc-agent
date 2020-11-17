package myjdbcagent.delegation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * MethodDelegation class for {@link javax.sql.DataSource} and
 * {@link java.sql.DriverManager}
 * 
 * @author panyu
 *
 */
public class DataSourceDelegation {

	private static ThreadLocal<Boolean> threadLocalInCall = new ThreadLocal<Boolean>();

	/** Delegate method for DataSource.getConnection */
	public static Connection getConnection(@SuperCall Callable<Connection> superCall, @AllArguments Object[] args,
			@This Object thisObj) throws SQLException {
		DataSource dataSource = null;
		if (thisObj != null && thisObj instanceof DataSource) {
			dataSource = (DataSource) thisObj;
		}
		try {
			Boolean inCall = threadLocalInCall.get();
			threadLocalInCall.set(Boolean.TRUE);
			Connection conn = superCall.call();
			// DataSources may wrap each other, call onConnectionOpen only once per get
			if (inCall == null) {
				JdbcEventListeners.onConnectionOpen(dataSource, conn);
				SupportObject.createSupportObject(conn);
			}
			return conn;
		} catch (Throwable e) {
			JdbcEventListeners.onConnectionOpenFail(dataSource, e);
			return CommonFunctions.rethrowException(e);
		} finally {
			threadLocalInCall.remove();
		}
	}

	/** Delegate method for DriverManager.getConnection */
	public static Connection getConnection(@SuperCall Callable<Connection> superCall, @AllArguments Object[] args)
			throws SQLException {
		try {
			Connection conn = superCall.call();
			JdbcEventListeners.onConnectionOpen(null, conn);
			SupportObject.createSupportObject(conn);
			return conn;
		} catch (Throwable e) {
			JdbcEventListeners.onConnectionOpenFail(null, e);
			return CommonFunctions.rethrowException(e);
		} finally {
			threadLocalInCall.remove();
		}
	}

}
