
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;


public class MailSender {
	public static String PROPERTIES_PATH = Config.ROOT_DIR_PATH + "//mail.properties";
	
	private static final Properties props = System.getProperties();
	
	private static Properties customeProps = null;
	
	private static MailAuthenticator authenticator;
	
	
	private static Session session;

	public static void main(String[] args) throws AddressException, MessagingException, FileNotFoundException, IOException {
		//remind("20151220");
	}
	
	public static boolean remind(String subject, String content, String filename) {
		if(!init()){
			Log.log("Init failed!");
			return false;
		}
		List<String> recipientsList = new ArrayList<String>();
		for(String recipient : customeProps.getProperty("recipients").split(";")){
			if(recipient.length()>0){
				recipientsList.add(recipient);
			}
		}
		if(recipientsList.size()==0){
			Log.log("No recipients!");
			return false;
		}
		try {
			send(recipientsList,subject,
					content, filename);//Log.logFile.getAbsolutePath()
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		
	}

	private static boolean init(){
		Log.log("Initting MailSender...");
		File file = new File(PROPERTIES_PATH);
		if(!file.exists()){
			Log.log("mail.properties not exists!");
			return false;
		}
		try {
			customeProps = new Properties();
			customeProps.load(new FileInputStream(PROPERTIES_PATH));
			
			Log.log("Loading properties...");
			customeProps.list(new PrintStream(Log.fos));
			customeProps.list(System.out);
			props.put("mail.smtp.auth", "true");
			if(customeProps.containsKey("smtpHostName")){
				props.put("mail.smtp.host", customeProps.getProperty("smtpHostName"));
			}else{
				props.put("mail.smtp.host", "smtp." + customeProps.getProperty("username").split("@")[1]);
			}
			if(customeProps.containsKey("port")){
				props.put("mail.smtp.port", customeProps.getProperty("port"));
			}
			
			authenticator = new MailAuthenticator(customeProps.getProperty("username"), customeProps.getProperty("password"));
			session = Session.getInstance(props, authenticator);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return true;
	}
	
	private static void send(List<String> recipients, String subject, String content, String filename) throws AddressException, MessagingException, IOException{
		Log.log("Sending mail...");
		final MimeMessage message = new MimeMessage(session);
		
		message.setFrom(new InternetAddress(authenticator.getUsername()));
		final int num = recipients.size();
		InternetAddress[] addresses = new InternetAddress[num];
		for(int i = 0; i < num; i++){
			addresses[i] = new InternetAddress(recipients.get(i));
		}
		
		
		
		FileInputStream fi=new FileInputStream(new File(filename));
		byte[] temp=new byte[2048];
		int len=0;
		StringBuffer sb=new StringBuffer();
		
		while((len=fi.read(temp))>0)
		{
			sb.append(new String(temp,0, len));
		}
		fi.close();
		FileOutputStream fo=new FileOutputStream(new File(Config.ROOT_DIR_PATH+"/temp.html"));
		fo.write(sb.toString().getBytes());
		fo.close();
		String mailcontent=new String(sb);
		mailcontent=mailcontent.replace("\r\n", "<br>");
		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(content, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		
		
		
		mbp = new MimeBodyPart();
		mbp.setDataHandler(new DataHandler(new FileDataSource(Config.ROOT_DIR_PATH+"/temp.html")));
		
		
		mbp.setFileName("proxies.html");
		mp.addBodyPart(mbp);
		
		message.setRecipients(RecipientType.TO, addresses);
		message.setSubject(subject);
		message.setContent(mp);
		Transport.send(message);
	}
	
}
