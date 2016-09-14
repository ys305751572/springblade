package com.smallchill.system.model;

import java.util.Date;

import org.beetl.sql.core.annotatoin.AutoID;
import org.beetl.sql.core.annotatoin.SeqID;
import org.beetl.sql.core.annotatoin.Table;

import com.smallchill.core.annotation.BindID;
import com.smallchill.core.base.model.BaseModel;

@Table(name = "tfw_attach")
@BindID(name = "id")
@SuppressWarnings("serial")
public class Attach extends BaseModel {
	private Integer id;
	private String code;
	private Integer creater;
	private String name;
	private Integer status;
	private String url;
	private Date createtime;

	@AutoID
	@SeqID(name = "SEQ_ATTACH")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getCreater() {
		return creater;
	}

	public void setCreater(Integer creater) {
		this.creater = creater;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

}
