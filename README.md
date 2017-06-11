# FoolishGoat-Server

## 愿景
从事 Java Web 快两年多了，天天围绕着 C-U-R-D ，每天重复着代码，重复着逻辑。面对 Struts Spring Hibernate Mybatis 等等复杂的框架，我想解脱出来。虽说他们承载着前人的智慧精华，但是如果我想要透彻的使用这些东西，所花费的精力几乎不亚于重新走一遍这些框架的创造过程。数据量相对较为小的网站与 APP ，根本用不到 S-S-H-M，用了反而不灵活了。而且现在前后端开始分离，后端需要专注服务即可，那么后端所谓的 MVC 也拆开好了。(M) 数据持久化层就交给数据库自己，(V) 前端这些年发着这么好，根本用不着服务端渲染，(C) 逻辑控制提供Restful API 就好了。

以上只是本人的粗浅的想法，企业级的开发毕竟需要 S-S-H-M 这类框架。但是我只是想做一个数据量小到未知的APP *OR* 博客 *OR* 微信小程序，用这些东西太重了。光是部署和调试好一个稳定的环境，就用了我大量的时间。最终下定决心，自己写一个适合自己思维习惯的东西，哪怕他蠢得要死，慢的要死，那也是我亲生的。

## 依赖

> 为了精简，本项目尽量依赖较少的包。提供 Http 服务而且要精简，那么 Jetty 是很好的选择。第二是C3P0 数据库链接池，用了都说好！最后还用了 org.json 进行 json 数据的转换。

```xml

		<!-- 测试框架 本人压根没写测试用例 （🤦‍♀️） -->
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		<!-- jetty server 核心 -->
		<dependency>
			<groupId>org.eclipse.jetty</groupId>
			<artifactId>jetty-server</artifactId>
			<version>9.4.5.v20170502</version>
		</dependency>
		<!-- mysql jdbc -->
		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<version>5.1.34</version>
		</dependency>
		<!-- c3p0数据源 -->
		<dependency>
			<groupId>com.mchange</groupId>
			<artifactId>c3p0</artifactId>
			<version>0.9.5-pre10</version>
		</dependency>
		<!-- json -->
		<dependency>
			<groupId>org.json</groupId>
			<artifactId>json</artifactId>
			<version>20160810</version>
		</dependency>

```
## 编译项目

可以使用命令编译项目

`mvn clean assembly:assembly `

***target*** 文件夹下会生成一个 ***.zip*** 文件。里面的目录结构是类似 ***Tomcat*** 的文件目录。

## 配置

`./conf` 文件夹下有三个配置文件

1. 	`c3p0.properties` 顾名思义，c3p0 数据库连接池的配置文件，详情可 [点击这里](https://github.com/swaldman/c3p0)
```
c3p0.driverClass=com.mysql.jdbc.Driver
c3p0.jdbcUrl=jdbc:mysql://localhost:3307/ty_bundle_db?useUnicode=true&characterEncoding=utf8
c3p0.user=root
c3p0.password=123456
c3p0.initialPoolSize=1
c3p0.minPoolSize=1
c3p0.maxPoolSize=2
c3p0.acquireIncrement=3
c3p0.maxIdleTime=1000
c3p0.acquireRetryAttempts=30
c3p0.acquireRetryDelay=1000

```
2. `server.properties` 本 Server 的配置文件
```
# server config （所有文件的路径全部使用相对地址，根地址为服务器当前解压地址）
# 开放的端口
server.port=8888
# 提供 https 的秘钥文件地址
server.cert=conf/keystore
# 秘钥文件密码
server.keystore.password=123456
server.keymanager.password=123456
# 是否开启https
server.https=true
# 静态文件的访问地址
server.app.path=work
# 路由，数据处理器的配置（下面详细说明）
server.resource.conf=conf/foolishgoat.conf.json
```
3. 	`foolishgoat.conf.json` 路由以及处理器配置大体如下。
```

{
	"processors":[
		{
			"name":"validate",
			"class":"com.foolishgoat.server.processor.ValidateProcessor"
		},
		{
			"name":"table_curd",
			"class":"com.foolishgoat.server.processor.TableCURDProcessor"
		},
		{
			"name":"utf8",
			"class":"com.foolishgoat.server.processor.UTF8Processor"
		},
		{
			"name":"router",
			"class":"com.foolishgoat.server.processor.RouterProcessor"
		}
	],
	"routers":[
		{
			"name":"/routers",
			"method":"GET",
			"processors":"utf8=>validate=>router",
			"resourceName":"test",
			"sql":"",
			"validators":{}
		}
	]
}

```
> 通常情况下，将数据库配置好后，即可启动

## 已经完成的功能

1. 为数据库表格提供 restful api.
2. 分页查询
2. Request 参数的格式校验
3. 静态资源文件获取(.html,.css,.js等)。(Jetty ResourceHandler 实现)
4. 只支持使用JKS格式证书提供 https 。

## 准备完成

1. 支持接入 OAuth2.0 认证

## 一些我瞎想的概念

1. Router 大部分情况下请求具有一定的共性，这里将他们提炼出来，Router 里面有名字，方法，数据处理器链(ProcessorChain),资源名称（目前对应着数据库表），数据校验器。

2. Processor  反正 http 协议只是请求 Url 加一些参数，然后返回结果。大部分的处理都是可以复用的，那不论是 Controller，Filter 都是在处理 Request，Response 里面的数据。用任务链模式，将这些实现了 Processor 接口的类串起来，每个 Router下配置一个类似 *utf8=>validate=>router*  的链子，请求到达后，按照这个链依次执行，这样配置起来能直观表达自己的处理逻辑。他的生命周期，就是 Jetty 中 Handler 的生命周期。

## 开源协议

MIT


