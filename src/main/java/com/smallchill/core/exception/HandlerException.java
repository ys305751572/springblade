/**
 * Copyright (c) 2015-2016, Chill Zhuang 庄骞 (smallchill@163.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smallchill.core.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.common.vo.ShiroUser;
import com.smallchill.core.base.controller.BladeController;
import com.smallchill.core.constant.Const;
import com.smallchill.core.constant.ConstShiro;
import com.smallchill.core.shiro.ShiroKit;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.kit.LogKit;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.core.toolbox.log.LogManager;

//@ControllerAdvice
public class HandlerException {
	private static final Logger log = LoggerFactory.getLogger(BladeController.class);
	private AjaxResult result = new AjaxResult();

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public Object exceptionHandler(Exception ex, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		String url = Const.error500Path;
		String msg = ex.getMessage();
		Object resultModel = null;
		try {
			String header = request.getHeader("X-Requested-With");
			boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
			if (ex.getClass() == HttpRequestMethodNotSupportedException.class) {
				url = Const.error500Path;// 请求方式不允许抛出的异常,后面可自定义页面
			} else if (ex.getClass() == NoPermissionException.class) {
				url = Const.noPermissionPath;// 无权限抛出的异常
				msg = ConstShiro.NO_PERMISSION;
			} else if (ex.getClass() == NoUserException.class) {
				url = Const.loginRealPath;// session过期抛出的异常
				msg = ConstShiro.NO_USER;
			}
			if (isAjax) {
				result.addFail(msg);
				resultModel = result;
			} else {
				ModelAndView view = new ModelAndView(url);
				view.addObject("error", msg);
				view.addObject("class", ex.getClass());
				view.addObject("method", request.getRequestURI());
				resultModel = view;
			}
			try {
				if (StrKit.notBlank(msg)) {
					ShiroUser user = ShiroKit.getUser();
					LogManager.doLog(user, msg, "异常日志", request, false);
				}
			} catch (Exception logex) {
				LogKit.logNothing(logex);
			}
			return resultModel;
		} catch (Exception exception) {
			log.error(exception.getMessage(), exception);
			return resultModel;
		} finally {
			log.error(msg, ex);
		}
	}
}
