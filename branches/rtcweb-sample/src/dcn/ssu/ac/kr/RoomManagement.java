package dcn.ssu.ac.kr;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;

public class RoomManagement {
	
	
	public static EmbeddedEntity getOtherUser(Entity room, String userName) {
		
		EmbeddedEntity user1 = (EmbeddedEntity) room.getProperty("user1");
		EmbeddedEntity user2 = (EmbeddedEntity) room.getProperty("user2");
		
		String name1 = "", name2 = "";
		if(user1 != null) {
			name1 = (String) user1.getProperty("name");
		}
		
		if(user2 != null) {
			name2 = (String) user2.getProperty("name");
		}
		
		if(userName.equals(name1))
			return user2;
		else if(userName.equals(name2))
			return user1;
		return null;
	}
	
	public static void removeUser(Entity room, String userName) {
		
		
		EmbeddedEntity user1 = (EmbeddedEntity) room.getProperty("user1");
		EmbeddedEntity user2 = (EmbeddedEntity) room.getProperty("user2");
		
		String name1 = "", name2 = "";
		if(user1 != null) {
			name1 = (String) user1.getProperty("name");
		}
		
		if(user2 != null) {
			name2 = (String) user2.getProperty("name");
		}
		
		MessageManagement.deleteSavedMessage(MessageManagement.makeToken(room, userName));
		
		if(userName.equals(name2)) {
			name2 = null;
		}
		
		if(userName.equals(name1)) {
			if(name2 != null) {
				user1.setProperty("name", name2);
				user2.setProperty("name", null);
			} else {
				user1.setProperty("name", null);
			}
		}
		room.setProperty("user1", user1);
		room.setProperty("user2", user2);
	}
	
	public static void addUser(Entity room, String userName) {
		EmbeddedEntity newUser = new EmbeddedEntity();
		newUser.setProperty("name", userName);
		newUser.setProperty("isConnected", false);
		
		if(room.getProperty("user1") == null)
			room.setProperty("user1", newUser);
		else if(room.getProperty("user2") == null) {
			room.setProperty("user2", newUser);
		} else {
			System.out.println("Room is full");
			return;
		}
		DatastoreServiceFactory.getDatastoreService().put(room);
	}
	
	public static int getOccupancy(Entity entity) {
		int occupancy = 0;
		if(entity.getProperty("user1") != null)
			occupancy += 1;
		
		if(entity.getProperty("user2") != null)
			occupancy += 1;
		
		return occupancy;
	}
	
	public static void setConnected(Entity room, String userName) {
		
		EmbeddedEntity user1 = (EmbeddedEntity) room.getProperty("user1");
		EmbeddedEntity user2 = (EmbeddedEntity) room.getProperty("user2");
		
		String name1 = "", name2 = "";
		if(user1 != null) {
			name1 = (String) user1.getProperty("name");
		}
		
		if(user2 != null) {
			name2 = (String) user2.getProperty("name");
		}
		
		if(name1.equals(userName)) {
			user1.setProperty("isConnected", true);
			room.setProperty("user1", user1);
			DatastoreServiceFactory.getDatastoreService().put(room);
		} else if(name2.equals(userName)) {
			user2.setProperty("isConnected", true);
			room.setProperty("user2", user2);
			DatastoreServiceFactory.getDatastoreService().put(room);
		}
	}
	
	public static Boolean isConnected(Entity room, String userName) {
		EmbeddedEntity user1 = (EmbeddedEntity) room.getProperty("user1");
		EmbeddedEntity user2 = (EmbeddedEntity) room.getProperty("user2");
		
		String name1 = "", name2 = "";
		if(user1 != null) {
			name1 = (String) user1.getProperty("name");
		}
		
		if(user2 != null) {
			name2 = (String) user2.getProperty("name");
		}
		
		EmbeddedEntity user = null;
		
		if(name1.equals(userName)) {
			user = user1;
		} else if(name2.equals(userName)) {
			user = user2;
		}
		
		if(user != null) {
			if(user.getProperty("isConnected") == null) {
				return false;
			} else {
				return (Boolean) user.getProperty("isConnected");
			}
		}
		return false;
	}
	
	
}
