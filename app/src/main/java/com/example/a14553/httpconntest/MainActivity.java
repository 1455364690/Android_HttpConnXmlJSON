package com.example.a14553.httpconntest;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    String path_xml ="http://sunjiahui.eyling.cn/get_data.xml?_360safeparam=89472046";
    String path_json ="http://sunjiahui.eyling.cn/get_data.json?_360safeparam=107406812";
    public static final int SHOW_RESPONSE = 0;
    private TextView responseText;
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    // 在这里进行UI操作，将结果显示到界面上
                    responseText.setText(response);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendRequest = findViewById(R.id.send_request);
        responseText = findViewById(R.id.response);
        sendRequest.setOnClickListener(this);
    }
    public void onClick(View v){
        if (v.getId() == R.id.send_request) {
            //sendRequestWithHttpURLConnection();
            sendRequestWithHttpURLConnectionXmlPull();
        }

    }
    private void sendRequestWithHttpURLConnection() {
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(path_xml);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    // 将服务器返回的结果存放到Message中
                    message.obj = response.toString();
                    handler.sendMessage(message);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    public void sendRequestWithHttpURLConnectionXmlPull(){
        // 开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(path_json);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    // 下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String res = response.toString();
                    String temp=parseJSONWithGSON(res);
                    //String temp=parseJSONWithJSONObject(res);
                    //String temp=parseXMLWithSAX(res);
                    //String temp=parseXMLWithPull(res);
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    // 将服务器返回的结果存放到Message中
                    message.obj = temp;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    private String parseXMLWithPull(String xmlData) {
        String res = "";
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            String id = "";
            String name = "";
            String version = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    // 开始解析某个结点
                    case XmlPullParser.START_TAG: {
                        if ("id".equals(nodeName)) {
                            id = xmlPullParser.nextText();
                            res+="id="+id+"\n";
                        } else if ("name".equals(nodeName)) {
                            name = xmlPullParser.nextText();
                            res+="name"+name+"\n";
                        } else if ("version".equals(nodeName)) {
                            version = xmlPullParser.nextText();
                            res+="version="+version+"\n";
                        }
                        break;
                    }
                    // 完成解析某个结点
                    case XmlPullParser.END_TAG: {
                        if ("app".equals(nodeName)) {
                            Log.d("MainActivity", "id is " + id);
                            Log.d("MainActivity", "name is " + name);
                            Log.d("MainActivity", "version is " + version);
                        }
                        break;
                    }
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
    private String parseXMLWithSAX(String xmlData) {
        String res="";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            ContentHandler handler = new ContentHandler();
            // 将ContentHandler的实例设置到XMLReader中
            xmlReader.setContentHandler(handler);
            // 开始执行解析
            xmlReader.parse(new InputSource(new StringReader(xmlData)));
            res = handler.backString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    private String parseJSONWithJSONObject(String jsonData) {
        String res="JSON\n";
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String version = jsonObject.getString("version");
                Log.d("MainActivity", "id is " + id);
                Log.d("MainActivity", "name is " + name);
                Log.d("MainActivity", "version is " + version);
                res+="id="+id+"\n";
                res+="name="+name+"\n";
                res+="version="+version+"\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    private String parseJSONWithGSON(String jsonData) {
        String res="";
        Gson gson = new Gson();
        List<App> appList = gson.fromJson(jsonData, new TypeToken<List<App>>() {}.getType());
        for (App app : appList) {
            Log.d("MainActivity", "id is " + app.getId());
            Log.d("MainActivity", "name is " + app.getName());
            Log.d("MainActivity", "version is " + app.getVersion());
            res+="id="+app.getId()+"\n";
            res+="name="+app.getName()+"\n";
            res+="version="+app.getVersion()+"\n";
        }
        return res;
    }
    public String parseXmlByDom(InputStream inputStream){
        String result = "";
        //
        DocumentBuilderFactory bdf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document doc = null;
        try{
            //
            builder = bdf.newDocumentBuilder();
        }catch (ParserConfigurationException e){
            e.printStackTrace();
        }
        try{
            //解析字符流
            doc = builder.parse(inputStream);
        }catch (SAXException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        Element ele = doc.getDocumentElement();
        //获取所有的fruit节点

        NodeList nl = ele.getElementsByTagName("fruit");
        if(nl!=null&&nl.getLength()!=0){
            for(int i=0;i<nl.getLength();i++){
                Element entry = (Element)nl.item(i);
                //
                result += "name:"+entry.getAttribute("name")+"-->"+entry.getTextContent()+"\n";
            }
        }
        return result;
    }

}
/*

    public void conn(){
        try{
            //本行代码可能产生MalformedURLException，new出一个URL对象
            URL url = new URL(path_xml);
            //本行代码可能产生IOException，得到HttpURLConnection的实例
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //设置Http请求使用的方法，GET或者POST
            connection.setRequestMethod("GET");
            //设置连接超时、读取超时的毫秒数
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            //获取到服务器返回的输入流
            InputStream in = connection.getInputStream();
            //调用disconnect()方法将这个HTTP连接关闭掉
            connection.disconnect();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }*/