package com.smallchill.core.controller;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.smallchill.core.base.controller.BladeController;
import com.smallchill.core.constant.Cst;
import com.smallchill.core.toolbox.Func;

@Controller
@RequestMapping("/view")
public class ViewController extends BladeController {

	@RequestMapping("/**")
	public ModelAndView view(){
		String url = getRequest().getRequestURI().replace(Cst.me().getContextPath(), "").replace("/view", "").replace(".html", "") + ".html";
		ModelAndView view = new ModelAndView(url);
		
		Map<String, String[]> parameterMap = getRequest().getParameterMap();
		String[] value = null;
		for (Entry<String, String[]> param : parameterMap.entrySet()) {
			value = param.getValue();
			if (!Func.isOneEmpty(value, value[0])) {
				Object o = value[0];
				view.addObject(param.getKey(), o);
			}
		}
		
		return view;
	}
	
}
