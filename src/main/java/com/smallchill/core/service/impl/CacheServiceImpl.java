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
package com.smallchill.core.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.smallchill.core.interfaces.ILoader;
import com.smallchill.core.plugins.dao.Blade;
import com.smallchill.core.plugins.dao.Db;
import com.smallchill.core.service.CacheService;
import com.smallchill.core.toolbox.kit.CacheKit;

@Service
public class CacheServiceImpl implements CacheService{

	public Map<String, Object> findOne(String cacheName, String key, final String sql) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectOne(sql);
			}
		});
	}

	public Map<String, Object> findOne(String cacheName, String key,
			final String sql, final Object modelOrMap) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectOne(sql, modelOrMap);
			}
		});
	}

	public Map<String, Object> findOneBySqlId(String cacheName, String key,
			final String sqlId) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Blade.dao().select(sqlId, Map.class, null);//selectOneBySqlId(sqlId, null);
			}
		});
	}

	public Map<String, Object> findOneBySqlId(String cacheName, String key,
			final String sqlId, final Object modelOrMap) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Blade.dao().select(sqlId, Map.class, modelOrMap);
			}
		});
	}

	public List<Map<String, Object>> find(String cacheName, String key,
			final String sql) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectList(sql);
			}
		});
	}

	public List<Map<String, Object>> find(String cacheName, String key,
			final String sql, final Object modelOrMap) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Db.init().selectList(sql, modelOrMap);
			}
		});
	}

	public List<Map<String, Object>> findBySqlId(String cacheName, String key,
			final String sqlId) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Blade.dao().select(sqlId, Map.class, null);
			}
		});
	}

	public List<Map<String, Object>> findBySqlId(String cacheName, String key,
			final String sqlId, final Object modelOrMap) {
		return CacheKit.get(cacheName, key, new ILoader() {
			@Override
			public Object load() {
				return Blade.dao().select(sqlId, Map.class, modelOrMap);
			}
		});
	}

}
