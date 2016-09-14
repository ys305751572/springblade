package com.smallchill.system.model;

import org.beetl.sql.core.annotatoin.AutoID;
import org.beetl.sql.core.annotatoin.SeqID;
import org.beetl.sql.core.annotatoin.Table;

import com.smallchill.core.annotation.BindID;
import com.smallchill.core.base.model.BaseModel;

@Table(name = "tfw_generate")
@BindID(name = "id")
@SuppressWarnings("serial")
public class Generate extends BaseModel {

	private Integer id;
	private String modelname;
	private String name;
	private String realpath;
	private String packagename;
	private String pkname;
	private String tablename;
	private String tips;

	@AutoID
	@SeqID(name = "SEQ_GENERATE")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getModelname() {
		return modelname;
	}

	public void setModelname(String modelname) {
		this.modelname = modelname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRealpath() {
		return realpath;
	}

	public void setRealpath(String realpath) {
		this.realpath = realpath;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getPkname() {
		return pkname;
	}

	public void setPkname(String pkname) {
		this.pkname = pkname;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

}
