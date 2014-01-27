select * from (
  select a.con_id, i, o, (select count(*) from con_close cc where cc.con_id = a.con_id) as closed_count
  from
      (select con_id, agg_blob(bytes) i from tf
      where dir = 'in'
      group by con_id) a
  join
      (select con_id, agg_blob(bytes) o from tf
      where dir = 'out'
      group by con_id) b
  on a.con_id = b.con_id
) d where closed_count = 0;


truncate table con;
truncate table tf;
truncate table con_close;
truncate table con_eof;
truncate table con_exc;
truncate table q_hist;