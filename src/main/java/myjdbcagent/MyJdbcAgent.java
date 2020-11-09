package myjdbcagent;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import myjdbcagent.delegation.ConnectionDelegation;
import myjdbcagent.delegation.DataSourceDelegation;
import myjdbcagent.delegation.ResultSetDelegation;
import myjdbcagent.delegation.StatementDelegation;
import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.listener.LoggingJdbcEventListener;
import myjdbcagent.support.AgentConfig;
import myjdbcagent.support.Logger;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;

/**
 * The MyJdbcAgent premain class.
 */
public class MyJdbcAgent {

	public static void premain(String options, Instrumentation inst) {
		Logger.log("Starting MyJdbcAgent");
		AgentConfig.init();
		Logger.init();
		JdbcEventListeners.addListener(new LoggingJdbcEventListener());
		/*
		 * some connection pool also uses bytecode generation which may produce invalid
		 * but runnable codes. Here we disable bytebuddy validation to prevent error.
		 */
		ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.DISABLED);
		AgentBuilder.Default.of().with(byteBuddy)
				// comment this line if no transformation log (useful for debugging) is needed
				.with(new AgentBuilder.Listener.StreamWriting(Logger.getPrintStream()).withTransformationsOnly())
				.type(named("java.sql.DriverManager")).transform(new DataSourceTransformer())
				.type(isSubTypeOf(DataSource.class).and(not(isInterface()))).transform(new DataSourceTransformer())
				.type(isSubTypeOf(Connection.class).and(not(isInterface()))).transform(new ConnectionTransformer())
				.type(isSubTypeOf(Statement.class).and(not(isInterface()))).transform(new StatementTransformer())
				.type(isSubTypeOf(ResultSet.class).and(not(isInterface()))).transform(new ResultSetTransformer())
				.installOn(inst);
	}

	public static class DataSourceTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			return builder.method(named("getConnection").and(not(isAbstract())))
					.intercept(MethodDelegation.to(DataSourceDelegation.class));
		}
	}

	public static class ConnectionTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			String[] names = new String[] { "createStatement", "prepareStatement", "prepareCall", "commit", "rollback",
					"close" };
			return builder.method(namedOneOf(names).and(not(isAbstract())))
					.intercept(MethodDelegation.to(ConnectionDelegation.class));
		}
	}

	public static class StatementTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			// Intercepts execute methods and set parameter methods of PreparedStatement
			String[] names = new String[] { "execute", "executeQuery", "executeUpdate" };
			return builder.method((not(isAbstract()))
					.and(namedOneOf(names)
							.or(nameStartsWith("set").and(takesArgument(0, Integer.TYPE)).and(takesArgument(1, any()))))
					.and(not(isAbstract()))).intercept(MethodDelegation.to(StatementDelegation.class));
		}
	}

	public static class ResultSetTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			return builder.method(named("next").and(not(isAbstract())))
					.intercept(MethodDelegation.to(ResultSetDelegation.class));
		}
	}

}
