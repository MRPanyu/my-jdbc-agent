package myjdbcagent.listener;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import myjdbcagent.support.AgentConfig;
import myjdbcagent.support.Logger;

/**
 * This JdbcEventListener trys to detect connection leak.
 * <p>
 * A connection which is opened without close for a certain amount of time is
 * considered "leak". This listener stores connection with it's opening
 * timestamp in a map, then starts a {@link Timer} which checks for leaked
 * connections on a fixed interval.
 * 
 * @author panyu
 *
 */
public class LeakDetectionJdbcEventListener implements JdbcEventListener {

	public static final String ENABLED = "listener.leakDetection.enabled";
	public static final String LEAK_DATASOURCE_TYPE = "listener.leakDetection.dataSourceType";
	public static final String LEAK_TIME_THRESHOLD = "listener.leakDetection.timeThreshold";
	public static final String LEAK_DETECTION_INTERVAL = "listener.leakDetection.detectionInterval";

	public static void init() {
		boolean enabled = AgentConfig.getBooleanProperty(ENABLED, false);
		if (enabled) {
			LeakDetectionJdbcEventListener instance = new LeakDetectionJdbcEventListener();
			instance.leakDataSourceType = AgentConfig.getProperty(LEAK_DATASOURCE_TYPE);
			instance.leakTimeThreshold = Long.parseLong(AgentConfig.getProperty(LEAK_TIME_THRESHOLD, "300000"));
			instance.leakDetectionInterval = Long.parseLong(AgentConfig.getProperty(LEAK_DETECTION_INTERVAL, "30000"));
			instance.startDetection(); // start the timer for leak detection
			JdbcEventListeners.addListener(instance);
		}
	}

	protected static class ConnectionInfo {
		private Connection conn;
		private long createTime;
		private StackTraceElement[] createStackTrace;

		public Connection getConn() {
			return conn;
		}

		public void setConn(Connection conn) {
			this.conn = conn;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}

		public StackTraceElement[] getCreateStackTrace() {
			return createStackTrace;
		}

		public void setCreateStackTrace(StackTraceElement[] createStackTrace) {
			this.createStackTrace = createStackTrace;
		}

	}

	private String leakDataSourceType = null;
	private long leakTimeThreshold = 0;
	private long leakDetectionInterval = 0;

	private WeakHashMap<Connection, ConnectionInfo> connectionMap = new WeakHashMap<Connection, LeakDetectionJdbcEventListener.ConnectionInfo>();

	protected void startDetection() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				List<ConnectionInfo> detected = new ArrayList<ConnectionInfo>();
				long now = System.currentTimeMillis();
				synchronized (LeakDetectionJdbcEventListener.this) {
					for (ConnectionInfo info : connectionMap.values()) {
						if (now - info.getCreateTime() >= leakTimeThreshold) {
							detected.add(info);
						}
					}
				}
				Logger.log("Checking connection leak: connections_count=" + connectionMap.size() + ", leak_count="
						+ detected.size());
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				for (ConnectionInfo info : detected) {
					StringWriter sw = new StringWriter();
					sw.append("Possible connection leak detected: ").append(info.getConn().toString());
					sw.append("\nOpened since: ").append(format.format(info.getCreateTime())).append(" (")
							.append(String.valueOf(now - info.getCreateTime())).append("ms)");
					sw.append("\nCreated: ");
					boolean usefulStackTrace = false;
					for (StackTraceElement s : info.getCreateStackTrace()) {
						String className = s.getClassName();
						/*
						 * we don't need to print the first 3 or 4 stacktraces which is
						 * Thread.getStackTrace() or method delegations
						 */
						if (!usefulStackTrace && !"java.lang.Thread".equals(className)
								&& !className.startsWith("myjdbcagent")) {
							usefulStackTrace = true;
						}
						if (usefulStackTrace) {
							sw.append("\n\tat ").append(className).append(".").append(s.getMethodName()).append("(")
									.append(s.getFileName()).append(":").append(String.valueOf(s.getLineNumber()))
									.append(")");
						}
					}
					Logger.log(sw.toString());
				}
			}
		};
		new Timer("MyJdbcAgent_LeakDetection", true).scheduleAtFixedRate(task, 0L, leakDetectionInterval);
	}

	@Override
	public void onConnectionOpen(DataSource dataSource, Connection conn) {
		boolean isCheck = true;
		if (leakDataSourceType != null && !leakDataSourceType.trim().isEmpty()) {
			if (dataSource == null || !dataSource.getClass().getName().startsWith(leakDataSourceType)) {
				isCheck = false;
			}
		}
		if (isCheck) {
			ConnectionInfo info = new ConnectionInfo();
			info.setConn(conn);
			info.setCreateTime(System.currentTimeMillis());
			info.setCreateStackTrace(Thread.currentThread().getStackTrace());
			synchronized (this) {
				connectionMap.put(conn, info);
			}
		}
	}

	@Override
	public void onConnectionOpenFail(DataSource dataSource, Throwable t) {
		// unused
	}

	@Override
	public void onConnectionClose(Connection conn, long connectionOpenTime) {
		synchronized (this) {
			connectionMap.remove(conn);
		}
	}

	@Override
	public void onCommit(Connection conn) {
		// unused
	}

	@Override
	public void onRollback(Connection conn) {
		// unused
	}

	@Override
	public void beforeExecuteSql(String sql, Object[] params) {
		// unused
	}

	@Override
	public void afterExecuteSqlSuccess(String sql, Object[] params, long useTime) {
		// unused
	}

	@Override
	public void afterExecuteSqlFail(String sql, Object[] params, long useTime, Throwable t) {
		// unused
	}

	@Override
	public void onResultSetNext(String sql, Object[] params, int rowNum, ResultSet rs) {
		// unused
	}

}
