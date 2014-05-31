package com.example.base64;

import hprose.client.HproseClient;
import hprose.client.HproseHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.phprpc.PHPRPC_Client;
import org.phprpc.util.AssocArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.R.xml;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	ImageView image=null;
	private String tagName = "MYAPI";
	private EditText inputValue = null;
	private Button Httpsubmit = null;
	private Button RPCsubmit = null;
	private Button HPROSEsubmit = null;
	
	private static final String BaseUrl = "http://api.uihoo.com/qrcode/qrcode.http.php?";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageView) findViewById(R.id.Image);
		inputValue = (EditText) findViewById(R.id.Context);
		Httpsubmit = (Button) findViewById(R.id.UseHttp);
		Httpsubmit.setOnClickListener(new UseHTTPInterface());
		RPCsubmit = (Button) findViewById(R.id.UseRPC);
		RPCsubmit.setOnClickListener(new UseRPCInterface());
		HPROSEsubmit = (Button) findViewById(R.id.UseHPROSE);
		HPROSEsubmit.setOnClickListener(new UseHPROSE());
		//网络访问兼容
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
        .detectDiskReads()  
        .detectDiskWrites()  
        .detectNetwork()   // or .detectAll() for all detectable problems  
        .penaltyLog()  
        .build());  
StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
        .detectLeakedSqlLiteObjects()  
        .detectLeakedClosableObjects()  
        .penaltyLog()  
        .penaltyDeath()  
        .build()); 
		
	}
	private final class UseHTTPInterface implements OnClickListener{
		URL url=null;
		URLConnection connection=null;
		@Override
		public void onClick(View v) {
			
			String value = inputValue.getText().toString();
			String base64 = null;
			InputStream fromurl=null;
			try {
				String urlcontext = "http://api.uihoo.com/qrcode/qrcode.http.php?string="+value+"&width=150&bgc=FFFFFF&fgc=000000&logo=http://api.uihoo.com/demo/images/logo.png&logosize=0.4&el=3&format=xml";		
				url = new URL(urlcontext);
				connection = url.openConnection();
				fromurl = connection.getInputStream();
				XmlPullParser xmlpull = XmlPullParserFactory.newInstance().newPullParser();
				xmlpull.setInput(fromurl, null);
				int num = xmlpull.getLineNumber();
				String code = connection.getContentType();
				Log.i(tagName, "Return code="+code);
				Log.i(tagName, "The colum Num = "+num);
				int eletype = xmlpull.getEventType();
				while(XmlPullParser.END_DOCUMENT!=eletype){
					eletype = xmlpull.next();
					if(XmlPullParser.START_TAG==eletype){
						if(xmlpull.getName().equals("base64")){
							base64 = xmlpull.nextText();
						}
					}
				}
				String[] subs = base64.split(",");
				byte[] forbitmap = Base64.decode(subs[1], Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(forbitmap, 0, forbitmap.length);
				image.setImageBitmap(bitmap);
				Log.i(tagName, "base64 context from HTTP = "+base64);
				Log.i(tagName, "HTTP End");
				Toast.makeText(v.getContext(), "请求成功", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(tagName, "Except:", e.getCause());
				Toast.makeText(v.getContext(),"请求失败", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
	}
	private final class UseRPCInterface implements OnClickListener{
		private PHPRPC_Client client = null;
		private AssocArray result = null;
		public UseRPCInterface(){
			client = new PHPRPC_Client("http://api.uihoo.com/qrcode/qrcode.phprpc.php");
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String value = inputValue.getText().toString();
			value.replaceAll(" ", "%");
			
			result = (AssocArray) client.invoke("MYAPI_qrcode",new Object[]{value,150,"FFFFFF","000000","http://api.uihoo.com/demo/images/logo.png",0.4,3});
			Map map = result.toHashMap();
			
			byte[] base64Obj = (byte[]) map.get("base64");
			String temp = new String(base64Obj);
			String[] subs = temp.split(",");
			Log.i(tagName, "base64 context from PHPRPC = " + new String(base64Obj));
			base64Obj = subs[1].getBytes();
			byte[] forbitmap = Base64.decode(base64Obj, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(forbitmap, 0, forbitmap.length);
			image.setImageBitmap(bitmap);
			Toast.makeText(v.getContext(),"请求成功", Toast.LENGTH_LONG).show();
		}
	}
	private final class UseHPROSE implements OnClickListener{
		private HproseHttpClient HttpClient = null;
		private String value = null;
		public UseHPROSE(){
			HttpClient = new HproseHttpClient();
			HttpClient.useService("http://api.uihoo.com/qrcode/qrcode.hprose.php");
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			value = inputValue.getText().toString();
			try {
				Map result = (Map) HttpClient.invoke("MYAPI_qrcode", new Object[]{value,150,"FFFFFF","000000","http://api.uihoo.com/demo/images/logo.png",0.4,3});

				String fromhprose = (String) result.get("base64");
				String temp = new String(fromhprose.getBytes());
				String[] subs = temp.split(",");
				Log.i(tagName, "Base64 context from HPROSE = "+fromhprose);
				byte[] fromprosebyte = subs[1].getBytes();
				byte[] forBitmap = Base64.decode(fromprosebyte, Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(forBitmap, 0, forBitmap.length);
				image.setImageBitmap(bitmap);
				Toast.makeText(v.getContext(),"请求成功", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.i(tagName, "Exception");
			}
		}
	}
}
