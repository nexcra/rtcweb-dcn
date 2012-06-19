package dcn.ssu.ac.kr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelPresence;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import dcn.ssu.ac.kr.message.Message;


@SuppressWarnings("serial")
public class MessageServlet extends HttpServlet{

	Message mROAPMessage = null;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = req.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		String message = stringBuilder.toString();
		String room_key = req.getParameter("r");

		try {
			Entity room = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("Room", room_key));
			
			String sender = req.getParameter("u");
			
			ArrayList<String> otherUser = RoomManagement.getOtherUser(room, sender);
			
			if(otherUser.size() > 0) {
				if(message.indexOf("*") < 0){
					int idxStart = message.indexOf("to:");
					int idxEnd = message.indexOf(" ", idxStart);
					String receiver = message.substring(idxStart + 3, idxEnd);
					ChannelService channelService = ChannelServiceFactory.getChannelService();
					message = message.substring(idxEnd + 1);
					mROAPMessage = new Message(message, sender, receiver);
					if(!receiver.equalsIgnoreCase("undefined"))
						channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + receiver, mROAPMessage.getFullMessage()));
					else{
						for(int i = 0; i < otherUser.size(); i++)
							if(otherUser.get(i) != null)
								channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + otherUser.get(i), mROAPMessage.getFullMessage()));
					}
				}else{
					String[] arrMessage = message.split("\\*");
					for(int j = 0; j < arrMessage.length; j++ ){
						message = arrMessage[j];
						int idxStart = message.indexOf("to:");
						int idxEnd = message.indexOf(" ", idxStart);
						String receiver = message.substring(idxStart + 3, idxEnd);
						ChannelService channelService = ChannelServiceFactory.getChannelService();
						message = message.substring(idxEnd + 1);
						mROAPMessage = new Message(message, sender, receiver);
						if(!receiver.equalsIgnoreCase("undefined"))
							channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + receiver, mROAPMessage.getFullMessage()));
						else{
							for(int i = 0; i < otherUser.size(); i++)
								if(otherUser.get(i) != null)
									channelService.sendMessage(new ChannelMessage(room.getKey().getName() + "/" + otherUser.get(i), mROAPMessage.getFullMessage()));
						}
					}
				}
			}

		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}

	}
}
