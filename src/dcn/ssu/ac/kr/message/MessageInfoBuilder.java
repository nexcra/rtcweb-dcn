package dcn.ssu.ac.kr.message;

import dcn.ssu.ac.kr.json.JSONException;
import dcn.ssu.ac.kr.json.JSONObject;

public class MessageInfoBuilder {
	public String prefix = "INFO\r\n";
	
	private JSONObject mJSONObject = new JSONObject();
	
	public void addSender(String senderName) {
		try {
			mJSONObject.append("sender", senderName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void addReceiver(String receiverName) {
		try {
			mJSONObject.append("receiver", receiverName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return prefix + mJSONObject.toString();
	}
	
}
