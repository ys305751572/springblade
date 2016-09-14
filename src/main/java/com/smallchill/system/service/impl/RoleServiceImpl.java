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
package com.smallchill.system.service.impl;

import org.springframework.stereotype.Service;

import com.smallchill.core.base.service.BaseService;
import com.smallchill.core.plugins.dao.Blade;
import com.smallchill.core.plugins.dao.Db;
import com.smallchill.core.toolbox.Func;
import com.smallchill.core.toolbox.Record;
import com.smallchill.system.model.Role;
import com.smallchill.system.service.RoleService;

@Service
public class RoleServiceImpl extends BaseService<Role> implements RoleService {

	@Override
	public int findLastNum(String id) {
		try{
			Blade blade = Blade.create(Role.class);
			Role rloe = blade.findFirstBy("pId = #{pId} order by num desc", Record.create().set("pId", id));
			return rloe.getNum() + 1;
		}
		catch(Exception ex){
			return 1;
		}
	}

	@Override
	public boolean saveAuthority(String ids, String roleId) {
		Db dao = Db.init();
		dao.deleteByIds("TFW_RELATION", "ROLEID", roleId);
		
		String sql = "";
		String insertSql = "";
		String union_all = "";
		String[] id = ids.split(",");
		String dual = (Func.isOracle()) ? " from dual " : "";
		for (int i = 0; i < id.length; i++) {
			union_all = (i < id.length - 1) ? " union all " : "";
			sql += " (select " + id[i] + " menuId," + roleId + " roleId "
					+ dual + ")" + union_all;
		}

		if (Func.isOracle()) {
			sql = "select SEQ_RELATION.nextval,i.* from (" + sql + ") i";
			insertSql = "insert into TFW_RELATION(id,menuId,roleId) ";
		} else {
			sql = "select i.* from (" + sql + ") i";
			insertSql = "insert into TFW_RELATION(menuId,roleId) ";
		}

		int cnt = dao.update(insertSql + sql, null);
		return cnt > 0;
	}

	@Override
	public int getParentCnt(String id) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT");
		sb.append("(CASE WHEN ");
		sb.append("	(select (CASE when (PID=0 or PID is null) then ID else 0 end) as ID from TFW_ROLE where ID=#{id})>0 ");
		sb.append("THEN 1 ");
		sb.append("ELSE");
		sb.append("	(select count(*) from TFW_RELATION where ROLEID=(select (CASE when (PID=0 or PID is null) then ID else PID end) as ID from TFW_ROLE where ID=#{id})) ");
		sb.append("END) CNT");
		if (Func.isOracle()) {
			sb.append(" from dual");
		}
		Object cnt = Db.init().selectOne(sb.toString(), Record.create().set("id", id)).get("CNT");
		return Func.toInt(cnt, 0);
	}

}
