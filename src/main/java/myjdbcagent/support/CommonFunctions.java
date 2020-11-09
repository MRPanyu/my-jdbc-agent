package myjdbcagent.support;

import java.sql.SQLException;

/**
 * Some common functions used by other classes.
 * 
 * @author panyu
 *
 */
public class CommonFunctions {

	/**
	 * Rethrow an exception without unnecessary wrapping.
	 */
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
