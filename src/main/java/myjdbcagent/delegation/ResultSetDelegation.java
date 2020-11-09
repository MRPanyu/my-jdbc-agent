package myjdbcagent.delegation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.support.CommonFunctions;
import myjdbcagent.support.SupportObject;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * MethodDelegation class for {@link java.sql.ResultSet}
 * 
 * @author panyu
 *
 */
public class ResultSetDelegation {

	public static boolean next(@SuperCall Callable<Boolean> superCall, @This Object thisObj) throws SQLException {
		SupportObject s = SupportObject.getSupportObject(thisObj);
		String sql = null;
		Object[] params = null;
		int rowNum = 0;
		if (s != null) {
			sql = s.getSql();
			params = s.getParamsArray();
			rowNum = s.getRowNum() + 1;
			s.setRowNum(rowNum);
		}
		try {
			boolean returnVal = superCall.call();
			if (returnVal && s != null) {
				JdbcEventListeners.onResultSetNext(sql, params, rowNum, (ResultSet) thisObj);
			}
			return returnVal;
		} catch (Throwable e) {
			return CommonFunctions.rethrowException(e);
		}
	}

}
