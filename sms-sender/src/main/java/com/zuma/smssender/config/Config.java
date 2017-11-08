package com.zuma.smssender.config;

/**
 * author:Administrator
 * datetime:2017/11/8 0008 09:09
 * 配置类
 */
public class Config {
    //单次最多发送手机号数
    public static final Integer MAX_PHONE_NUM = 30;
    //掌游短信内容最大长度
    public static final Integer ZHANGYOU_SMS_MAX_LEN = 210;

    //regxep
    //移动手机号正则
    public static final String YIDONG_PHONE_NUMBER_REGEXP = "(^((13[4-9])|147|(15[0|1|2|7|8|9])|178|(18[2|3|4|7|8]))\\\\d{8}$)|(^1705\\\\d{7}$)";
    //联通手机号正则
    public static final String LIANTONG_PHONE_NUMBER_REGEXP = "(^((13[0-2])|145|(15[5|6])|17[5|6]|(18[5|6]))\\\\d{8}$)|(^1709\\\\d{7}$)";
    //电信手机号正则
    public static final String DIANXIN_PHONE_NUMBER_REGEXP = "(^(133|153|173|177|(18[0|1|9]))\\\\d{8}$)|(^1(349|700)\\\\d{7}$)";

    //URL
    //掌游发送短信接口url
    public static final String ZHANGYOU_SEND_SMS_URL = "";
    //宽信发送短信接口url
    public static final String KUANXIN_SEND_SMS_URL = "";
    //群正发送短信接口url
    public static final String QUNZHENG_SEND_SMS_URL = "";

    //分隔符
    //手机号数组分隔符
    public static final String PHONES_SEPARATOR = ",";
    //短信内容分隔符
    public static final String SMS_MESSAGE_SEPARATOR = "!&";



}