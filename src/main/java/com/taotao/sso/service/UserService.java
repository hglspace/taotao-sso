package com.taotao.sso.service;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.service.RedisService;
import com.taotao.sso.mapper.UserMapper;
import com.taotao.sso.pojo.User;

@Service
public class UserService {

	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private RedisService redisService;
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final static String TT_KEY = "TT_KEY";
	
	public Boolean checkParam(String param, Integer type) {
		User user = new User();
		switch (type) {
		case 1:user.setUsername(param);

			break;
		case 2:user.setPhone(param);

			break;
		case 3:user.setEmail(param);

			break;
		default:return null;
		}
		return this.userMapper.checkParam(user).intValue() == 1;
	}

	public Boolean doRegister(User user) {
		user.setPassword(DigestUtils.md5Hex(user.getPassword()));
		Integer count = this.userMapper.doRegister(user);
		if(count.intValue() == 1){
			return true;
		}
		return false;
	}

	public String doLogin(String username, String password) throws JsonProcessingException {
        User user = this.userMapper.doLogin(username);
        if(user == null){
        	   return null;
        }
		if(!StringUtils.equals(DigestUtils.md5Hex(password), user.getPassword())){
			return null;
		}
		String token = TT_KEY + DigestUtils.md5Hex(username+System.currentTimeMillis());
		this.redisService.set(token, MAPPER.writeValueAsString(user), 60 * 30);
		return token;
	}

	public User queryUserByToken(String token){
		String jsonData = this.redisService.get(token);
		if(StringUtils.isEmpty(jsonData)){
			return null;
		}
		try{
			User user = MAPPER.readValue(jsonData, User.class);
			//刷新时间，非常重要
			this.redisService.expire(token, 60 * 30);
			return user;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
