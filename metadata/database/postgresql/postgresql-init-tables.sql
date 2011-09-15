insert into @TOKEN.TABLE.PREFIX@resources (puid,r_type,resource_name) values(nextval('@TOKEN.TABLE.PREFIX@resources_seq'), 0,'$instance$');
insert into @TOKEN.TABLE.PREFIX@resources (puid,r_type,resource_name) values(nextval('@TOKEN.TABLE.PREFIX@resources_seq'), 1,'$SYSTEM$');

insert into @TOKEN.TABLE.PREFIX@roles (puid,name,display_name,description, role_type) values(nextval('@TOKEN.TABLE.PREFIX@roles_seq'), 'ROLE_SYS_ADMIN','Admin Group','system administrator group',0);
insert into @TOKEN.TABLE.PREFIX@roles (puid,name,display_name,description, role_type) values(nextval('@TOKEN.TABLE.PREFIX@roles_seq'), 'ROLE_SYS_ANONYMOUS','Public','unregister users group',0);
insert into @TOKEN.TABLE.PREFIX@roles (puid,name,display_name,description, role_type) values(nextval('@TOKEN.TABLE.PREFIX@roles_seq'), 'ROLE_SYS_USERS','Registered Users','registered users group',0);

insert into @TOKEN.TABLE.PREFIX@permissions (puid,operation,resource_puid) values(nextval('@TOKEN.TABLE.PREFIX@permissions_seq'),3,1);
insert into @TOKEN.TABLE.PREFIX@permissions (puid,operation,resource_puid) values(nextval('@TOKEN.TABLE.PREFIX@permissions_seq'),0,1);
insert into @TOKEN.TABLE.PREFIX@permissions (puid,operation,resource_puid) values(nextval('@TOKEN.TABLE.PREFIX@permissions_seq'),1,1);
insert into @TOKEN.TABLE.PREFIX@permissions (puid,operation,resource_puid) values(nextval('@TOKEN.TABLE.PREFIX@permissions_seq'),8,1);

INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(1,1);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(2,1);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(3,1);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(4,1);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(2,2);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(2,3);
INSERT INTO @TOKEN.TABLE.PREFIX@role_permissions (permission_puid,role_puid) values(3,3);


INSERT INTO @TOKEN.TABLE.PREFIX@spaces (puid, created_date, modified_date, description, name, removed, score,s_type, unix_name, creator_puid, modifier_puid, homepage_puid, configuration_puid,ext_link_type) VALUES (nextval('@TOKEN.TABLE.PREFIX@spaces_seq'),CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,'System use','$SYSTEM$',False,0,-1,'$SYSTEM$',NULL,NULL,NULL,NULL,0);
commit; 

