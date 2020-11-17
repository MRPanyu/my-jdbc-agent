package myjdbcagent.support;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * The static Logger class which logs to standard output and/or a file with a
 * default format.
 * <p>
 * Setting config values in my-jdbc-agent.properties to control the output
 * target.
 * 
 * @author panyu
 *
 */
public class Logger {

	public static final String LOG_TO_SYSOUT = "logger.logToSysout";
	public static final String LOG_TO_FILE = "logger.logToFile";
	public static final String LOG_TO_FILE_PATH = "logger.logToFilePath";

	private static PrintStream out = System.out;

	public static void init() {
		try {
			boolean logToSysout = AgentConfig.getBooleanProperty(LOG_TO_SYSOUT, true);
			boolean logToFile = AgentConfig.getBooleanProperty(LOG_TO_FILE, false);
			String logToFilePath = AgentConfig.getProperty(LOG_TO_FILE_PATH);
			if (logToSysout && logToFile) {
				PrintStream fout = new PrintStream(logToFilePath);
				out = new TeePrintStream(System.out, fout);
			} else if (logToFile) {
				out = new PrintStream(logToFilePath);
			}
		} catch (Exception e) {
			out = System.out;
			e.printStackTrace(out);
		}
	}

	public static PrintStream getPrintStream() {
		return out;
	}

	public static void log(String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("[MyJdbcAgent][")
				.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())).append("][")
				.append(Thread.currentThread().getName()).append("]: ").append(message);
		out.println(sb.toString());
	}

	public static void log(String message, Throwable t) {
		log(message);
		t.printStackTrace(out);
	}

	/** Just a very stupid method to "tee" a PrintStream */
	private static class TeePrintStream extends PrintStream {

		private PrintStream out1;
		private PrintStream out2;

		public TeePrintStream(PrintStream out1, PrintStream out2) {
			super(out1);
			this.out1 = out1;
			this.out2 = out2;
		}

		@Override
		public void flush() {
			out1.flush();
			out2.flush();
		}

		@Override
		public void close() {
			out1.close();
			out2.close();
		}

		@Override
		public boolean checkError() {
			return out1.checkError() || out2.checkError();
		}

		@Override
		public void write(int b) {
			out1.write(b);
			out2.write(b);
		}

		@Override
		public void write(byte[] buf, int off, int len) {
			out1.write(buf, off, len);
			out2.write(buf, off, len);
		}

		@Override
		public void print(boolean b) {
			out1.print(b);
			out2.print(b);
		}

		@Override
		public void print(char c) {
			out1.print(c);
			out2.print(c);
		}

		@Override
		public void print(int i) {
			out1.print(i);
			out2.print(i);
		}

		@Override
		public void print(long l) {
			out1.print(l);
			out2.print(l);
		}

		@Override
		public void print(float f) {
			out1.print(f);
			out2.print(f);
		}

		@Override
		public void print(double d) {
			out1.print(d);
			out2.print(d);
		}

		@Override
		public void print(char[] s) {
			out1.print(s);
			out2.print(s);
		}

		@Override
		public void print(String s) {
			out1.print(s);
			out2.print(s);
		}

		@Override
		public void print(Object obj) {
			out1.print(obj);
			out2.print(obj);
		}

		@Override
		public void println() {
			out1.println();
			out2.println();
		}

		@Override
		public void println(boolean x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(char x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(int x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(long x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(float x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(double x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(char[] x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(String x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public void println(Object x) {
			out1.println(x);
			out2.println(x);
		}

		@Override
		public PrintStream printf(String format, Object... args) {
			out1.printf(format, args);
			out2.printf(format, args);
			return this;
		}

		@Override
		public PrintStream printf(Locale l, String format, Object... args) {
			out1.printf(l, format, args);
			out2.printf(l, format, args);
			return this;
		}

		@Override
		public PrintStream format(String format, Object... args) {
			out1.format(format, args);
			out2.format(format, args);
			return this;
		}

		@Override
		public PrintStream format(Locale l, String format, Object... args) {
			out1.format(l, format, args);
			out2.format(l, format, args);
			return this;
		}

		@Override
		public PrintStream append(CharSequence csq) {
			out1.append(csq);
			out2.append(csq);
			return this;
		}

		@Override
		public PrintStream append(CharSequence csq, int start, int end) {
			out1.append(csq, start, end);
			out2.append(csq, start, end);
			return this;
		}

		@Override
		public PrintStream append(char c) {
			out1.append(c);
			out2.append(c);
			return this;
		}

		@Override
		public void write(byte[] b) throws IOException {
			out1.write(b);
			out2.write(b);
		}

	}

}
