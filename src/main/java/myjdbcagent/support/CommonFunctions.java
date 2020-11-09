package myjdbcagent.support;

import java.sql.SQLException;

public class CommonFunctions {

	public static <T> T rethrowException(Throwable e) throws SQLException {
		if (e instanceof SQLException) {
			throw (SQLException) e;
		} else if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else if (e instanceof Error) {
			throw (Error) e;
		} else {
			throw new RuntimeException(e);
		}
	}

}
