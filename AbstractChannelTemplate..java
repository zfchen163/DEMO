

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 三方通道调用模板方法
 */
@RefreshScope
public abstract class AbstractChannelTemplate<T extends AbstractReqModel, R extends AbstractRspModel> implements IChannelService<T, R> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    private String APP_URL;
    private String APP_KEY;
    public String APP_SECRET;

    @Override
    public R invoke(T request) throws ChannelServiceException {
        try {
            // 组织参数
            JSONObject resData = params(request);

            // 初始化地址信息
            String url = initUrl();

            // 获取签名数据
            Map<String, String> map = this.getSignMap();

            // 签名
            String sign = this.getSign(map, resData, url);

            // 设置请求头部
            Headers headers = setHeaders(map, sign);

            // 发起请求，网络通信
            Response response = this.doRequest(headers, resData, url);

            // 转换请求结果
            R r = this.convertResult(response, url);

            return r;
        } catch (Exception e) {
            logger.error("请求渠道过程中抓到了异常信息:{}", e.getLocalizedMessage());
            e.printStackTrace();
            handleException(e);
        }
        return null;
    }

    protected abstract String initUrl();

    private void handleException(Exception e) throws ChannelServiceException {
        logger.error("调用通道服务发送异常了,原因:{}", e.getMessage());
        if (e instanceof ChannelServiceException) {
            throw new ChannelServiceException(((ChannelServiceException) e).getCode(), e.getMessage());
        } else if (e instanceof IOException) {
            throw new ChannelServiceException(ResultCodeEnum.CHANNEL_SERVICE_EXCEPTION);
        }
    }

    private Headers setHeaders(Map<String, String> map, String sign) {
        return Headers.of(map)
                .newBuilder()
                .add("sign", sign)
                .add("Content-Type", org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .build();

    }

    /**
     * 校验数据合法性
     */
    private void verify(Response response, String url, String requestBody) {
        Map<String, String> mapReponse = new HashMap<>();
        mapReponse.put("appkey", response.header("appkey"));
        mapReponse.put("nonce", response.header("nonce"));
        mapReponse.put("timestamp", response.header("timestamp"));
        String signatureResponse = SignatureUtil.genAppkeySign(HttpMethod.POST.name(), url, mapReponse, APP_SECRET, requestBody);
        if (signatureResponse.equals(response.header("es-auth-sign"))) {
            logger.info("url:{},验证数据合法:{}", url, requestBody);
        } else {
            logger.error("url:{},验证数据非法:{}", url, requestBody);
        }
    }


    /**
     */
    private String getSign(Map<String, String> map, JSONObject resData, String url) {
        // 1. 获取请求的 http method
        return SignatureUtil.genAppkeySign(HttpMethod.POST.name(), url, map, APP_SECRET, JSONUtil.toJsonStr(JSONUtil.parseObj(resData, false)));
    }


    /**
     * Http头部参数设置
     * <p>
     * es-auth-appkey:{appKey}
     * es-auth-nonce:a26ecb9cda4d7d8a（初始向量,由客户端随机产生）
     * es-auth-timestamp:1635479733167(时间戳)
     * </p>
     */
    private Map<String, String> getSignMap() {
        Map<String, String> map = new HashMap<>();
        map.put("es-auth-appkey", APP_KEY);
        //es-auth-nonce为16为随机数字加字母字符串
        map.put("es-auth-nonce", RandomStringUtils.randomAlphanumeric(16));
        //13位时间戳
        map.put("es-auth-timestamp", System.currentTimeMillis() + "");
        return map;

    }


    /**
     * 发起请求，网络通信
     */
    private Response doRequest(Headers headers, JSONObject resData, String url) throws IOException {
        logger.info("-----------------HTTP数据请求开始-----------------");
        String httpUrl = APP_URL.concat(url);
        logger.info("headers={},requestData={},url={}", headers, resData.toString(), httpUrl);
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();
        MediaType mediaType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        RequestBody body = RequestBody.create(resData.toString(), mediaType);
        Request request = new Request.Builder().url(httpUrl).method(HttpMethod.POST.name(), body).headers(headers).build();
        Response response = client.newCall(request).execute();
        logger.info("headers={},requestData={},url={},responseData={}", headers, resData, httpUrl, JSONUtil.toJsonStr(JSONUtil.parseObj(response, false)));
        logger.info("------------------HTTP数据请求结束-----------------------");
        return response;
    }

    protected JSONObject params(T t) {
        return JSONUtil.parseObj(t);
    }

    /**
     * 转换请求结果
     */
    protected R convertResult(Response response, String url) throws IOException, ChannelServiceException {
        String errorCode = response.header("CVSS-Error");
        if (StringUtils.isNotBlank(errorCode)) {
            ResultCodeEnum resultCodeEnum = ResultCodeEnum.toEnum(Integer.parseInt(errorCode));
            if (Objects.nonNull(resultCodeEnum)) {
                logger.error("调用渠道服务发生业务异常如下:{},{}", errorCode, resultCodeEnum.getMsg());
                throw new ChannelServiceException(resultCodeEnum);
            }
        }

        if (response.code() == 200) {
            ResponseBody body = response.body();
            // okhttp body.string()只能调用一次
            String requestBody = body.string();
            JSONObject res = JSONUtil.parseObj(requestBody);
            // 校验合法性
            this.verify(response, url, requestBody);
            // 校验合法可以返回
            Type type = getClass().getGenericSuperclass();
            Type trueType = ((ParameterizedType) type).getActualTypeArguments()[1];
            return res.toBean(trueType);
        }
        return null;
    }
}
