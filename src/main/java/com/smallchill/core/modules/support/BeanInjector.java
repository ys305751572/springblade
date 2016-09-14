package com.smallchill.core.modules.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.smallchill.core.constant.Const;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.core.toolbox.kit.BeanKit;
import com.smallchill.core.toolbox.kit.StrKit;

/**
 * javabean 、 maps映射
 *
 */
public class BeanInjector {

	public static final <T> T inject(Class<T> beanClass, HttpServletRequest request) {
		try {
			return BeanKit.mapToBeanIgnoreCase(request.getParameterMap(), beanClass);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static final <T> T inject(Class<T> beanClass, Map<String, Object> switchMap, HttpServletRequest request) {
		try {
			return BeanKit.mapToBeanIgnoreCase(BeanKit.map2Map(request.getParameterMap(), switchMap), beanClass);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static final <T> T inject(Class<T> beanClass, String recordName, HttpServletRequest request) {
		try {
			Map<String, Object> map = injectPara(recordName, request);
			return BeanKit.mapToBeanIgnoreCase(map, beanClass);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static final <T> T inject(Class<T> beanClass, Map<String, Object> switchMap, String recordName, HttpServletRequest request) {
		try {
			Map<String, Object> map = injectPara(recordName, request);
			return BeanKit.mapToBeanIgnoreCase(BeanKit.map2Map(map, switchMap), beanClass);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public static final Record injectMaps(HttpServletRequest request) {
		return Record.parse(request.getParameterMap());
	}

	public static final Record injectMaps(Map<String, Object> switchMap, HttpServletRequest request) {
		return Record.parse(BeanKit.map2Map(request.getParameterMap(), switchMap));
	}

	public static final Record injectMaps(String recordName, HttpServletRequest request) {
		Map<String, Object> map = injectPara(recordName, request);
		return Record.parse(map);
	}

	public static final Record injectMaps(Map<String, Object> switchMap, String recordName, HttpServletRequest request) {
		Map<String, Object> map = injectPara(recordName, request);
		return Record.parse(BeanKit.map2Map(map, switchMap));
	}

	private static final Map<String, Object> injectPara(String recordName, HttpServletRequest request) {
		Map<String, String[]> paramMap = request.getParameterMap();
		Map<String, Object> map = new HashMap<>();
		String start = recordName.toLowerCase() + ".";
		String[] value = null;
		for (Entry<String, String[]> param : paramMap.entrySet()) {
			if (!param.getKey().toLowerCase().startsWith(start)) {
				continue;
			}
			value = param.getValue();
			if (ArrayUtils.isNotEmpty(value) && null != value[0]) {
				Object o = value[0];
				map.put(StringUtils.removeStart(param.getKey().toLowerCase(), start).toLowerCase(), o);

			} else {
				map.put(StringUtils.removeStart(param.getKey().toLowerCase(), start).toLowerCase(), null);
			}
		}
		String versionL = request.getParameter(Const.OPTIMISTIC_LOCK.toLowerCase());
		String versionU = request.getParameter(Const.OPTIMISTIC_LOCK);
	    if (StrKit.notBlank(versionL)){
			map.put(Const.OPTIMISTIC_LOCK.toLowerCase(), Func.toInt(versionL) + 1);
		} else if(StrKit.notBlank(versionU)){
			map.put(Const.OPTIMISTIC_LOCK.toLowerCase(), Func.toInt(versionU) + 1);
		}
		return map;
	}


}
