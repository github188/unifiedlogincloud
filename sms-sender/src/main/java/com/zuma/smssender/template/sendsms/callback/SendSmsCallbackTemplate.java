package com.zuma.smssender.template.sendsms.callback;

import com.zuma.smssender.dto.CommonCacheDTO;
import com.zuma.smssender.dto.ErrorData;
import com.zuma.smssender.dto.ResultDTO;
import com.zuma.smssender.entity.Platform;
import com.zuma.smssender.enums.BooleanStatusEnum;
import com.zuma.smssender.enums.error.ErrorEnum;
import com.zuma.smssender.exception.SmsSenderException;
import com.zuma.smssender.service.PlatformService;
import com.zuma.smssender.service.SmsSendRecordService;
import com.zuma.smssender.util.CacheUtil;
import com.zuma.smssender.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * author:Administrator
 * datetime:2017/11/10 0010 09:55
 * 发送短信回调模版类
 * T，每个平台对应的回调对象
 */
@Slf4j
@Component
public abstract class SendSmsCallbackTemplate<T> {
    

    private static PlatformService platformService;
    private static SmsSendRecordService smsSendRecordService;
    @Autowired
    public  void setup(PlatformService platformService, SmsSendRecordService smsSendRecordService) {
        SendSmsCallbackTemplate.platformService = platformService;
        SendSmsCallbackTemplate.smsSendRecordService = smsSendRecordService;
    }

    private HttpClientUtil httpClientUtil = HttpClientUtil.getInstance();


    /**
     * 回调处理方法
     * @param response
     * @return
     */
    public  boolean callbackHandle(T response) {
        //根据返回对象,获取到之前存入缓存的key
        String key = getKey(response);
        //获取到缓存中的数据
        CommonCacheDTO cacheDTO = getCache(key);
        //保存异步回调对象信息到数据库
        saveResponse(cacheDTO.getRecordId(), response);

        //根据数据拼接出 返回对象
        ResultDTO<ErrorData> resultDTO = getResultDTO(cacheDTO, response);
        //发送返回对象给调用者
        sendCallback(resultDTO,cacheDTO.getPlatformId(),0);
        return true;
    }

    /**
     * 保存异步回调对象到数据库
     * @param recordId 数据库记录id
     * @param response 异步响应对象
     */
    void saveResponse(Long recordId,T response) {
        smsSendRecordService.updateStatusOfAsync(recordId, BooleanStatusEnum.TRUE,response.toString());
    }



    //从T中获取到id（发送短信流水号）,根据id获取key
    abstract String getKey(T response);
    
    //获取缓存中的信息
    CommonCacheDTO getCache(String key) {
        CommonCacheDTO result = CacheUtil.getAndDelete(key, CommonCacheDTO.class);
        if (result == null) {
            log.error("【发送短信异步回调】缓存过期，无法获取对应信息.");
            throw new SmsSenderException(ErrorEnum.CACHE_EXPIRE);
        }
        return result;
    }
    
    //将缓存信息和T组装为ResultDTO对象
    abstract ResultDTO<ErrorData> getResultDTO(CommonCacheDTO cacheDTO,T response);

    /**
     * 将ResultDTO发送给对应调用者
     * @param resultDTO 返回对象
     * @param platformId 返回给的平台id
     * @param sendInfoRetryCount 发送给调用者回调url的请求失败，重试次数
     */
    void sendCallback(ResultDTO<ErrorData> resultDTO, Long platformId,Integer sendInfoRetryCount){
        //获取对应平台的回调url
        Platform platform = platformService.findOne(platformId);
        String callbackUrl = platform.getCallbackUrl();
        try {
            httpClientUtil.doPostForString(callbackUrl, resultDTO);
        } catch (Exception e) {
            //重试
            log.error("【给调用者发送回调】发送失败.重试次数={},调用者={},error={}",sendInfoRetryCount,platform,e.getMessage(),e);
            //超过3次后，不再发送，抛出异常记录
            if(sendInfoRetryCount >= 3){
                throw new SmsSenderException(ErrorEnum.SEND_CALLBACK_TO_PLATFORM_ERROR);
            }
            //未超过三次重试
            sendCallback(resultDTO,platformId,++sendInfoRetryCount);
        }
    }
}
