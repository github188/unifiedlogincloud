package com.zuma.smssender.template.sendsms;

import com.zuma.smssender.config.CommonSmsAccount;
import com.zuma.smssender.config.Config;
import com.zuma.smssender.dto.ErrorData;
import com.zuma.smssender.dto.ResultDTO;
import com.zuma.smssender.dto.CommonCacheDTO;
import com.zuma.smssender.dto.request.ZhangYouSendSmsRequest;
import com.zuma.smssender.dto.response.sendsms.sync.ZhangYouSendSmsResponse;
import com.zuma.smssender.enums.error.ErrorEnum;
import com.zuma.smssender.enums.error.ZhangYouErrorEnum;
import com.zuma.smssender.exception.SmsSenderException;
import com.zuma.smssender.form.SendSmsForm;
import com.zuma.smssender.util.CacheUtil;
import com.zuma.smssender.util.CodeUtil;
import com.zuma.smssender.util.EnumUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * author:Administrator
 * datetime:2017/11/8 0008 15:46
 * 掌游，策略实现类
 */
@Slf4j
@Component
public class ZhangYouSendSmsTemplate extends SendSmsTemplate<ZhangYouSendSmsRequest,ZhangYouSendSmsResponse> {


    /**
     * 直接获取response，组合下面两个方法。
     * 并解析response对象，组装为ResultDTO<ErrorData>
     * @param account
     * @param phones
     * @param smsMessae
     * @return
     */
    public ResultDTO<ErrorData> getResponse(CommonSmsAccount account,
                                             String phones, String smsMessae,
                                             SendSmsForm sendSmsForm,Long recordId) {
        //转为 请求对象
        ZhangYouSendSmsRequest request = toRequestObject(account, phones, smsMessae);
        ZhangYouSendSmsResponse response = null;
        //发送请求，并返回ZhangYouResponse对象
        try {
            response = sendHttpRequest(request,Config.ZHANGYOU_SEND_SMS_URL);
        } catch (SmsSenderException e) {
            //自定义异常捕获到,日志已经记录
            //返回异常返回对象
            return ResultDTO.error(e.getCode(), e.getMessage(),new ErrorData(phones,smsMessae));
        }
        //判断是否成功-根据api的response
        if(!ZhangYouErrorEnum.SUCCESS.getCode().equals(response.getCode())){
            //根据掌游异常码获取异常枚举
            ZhangYouErrorEnum errorEnum = EnumUtil.getByCode(response.getCode(), ZhangYouErrorEnum.class);
            return ResultDTO.error(errorEnum,new ErrorData(phones,smsMessae));
        }

        //流水号处理
        //构建缓存对象
        CommonCacheDTO cacheDTO = CommonCacheDTO.builder()
                .id(response.getId())//流水号
                .platformId(sendSmsForm.getPlatformId())//平台id
                .timestamp(sendSmsForm.getTimestamp())//时间戳
                .phones(phones)//手机号
                .smsMessage(smsMessae)//短信消息
                .recordId(recordId)//该次发送记录数据库id
                .build();
        //存入缓存,key使用 掌游前缀 + 流水号
        CacheUtil.put(Config.ZHANGYOU_PRE + cacheDTO.getId(),cacheDTO);

        //成功
        return ResultDTO.success();
    }

    /**
     * 根据帐号、手机号s，短信消息 转 请求对象
     *
     * @param account
     * @param phones
     * @param msg
     * @return
     */
    public ZhangYouSendSmsRequest toRequestObject(CommonSmsAccount account, String phones, String msg) {
        //签名
        String sign = CodeUtil.stringToMd5(account.getAKey() + account.getCKey());
        //创建请求对象
        return ZhangYouSendSmsRequest.builder()
                .sid(account.getAKey())
                .cpid(account.getBKey())
                .sign(sign)
                .mobi(phones)
                .msg(CodeUtil.stringToUrlEncode(CodeUtil.stringToBase64(msg)))
                .build();
    }


    /**
     * 将返回的string转为对应response对象
     *
     * @param result
     * @return
     */
    public ZhangYouSendSmsResponse stringToResponseObject(String result) {
        try {
            //根据| 分割，获取[0]code 和[1]流水号
            String[] temp = StringUtils.split(result, "|");
            return ZhangYouSendSmsResponse.builder()
                    .code(temp[0])
                    .id(temp[1])
                    .build();
        } catch (Exception e) {
            log.error("【掌游sendSms】返回的string转为response对象失败.resultString={},error={}", result, e.getMessage(), e);
            throw new SmsSenderException(ErrorEnum.STRING_TO_RESPONSE_ERROR);
        }
    }


}
