# FoolishGoat-Server

## 愿景
从事 Java Web 快两年多了，天天围绕着 C-U-R-D ，每天重复着代码，重复着逻辑。面对 Struts Spring Hibernate Mybatis 等等复杂的框架，我想解脱出来。虽说他们承载着前人的智慧精华，但是如果我想要透彻的使用这些东西，所花费的精力几乎不亚于重新走一遍这些框架的创造过程。数据量相对较为小的网站与 APP ，根本用不到 S-S-H-M，用了反而不灵活了。而且现在前后端开始分离，后端需要专注服务即可，那么后端所谓的 MVC 也拆开好了。(M) 数据持久化层就交给数据库自己，(V) 前端这些年发着这么好，根本用不着服务端渲染，(C) 逻辑控制提供Restful API 就好了。

以上只是本人的粗浅的想法，企业级的开发毕竟需要 S-S-H-M 这类框架。但是我只是想做一个数据量小到未知的APP *OR* 博客 *OR* 微信小程序，用这些东西太重了。光是部署和调试好一个稳定的环境，就用了我大量的时间。最终下定决心，自己写一个适合自己思维习惯的东西，哪怕他蠢得要死，慢的要死，那也是我亲生的。

## 依赖

> 为了精简，本项目尽量依赖较少的包，提供 Http 服务 Jetty 是很好的选择。C3P0 数据库链接池，用了都说好！

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



