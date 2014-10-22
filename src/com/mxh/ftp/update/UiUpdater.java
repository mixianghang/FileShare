

package com.mxh.ftp.update;

import java.util.ArrayList;
import java.util.List;

import com.mxh.ftp.log.MyLog;

import android.os.Handler;
/**
 * 观察者模式,登记客户,有新的消息的时候,通知各个客户
 * @author XiangHang Mi
 *
 */
public class UiUpdater {
	protected static MyLog myLog = new MyLog("UiUpdater");
	protected static List<Handler> clients = new ArrayList<Handler>();
	
	public static void registerClient(Handler client) {
		if(!clients.contains(client)) {
			clients.add(client);
		}
	}
	
	public static void unregisterClient(Handler client) {
		while(clients.contains(client)) {
			clients.remove(client);
		}
	}
	
	public static void updateClients() {
		//myLog.l(Log.DEBUG, "UI update");
		//Log.d("UiUpdate", "Update now");
		for (Handler client : clients) {
			client.sendEmptyMessage(0);
		}
	}
}
