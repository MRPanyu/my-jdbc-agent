package myjdbcagent.delegation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * MethodDelegation class for {@link javax.sql.DataSource} and
 * {@link java.sql.DriverManager}
 * 
 * @author panyu
 *
 */
public class DataSourceDelegation {

	private static ThreadLocal<Boolean> threadLocalInCall = new ThreadLocal<Boolean>();

	public static Connection getConnection(@SuperCall Callable<Connection> superCall, @AllArguments Object[] args)
			throws SQLException {
		try {
			Boolean inCall = threadLocalInCall.get();
			threadLocalInCall.set(Boolean.TRUE);
			Connection conn = superCall.call();
			// DataSources may wrap each other, call onConnectionOpen only once per get
			if (inCall != null) {
				JdbcEventListeners.onConnectionOpen(conn);
				SupportObject.createSupportObject(conn);
			}
			return conn;
		} catch (Throwable e) {
			JdbcEventListeners.onConnectionOpenFail(e);
			return CommonFunctions.rethrowException(e);
		} finally {
			threadLocalInCall.remove();
		}
	}

}
