package com.intel.cedar.util;

import org.apache.commons.codec.net.URLCodec;

import com.thoughtworks.xstream.core.util.Base64Encoder;

public class Utils {
    public static String encodeURL(String url){
        try{
            if(url.indexOf("%") < 0)
                return  new URLCodec().encode(url);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return url;
    }
    
    public static String decodeURL(String url){
        try{
            if(url.indexOf("%") >= 0)
                return  new URLCodec().decode(url);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return url;
    }
    
    public static byte[] decodeBase64(String str) throws Exception{
        Base64Encoder encoder =  new Base64Encoder();
        return encoder.decode(str);
    }
    
    public static String encodeBase64(byte[] d) throws Exception{
        Base64Encoder encoder =  new Base64Encoder();
        return encoder.encode(d);
    }
}
