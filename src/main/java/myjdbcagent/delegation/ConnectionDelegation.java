package myjdbcagent.delegation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * MethodDelegation class for {@link java.sql.Connection}
 * 
 * @author panyu
 *
 */
public class ConnectionDelegation {

	public static Statement createStatement(@SuperCall Callable<PreparedStatement> superCall, @This Object thisObj)
			throws SQLException {
		try {
			Statement st = superCall.call();
			if (SupportObject.hasSupportObject(thisObj)) {
				SupportObject.createSupportObject(st);
			}
			return st;
		} catch (Throwable e) {
			return CommonFunctions.rethrowException(e);
		}
	}

	public static PreparedStatement prepareStatement(@Argument(0) String sql,
			@SuperCall Callable<PreparedStatement> superCall, @This Object thisObj) throws SQLException {
		try {
			PreparedStatement ps = superCall.call();
			if (SupportObject.hasSupportObject(thisObj)) {
				SupportObject s = SupportObject.createSupportObject(ps);
				s.setSql(sql);
			}
			return ps;
		} catch (Throwable e) {
			return CommonFunctions.rethrowException(e);
		}
	}

	public static CallableStatement prepareCall(@Argument(0) String sql,
			@SuperCall Callable<CallableStatement> superCall, @This Object thisObj) throws SQLException {
		try {
			CallableStatement ps = superCall.call();
			if (SupportObject.hasSupportObject(thisObj)) {
				SupportObject s = SupportObject.createSupportObject(ps);
				s.setSql(sql);
			}
			return ps;
		} catch (Throwable e) {
			return CommonFunctions.rethrowException(e);
		}
	}

	public static void commit(@SuperCall Callable<Void> superCall, @This Connection conn) throws SQLException {
		try {
			if (SupportObject.hasSupportObject(conn)) {
				JdbcEventListeners.onCommit(conn);
			}
			superCall.call();
		} catch (Throwable e) {
			CommonFunctions.rethrowException(e);
		}
	}

	public static void rollback(@SuperCall Callable<Void> superCall, @This Connection conn) throws SQLException {
		try {
			if (SupportObject.hasSupportObject(conn)) {
				JdbcEventListeners.onRollback(conn);
			}
			superCall.call();
		} catch (Throwable e) {
			CommonFunctions.rethrowException(e);
		}
	}

	public static void close(@SuperCall Callable<Void> superCall, @This Connection conn) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(conn);
		try {
			if (s != null) {
				JdbcEventListeners.onConnectionClose(conn, System.currentTimeMillis() - s.getCreateTime());
			}
			superCall.call();
		} catch (Throwable e) {
			CommonFunctions.rethrowException(e);
		}
	}

}
