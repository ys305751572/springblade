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
package com.smallchill.core.base.controller;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.common.base.BaseController;
import com.smallchill.core.aop.AopContext;
import com.smallchill.core.interfaces.IMeta;
import com.smallchill.core.meta.MetaIntercept;
import com.smallchill.core.plugins.dao.Blade;
import com.smallchill.core.plugins.dao.Db;
import com.smallchill.core.service.CurdService;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.kit.JsonKit;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.core.toolbox.support.Singleton;

public abstract class CurdController<M> extends BaseController {
	@Autowired
	CurdService service;

	private final BladeController ctrl = this;
	/** 自定义拦截器 **/
	private MetaIntercept intercept = null;
	private Class<M> modelClass;
	private IMeta metaFactory;
	private String controllerKey;
	private String paraPerfix;
	private Map<String, Object> switchMap;
	private Map<String, String> renderMap;
	private Map<String, String> sourceMap;
	private Map<String, Object> reverseMap;

	@SuppressWarnings("unchecked")
	private Class<M> getClazz() {
		Type t = getClass().getGenericSuperclass();
		Type[] params = ((ParameterizedType) t).getActualTypeArguments();
		return (Class<M>) params[0];
	}

	private void init() {
		this.modelClass = getClazz();
		this.metaFactory = Singleton.create(metaFactoryClass());
		this.controllerKey = metaFactory.controllerKey();
		this.paraPerfix = metaFactory.paraPerfix();
		this.switchMap = metaFactory.switchMap();
		this.renderMap = metaFactory.renderMap();
		this.sourceMap = metaFactory.sourceMap();
		this.intercept = Singleton.create(metaFactory.intercept());
		this.reverseMap = reverseMap();
	}

	public CurdController() {
		this.init();
	}

	protected abstract Class<? extends IMeta> metaFactoryClass();

	/**
	 * TODO 前端字段混淆map翻转
	 * 
	 * @return Map<String,String>
	 */
	protected Map<String, Object> reverseMap() {
		if (Func.isEmpty(switchMap)) {
			return null;
		}
		Map<String, Object> map = new HashMap<>();
		for (String key : switchMap.keySet()) {
			map.put((String) switchMap.get(key), key);
		}
		return map;
	}

	@RequestMapping("/")
	public ModelAndView index() {
		ModelAndView view = new ModelAndView(renderMap.get(KEY_INDEX));
		view.addObject("code", controllerKey);
		if (null != intercept) {
			AopContext ac = new AopContext(ctrl, view);
			intercept.renderIndexBefore(ac);
		}
		return view;
	}

	@RequestMapping(KEY_ADD)
	public ModelAndView add() {
		ModelAndView view = new ModelAndView(renderMap.get(KEY_ADD));
		view.addObject("code", controllerKey);
		if (null != intercept) {
			AopContext ac = new AopContext(ctrl, view);
			intercept.renderAddBefore(ac);
		}
		return view;
	}

	@RequestMapping(KEY_EDIT + "/{id}")
	public ModelAndView edit(@PathVariable String id) {
		ModelAndView view = new ModelAndView(renderMap.get(KEY_EDIT));
		Record rd = Record.create();
		if (StrKit.isBlank(sourceMap.get(KEY_EDIT))) {
			M model = Blade.create(modelClass).findById(id);
			rd.parseBean(model);
		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			Map<String, Object> model = this.find(sourceMap.get(KEY_EDIT), map);
			rd.parseMap(model);
		}
		if (null != intercept) {
			AopContext ac = new AopContext(ctrl, rd, view);
			intercept.renderEditBefore(ac);
		}
		view.addObject("code", controllerKey);
		view.addObject("_model", autoReverse(rd));
		view.addObject("model", JsonKit.toJson(rd));
		return view;
	}

	@RequestMapping(KEY_VIEW + "/{id}")
	public ModelAndView view(@PathVariable String id) {
		ModelAndView view = new ModelAndView(renderMap.get(KEY_VIEW));
		Record rd = Record.create();
		if (StrKit.isBlank(sourceMap.get(KEY_VIEW))) {
			M model = Blade.create(modelClass).findById(id);
			rd.parseBean(model);
		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			Map<String, Object> model = this.find(sourceMap.get(KEY_VIEW), map);
			rd.parseMap(model);
		}
		if (null != intercept) {
			AopContext ac = new AopContext(ctrl, rd, view);
			intercept.renderViewBefore(ac);
		}
		view.addObject("code", controllerKey);
		view.addObject("_model", autoReverse(rd));
		view.addObject("model", JsonKit.toJson(rd));
		return view;
	}

	@ResponseBody
	@RequestMapping(KEY_SAVE)
	public AjaxResult save() {
		M model = autoMapping();
		boolean temp = service.save(ctrl, model, modelClass, intercept);
		if (temp) {
			if (null != intercept) {
				AopContext ac = new AopContext(ctrl, model);
				AjaxResult result = intercept.saveSucceed(ac);
				return result;
			}
			return success(SAVE_SUCCESS_MSG);
		} else {
			return error(SAVE_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping(KEY_UPDATE)
	public AjaxResult update() {
		M model = autoMapping();
		boolean temp = service.update(ctrl, model, modelClass, intercept);
		if (temp) {
			if (null != intercept) {
				AopContext ac = new AopContext(ctrl, model);
				AjaxResult result = intercept.updateSucceed(ac);
				return result;
			}
			return success(UPDATE_SUCCESS_MSG);
		} else {
			return error(UPDATE_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping(KEY_REMOVE)
	public AjaxResult remove(@RequestParam String ids) {
		boolean temp = service.deleteByIds(ctrl, ids, modelClass, intercept);
		if (temp) {
			if (null != intercept) {
				AopContext ac = new AopContext(ctrl);
				ac.setId(ids);
				AjaxResult result = intercept.removeSucceed(ac);
				return result;
			}
			return success(DEL_SUCCESS_MSG);
		} else {
			return error(DEL_FAIL_MSG);
		}
	}
	
	@ResponseBody
	@RequestMapping(KEY_DEL)
	public AjaxResult del(@RequestParam String ids) {
		boolean temp = service.delByIds(ctrl, ids, modelClass, intercept);
		if (temp) {
			if (null != intercept) {
				AopContext ac = new AopContext(ctrl);
				ac.setId(ids);
				AjaxResult result = intercept.delSucceed(ac);
				return result;
			}
			return success(DEL_SUCCESS_MSG);
		} else {
			return error(DEL_FAIL_MSG);
		}
	}
	
	@ResponseBody
	@RequestMapping(KEY_RESTORE)
	public AjaxResult restore(@RequestParam String ids) {
		boolean temp = service.restoreByIds(ctrl, ids, modelClass, intercept);
		if (temp) {
			if (null != intercept) {
				AopContext ac = new AopContext(ctrl);
				ac.setId(ids);
				AjaxResult result = intercept.restoreSucceed(ac);
				return result;
			}
			return success(RESTORE_SUCCESS_MSG);
		} else {
			return error(RESTORE_FAIL_MSG);
		}
	}

	@ResponseBody
	@RequestMapping(KEY_LIST)
	public Object list() {
		Integer page = getParameterToInt("page");
		Integer rows = getParameterToInt("rows");
		String where = getParameter("where");
		String sidx = getParameter("sidx");
		String sord = getParameter("sord");
		String sort = getParameter("sort");
		String order = getParameter("order");
		if (StrKit.notBlank(sidx)) {
			sort = sidx + " " + sord
					+ (StrKit.notBlank(sort) ? ("," + sort) : "");
		}
		if (StrKit.isBlank(sourceMap.get(KEY_INDEX))) {
			throw new RuntimeException(modelClass.getName() + "没有配置数据源！");
		}
		Object grid = service.paginate(page, rows,
				sourceMap.get(KEY_INDEX), where, sort, order,
				intercept, ctrl);
		return grid;
	}

	/**
	 * TODO 根据子类的paraPerfix,switchMap实现主表的自动映射
	 * 
	 * @return M
	 */
	protected M autoMapping() {
		if (Func.isAllEmpty(paraPerfix, switchMap)) {
			return mapping(modelClass);
		} else if (Func.isEmpty(paraPerfix) && !Func.isEmpty(switchMap)) {
			return mapping(switchMap, modelClass);
		} else if (Func.isEmpty(switchMap) && !Func.isEmpty(paraPerfix)) {
			return mapping(paraPerfix, modelClass);
		} else {
			return mapping(paraPerfix, switchMap, modelClass);
		}
	}

	/**
	 * TODO 将返回的model根据switchMap实现自动翻转
	 * 
	 * @param model
	 * @return Object
	 */
	protected Map<String, Object> autoReverse(Object model) {
		if (Func.isEmpty(reverseMap)) {
			return reverse(model);
		} else {
			return reverse(reverseMap, model);
		}
	}

	/**
	 * TODO 翻转bean
	 * 
	 * @param switchMap
	 *            翻转map
	 * @param model
	 *            bean
	 * @return Map<String,Object>
	 */
	protected Map<String, Object> reverseBean(Map<String, Object> reverseMap,
			Object model) {
		if (Func.isEmpty(reverseMap)) {
			return reverse(model);
		} else {
			return reverse(reverseMap, model);
		}
	}

	private Map<String, Object> find(String source, Map<String, Object> map) {
		if (source.indexOf("select") == -1) {
			return findOneById(source, map);
		} else {
			return findOneBySql(source, map);
		}
	}

	private Map<String, Object> findOneBySql(String sql, Map<String, Object> map) {
		Map<String, Object> model = Db.init().selectOne(sql, map);
		return Func.caseInsensitiveMap(model);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> findOneById(String sqlId, Map<String, Object> map) {
		Map<String, Object> model =  Blade.dao().selectSingle(sqlId, map, Map.class); //Db.init().selectOneBySqlId(sqlId, map);
		return Func.caseInsensitiveMap(model);
	}

}
