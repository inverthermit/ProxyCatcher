import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Log{
	public static File logFile = null;
	public static FileOutputStream fos = null;
	
	static{
		logFile = new File(Config.ROOT_DIR_PATH + "/tmp.log");
		if(logFile.exists()){
			logFile.delete();
		}
		try {
			logFile.createNewFile();
			fos = new FileOutputStream(logFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void log(String msg){
		try {
			fos.write((msg+"\r\n").getBytes());
			fos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(msg);
	}
	
	public static void copyTo(String dir, String newName){
		File newFile = new File(dir, newName);
		FileInputStream fis = null; 
		FileOutputStream newFOS = null;
		if(newFile.exists()){
			newFile.delete();
		}
		try {
			newFile.createNewFile();
			fis = new FileInputStream(logFile);
			newFOS = new FileOutputStream(newFile);
			byte[] bytes = new byte[10240];
			int len = 0;
			while((len = fis.read(bytes)) > 0){
				newFOS.write(bytes, 0, len);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(fis != null){
					fis.close();
				}
				if(newFOS != null){
					newFOS.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void closeLogFile(){
		try {
			if(fos != null){
				fos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}