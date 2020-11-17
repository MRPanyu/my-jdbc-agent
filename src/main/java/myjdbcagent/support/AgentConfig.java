package myjdbcagent.support;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * The AgentConfig class provides configuration to other classes in this
 * project.
 * <p>
 * It will try to load a "my-jdbc-agent.properties" file in the same directory
 * of the agent jar file.
 * <p>
 * If the file is not found, it will fallback to the default config file in
 * classpath:myjdbcagent/support/my-jdbc-agent-default.properties
 * 
 * @author panyu
 *
 */
public class AgentConfig {

	private static Properties props = new Properties();

	public static void init() {
		try {
			String path = AgentConfig.class.getResource("AgentConfig.class").getPath();
			path = path.substring(0, path.indexOf(".jar"));
			path = path.substring(0, path.lastIndexOf('/'));
			path += "/my-jdbc-agent.properties";
			Logger.log("Loading config from file: " + path);
			InputStream in = null;
			try {
				in = new URL(path).openStream();
				props.load(in);
			} catch (FileNotFoundException e) {
				/* if not found, use the default config */
				Logger.log("Config file not found on: " + path);
				in = AgentConfig.class.getResourceAsStream("my-jdbc-agent-default.properties");
				props.load(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
			StringBuilder sb = new StringBuilder("Starting MyJdbcAgent with configs:");
			sb.append("\n==============================");
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				sb.append("\n").append(entry.getKey()).append("=").append(entry.getValue());
			}
			sb.append("\n==============================");
			Logger.log(sb.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getProperty(String key) {
		return props.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public static boolean getBooleanProperty(String key, boolean defaultValue) {
		String strValue = getProperty(key);
		if (strValue == null) {
			return defaultValue;
		} else {
			return "true".equalsIgnoreCase(strValue.trim());
		}
	}

}
