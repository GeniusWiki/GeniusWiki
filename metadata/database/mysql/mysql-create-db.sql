drop database if exists @TOKEN.DATABASE.NAME@;
commit;
create database if not exists @TOKEN.DATABASE.NAME@ CHARACTER SET utf8 COLLATE utf8_general_ci;
grant all privileges on @TOKEN.DATABASE.NAME@.* to @TOKEN.DATABASE.USERNAME@@localhost identified by "@TOKEN.DATABASE.PASSWORD@";

-- For all domain name to connect permission(NOT recommended). For example:
-- grant all privileges on @TOKEN.DATABASE.NAME@.* to @TOKEN.DATABASE.USERNAME@@"%" identified by "@TOKEN.DATABASE.PASSWORD@";

-- You may have to explicitly define your hostname in order for things For example:
-- grant all privileges on username.* to dbname@host.domain.com identified by "password";
