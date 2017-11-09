package com.zuma.smssender.dto;

import com.google.gson.Gson;
import com.zuma.smssender.enums.SmssAndPhonesRelationEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * author:Administrator
 * datetime:2017/11/9 0009 10:46
 * 指定掌游通道时的返回对象，也就是ResultDTO<T>中的T
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhangYouResult {
    private Integer apiRequestCount;//接口调用次数
    //每次调用返回的对象(code和message,如果全部成功，在该对象外层，将有标识)
    //如果失败，data中将包含失败的手机号和消息内容
    private List<ResultDTO<ErrorData>> errorResults;
}
