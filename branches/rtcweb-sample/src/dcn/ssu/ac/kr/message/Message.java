package dcn.ssu.ac.kr.message;

import dcn.ssu.ac.kr.json.JSONException;
import dcn.ssu.ac.kr.json.JSONObject;

public class Message {
	public static String OFFER = "OFFER";
	public static String ANSWER = "ANSWER";
	public static String OK = "OK";
	public static String NOMATCH = "NOMATCH";
	public String mMessageType = "";
	public String offererSessionId = "";
	public String answererSessionId = "";
	public String mMessageSdp = "";
	public MessageInfoBuilder mMessageInfoBuilder = new MessageInfoBuilder();
	
	public Message(String str, String sender, String receiver) {	//only the SDP from client to server.
		mMessageInfoBuilder.addSender(sender);
		if(receiver != null)
			mMessageInfoBuilder.addReceiver(receiver);
		try {
			mMessageSdp = str.substring(str.indexOf("SDP"));
			JSONObject obj = new JSONObject(mMessageSdp.substring(mMessageSdp.indexOf("{")));
			if(obj.has("messageType"))
				mMessageType = obj.get("messageType").toString();
			if(obj.has("offererSessionId"))
				offererSessionId = obj.get("offererSessionId").toString();
			if(obj.has("answererSessionId"))
				answererSessionId = obj.get("answererSessionId").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isOffer() {
		return mMessageType.equals(OFFER);
	}
	
	public boolean isAnswer() {
		return mMessageType.equals(ANSWER);
	}
	
	public String getSDP() {
		return mMessageSdp;
	}
	
	public String getFullMessage() {
		/**
		 * INFO
		 * {
		 *    "sender": "alice"
		 * }
		 * SDP
		 * {
		 *    "messageType":"OFFER",
		 *    ...
		 * }
		 * 
		 */
		return mMessageInfoBuilder.toString() + "\r\n" + mMessageSdp;		
	}
	
}
