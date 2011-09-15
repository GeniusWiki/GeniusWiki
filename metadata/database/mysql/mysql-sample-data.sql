--  add admin user with ROLE_ADMIN, then give its INSTANCE admin permission
--  password is "admin"
--  ROLE_ADMIN must be first row
--  username/password: demo/demouser

set foreign_key_checks  =  0;

-- B'1' is only work for MYSQL5 (bit), but not for MYSQL4 (tinyint); '\1' looks working on MYSQL command line,but not for JDBC; True or False looks working both (not test yet)
INSERT INTO @TOKEN.TABLE.PREFIX@USERS (puid, full_name, title, user_name, password, email, created_date,enabled,account_expired, account_locked, credentials_expired,score,position_level ) VALUES (1,'administrator',0,'admin','21232f297a57a5a743894a0e4a801fc3','admin@geniuswiki.com',now(),True,False,False,False,0,0);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(1,1);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(1,2);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(1,3);

-- INSERT INTO @TOKEN.TABLE.PREFIX@USERS (puid, full_name, title, user_name, password, email, created_date,enabled,account_expired, account_locked, credentials_expired,score ,level) VALUES (2,'demo user',0,'demo','91017d590a69dc49807671a51f10ab7f','demo@geniuswiki.com',now(),1,1,1,1,0,0);
-- INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(2,3);

commit; 
set foreign_key_checks  =  1;