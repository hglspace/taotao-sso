package com.taotao.sso.mapper;

import com.taotao.sso.pojo.User;

public interface UserMapper {

	public Integer checkParam(User user);

	public Integer doRegister(User user);

	public User doLogin(String username);

}
