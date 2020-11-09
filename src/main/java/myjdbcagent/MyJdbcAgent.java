package myjdbcagent;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.instrument.Instrumentation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import myjdbcagent.delegation.ConnectionDelegation;
import myjdbcagent.delegation.DataSourceDelegation;
import myjdbcagent.delegation.PreparedStatementDelegation;
import myjdbcagent.delegation.ResultSetDelegation;
import myjdbcagent.delegation.StatementDelegation;
import myjdbcagent.listener.JdbcEventListeners;
import myjdbcagent.listener.LoggingJdbcEventListener;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;

public class MyJdbcAgent {

	public static void premain(String options, Instrumentation inst) {
		System.out.println("[MyJdbcAgent] Starting MyJdbcAgent");
		JdbcEventListeners.addListener(new LoggingJdbcEventListener());
		ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.DISABLED);
		AgentBuilder.Default.of().with(byteBuddy)
				.with(new AgentBuilder.Listener.StreamWriting(System.out).withTransformationsOnly())
				.type(named("java.sql.DriverManager")).transform(new DataSourceTransformer())
				.type(isSubTypeOf(DataSource.class).and(not(isInterface()))).transform(new DataSourceTransformer())
				.type(isSubTypeOf(Connection.class).and(not(isInterface()))).transform(new ConnectionTransformer())
				.type(isSubTypeOf(Statement.class).and(not(isSubTypeOf(PreparedStatement.class)))
						.and(not(isInterface())))
				.transform(new StatementTransformer())
				.type(isSubTypeOf(PreparedStatement.class).and(not(isInterface())))
				.transform(new PreparedStatementTransformer())
				.type(isSubTypeOf(ResultSet.class).and(not(isInterface()))).transform(new ResultSetTransformer())
				.installOn(inst);
	}

	public static class DataSourceTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			System.out.println("[MyJdbcAgent] Transforming DataSource class: " + typeDescription.getCanonicalName());
			return builder.method(named("getConnection").and(not(isAbstract())))
					.intercept(MethodDelegation.to(DataSourceDelegation.class));
		}
	}

	public static class ConnectionTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			System.out.println("[MyJdbcAgent] Transforming Connection class: " + typeDescription.getCanonicalName());
			String[] names = new String[] { "prepareStatement", "prepareCall", "commit", "rollback", "close" };
			return builder.method(namedOneOf(names).and(not(isAbstract())))
					.intercept(MethodDelegation.to(ConnectionDelegation.class));
		}
	}

	public static class StatementTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			System.out.println("[MyJdbcAgent] Transforming Statement class: " + typeDescription.getCanonicalName());
			String[] names = new String[] { "execute", "executeQuery", "executeUpdate" };
			return builder.method(namedOneOf(names).and(not(isAbstract())))
					.intercept(MethodDelegation.to(StatementDelegation.class));
		}
	}

	public static class PreparedStatementTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			System.out.println(
					"[MyJdbcAgent] Transforming PreparedStatement class: " + typeDescription.getCanonicalName());
			String[] names = new String[] { "execute", "executeQuery", "executeUpdate" };
			return builder.method((not(isAbstract()))
					.and(namedOneOf(names)
							.or(nameStartsWith("set").and(takesArgument(0, Integer.TYPE)).and(takesArgument(1, any()))))
					.and(not(isAbstract()))).intercept(MethodDelegation.to(PreparedStatementDelegation.class));
		}
	}

	public static class ResultSetTransformer implements Transformer {
		@Override
		public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
				JavaModule module) {
			System.out.println("[MyJdbcAgent] Transforming ResultSet class: " + typeDescription.getCanonicalName());
			return builder.method(named("next").and(not(isAbstract())))
					.intercept(MethodDelegation.to(ResultSetDelegation.class));
		}
	}

}
