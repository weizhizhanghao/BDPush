package com.example.think.emulatebd.baidupush.server;

import com.example.think.emulatebd.common.util.L;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.PortUnreachableException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Created by HuangMei on 2016/12/23.
 */

public class BaiduPush {

    public final static String mUrl = "http://channel.api.duapp.com/rest/2.0/channel/";

    public final static String HTTP_METHOD_POST = "POST";
    public final static String HTTP_METHOD_GET = "GET";

    public static final String SEND_MSG_ERROR = "send_msg_error";

    private final static int HTTP_CONNECT_TIMEOUT = 10000;// 连接超时时间，10s
    private final static int HTTP_READ_TIMEOUT = 10000;// 读消息超时时间，10s

    public String mHttpMethod;// 请求方式，Post or Get
    public String mSecretKey;// 安全key

    public BaiduPush(String http_mehtod, String secret_key, String api_key) {
        this.mHttpMethod = http_mehtod;
        this.mSecretKey = secret_key;
        RestApi.mApiKey = api_key;
    }

    private String urlEncode(String str) throws UnsupportedEncodingException{
        String rc = URLEncoder.encode(str, "utf-8");
        return rc.replace("*", "%2A");
    }

    public String jsonEncode(String str){
        String rc = str.replace("\\", "\\\\");
        rc.replace("\"", "\\\"");
        rc.replace("\'", "\\\'");
        return rc;
    }

    public String PostHttpRequest(RestApi data){
        StringBuilder sb = new StringBuilder();

        String channel = data.remove(RestApi._CHANNEL_ID);
        if (channel == null){
            channel = "channel";
        }

        try {
            data.put(RestApi._TIMESTAMP,
                    Long.toString(System.currentTimeMillis() / 1000));
            data.remove(RestApi._SIGN);

            sb.append(mHttpMethod);
            sb.append(mUrl);
            sb.append(channel);

            for (Map.Entry<String, String> i : data.entrySet()){
                sb.append(i.getKey());
                sb.append('=');
                sb.append(i.getValue());
            }
            sb.append(mSecretKey);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();

            md.update(urlEncode(sb.toString()).getBytes());
            byte[] md5 = md.digest();

            sb.setLength(0);
            for (byte b : md5){
                sb.append(String.format("%02x", b & 0Xff));
            }
            data.put(RestApi._SIGN, sb.toString());

            sb.setLength(0);
            for (Map.Entry<String, String> i : data.entrySet()){
                sb.append(i.getKey());
                sb.append('=');

                sb.append(urlEncode(i.getValue()));
                sb.append('&');
            }
            sb.setLength(sb.length() - 1);
        } catch (Exception e){
            e.printStackTrace();
            L.i("PostHttpRequest Exception:" + e.getMessage());
            return SEND_MSG_ERROR;//消息发送失败，返回错误，执行重发
        }

        StringBuilder response = new StringBuilder();
        HttpRequest(mUrl + channel, sb.toString(), response);
        return response.toString();
    }

    private int HttpRequest(String url, String query, StringBuilder out){
        URL urlObj;
        HttpURLConnection connection = null;

        try {
            urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + query.length());
            connection.setRequestProperty("charset", "utf-8");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
            connection.setReadTimeout(HTTP_READ_TIMEOUT);

            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream()
            );
            wr.writeBytes(query.toString());
            wr.flush();
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = rd.readLine()) != null){
                out.append(line);
                out.append('\r');
            }
            rd.close();
        } catch (Exception e){
            e.printStackTrace();
            L.i("HttpRequest Exception:" + e.getMessage());
            out.append(SEND_MSG_ERROR);//消息发送失败，返回错误，执行重发
        }

        if (connection != null){
            connection.disconnect();
        }
        return 0;
    }

    public String QueryBindlist(String userid, String channelid){
        RestApi ra = new RestApi(RestApi.METHOD_QUERY_BIND_LIST);
        ra.put(RestApi._USER_ID, userid);
        ra.put(RestApi._CHANNEL_ID, channelid);
        return PostHttpRequest(ra);
    }

    public String SetTag(String tag, String userId){
        RestApi ra = new RestApi(RestApi.METHOD_SET_TAG);
        ra.put(RestApi._USER_ID, userId);
        ra.put(RestApi._TAG, tag);
        return PostHttpRequest(ra);
    }

    public String FetchTag(){
        RestApi ra = new RestApi(RestApi.METHOD_FETCH_TAG);
        return PostHttpRequest(ra);
    }

    public String DeleteTag(String tag, String userId){
        RestApi ra = new RestApi(RestApi.METHOD_DELETE_TAG);
        ra.put(RestApi._USER_ID, userId);
        ra.put(RestApi._TAG, tag);
        return PostHttpRequest(ra);
    }

    public String QueryUserTag(String userId){
        RestApi ra = new RestApi(RestApi.METHOD_QUERY_USER_TAG);
        ra.put(RestApi._USER_ID, userId);
        return PostHttpRequest(ra);
    }

    public String QueryDeviceType(String channelid) {
        RestApi ra = new RestApi(RestApi.METHOD_QUERY_DEVICE_TYPE);
        ra.put(RestApi._CHANNEL_ID, channelid);
        return PostHttpRequest(ra);
    }

    private final static String MSGKEY = "msgkey";

    public String PushMessage(String message, String userid) {
        RestApi ra = new RestApi(RestApi.METHOD_PUSH_MESSAGE);
        ra.put(RestApi._MESSAGE_TYPE, RestApi.MESSAGE_TYPE_MESSAGE);
        ra.put(RestApi._MESSAGES, message);
        ra.put(RestApi._MESSAGE_KEYS, MSGKEY);
        // ra.put(RestApi._MESSAGE_EXPIRES, "86400");
        // ra.put(RestApi._CHANNEL_ID, "");
        ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_USER);
        // ra.put(RestApi._DEVICE_TYPE, RestApi.DEVICE_TYPE_ANDROID);
        ra.put(RestApi._USER_ID, userid);
        return PostHttpRequest(ra);
    }

    public String PushTagMessage(String message, String tag) {
        RestApi ra = new RestApi(RestApi.METHOD_PUSH_MESSAGE);
        ra.put(RestApi._MESSAGE_TYPE, RestApi.MESSAGE_TYPE_MESSAGE);
        ra.put(RestApi._MESSAGES, message);
        ra.put(RestApi._MESSAGE_KEYS, MSGKEY);
        // ra.put(RestApi._MESSAGE_EXPIRES, "86400");
        ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_TAG);
        // ra.put(RestApi._DEVICE_TYPE, RestApi.DEVICE_TYPE_ANDROID);
        ra.put(RestApi._TAG, tag);
        return PostHttpRequest(ra);
    }

    public String PushMessage(String message) {
        RestApi ra = new RestApi(RestApi.METHOD_PUSH_MESSAGE);
        ra.put(RestApi._MESSAGE_TYPE, RestApi.MESSAGE_TYPE_MESSAGE);
        ra.put(RestApi._MESSAGES, message);
        ra.put(RestApi._MESSAGE_KEYS, MSGKEY);
        // ra.put(RestApi._MESSAGE_EXPIRES, "86400");
        ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_ALL);
        // ra.put(RestApi._DEVICE_TYPE, RestApi.DEVICE_TYPE_ANDROID);
        return PostHttpRequest(ra);
    }


    public String PushNotify(String title, String message, String userid) {
        RestApi ra = new RestApi(RestApi.METHOD_PUSH_MESSAGE);
        ra.put(RestApi._MESSAGE_TYPE, RestApi.MESSAGE_TYPE_NOTIFY);

        // notification_builder_id : default 0

        // String msg =
        // String.format("{'title':'%s','description':'%s','notification_basic_style':7}",
        // title, jsonencode(message));
        // String msg =
        // String.format("{'title':'%s','description':'%s','notification_builder_id':0,'notification_basic_style':5,'open_type':2}",
        // title, jsonencode(message));
        // String msg =
        // String.format("{'title':'%s','description':'%s','notification_builder_id':2,'notification_basic_style':7}",
        // title, jsonencode(message));

        String msg = String
                .format("{'title':'%s','description':'%s','notification_builder_id':0,'notification_basic_style':7,'open_type':2,'custom_content':{'test':'test'}}",
                        title, jsonEncode(message));

        // String msg =
        // String.format("{\"title\":\"%s\",\"description\":\"%s\",\"notification_basic_style\":\"7\"}",
        // title, jsonencode(message));
        // String msg =
        // String.format("{\"title\":\"%s\",\"description\":\"%s\",\"notification_builder_id\":0,\"notification_basic_style\":1,\"open_type\":2}",
        // title, jsonencode(message));

        System.out.println(msg);

        ra.put(RestApi._MESSAGES, msg);

        ra.put(RestApi._MESSAGE_KEYS, MSGKEY);
        ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_USER);
        // ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_ALL);
        ra.put(RestApi._USER_ID, userid);
        return PostHttpRequest(ra);
    }

    public String PushNotifyAll(String title, String message) {
        RestApi ra = new RestApi(RestApi.METHOD_PUSH_MESSAGE);
        ra.put(RestApi._MESSAGE_TYPE, RestApi.MESSAGE_TYPE_NOTIFY);

        String msg = String
                .format("{'title':'%s','description':'%s','notification_builder_id':0,'notification_basic_style':7,'open_type':2,'custom_content':{'test':'test'}}",
                        title, jsonEncode(message));

        System.out.println(msg);

        ra.put(RestApi._MESSAGES, msg);

        ra.put(RestApi._MESSAGE_KEYS, MSGKEY);
        ra.put(RestApi._PUSH_TYPE, RestApi.PUSH_TYPE_ALL);
        return PostHttpRequest(ra);
    }

    public String FetchMessage(String userid) {
        RestApi ra = new RestApi(RestApi.METHOD_FETCH_MESSAGE);
        ra.put(RestApi._USER_ID, userid);
        // ra.put(RestApi._START, "0");
        // ra.put(RestApi._LIMIT, "10");
        return PostHttpRequest(ra);
    }

    public String FetchMessageCount(String userid) {
        RestApi ra = new RestApi(RestApi.METHOD_FETCH_MSG_COUNT);
        ra.put(RestApi._USER_ID, userid);
        return PostHttpRequest(ra);
    }

    public String DeleteMessage(String userid, String msgids) {
        RestApi ra = new RestApi(RestApi.METHOD_DELETE_MESSAGE);
        ra.put(RestApi._USER_ID, userid);
        ra.put(RestApi._MESSAGE_IDS, msgids);
        return PostHttpRequest(ra);
    }
}
