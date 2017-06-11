package com.foolishgoat.server.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import com.foolishgoat.server.db.C3P0DBPool;
import com.foolishgoat.server.db.Table;
import com.foolishgoat.server.exception.RouterConfigException;
import com.foolishgoat.server.processor.Processor;
import com.foolishgoat.server.processor.ProcessorChain;
import com.foolishgoat.server.router.Router;
import com.foolishgoat.server.utils.RequestUtil;
import com.foolishgoat.server.utils.RouterUtil;

public class FoolishGoatHandler extends AbstractHandler {

	public static final Logger logger = Logger.getLogger("FoolishGoatHandler");

	private static Map<String, Class<Processor>> allProcessors;
	private static List<Router> allRouters;
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (baseRequest.isHandled()) {
			return;
		}
		// 选择路由
		Router router = this.chroose(target, baseRequest.getMethod());
		if (router != null) {
			// 根据路由将url中符合规则的参数提取出来，设置到req.setAttribute();
			try {
				RequestUtil.setUrlAttributes(target, router.getName(), request);
			} catch (RouterConfigException e) {
				// TODO Auto-generated catch block
				logger.log(Level.WARNING,"提取参数出错，请检查路由配置是否符合规则。", e);
			}
			// 执行服务
			ProcessorChain pc = new ProcessorChain();
			// 数据处理器链条  用 => 连接，否则识别将会出错
			String[] pross = router.getProcessors().split("=>");
			for(int i=0;i < pross.length;i++){
				try {
					pc.addProcessor(FoolishGoatHandler.getProcessors(pross[i]).newInstance());
				} catch (InstantiationException e) {
					logger.log(Level.WARNING,pross[i] + "加入数据处理器链失败！", e);
				} catch (IllegalAccessException e) {
					logger.log(Level.WARNING,pross[i] + "加入数据处理器链失败！", e);
				}
			}
			pc.doProcessor(router, request, response, pc);
			baseRequest.setHandled(true);
		}

	}
	/**
	 * 根据传入json配置文件的路径，加载配置的所有resource，包括处理器(Processor),路由(Router)
	 * @param name
	 */
	public void init(String path) {
		BufferedReader brname = null;
		try {
			brname = new BufferedReader(new FileReader(path));
			StringBuilder builder = new StringBuilder();
			String str = "";
			while ((str = brname.readLine()) != null) {
				builder.append(str);
			}
			JSONObject obj = new JSONObject(builder.toString());
			this.loadProcessors(obj.getJSONArray("processors"));
			this.loadRouters(obj.getJSONArray("routers"));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "FoolishGoat配置文件装载失败！", e);
		}finally{
			if(brname != null){
				try {
					brname.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.log(Level.WARNING, "resources.json 文件流关闭失败！", e);
				}
			}
		}
	}

	/**
	 * 根据json数组装载并且配置的所有Processor
	 * 
	 * @param array(json数组)
	 */
	@SuppressWarnings("unchecked")
	public void loadProcessors(JSONArray array) {
		allProcessors = new HashMap<String, Class<Processor>>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			String name = (String) obj.getString("name");
			String clazz = (String) obj.getString("class");
			try {
				allProcessors.put(name, (Class<Processor>) Class.forName(clazz));
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "装载Processor失败！", e);
				System.exit(0);
			}
		}
	}

	/**
	 * 根据json数组装载配置的所有Router
	 * 
	 * @param array
	 */
	public void loadRouters(JSONArray array) {
		allRouters = new ArrayList<Router>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			allRouters.add(new Router(obj));
		}
		// 将数据表映射为 Router
		Connection con = C3P0DBPool.getInstance().getConnection();
		DatabaseMetaData metaData;
		try {
			metaData = con.getMetaData();
			ResultSet rs_table = metaData.getTables(con.getCatalog(), metaData.getUserName(), null, new String[]{"TABLE"});
			while(rs_table.next()){
				Table table = new Table(rs_table.getString("TABLE_NAME"),con);
				this.addRouter(table.toRouter());
			}
			rs_table.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "DBResource初始化失败", e);
			// 退出
            System.exit(0);
		}finally{
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据target选择路由
	 * 
	 * @param target
	 * @return
	 */
	public Router chroose(String target, String method){
		Router router = null;
		for(int i=0;i<allRouters.size();i++){
			router = allRouters.get(i);
			if(method.equals(router.getMethod().toUpperCase())){
				try {
					if(RouterUtil.isAccordant(target, router.getName())){
						return router;
					}
				} catch (RouterConfigException e) {
					// TODO Auto-generated catch block
					logger.log(Level.WARNING, router.getName() + "路由配置错误！", e);
				}
			}
		}
		return null;
	}

	/**
	 * 根据名称获取处理器
	 * @param key
	 * @return
	 */
	public static Class<Processor> getProcessors(String key) {
		return FoolishGoatHandler.allProcessors.get(key);
	}
	/**
	 * 将所有router转化为json
	 * @return
	 */
	public static JSONArray routerToJson(){
		JSONArray json = new JSONArray();
		for(int i=0;i<allRouters.size();i++){
			json.put(i, allRouters.get(i).toJson());
		}
		return json;
	}
	/**
	 * 增加多个router
	 * @param routers
	 */
	public void addRouter(List<Router> routers){
		for(int i=0;i<routers.size();i++){
			this.addRouter(routers.get(i));
		}
	}
	/**
	 * 增加单个router
	 * @param routers
	 */
	public void addRouter(Router router){
		for(int i=0;i<allRouters.size();i++){
			if(allRouters.get(i).getName().equals(router.getName()) && allRouters.get(i).getMethod().equals(router.getMethod())){
				allRouters.remove(i);
				allRouters.add(i, router);
				return;
			}
		}
		allRouters.add(router);
	}

}
