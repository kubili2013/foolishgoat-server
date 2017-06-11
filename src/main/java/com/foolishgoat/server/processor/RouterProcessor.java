package com.foolishgoat.server.processor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foolishgoat.server.handler.FoolishGoatHandler;
import com.foolishgoat.server.router.Router;

public class RouterProcessor implements Processor {
	
	public static final Logger logger = Logger.getLogger("DBProcessor");
	@Override
	public void doProcessor(Router router, HttpServletRequest req, HttpServletResponse rep, ProcessorChain chain) {
		/** Connection con = C3P0DBPool.getInstance().getConnection();
		DatabaseMetaData metaData;
		try {
			metaData = con.getMetaData();
			ResultSet rs_table = metaData.getTables(con.getCatalog(), metaData.getUserName(), null, new String[]{"TABLE"});
			while(rs_table.next()){
				Table table = new Table(rs_table.getString("TABLE_NAME"),con);
				FoolishGoatHandler.addRouter(table.toRouter());
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
		} **/
		
		try {
			rep.getWriter().print(FoolishGoatHandler.routerToJson().toString());
		} catch (IOException e) {
			logger.log(Level.WARNING, "执行" + router.getName() + "处理器失败！", e);
		}
		
		// Table table = DBResource.getTable(router.getResourceName().toLowerCase());
		// Connection con = C3P0DBPool.getInstance().getConnection();
		
		chain.doProcessor(router, req, rep, chain);
	}

}
