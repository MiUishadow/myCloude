package com.example.base64;

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
	private Button submit = null;
	URL url=null;
	URLConnection connection=null;
	private static final String BaseUrl = "http://api.uihoo.com/qrcode/qrcode.http.php?";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		image = (ImageView) findViewById(R.id.Image);
		inputValue = (EditText) findViewById(R.id.Context);
		submit = (Button) findViewById(R.id.Button);
		submit.setOnClickListener(new GenrateBitmap());
		
	}
	private final class GenrateBitmap implements OnClickListener{

		@Override
		public void onClick(View v) {
			String format = "xml";
			int width = 200;
			Log.i(tagName, "initFinish");
			String value = inputValue.getText().toString().trim();
			value.replaceAll(" ", "%");
			String base64 = null;
			InputStream fromurl=null;
			try {
//				String urlcontext = BaseUrl+"string="+value+"&width="+width+"&bgc=FFFFFF&fgc=000000&logo=http://api.uihoo.com/demo/images/logo.png&logosize=0.4&el=3&format="
//						+format;
				String urlcontext = "http://api.uihoo.com/qrcode/qrcode.http.php?string="+value+"&width=150&bgc=FFFFFF&fgc=000000&logo=http://api.uihoo.com/demo/images/logo.png&logosize=0.4&el=3&format=xml";
				PHPRPC_Client client = new PHPRPC_Client("http://api.uihoo.com/qrcode/qrcode.phprpc.php");
				AssocArray result = (AssocArray) client.invoke("MYAPI_qrcode",new Object[]{"http://api.uihoo.com",150,"FFFFFF","000000","http://api.uihoo.com/demo/images/logo.png",0.4,3});
				Map map = result.toHashMap();
				Iterator keys = map.keySet().iterator();
				while(keys.hasNext()){
					Log.i(tagName,"Keys name ="+ keys.next().toString());
				}
				byte[] base64Obj = (byte[]) map.get("base64");
				/*
				 * Try to use the RPHApi to generate the bitmap 
				 */
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
				Log.i(tagName, "base64 length"+forbitmap.length);
				Log.i(tagName, "End");
				Toast.makeText(v.getContext(), "生成结束", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(tagName, "Except:", e.getCause());
				Toast.makeText(v.getContext(), "运行错误", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}finally{
				try {
					fromurl.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
