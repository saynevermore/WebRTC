package com.codebouy.webrtcforandroid;

import java.util.Random;


//随机字符串类:根据传入长度生成随机ID
public class RandomString {
    static final String str = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final Random rnd = new Random();

    static public String length(Integer length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(str.charAt(rnd.nextInt(str.length())));
            //append()对字符串类型的对象进行追加
            //charAt()返回这个字符串的指定索引处的char值，第一个char值的索引为0.
            //random.nextInt(int n)产生[0,n)之间的随机值
        }
        return sb.toString();
    }
}
