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
package com.smallchill.core.meta;

import com.smallchill.core.aop.AopContext;

/**
 * @title 菜单按钮拦截器<br>
 *        可以在(菜单/按钮)前后进行操作<br>
 *        已自带事务回滚机制,无需自行设置<br>
 * @author zhuangqian
 * @email smallchill@163.com
 * @date 2016-1-23下午12:53:22
 * @copyright 2016
 */
public class AuthIntercept extends MetaTool{

	/**
	 * 初始化前操作
	 * 
	 * @param ctrl
	 */
	public void initBefore(AopContext ac) {

	}

	/**
	 * 初始化后操作
	 * 
	 * @param ctrl
	 * @param grid
	 */
	public void initAfter(AopContext ac) {

	}
	
}
