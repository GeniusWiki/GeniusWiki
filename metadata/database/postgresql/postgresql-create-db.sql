-- This script is just as refrence usage, it won't be used by JDBC to create Database...
drop database @TOKEN.DATABASE.NAME@;
create user @TOKEN.DATABASE.USERNAME@ with PASSWORD '@TOKEN.DATABASE.PASSWORD@';
create database @TOKEN.DATABASE.NAME@ owner @TOKEN.DATABASE.USERNAME@ ENCODING 'utf8';

