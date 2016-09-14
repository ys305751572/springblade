list
===
select t.*,
		d.name DIC_F_IT_LX 
	from 
		tb_tfw_tzgg t 
		left join (select num,name from tfw_dict where code=102)d on t.f_it_lx=d.num
        		
data
===
select t.*,
		d.name DIC_F_IT_LX 
	from 
		tb_tfw_tzgg t 
		left join (select num,name from tfw_dict where code=102)d on t.f_it_lx=d.num 
		left join tfw_attach a on t.F_IT_TP=a.ID
where t.F_IT_XL = #{id}
		
diy
===
select NUM as "num",
	ID as "id",
	PID as "pId",
	NAME as "name",
	(case when (pId=0 or pId is null) then 'true' else 'false' end) "open" 
from  TFW_DICT
where CODE = 102