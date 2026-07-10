create role "app-web" nologin;
create role "app-etl" nologin;

create user "app-web-dev" password 'webapi123';
create user "app-etl-dev" password 'etl123';
create user flyway password 'flyway123';

grant "app-web" to "app-web-dev";
grant "app-etl" to "app-etl-dev";

grant connect on database produto to "app-web";
grant connect on database produto to "app-etl";
grant connect on database produto to flyway;

grant usage on schema public to "app-web";
grant usage on schema public to "app-etl";
grant create, usage on schema public to flyway;

grant select, insert, update, delete on all tables in schema public to "app-web";
grant select, insert, update, delete on all tables in schema public to "app-etl";

alter default privileges for role flyway in schema public
grant select, insert, update, delete on tables to "app-web";

alter default privileges for role flyway in schema public
grant select, insert, update, delete on tables to "app-etl";