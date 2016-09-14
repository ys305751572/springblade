/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
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
package com.smallchill.core.base.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.common.vo.ShiroUser;
import com.smallchill.core.constant.Const;
import com.smallchill.core.constant.ConstCache;
import com.smallchill.core.constant.ConstCurd;
import com.smallchill.core.constant.ConstShiro;
import com.smallchill.core.constant.Cst;
import com.smallchill.core.exception.NoPermissionException;
import com.smallchill.core.exception.NoUserException;
import com.smallchill.core.interfaces.IQuery;
import com.smallchill.core.modules.support.BeanInjector;
import com.smallchill.core.shiro.ShiroKit;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.file.BladeFile;
import com.smallchill.core.toolbox.grid.GridManager;
import com.smallchill.core.toolbox.kit.BeanKit;
import com.smallchill.core.toolbox.kit.LogKit;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.core.toolbox.log.LogManager;

/**
 * @author James Zhan, Chill Zhuang
 */
public class BladeController implements ConstCurd, ConstCache{
	private static final Logger log = LoggerFactory.getLogger(BladeController.class);

	@Resource
	private HttpServletRequest request;
	
	@Resource
	private HttpServletResponse response;

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public Object exceptionHandler(Exception ex, HttpServletResponse response, HttpServletRequest request) throws IOException {
		AjaxResult result = new AjaxResult();
		String url = Const.error500Path;
		String msg = ex.getMessage();
		Object resultModel = null;
		try {
			if (ex.getClass() == HttpRequestMethodNotSupportedException.class) {
				url = Const.error500Path;// 请求方式不允许抛出的异常,后面可自定义页面
			} else if (ex.getClass() == NoPermissionException.class) {
				url = Const.noPermissionPath;// 无权限抛出的异常
				msg = ConstShiro.NO_PERMISSION;
			} else if (ex.getClass() == NoUserException.class) {
				url = Const.loginRealPath;// session过期抛出的异常
				msg = ConstShiro.NO_USER;
			}
			if (isAjax()) {
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
				if(StrKit.notBlank(msg)){
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
	
	public HttpServletRequest getRequest() {
		return this.request;
	}

	public boolean isAjax(){
		String header = request.getHeader("X-Requested-With");
		boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(header);
		return isAjax;
	}

	public String getParameter(String name) {
		return request.getParameter(name);
	}

	public String getParameter(String name, String defaultValue) {
		String result = request.getParameter(name);
		return StrKit.notBlank(result) ? result : defaultValue;
	}

	public String getParaToDecode(String para) {
		return Func.decodeUrl(request.getParameter(para));
	}

	public Integer getParameterToInt(String name) {
		return Func.toInt(request.getParameter(name));
	}

	public Integer getParameterToInt(String name, Integer defaultValue) {
		return Func.toInt(request.getParameter(name), defaultValue);
	}

	public String getContextPath() {
		return request.getContextPath();
	}

	public String getSqlId(Class<?> clazz, String sqlId) {
		return new StringBuffer().append(clazz.getName()).append(".").append(sqlId).toString();
	}

	/** ============================     mapping    =================================================  */
	
	/**
	 * 表单值映射为javabean
	 * 
	 * @param beanClass
	 *            javabean.class
	 * @return T
	 */
	public <T> T mapping(Class<T> beanClass) {
		return paraInject(beanClass);
	}

	/**
	 * 表单值映射为javabean
	 * 
	 * @param switchMap
	 *            字段混淆Map  map.put("前端字段","数据库字段");
	 * @param beanClass
	 *            javabean.class
	 * @return T
	 */
	public <T> T mapping(Map<String, Object> switchMap, Class<T> beanClass) {
		return paraInject(beanClass, switchMap);
	}

	/**
	 * 表单值映射为javabean
	 * 
	 * @param paraPerfix
	 *            name前缀
	 * @param beanClass
	 *            javabean.class
	 * @return T
	 */
	public <T> T mapping(String paraPerfix, Class<T> beanClass) {
		return paraInject(beanClass, paraPerfix);
	}

	/**
	 * 表单值映射为javabean
	 * 
	 * @param paraPerfix
	 *            name前缀
	 * @param switchMap
	 *            字段混淆Map map.put("前端字段","数据库字段");
	 * @param beanClass
	 *            javabean.class
	 * @return T
	 */
	public <T> T mapping(String paraPerfix, Map<String, Object> switchMap, Class<T> beanClass) {
		return paraInject(beanClass, switchMap, paraPerfix);
	}

	/**
	 * 表单值映射为Maps
	 * 
	 * @return Maps
	 */
	public Record getMaps() {
		return paraMapsInject();
	}

	/**
	 * 表单值映射为Maps
	 * 
	 * @param switchMap 字段混淆Map  map.put("前端字段","数据库字段");
	 * @return Maps
	 */
	public Record getMaps(Map<String, Object> switchMap) {
		return paraMapsInject(switchMap);
	}

	/**
	 * 表单值映射为Maps
	 * 
	 * @param paraPerfix  name前缀
	 * @return Maps
	 */
	public Record getMaps(String paraPerfix) {
		return paraMapsInject(paraPerfix);
	}

	/**
	 * 表单值映射为Maps
	 * 
	 * @param paraPerfix name前缀
	 * @param switchMap 字段混淆Map  map.put("前端字段","数据库字段");
	 * @return Maps
	 */
	public Record getMaps(String paraPerfix, Map<String, Object> switchMap) {
		return paraMapsInject(switchMap, paraPerfix);
	}
	
	private <T> T paraInject(Class<T> beanClass) {
		return (T) BeanInjector.inject(beanClass, this.request);
	}

	private <T> T paraInject(Class<T> beanClass, Map<String, Object> switchMap) {
		return (T) BeanInjector.inject(beanClass, switchMap, this.request);
	}

	private <T> T paraInject(Class<T> beanClass, String paraPerfix) {
		return (T) BeanInjector.inject(beanClass, paraPerfix, this.request);
	}

	private <T> T paraInject(Class<T> beanClass, Map<String, Object> switchMap, String paraPerfix) {
		return (T) BeanInjector.inject(beanClass, switchMap, paraPerfix, this.request);
	}
	
	private Record paraMapsInject() {
		return BeanInjector.injectMaps(this.request);
	}

	private Record paraMapsInject(Map<String, Object> switchMap) {
		return BeanInjector.injectMaps(switchMap, this.request);
	}

	private Record paraMapsInject(String paraPerfix) {
		return BeanInjector.injectMaps(paraPerfix, this.request);
	}

	private Record paraMapsInject(Map<String, Object> switchMap, String paraPerfix) {
		return BeanInjector.injectMaps(switchMap, paraPerfix, this.request);
	}

	public <T> T reverse(Object model) {
		return paraReverse(null, model);
	}

	public <T> T reverse(Map<String, Object> reverseMap, Object model) {
		return paraReverse(reverseMap, model);
	}

	@SuppressWarnings("unchecked")
	private <T> T paraReverse(Map<String, Object> reverseMap, Object model) {
		return (T) BeanKit.reverse(reverseMap, model);
	}
	
	/**============================     file    =================================================  */
	
	/**
	 * 获取BladeFile封装类
	 * @param file
	 * @return
	 */
	public BladeFile getFile(MultipartFile file){
		return getFile(file, null);
	}
	
	/**
	 * 获取BladeFile封装类
	 * @param file
	 * @param path
	 * @return
	 */
	public BladeFile getFile(MultipartFile file, String path){
		return new BladeFile(file, path);
	}
	
	/**
	 * 获取BladeFile封装类
	 * @param files
	 * @return
	 */
	public List<BladeFile> getFiles(List<MultipartFile> files){
		return getFiles(files, null);
	}
	
	/**
	 * 获取BladeFile封装类
	 * @param files
	 * @param path
	 * @return
	 */
	public List<BladeFile> getFiles(List<MultipartFile> files, String path){
		List<BladeFile> list = new ArrayList<>();
		for (MultipartFile file : files){
			list.add(new BladeFile(file, path));
		}
		return list;
	}

	
	/**   
	 * 返回ajaxresult
	 * @param data
	 * @return AjaxResult
	*/
	public AjaxResult json(Object data) {
		return new AjaxResult().success(data);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param data
	 * @param message
	 * @return AjaxResult
	*/
	public AjaxResult json(Object data, String message) {
		return json(data).setMessage(message);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param data
	 * @param message
	 * @param code
	 * @return AjaxResult
	*/
	public AjaxResult json(Object data, String message, int code) {
		return json(data, message).setCode(code);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param message
	 * @return AjaxResult
	*/
	public AjaxResult success(String message) {
		return new AjaxResult().addSuccess(message);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param message
	 * @return AjaxResult
	*/
	public AjaxResult error(String message) {
		return new AjaxResult().addError(message);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param message
	 * @return AjaxResult
	*/
	public AjaxResult warn(String message) {
		return new AjaxResult().addWarn(message);
	}
	
	/**   
	 * 返回ajaxresult
	 * @param message
	 * @return AjaxResult
	*/
	public AjaxResult fail(String message) {
		return new AjaxResult().addFail(message);
	}
	
	
	/** ============================     paginate    =================================================  */
	
	private Object basepage(String slaveName, String source, IQuery intercept){
		Integer page = getParameterToInt("page", 1);
		Integer rows = getParameterToInt("rows", 10);
		String where = getParameter("where", "");
		String sidx =  getParameter("sidx", "");
		String sord =  getParameter("sord", "");
		String sort =  getParameter("sort", "");
		String order =  getParameter("order", "");
		if (StrKit.notBlank(sidx)) {
			sort = sidx + " " + sord
					+ (StrKit.notBlank(sort) ? ("," + sort) : "");
		}
		Object grid = GridManager.paginate(slaveName, page, rows, source, where, sort, order, intercept, this);
		return grid;
	}
	
	/**
	 * @param 数据源
	 * @return
	 */
	protected Object paginate(String source){
		return basepage(null, source, Cst.me().getDefaultPageFactory());
	}
	
	/**
	 * @param 数据源
	 * @param 自定义拦截器
	 * @return
	 */
	protected Object paginate(String source, IQuery intercept){
		return basepage(null, source, intercept);
	}
	
	/**
	 * @param 数据库别名
	 * @param 数据源
	 * @return
	 */
	protected Object paginate(String slaveName, String source){
		return basepage(slaveName, source, Cst.me().getDefaultPageFactory());
	}
	
	/**
	 * @param 数据库别名
	 * @param 数据源
	 * @param 自定义拦截器
	 * @return
	 */
	protected Object paginate(String slaveName, String source, IQuery intercept){
		return basepage(slaveName, source, intercept);
	}

}
