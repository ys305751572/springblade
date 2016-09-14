package com.smallchill.core.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.core.aop.AopContext;
import com.smallchill.core.base.controller.BladeController;
import com.smallchill.core.constant.Cst;
import com.smallchill.core.interfaces.ILoader;
import com.smallchill.core.interfaces.IQuery;
import com.smallchill.core.plugins.dao.Blade;
import com.smallchill.core.plugins.dao.Db;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.core.toolbox.ajax.AjaxResult;
import com.smallchill.core.toolbox.kit.CacheKit;
import com.smallchill.core.toolbox.kit.ClassKit;
import com.smallchill.core.toolbox.kit.JsonKit;
import com.smallchill.core.toolbox.kit.StrKit;

@Controller
@RequestMapping("/ztree")
public class ZTreeController extends BladeController {
	
	@RequestMapping("/open/{type}/{index}/{name}/{source}/{check}/{where}/{intercept}/{ext}/{val}")
	public ModelAndView open(@PathVariable String type, @PathVariable String index, @PathVariable String name, @PathVariable String source, @PathVariable String check, @PathVariable String where, @PathVariable String intercept, @PathVariable String ext, @PathVariable String val){
		ModelAndView view = new ModelAndView("/common/_function/_ztree.html");	
		view.addObject("type", getTypeName(type, source));
		view.addObject("index", index);
		view.addObject("name", name);
		view.addObject("source", source);
		view.addObject("check", check);
		view.addObject("where", where);
		view.addObject("intercept", intercept);
		view.addObject("ext", ext);
		view.addObject("val", val);
		return view;
	}
	
	@ResponseBody
	@SuppressWarnings("unchecked")
	@RequestMapping("/getTreeList")
	public AjaxResult getTreeList(@RequestParam String type, @RequestParam String source, @RequestParam String where, @RequestParam String intercept, @RequestParam String ext, @RequestParam String val) {	
		final String sqlSource = getSql(type, source);
		
		Map<String, Object> params = Record.createHashMap();
		if(!where.equals("0")){
			params = JsonKit.parse(where, Map.class);
		}
		
		final Map<String, Object> modelOrMap = params;
		
		IQuery _intercept = getIntercept(type);
		if(StrKit.notBlank(intercept) && !Func.equals(intercept, "0")){
			_intercept = ClassKit.newInstance(intercept);
		}
		
		AopContext ac = new AopContext();
		ac.setObject(ext);
		ac.setTips("ztree");
		
		List<Record> list = Db.init().selectList(sqlSource, modelOrMap, ac, _intercept);

		String key = "id";
		if(type.indexOf("dict") >= 0){
			key = "num";
		}
		String [] arr = val.split(",");
		for(Map<String, Object> map : list){
			for(String v : arr){
				if(Func.format(map.get(key)).equals(v) && !v.equals("0")){
					map.put("checked", "true");
				}
			}
		}
		
		return json(list);
	}
	
	@ResponseBody
	@RequestMapping("/getTreeListName")
	@SuppressWarnings("unchecked")
	public AjaxResult getTreeListName(@RequestParam String type, @RequestParam String source, @RequestParam String where, @RequestParam String val){
		type = getTypeName(type, source);
		
		final String sqlSource = getSql(type, source);
		
		Map<String, Object> params = Record.createHashMap();
		if(StrKit.notBlank(where)){
			params = JsonKit.parse(where, Map.class);
		}
		
		final Map<String, Object> modelOrMap = params;
		
		List<Map<String, Object>> list = CacheKit.get(DICT_CACHE, "ztree_list_" + type,
				new ILoader() {
					public Object load() {
						return Db.init().selectList(sqlSource, modelOrMap);
					}
				});
		
		String name = "";
		String key = "id";
		if(type.indexOf("dict") >= 0){
			key = "num";
		}
		String [] arr = val.split(",");
		for(Map<String, Object> map : list){
			for(String v : arr){
				if(Func.format(map.get(key)).equals(v)){
					name += Func.format(map.get("name")) + ",";
				}
			}
		}
		name = StrKit.removeSuffix(name, ",");
		
		return json(name);
	}
	
	private String getTypeName(String type, String source){
		if(type.indexOf("opentreeUser") >= 0){
			type = "user";
		} else if(type.indexOf("opentreeDept") >= 0){
			type = "dept";
		} else if(type.indexOf("opentreeRole") >= 0){
			type = "role";
		} else if(type.indexOf("opentree_") >= 0 || type.indexOf("opentreeDict") >= 0){
			type = "dict_" + type.replace("opentree_", "").replace("opentreeDict", "");
		} else {
			type = "diy_" + source;
		}
		return type;
	}
	
	private String getSql(String type, String source){
		String sql = "";
		if (type.indexOf("dict") >= 0) {
			String code = type.replace("dict_", "");
			sql = "select NUM as \"num\",ID as \"id\",PID as \"pId\",NAME as \"name\",(case when (pId=0 or pId is null) then 'true' else 'false' end) \"open\" from  TFW_DICT where code=" + code;
		} else if (type.equals("user")) {
			sql = "select ID as \"id\",0 as \"pId\",NAME as \"name\",(case when (pId=0 or pId is null) then 'true' else 'false' end) \"open\" from  TFW_USER where status=1";
		} else if (type.equals("dept")) {
			sql = "select ID as \"id\",PID as \"pId\",SIMPLENAME as \"name\",(case when (pId=0 or pId is null) then 'true' else 'false' end) \"open\" from  TFW_DEPT";
		} else if (type.equals("role")) {
			sql = "select ID as \"id\",0 as \"pId\",NAME as \"name\",(case when (pId=0 or pId is null) then 'true' else 'false' end) \"open\" from  TFW_ROLE";
		} else {
			sql = Blade.dao().getScript(source).getSql();
		}
		return sql;
	}
	
	private IQuery getIntercept(String type) {
		IQuery intercept = Cst.me().getDefaultQueryFactory();
		if (type.indexOf("dict") >= 0) {
			intercept = Cst.me().getDefaultSelectFactory().dictIntercept();
		} else if (type.equals("user")) {
			intercept = Cst.me().getDefaultSelectFactory().userIntercept();
		} else if (type.equals("dept")) {
			intercept = Cst.me().getDefaultSelectFactory().deptIntercept();
		} else if (type.equals("role")) {
			intercept = Cst.me().getDefaultSelectFactory().roleIntercept();
		}  
		return intercept;
	}
}
