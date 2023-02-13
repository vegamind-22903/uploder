package si.vegamind;

import okhttp3.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		File root = new File(args[0]);

		for(File file : findFiles(root.listFiles())) {
			upload(file);
		}

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
			System.out.println(response.body().string());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void compile() {
		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();

			Request request = new Request.Builder()
					.url("http://192.168.43.1:8080/java/build/start")
					.get()
					.build();

			Response response = client.newCall(request).execute();
			System.out.println(response.body().string());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}