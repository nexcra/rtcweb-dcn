package dcn.ssu.ac.kr;

import java.util.ArrayList;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class RoomManagement {
	public static ArrayList<String> getOtherUser(Entity room, String user) {
		
		ArrayList<String> arr =new ArrayList<String>();
		
		Integer NoU = new Integer((String)room.getProperty("NoU"));
		int num = 1;
		String userName = "user" + num;
		for(int i = 0; i < NoU.intValue(); i++){
			if(!user.equalsIgnoreCase((String)room.getProperty(userName)))
				arr.add((String)room.getProperty(userName));
			num++;
			userName = "user" + num;
		}
		return arr;
	}
	
	public static void removeUser(Entity room, String user) {
		
		int num = 1;
		String userName = "user" + num;
		Integer NoU = new Integer((String)room.getProperty("NoU"));
	
		for(int i = 0; i < NoU.intValue(); i++){
			if(user.equalsIgnoreCase((String)room.getProperty(userName))){
				String lastUserName = "user"  + NoU.intValue();
				room.setProperty(userName, (String)room.getProperty(lastUserName));
				room.setProperty(lastUserName, null);
				room.setProperty("NoU", (NoU.intValue() -1) + "");
				break;
			}
			num++;
			userName = "user" + num;
		}
	}
	
	public static void addUser(Entity entity, String user) {
		String strNoU = (String)entity.getProperty("NoU");
		int num = (new Integer(strNoU)).intValue() + 1;
		String userName = "user" + num;
		entity.setProperty(userName, user);
		//get current number of user and plus 1
		entity.setProperty("NoU", num + "");
		
		DatastoreServiceFactory.getDatastoreService().put(entity);
	}
	
	public static int getOccupancy(Entity entity) {
		Integer NoU = new Integer((String)entity.getProperty("NoU"));
		return NoU.intValue();
	}
}
