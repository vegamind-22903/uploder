package si.vegamind;

import okhttp3.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
	private static WebSocketClient webSocketClient;

	public static void main(String[] args) {
		try {
			webSocketClient = new WebSocketClient(new URI("ws://192.168.43.1:8081/")) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
				}

				@Override
				public void onMessage(String messageRaw) {
					System.out.println(messageRaw);

					JSONObject message = new JSONObject(messageRaw);
					String payloadRaw = message.getString("payload");
					JSONObject payload = new JSONObject(payloadRaw);

					switch(payload.getString("status")) {
						case "SUCCESSFUL" -> {
							System.out.println("BUILD SUCCEEDED");
							webSocketClient.close();
						}
						case "FAILED" -> {
							System.err.println("BUILD FAILED");
							webSocketClient.close();
						}
					}
				}

				@Override
				public void onClose(int i, String s, boolean b) {
				}

				@Override
				public void onError(Exception e) {
					System.err.println(e.getMessage());
					System.exit(0);
				}
			};
		} catch(Exception e) {
			e.printStackTrace();
		}

		webSocketClient.connect();

		File root = new File(args[0]);

		for(File file : findFiles(root.listFiles())) {
			upload(file);
		}

		webSocketClient.send("{\"namespace\":\"system\",\"type\":\"subscribeToNamespace\",\"payload\":\"ONBOTJAVA\"}");
		compile();
	}

	private static ArrayList<File> findFiles(File[] files) {
		ArrayList<File> toReturn = new ArrayList<>();

		for(File file : files) {
			if(file.isDirectory()) {
				toReturn.addAll(findFiles(file.listFiles()));
			} else {
				String[] name = file.getName().split("\\.");

				if(name[name.length - 1].equals("java")) {
					toReturn.add(file);
				}
			}
		}

		return toReturn;
	}

	private static void upload(File file) {
		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();

			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("data", Files.readString(Path.of(file.getPath())))
					.build();

			Request request = new Request.Builder()
					.url("http://192.168.43.1:8080/java/file/save?f=/src/org/firstinspires/ftc/teamcode/" + file.getName())
					.method("POST", body)
					.build();

			Response response = client.newCall(request).execute();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void compile() {
		webSocketClient.send("{namespace: \"ONBOTJAVA\", type: \"build:launch\", payload: \"\"}");
	}
}