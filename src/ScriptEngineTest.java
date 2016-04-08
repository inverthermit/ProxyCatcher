import java.io.*; 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;  

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
 /**  * Java调用并执行js文件，传递参数，并活动返回值  *   * @author Tim Luo  */ 

 	public class ScriptEngineTest {    

 		public static int MAX_THREAD=60;
 		public static int proxy_index=0;
 		public static int MAX_TRY=25;
 		public static ArrayList<String> list=null;
 		public static ArrayList<String> AvailableProxyList=new ArrayList<String>();
	public static void main(String[] args){  
		
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Calendar calendar = Calendar.getInstance();
		String date = format.format(calendar.getTime());
		for(int trynum=0;trynum<MAX_TRY;trynum++){
	 try{
		 Log.log("Starting getProxies()...");
		list=getProxies();
		list.addAll(getProxies3());
		Log.log("getProxies() done.Size:"+list.size());
		Log.log("Starting availableProxy()...");
		ExecutorService pool = Executors.newCachedThreadPool();
		for(int tIndex=0;tIndex<MAX_THREAD;tIndex++)
		{
			pool.execute(
			new Runnable(){
				public void run()
				{
					while(true)
					{
						String data= getUnhandledProxy();
						if(data!=null)
						{
							if(availableProxy(data))
							{
								addAvailable(data);
							}
						}
						else{
							//System.out.println("No Unhandled");
							break;
						}
					}
					
					
				}
			});
		}
		pool.shutdown();
		pool.awaitTermination(100, TimeUnit.SECONDS);
		Log.log("availableProxy() done.Size:"+AvailableProxyList.size());
		
		if(AvailableProxyList.size()>0)
		{
			StringBuffer sb=new StringBuffer();
			sb.append("\n");
			for(int i=0;i<AvailableProxyList.size();i++)
			{
				sb.append(AvailableProxyList.get(i)+";\n");
			}
			Properties Accountpr=new Properties();
			Properties pr=new Properties();
			pr.setProperty("proxy",sb.toString() );
			
			PrintStream ps=new PrintStream(new FileOutputStream(new File(Config.ROOT_DIR_PATH+"/InitArgs"+date+".xml")));
			Accountpr.loadFromXML(new FileInputStream(new File(Config.ROOT_DIR_PATH+"/Accounts.xml")));
			pr.setProperty("account", Accountpr.getProperty("account"));
			pr.storeToXML(ps, "");
			ps=new PrintStream(new FileOutputStream(new File(Config.XML_DESTINATION+"/InitArgs.xml")));
			pr.storeToXML(ps, "");
			ps.close();
			
		}
		/*Log.log("Starting Sending mails...");
		MailSender.remind("Proxy Catcher Succeeded! "+AvailableProxyList.size(), date+" Amount:"+AvailableProxyList.size(), Config.ROOT_DIR_PATH+"/InitArgs"+date+".xml");
		Log.log("Mails sent.\n\nEND SUCCESSFULLY.");*/
		return;
	 }
	 catch(Exception ee)
	 {
		 ee.printStackTrace();
		 Log.log("Error time:"+trynum);
	 }
		}
		//System.out.println("Failed.Needed Check.");
		/*Log.log("Failed.Needed Check.");
		MailSender.remind("Proxy Catcher Failed!", date+"Failed.Needed Check.", Log.logFile.getAbsolutePath());
		*/
	}
	
	
	
	public static synchronized String getUnhandledProxy()
	{
		String result=null;
		if(proxy_index<list.size())
		{
			result=list.get(proxy_index);
			System.out.println(proxy_index+" checking...");
			proxy_index++;
		}
		return result;
	}
	
	public static synchronized void addAvailable(String data)
	{
		AvailableProxyList.add(data);
	}
	
	public static ArrayList<String> getProxies() throws Exception
	{
		
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();  
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();  
		
		HttpGet httpGet = new HttpGet(Config.PROXY_LIST_URL);
		HttpResponse response = httpclient.execute(httpGet); 
		HttpEntity entity = response.getEntity(); 
		String htmls="";
		if (entity != null) { 
		    htmls=EntityUtils.toString(entity);
		    //System.out.println(htmls);
		}   
		httpclient.close();
		
		Pattern p = Pattern.compile("var baidu_union_id = \"[\\d\\w]*\";");
    	Matcher m = p.matcher(htmls);
    	String union_id="";
    	while (m.find()) 
    	{
    		//System.out.println(m.group());
    		union_id=m.group().replace("var baidu_union_id = \"", "").replace("\";", "");
    		//System.out.println(union_id);
    		break;
    	}
    	
    	p = Pattern.compile("decrypt\\(\"[\\s\\S]*?\"");
    	m = p.matcher(htmls);
    	ArrayList<String> proxies=new ArrayList<String>();
    	while (m.find())  
    	{
    		//System.out.println(m.group());
    		String code=m.group().replace("decrypt(\"", "").replace("\"", "");
    		proxies.add(decrypt(code,union_id));
    		
    	}
    	/*for(int i=0;i<proxies.size();i++)
    	{
    		System.out.println(proxies.get(i));
    	}*/
    	return proxies;
	}
	
	public static ArrayList<String> getProxies3() throws Exception
	{
		
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();  
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();  
		
		HttpGet httpGet = new HttpGet("http://www.xicidaili.com/nn");//Config.PROXY_LIST_URL2
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip, deflate, sdch");
		httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
		//httpGet.setHeader("", "");
		//httpGet.setHeader("", "");
		//httpGet.setHeader("", "");
		HttpResponse response = httpclient.execute(httpGet); 
		HttpEntity entity = response.getEntity(); 
		String htmls="";
		if (entity != null) { 
		    htmls=EntityUtils.toString(entity);
		    //System.out.println(htmls);
		}   
		httpclient.close();
		ArrayList<String> proxies=new ArrayList<String>();
		Parser parser=Parser.createParser(htmls, "utf-8");
		TagNameFilter trFilter=new TagNameFilter("tr");
        NodeList nodes1 = parser.extractAllNodesThatMatch(trFilter);
        //System.out.println(nodes1.size());
        for(int i=1;i<nodes1.size();i++)
        {
        	parser=Parser.createParser(nodes1.elementAt(i).toHtml(), "utf-8");
    		TagNameFilter tdFilter=new TagNameFilter("td");
            NodeList nodes2 = parser.extractAllNodesThatMatch(tdFilter);
            proxies.add(html2Str(nodes2.elementAt(2).toHtml()+":"+nodes2.elementAt(3).toHtml()));
            
        }
		
    	
    	/*for(int i=0;i<proxies.size();i++)
    	{
    		System.out.println(proxies.get(i));
    	}*/
    	return proxies;
	}
	public static boolean availableProxy(String url)
	{
		boolean flag=false;
			try{
				HttpHost proxy = new HttpHost(url.split(":")[0], Integer.parseInt(url.split(":")[1]));
				DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(8000).setConnectTimeout(8000).build();  
				CloseableHttpClient httpclient = HttpClients.custom()
						.setRoutePlanner(routePlanner).setDefaultRequestConfig(requestConfig).build();  
				
				//httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
				HttpGet httpGet = new HttpGet(Config.IP_CHECK_URL);
				HttpResponse response = httpclient.execute(httpGet); 
				HttpEntity entity = response.getEntity();  
				if (entity != null) { 
				    String htmls=EntityUtils.toString(entity);
				    if(htmls.contains(url.split(":")[0]))
				    {
				    	flag=true;
				    }
				    //getMainPages(htmls);
				}   
				httpclient.close();
			}
			catch(Exception ee)
			{
				//ee.printStackTrace();
			}
			return flag;
			
		
	}
	
	
	public static String decrypt(String code,String baidu_union) throws Exception
	{
		ScriptEngineManager manager = new ScriptEngineManager();   
		ScriptEngine engine = manager.getEngineByName("javascript");     
		
		String jsFileName = Config.ROOT_DIR_PATH+"/js/aes.js";   // 读取js文件   
		
		FileReader reader = new FileReader(jsFileName);   // 执行指定脚本   
		engine.eval(reader);   
		String result="";
		if(engine instanceof Invocable) {    
		Invocable invoke = (Invocable)engine;    // 调用merge方法，并传入两个参数    
		
		// c = merge(2, 3);    
		//decrypt("ifau6NhMumgrkY2s5zWPjVAWIZWMaCAuADiEgNcOTSg=")
		
		//var baidu_union_id = "ca2922c6a94211e5";
		//"var baidu_union_id = "...";"
		//result = (String)invoke.invokeFunction("decrypt", "Oh2q1x5yqG+uAeXtBoroYqzcZeuNQ7QDILpV/FmiBmY=","ca2922c6a94211e5");    
		result = (String)invoke.invokeFunction("decrypt", code,baidu_union);    
		
		//System.out.println(result);   
		}   
		
		reader.close(); 
		return result;
	}
	public static String html2Str(String html) { 
		return html.replaceAll("<[^>]+>", "");
	}

}