package myjdbcagent.delegation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class DataSourceDelegation {

	public static Connection getConnection(@SuperCall Callable<Connection> superCall, @AllArguments Object[] args)
			throws SQLException {
		try {
			Connection conn = superCall.call();
			JdbcEventListeners.onConnectionOpen(conn);
			SupportObject.createSupportObject(conn);
			return conn;
		} catch (Throwable e) {
			JdbcEventListeners.onConnectionOpenFail(e);
			return CommonFunctions.rethrowException(e);
		}
	}

}
