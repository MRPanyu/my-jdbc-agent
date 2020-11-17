# my-jdbc-agent README

## 1. 工程综述

工作中接触到不少类型的工程项目，其使用的JDBC数据源也各有千秋，如中间件提供的（比如tomcat的dbcp或tomcat-jdbc数据源，weblogic中提供的各种连接池数据源等），工程中自己配置的（如常用的Druid数据源或者spring-boot类工程常用的HikariCP等）。

排查系统问题的时候，有时会想要把某些数据源的某种功能（比如tomcat-jdbc中的连接泄露检测等）搬到其他数据源上使用，但限于系统部署方式等等情况，要更换一个数据源类型，或者用其他类型的连接池进行包装，往往又要涉及程序调整，打包更新等繁琐操作。然后就想到是否可以用javaagent增强的方式，对现有的数据源进行一些字节码增强，来实现自己想要的各种监听功能。

这个工程就是按这种思路进行开发的，通过javaagent的字节码增强在JDBC API上增加一些监听，做成一个基本框架，然后可以自己根据想要的功能来增加各种监听器组件，从而尽量不依赖具体数据源实现来实现一些通用的监控、排查功能。

工程本身使用bytebuddy进行字节码增强，再使用maven-shade-plugin进行包名转换避免依赖冲突，本身应该不会与任何中间件或工程中的jar包产生依赖冲突。

## 2. 现有功能使用

现有工程里面包含了以下两个功能：

1. JDBC各种事件的日志输出
2. 连接泄露检测

简单的使用方法：

1. 将工程 `mvn package` 打包生成 my-jdbc-agent.jar 包
2. 将这个jar包放到任意目录下面，下面举例为 /ag_home 目录
3. 将工程中 *src/resources/myjdbcagent/support/my-jdbc-agent-default.properties* 复制到 */ag_home/my-jdbc-agent.properties*，用于修改配置
4. 修改应用或中间件的启动脚本，增加 `-javaagent:/ag_home/my-jdbc-agent.jar` VM参数（根据具体的启动脚本，VM参数指`-D`类参数或者`-Xmx`之类的参数，加在这些参数相同的位置），然后重新启动

配置文件的中文说明：

```properties
# --- 监听器配置 ---

# 是否进行SQL日志输出
listener.logging.enabled=true
# 是否输出Connection事件 (open, close, commit, rollback)
listener.logging.logConnectionEvent=true
# 是否输出SQL执行信息 (executeQuery, executeUpdate)
listener.logging.logSqlExecution=true
# 如果开启了SQL执行信息输出，只输出执行时间超过这个毫秒数的SQL（用于仅输出慢SQL）
listener.logging.logSqlExecutionOfMinimalUseTime=0
# 是否输出ResultSet.next()相关信息
listener.logging.logResultSetEvent=true
# 如果开启了ResultSet.next()输出，只输出行数大于这个的信息（比如这里配置10000，则一个ResultSet前9999次next是不输出信息的，用于检测大ResultSet提取的信息）
listener.logging.logResultSetEventOfMinimalRow=0

# 是否开启连接泄露检查
listener.leakDetection.enabled=true
# 连接泄露检查对应的数据源类名 (例如 com.zaxxer.hikari.HikariDataSource) 
# 有些连接池会自己缓存一部分连接（如HikariDataSource），如果监听所有DataSource类型的打开连接事件，很可能会误报连接泄露（实际为外层包装的连接池在缓存着这些连接）
listener.leakDetection.dataSourceType=
# 多少毫秒未关闭的连接，认为是连接泄露
listener.leakDetection.timeThreshold=300000
# 每多少毫秒进行一次连接泄露检查（定时任务间隔）
listener.leakDetection.detectionInterval=30000

# --- 日志输出配置 ---
# 是否输出到System.out
logger.logToSysout=true
# 是否输出到文件
logger.logToFile=false
# 如果输出到文件为true，输出的位置（如果是相对路径是相对于应用启动目录，不是my-jdbc-agent.jar的目录）
logger.logToFilePath=./my-jdbc-agent.log
```

## 3. 工程扩展

现有的功能主要还是一个示例作用，主要是在现有的框架上可以再自己写各种监听器进行扩展。

工程中最主要的扩展接口为 *myjdbcagent.listener.JdbcEventListener* ，这个接口包含了多个回调方法：

- onConnectionOpen：从数据源或DriverManager获取连接成功时回调
- onConnectionOpenFail：从数据源或DriverManager获取连接异常时回调
- onConnectionClose：连接关闭时回调
- onCommit：`Connection.commit()`时回调
- onRollback：`Connection.rollback()`时回调
- beforeExecuteSql：任意sql语句执行前回调，包括`executeQuery`和`executeUpdate`
- afterExecuteSqlSuccess：任意sql语句执行返回后回调
- afterExecuteSqlFail：任意sql语句执行异常时回调
- onResultSetNext：`ResultSet.next()`时回调

工程中现有的 *myjdbcagent.listener.LoggingJdbcEventListener* 和 *myjdbcagent.listener.LeakDetectionJdbcEventListener* 是两个例子。

创建了新的 *JdbcEventListener* 实现类以后，可以参考现有的两个实现类，通过静态 *init()* 方法进行配置读取并注册到 *myjdbcagent.listener.JdbcEventListeners* ，然后在 *JdbcEventListeners.init()* 方法中也增加一个新实现类的调用，使程序初始化时可以找到这个新实现类。

## 4. 工程架构

工程本身目前就几个类不是很复杂：

- myjdbcagent.MyJdbcAgent：工程的premain类，包含了bytebuddy字节码增强声明的主要代码
- myjdbcagent.support：这个包下面的类提供各种辅助功能
    - AgentConfig：提供读取配置文件的功能
    - CommonFunctions：其他共用方法
    - Logger：日志输出工具类，目前可以根据配置文件输出到System.out或一个固定路径的文件
    - SupportObject：作为Connection/Statements等的辅助对象，用于存储一些额外数据，如创建PreparedStatement时的sql等。
- delegetion：这个包下面集中了bytebuddy对于各个类型的代理方法
- listener：监听器API以及现在的几个具体实现类
