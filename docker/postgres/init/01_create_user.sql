create user produtowebapi password 'webapi123';
create user produtoetl password 'etl123';
create user flyway password 'flyway123';

grant connect on database produto to produtowebapi;
grant connect on database produto to produtoetl;
grant connect on database produto to flyway;

grant usage on schema public to produtowebapi;
grant usage on schema public to produtoetl;
grant create, usage on schema public to flyway;

grant select, insert, update, delete on all tables in schema public to produtowebapi;
grant select, insert, update, delete on all tables in schema public to produtoetl;

alter default privileges for user flyway in schema public
grant select, insert, update, delete on tables to produtowebapi;

alter default privileges for user flyway in schema public
grant select, insert, update, delete on tables to produtoetl;