package com.taotao.sso.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.utils.CookieUtils;
import com.taotao.sso.pojo.User;
import com.taotao.sso.service.UserService;

@RequestMapping("user")
@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	private final static String  COOK_NAME = "TAOTAO_TOKEN";
	@RequestMapping(value="register",method=RequestMethod.GET)
	public String toRegister(){
		return "register";
	}
	
	@RequestMapping(value="login",method = RequestMethod.GET)
	public String toLogin(){
		return "login";
	}
	
	@RequestMapping(value="{param}/{type}",method = RequestMethod.GET)
	public ResponseEntity<Boolean> checkParam(@PathVariable("param")String param,
			@PathVariable("type") Integer type){
		   try{
			   Boolean bool = this.userService.checkParam(param,type);
			   if(null == bool){
				   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
			   }
			   return ResponseEntity.ok(bool);
		   }catch(Exception e){
			   e.printStackTrace();
		   }
		   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	
	@RequestMapping(value="doRegister",method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> doRegister(@Valid User user, BindingResult bindingResult){
		Map<String,Object> result = new HashMap<String,Object>();
		try{
			if(bindingResult.hasErrors()){
				List<String> errorList = new ArrayList<String>();
				List<ObjectError> allErrors = bindingResult.getAllErrors();
				for (ObjectError objectError : allErrors) {
					String defaultMessage = objectError.getDefaultMessage();
					errorList.add(defaultMessage);
				}
				result.put("status", "400");
				result.put("data", StringUtils.join(errorList,"|"));
				return result;
			}
			Boolean bool = this.userService.doRegister(user);
			if(bool){
				result.put("status", "200");
			}else{
				result.put("status", "500");
				result.put("data", "哈哈");
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("status", "500");
			result.put("data", "哈哈");
		}
		return result;
	}
	
	@RequestMapping(value="doLogin",method=RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> doLogin(User user,
			HttpServletRequest request,
			HttpServletResponse response){
		Map<String,Object> result = new HashMap<String,Object>();
		try{
			String token = this.userService.doLogin(user.getUsername(),user.getPassword());
			if(StringUtils.isEmpty(token)){
				result.put("status", 250);
				result.put("data", "用户名或密码错误");
			}else{
				result.put("status", 200);
				CookieUtils.setCookie(request, response, COOK_NAME, token);
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("status", 500);
			result.put("data", "系统繁忙，请稍后再试");
		}
		return ResponseEntity.ok(result);
	}
	
	@RequestMapping(value="{token}",method = RequestMethod.GET)
	public ResponseEntity<User> queryUserByToken(@PathVariable("token") String token){
		try{
			User user = this.userService.queryUserByToken(token);
			if(user == null){
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			return ResponseEntity.status(HttpStatus.OK).body(user);
		}catch(Exception e){
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
}
