package com.foolishgoat.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.foolishgoat.server.handler.FoolishGoatHandler;

public class FoolishGoatServer {
	
	public static final Logger logger = Logger.getLogger("FoolishGoat");
	
	private final Server server;
	private final Properties props;
	private final HttpConfiguration config;
	private final SslContextFactory sslContextFactory;
	private final ServerConnector connector;
	private final String protocal;
	
	public FoolishGoatServer(){
		props = new Properties();
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("server.properties");  
        try {  
            props.load(input);  
        } catch (IOException e) {
            logger.log(Level.SEVERE, "配置文件读取失败", e);
            // 退出
            System.exit(0);
        }finally{
        		if(input != null){
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.log(Level.SEVERE, "关闭失败！", e);
					System.exit(0);
				}
        		}
        }
        
        server = new Server();
        server.setAttribute("CharacterEncoding", "UTF-8");
        config = new HttpConfiguration();
        if(Boolean.parseBoolean(props.getProperty("server.https", "true"))){
        		// https
        		protocal = "HTTPS";
        		config.setSecureScheme("https");
        		sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(System.getenv("FOOLISHGOAT_SERVER_HOME") + "/" + props.getProperty("server.cert"));
            sslContextFactory.setKeyStorePassword(props.getProperty("server.keystore.password",""));
            sslContextFactory.setKeyManagerPassword(props.getProperty("server.keymanager.password",""));
            connector = new ServerConnector(server,
                    new SslConnectionFactory(sslContextFactory,"http/1.1"),
                    new HttpConnectionFactory(config));
            // 设置访问端口
            connector.setPort(8888);
            connector.setIdleTimeout(300000);
            server.addConnector(connector);
        }else{
        		protocal = "HTTP";
        		sslContextFactory = null;
        		connector = new ServerConnector(server,
                        new HttpConnectionFactory(config));
        		// 设置访问端口
            connector.setPort(Integer.valueOf(props.getProperty("server.port","58452")));
            connector.setIdleTimeout(Integer.valueOf(props.getProperty("server.idle.timeout","300000")));
            server.addConnector(connector);
           
        }
        HandlerList handlers = new HandlerList();
        ResourceHandler resourceHandler =  new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(System.getenv("FOOLISHGOAT_SERVER_HOME") + "/" + props.getProperty("server.app.path","./"));
        FoolishGoatHandler fgHandler = new FoolishGoatHandler();
        fgHandler.init(System.getenv("FOOLISHGOAT_SERVER_HOME") + "/" + props.getProperty("server.resource.conf","foolishgoat.conf.json"));
        handlers.setHandlers(new Handler[]{resourceHandler,fgHandler,new DefaultHandler()});
        server.setHandler(handlers);
	}
	
	/**
	 * 启动服务
	 */
	public void start(){
		try {
			server.start();
	        server.join();  
		} catch (Exception e) {
			logger.log(Level.SEVERE, "服务启动失败", e);
			// 退出
            System.exit(0);
		}  
	}
	
	public void stop(){
		try {
			server.stop();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "服务停止失败", e);
		}
	}

	

	public static Logger getLogger() {
		return logger;
	}


	public Server getServer() {
		return server;
	}


	public Properties getProps() {
		return props;
	}


	public HttpConfiguration getConfig() {
		return config;
	}


	public SslContextFactory getSslContextFactory() {
		return sslContextFactory;
	}


	public ServerConnector getConnector() {
		return connector;
	}


	public String getProtocal() {
		return protocal;
	}
	
}
