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
package com.smallchill.core.plugins.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.beetl.sql.core.SQLManager;

import com.smallchill.core.aop.AopContext;
import com.smallchill.core.constant.Cst;
import com.smallchill.core.interfaces.IQuery;
import com.smallchill.core.plugins.connection.ConnectionPlugin;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.core.toolbox.kit.StrKit;
import com.smallchill.core.toolbox.support.BladePage;

@SuppressWarnings({"unchecked","rawtypes"})
public class Db {
	private static Map<String, Db> pool = new ConcurrentHashMap<String, Db>();
	
	private volatile SQLManager sql = null;
	
	public static Db init() {
		return init(ConnectionPlugin.init().MASTER);
	}

	public static Db init(String name) {
		Db db = pool.get(name);
		if (null == db) {
			synchronized (Db.class) {
				db = pool.get(name);
				if (null == db) {
					db = new Db(name);
					pool.put(name, db);
				}
			}
		}
		return db;
	}
	
	private Db(String dbName) {
		this.sql = ConnectionPlugin.init().getPool().get(dbName);
	}

	private Db() {}
	
	private SQLManager getSqlManager() {
		if (null == sql) {
			synchronized (Db.class) {
				sql = ConnectionPlugin.init().getPool().get(ConnectionPlugin.init().MASTER);
			}
		}
		return sql;
	}
	
	
	/************   ↓↓↓   ********     通用     *********   ↓↓↓   ****************/
	
	/**
	 * 根据sql新增数据
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public int insert(String sqlTemplate, Object modelOrMap){
		return getSqlManager().executeUpdate(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 根据sql修改数据
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public int update(String sqlTemplate, Object modelOrMap){
		return getSqlManager().executeUpdate(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 根据sql删除数据
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public int delete(String sqlTemplate, Object modelOrMap){
		return getSqlManager().executeUpdate(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 获取一条数据
	 * @param sqlTemplate	sql语句
	 * @return
	 */
	public Record selectOne(String sqlTemplate){
		return queryMap(sqlTemplate, Record.create());
	}
	
	/**
	 * 获取一条数据
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public Record selectOne(String sqlTemplate, Object modelOrMap){
		return queryMap(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 获取一条数据
	 * @param sqlTemplate	sql语句
	 * @return
	 */
	public List<Record> selectList(String sqlTemplate){	
		return queryListMap(sqlTemplate, Record.create());
	}
	
	/**
	 * 获取多条数据
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public List<Record> selectList(String sqlTemplate, Object modelOrMap){	
		return queryListMap(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 根据表名、主键获取一条数据
	 * @param tableName	表名
	 * @param pkValue	主键值
	 * @return
	 */
	public Record findById(String tableName, String pkValue) {
		return selectOneBy(tableName, "id = #{id}", Record.create().set("id", pkValue));
	}
	
	/**
	 * 根据表名、主键名、主键值获取一条数据
	 * @param tableName	表名
	 * @param pk		主键名
	 * @param pkValue	主键值
	 * @return
	 */
	public Record findById(String tableName, String pk, String pkValue) {
		return selectOneBy(tableName, pk + " = #{id}", Record.create().set("id", pkValue));
	}
	
	/**
	 * 获取一条数据
	 * @param tableName	 表名
	 * @param where		 条件
	 * @param modelOrMap 实体类或map
	 * @return
	 */
	public Record selectOneBy(String tableName, String where, Object modelOrMap){
		String sqlTemplate = Func.format("select * from {} where {} ", tableName, where);
		return selectOne(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 获取list map
	 * @param tableName	 表名
	 * @param where		 条件
	 * @param modelOrMap 实体类或map
	 * @return
	 */
	public List<Record> selectListBy(String tableName, String where, Object modelOrMap){
		String sqlTemplate = Func.format("select * from {} where {} ", tableName, where);
		return selectList(sqlTemplate, modelOrMap);
	}
	
	/**
	 * 获取Integer
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public Integer queryInt(String sqlTemplate, Object modelOrMap){
		List<Integer> list = getSqlManager().execute(sqlTemplate, Integer.class, modelOrMap, 1, 1);
		if(list.size() == 0){
			return 0;
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * 获取Integer集合
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public List<Integer> queryListInt(String sqlTemplate, Object modelOrMap){
		List<Integer> list = getSqlManager().execute(sqlTemplate, Integer.class, modelOrMap);
		return list;
	}
	
	/**
	 * 获取字符串
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public String queryStr(String sqlTemplate, Object modelOrMap){
		List<String> list = getSqlManager().execute(sqlTemplate, String.class, modelOrMap, 1, 1);
		if(list.size() == 0){
			return "";
		} else {
			return list.get(0);
		}
	}
	
	/**
	 * 获取字符串集合
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public List<String> queryListStr(String sqlTemplate, Object modelOrMap){
		List<String> list = getSqlManager().execute(sqlTemplate, String.class, modelOrMap);
		return list;
	}
	
	/**
	 * 获取map
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public Record queryMap(String sqlTemplate, Object modelOrMap){
		List<Map> list = getSqlManager().execute(sqlTemplate, Map.class, modelOrMap, 1, 1);
		if(list.size() == 0){
			return null;
		} else {
			return Record.parse(list.get(0));
		}
	}	
	
	/**
	 * 获取map集合
	 * @param sqlTemplate	sql语句
	 * @param modelOrMap	实体类或map
	 * @return
	 */
	public List<Record> queryListMap(String sqlTemplate, Object modelOrMap){
		List<Map> list = getSqlManager().execute(sqlTemplate, Map.class, modelOrMap);
		List<Record> rd = new ArrayList<>();
		for (Map m : list){
			rd.add(Record.parse(m));
		}
		return rd;
	}
	
	/** 查询aop返回单条数据
	 * @param sqlTemplate
	 * @param modelOrMap
	 * @param ac
	 * @return
	 */
	public Record selectOne(String sqlTemplate, Map<String, Object> param, AopContext ac) {
		return selectOne(sqlTemplate, param, ac, Cst.me().getDefaultQueryFactory());
	}
	
	/**查询aop返回多条数据
	 * @param sqlTemplate
	 * @param modelOrMap
	 * @param ac
	 * @return
	 */
	public List<Record> selectList(String sqlTemplate, Map<String, Object> param, AopContext ac) {
		return selectList(sqlTemplate, param, ac, Cst.me().getDefaultQueryFactory());
	}
	
	/** 查询aop返回单条数据
	 * @param sqlTemplate
	 * @param modelOrMap
	 * @param ac
	 * @param intercept
	 * @return
	 */
	public Record selectOne(String sqlTemplate, Map<String, Object> param, AopContext ac, IQuery intercept) {
		ac.setSql(sqlTemplate);
		ac.setCondition("");
		ac.setParam(param);
		if (null != intercept) {
			intercept.queryBefore(ac);
			sqlTemplate = (StrKit.notBlank(ac.getWhere())) ? ac.getWhere() : (sqlTemplate + " " + ac.getCondition());
		}
		Record rst = selectOne(sqlTemplate, param);
		if (null != intercept) {
			ac.setObject(rst);
			intercept.queryAfter(ac);
		}
		return rst;
	}
	
	/**查询aop返回多条数据
	 * @param sqlTemplate
	 * @param modelOrMap
	 * @param ac
	 * @param intercept
	 * @return
	 */
	public List<Record> selectList(String sqlTemplate, Map<String, Object> param, AopContext ac, IQuery intercept) {
		ac.setSql(sqlTemplate);
		ac.setCondition("");
		ac.setParam(param);
		if (null != intercept) {
			intercept.queryBefore(ac);
			sqlTemplate = (StrKit.notBlank(ac.getWhere())) ? ac.getWhere() : (sqlTemplate + " " + ac.getCondition());
		}
		List<Record> rst = selectList(sqlTemplate, param);
		if (null != intercept) {
			ac.setObject(rst);
			intercept.queryAfter(ac);
		}
		return rst;
	}
	
	/************   ↑↑↑   ********     通用     *********   ↑↑↑   ****************/
	
	
	/**
	 * 新增一条数据
	 * @param rd		rd
	 * @return
	 */
	public int save(Record rd){
		return save(rd.getTableName(), rd.getPkName(), rd);
	}
	
	/**
	 * 修改一条数据
	 * @param tableName	表名
	 * @param pk		主键名
	 * @param rd		msps
	 * @return
	 */
	public int update(Record rd){
		return update(rd.getTableName(), rd.getPkName(), rd);
	}
	
	/**
	 * 新增一条数据
	 * @param tableName	表名
	 * @param pk		主键名
	 * @param rd		rd
	 * @return
	 */
	public int save(String tableName, String pk, Record rd) {
		if(Func.isOneEmpty(tableName, pk)){
			throw new RuntimeException("表名或主键不能为空!");
		}
		String mainSql = " insert into {} ({}) values ({})";
		pk = (String) Func.getValue(pk, "ID");
		if(Func.isOracle()){
			String pkValue = rd.getStr(pk);
			if(pkValue.indexOf(".nextval") > 0){
				Map<String, Object> map = selectOne("select " + pkValue + " as PK from dual");
				Object val = map.get("PK");
				rd.set(pk, val);
			}
		}
		StringBuilder fields = new StringBuilder();
		StringBuilder values = new StringBuilder();
		for(String key : rd.keySet()){
			fields.append(key + ",");
			values.append("#{" + key + "},");
		}
		String sqlTemplate = Func.format(mainSql, tableName, StrKit.removeSuffix(fields.toString(), ","), StrKit.removeSuffix(values.toString(), ","));
		int cnt = insert(sqlTemplate, rd);
		if(cnt > 0 && Func.isMySql()){
			Object pkValue = rd.get(pk);
			if(Func.isEmpty(pkValue)){
				Map<String, Object> map = selectOne(" select LAST_INSERT_ID() as PK ");
				Object val = map.get("PK");
				rd.set(pk, val);
			}
		}
		return cnt;
	}
	
	/**
	 * 修改一条数据
	 * @param tableName	表名
	 * @param pk		主键名
	 * @param rd		map
	 * @return
	 */
	public int update(String tableName, String pk, Record rd) {
		if(Func.isOneEmpty(tableName, pk)){
			throw new RuntimeException("表名或主键不能为空!");
		}
		pk = (String) Func.getValue(pk, "ID");
		String mainSql = " update {} set {} where {} = #{" + pk + "}";
		StringBuilder fields = new StringBuilder();
		for(String key : rd.keySet()){
			if(!key.equals(pk)){
				fields.append(key + " = #{" + key + "},");
			}
		}
		String sqlTemplate = Func.format(mainSql, tableName, StrKit.removeSuffix(fields.toString(), ","), pk);
		return update(sqlTemplate, rd);
	}
	
	/**
	 * 根据表名、字段名、值删除数据
	 * @param table	表名
	 * @param col	字段名
	 * @param ids	字段值集合(1,2,3)
	 * @return
	 */
	public int deleteByIds(String table, String col, String ids) {
		String sqlTemplate = " DELETE FROM " + table + " WHERE " + col + " IN (#{join(ids)}) ";
		Record paras = Record.create().set("ids", ids.split(","));
		int result = getSqlManager().executeUpdate(sqlTemplate, paras);
		return result;
	}
	
	
	/**
	 * 获取list
	 * @param model 实体类
	 * @param start 页号
	 * @param size	数量
	 * @return
	 */
	public <T> List<T> getList(Object model, int start, int size) {
		List<T> all = (List<T>) getSqlManager().template(model, (start - 1) * size + 1, size);
		return all;
	}
	

	/**
	 * 获取list
	 * @param sqlTemplate sql语句
	 * @param clazz	返回类型
	 * @param paras	参数
	 * @param start	页号
	 * @param size	数量
	 * @return
	 */
	public <T> List<T> getList(String sqlTemplate, Class<?> clazz, Object paras, int start, int size) {
		List<T> all = (List<T>) getSqlManager().execute(sqlTemplate, clazz, paras, (start - 1) * size + 1, size);
		return all;
	}
	
	/**
	 * 分页
	 * @param sqlTemplate sql语句
	 * @param paras	参数
	 * @param start	页号
	 * @param size	数量
	 * @return
	 */
	public <T> BladePage<T> paginate(String sqlTemplate, Object paras, int start, int size){
		List<T> rows = getList(sqlTemplate, Map.class, paras, start, size);
		long count = queryInt(" SELECT COUNT(*) CNT FROM (" + sqlTemplate + ") a", paras).longValue();
		BladePage<T> page = new BladePage<>(rows, start, size, count);
		return page;
	}
	
	/**
	 * 分页
	 * @param sqlTemplate sql语句
	 * @param clazz	返回类型
	 * @param paras	参数
	 * @param start	页号
	 * @param size	数量
	 * @return
	 */
	public <T> BladePage<T> paginate(String sqlTemplate, Class<?> clazz, Object paras, int start, int size){
		List<T> rows = getList(sqlTemplate, clazz, paras, start, size);
		long count = queryInt(" SELECT COUNT(*) CNT FROM (" + sqlTemplate + ") a", paras).longValue();
		BladePage<T> page = new BladePage<>(rows, start, size, count);
		return page;
	}
	
	/**
	 * 是否存在
	 * 
	 * @param sqlTemplate
	 * @param paras
	 * @return
	 */
	public boolean isExist(String sqlTemplate, Object modelOrMap) {
		int count = getSqlManager().execute(sqlTemplate, Map.class, modelOrMap).size();
		if (count != 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 根据sqlId获取beetlsql的sql语句
	 * @param sqlId
	 * @return
	 */
	public String getSql(String sqlId){
		return getSqlManager().getScript(sqlId).getSql();
	}
}
