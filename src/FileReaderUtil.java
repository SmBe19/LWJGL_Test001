import java.io.*;

public class FileReaderUtil {

	public static String readAllLines(String file){
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine()) != null){
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}
}
