
create table if not exists con (
  id bigint,
  time timestamp,
  desc varchar,
  rt_id bigint,
  primary key (id)
);

create table if not exists tf (
  id bigint,
  time timestamp,
  bytes blob,
  con_id bigint,
  dir varchar,
  primary key (id),
  foreign key (con_id) references con(id)
);

create table if not exists con_close (
  con_id bigint,
  time timestamp,
  dir varchar,
  foreign key (con_id) references con(id)
);

create table if not exists con_eof (
  con_id bigint,
  dir varchar,
  time timestamp,
  foreign key (con_id) references con(id)
);

create table if not exists con_exc (
  id bigint,
  con_id bigint,
  time timestamp,
  type varchar,
  message varchar,
  details varchar,
  primary key (id),
  foreign key (con_id) references con(id)
);

create table if not exists q_hist (
  id bigint,
  time timestamp,
  q varchar(4000),
  res varchar(4000),
  primary key (id),
);

create sequence if not exists run_id_seq;
create sequence if not exists con_id_seq;
create sequence if not exists tf_id_seq;
create sequence if not exists con_exc_id_seq;
create sequence if not exists q_hist_id_seq;

create aggregate if not exists agg_blob for "net.kkolyan.jhole2.log.ConcatBlob";
