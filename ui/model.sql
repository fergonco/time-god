create table app.users (name varchar primary key references app."Developer"(name), password varchar) ;
insert into app.users values('fergonco', md5('boh'));
insert into app.users values('michogar', md5('boh'));
insert into app.users values('vicgonco', md5('boh'));
create table app.user_roles(name varchar, role varchar);
insert into app.user_roles values('fergonco', 'developer');
insert into app.user_roles values('michogar', 'developer');
insert into app.user_roles values('vicgonco', 'developer');
