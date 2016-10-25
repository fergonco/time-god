create table app.user_roles(name varchar references app."Developer"(name), role varchar);
insert into app.user_roles values('fergonco', 'developer');
insert into app.user_roles values('michogar', 'developer');
insert into app.user_roles values('vicgonco', 'developer');
