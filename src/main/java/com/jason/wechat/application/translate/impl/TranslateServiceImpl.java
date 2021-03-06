package com.jason.wechat.application.translate.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jason.framework.mapper.JsonMapper;
import com.jason.framework.util.EncodeUtils;
import com.jason.wechat.application.translate.TranslateService;
import com.jason.wechat.infrastruture.http.HttpRequester;
import com.jason.wechat.infrastruture.http.HttpRespons;


/**
 * 百度翻译
 * 请参看：http://developer.baidu.com/wiki/index.php?title=%E5%B8%AE%E5%8A%A9%E6%96%87%E6%A1%A3%E9%A6%96%E9%A1%B5/%E7%99%BE%E5%BA%A6%E7%BF%BB%E8%AF%91/%E7%BF%BB%E8%AF%91API
 * 
 * @author Jason
 * @data 2015-1-2 下午08:31:11
 */
@Service
public class TranslateServiceImpl implements TranslateService {
	
	private static final Logger logger = LoggerFactory.getLogger(TranslateServiceImpl.class);

	@Override
	public String translate(String q) {
		String url = "http://openapi.baidu.com/public/2.0/bmt/translate?client_id={client_id}&q={q}&from=auto&to=auto";
		String apiKey = "igavT9osFwtoRpZ58hfop3mO";
		
		url = url.replace("{client_id}", apiKey);
		url = url.replace("{q}", EncodeUtils.urlEncode(q));
		
		String s = null;
		HttpRespons httpRespons =  HttpRequester.sendGet(url);
		int statusCode = httpRespons.getStatusCode();
		if(HttpStatus.SC_OK == statusCode){
			String content = httpRespons.getContent();
			s = parseTranslate(content);
		}else{
			logger.error("请求返回报错 不是200！而是"+statusCode);
		}
		return s;
	}

	/**
	 * 解析
	 * @param json 返回的字符串
	 * @return String
	 */
	private static String parseTranslate(String json) {
		String str = "暂时木有结果";
		try {
            JsonNode rootNode = JsonMapper.readTree(json);// 读取Json
            
            String from = JsonMapper.asText(rootNode, "from");
            String to = JsonMapper.asText(rootNode, "to");
            
            String error_code = JsonMapper.asText(rootNode, "error_code");
            String error_msg = JsonMapper.asText(rootNode, "error_msg");
            if(StringUtils.isNotBlank(error_code)){
            	logger.error("调用百度翻译有误！error_code："+error_code+" error_msg:"+error_msg);
            }
            
            JsonNode trans_result = JsonMapper.path(rootNode, "trans_result");
            
            JsonNode result = trans_result.get(0);
            if(null != result){
            	String src = JsonMapper.asText(result, "src");
                String dst = JsonMapper.asText(result, "dst");
                logger.info("parseTranslate.....from:"+from+" to"+to+" src:"+src+" dst:"+dst);
                str = dst;
            }
		} catch (Exception e) {
			logger.error("parse translate is error!", e);
		}
		return str;
	}
}