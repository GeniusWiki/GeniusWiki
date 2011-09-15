-- test data
set foreign_key_checks  =  0;

INSERT INTO @TOKEN.TABLE.PREFIX@USERS (puid, full_name, title, user_name, password, email, created_date,enabled,account_expired, account_locked, credentials_expired,score ,position_level) VALUES (2,'demo user',0,'demo','91017d590a69dc49807671a51f10ab7f','demo@geniuswiki.com',now(),1,1,1,1,0,0);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(2,3);
INSERT INTO @TOKEN.TABLE.PREFIX@USERS (puid, full_name, title, user_name, password, email, created_date,enabled,account_expired, account_locked, credentials_expired,score ,position_level) VALUES (3,'user1 fullname',0,'user1','91017d590a69dc49807671a51f10ab7f','user1@geniuswiki.com',now(),1,1,1,1,0,0);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(3,3);
INSERT INTO @TOKEN.TABLE.PREFIX@USERS (puid, full_name, title, user_name, password, email, created_date,enabled,account_expired, account_locked, credentials_expired,score ,position_level) VALUES (4,'user2 fullname',0,'user2','91017d590a69dc49807671a51f10ab7f','user2@geniuswiki.com',now(),1,1,1,1,0,0);
INSERT INTO @TOKEN.TABLE.PREFIX@USER_ROLE  (user_puid,role_puid) values(4,3);

INSERT INTO @TOKEN.TABLE.PREFIX@SPACES (puid,created_date, modified_date, description, name, removed, score,s_type, unix_name, creator_puid, modifier_puid, homepage_puid, configuration_puid,ext_link_type) VALUES (2,CURDATE(),CURDATE(),'test space','test space',False,0,-1,'test space',2,2,NULL,NULL,0);
-- Give space read/write permission to admin role
INSERT INTO @TOKEN.TABLE.PREFIX@RESOURCES (puid,r_type, resource_name) VALUES (3,1,'test space');
INSERT INTO @TOKEN.TABLE.PREFIX@PERMISSIONS (puid,operation, resource_puid) VALUES (5,0,3);
INSERT INTO @TOKEN.TABLE.PREFIX@ROLE_PERMISSIONS (permission_puid,role_puid) VALUES (5,1);
INSERT INTO @TOKEN.TABLE.PREFIX@PERMISSIONS (puid,operation, resource_puid) VALUES (8,1,3);
INSERT INTO @TOKEN.TABLE.PREFIX@ROLE_PERMISSIONS (permission_puid,role_puid) VALUES (8,1);

INSERT INTO @TOKEN.TABLE.PREFIX@SPACES (puid,created_date, modified_date, description, name, removed, score,s_type, unix_name, creator_puid, modifier_puid, homepage_puid, configuration_puid,ext_link_type) VALUES (3,CURDATE(),CURDATE(),'test space2','test space2',False,0,-1,'test space2',2,2,NULL,NULL,0);
-- Give space read permission to admin role
INSERT INTO @TOKEN.TABLE.PREFIX@RESOURCES (puid,r_type, resource_name) VALUES (4,1,'test space2');
INSERT INTO @TOKEN.TABLE.PREFIX@PERMISSIONS (puid,operation, resource_puid) VALUES (6,0,4);
INSERT INTO @TOKEN.TABLE.PREFIX@ROLE_PERMISSIONS (permission_puid,role_puid) VALUES (6,1);


INSERT INTO @TOKEN.TABLE.PREFIX@PAGES (puid,created_date, modified_date, touched_date, tree_level, page_uuid, title, p_type, unix_name, version, removed, modifier_puid, parent_page_puid, progress_puid, creator_puid, content_puid, space_puid) VALUES (1,now(),now(),now(),0,'puuid1','link1',0,NULL,1,0,2,NULL,1,2,1,2);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES (puid,created_date, modified_date, touched_date, tree_level, page_uuid, title, p_type, unix_name, version, removed, modifier_puid, parent_page_puid, progress_puid, creator_puid, content_puid, space_puid) VALUES (2,now(),now(),now(),0,'puuid2','link2',0,NULL,1,0,2,NULL,2,2,1,2);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES (puid,created_date, modified_date, touched_date, tree_level, page_uuid, title, p_type, unix_name, version, removed, modifier_puid, parent_page_puid, progress_puid, creator_puid, content_puid, space_puid) VALUES (3,now(),now(),now(),0,'puuid3','abc > and \\" and \\] \\& and',0,NULL,1,0,2,NULL,3,2,1,2);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_CONTENT (puid, content,content_type) VALUES  (1,'page content1',0);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_CONTENT (puid, content,content_type) VALUES  (2,'page content2',0);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_CONTENT (puid, content,content_type) VALUES  (3,'page content3',0);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_PROGRESS (puid) VALUES  (1);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_PROGRESS (puid) VALUES  (2);
INSERT INTO @TOKEN.TABLE.PREFIX@PAGES_PROGRESS (puid) VALUES  (3);



INSERT INTO @TOKEN.TABLE.PREFIX@CR_WORKSPACE (puid, name, password, quota, space_uuid, username) VALUES (1,'test space',NULL,209715200,'0e0db23c-4fee-3da0-bdbe-5ff891c196c9',NULL);
INSERT INTO @TOKEN.TABLE.PREFIX@CR_FILENODES (puid, description, content_type, creator_name, encoding, filename, identifier_uuid, modified_date, node_type, node_uuid, reference_node_uuid, shared, file_size, space_uname, status, version) VALUES (1,'five ring','image/jpeg','demo','','image.jpg','puuid1',now(),'page','nuuid1',NULL,0,31688,'test space',0,1);
INSERT INTO @TOKEN.TABLE.PREFIX@CR_FILENODES (puid, description, content_type, creator_name, encoding, filename, identifier_uuid, modified_date, node_type, node_uuid, reference_node_uuid, shared, file_size, space_uname, status, version) VALUES (2,'test png 2','image/png','demo','','{pre}abc\{pre}.png','puuid1',now(),'page','nuuid2',NULL,0,31688,'test space',0,1);

-- widget
INSERT INTO @TOKEN.TABLE.PREFIX@WIDGETS (puid, WIDGET_TYPE, WIDGET_UUID, TITLE, DESCRIPTION, CONTENT, SHARED,REFERED_COUNT) VALUES (1,'com.edgenius.wiki.widget.MarkupRenderWidgetTemplate','wuuid1','test widget1 title','test widget1 desc','test widget1 markup',true,0);
-- Give widget read permission to admin role 
INSERT INTO @TOKEN.TABLE.PREFIX@RESOURCES (puid,r_type, resource_name) VALUES (5,3,'wuuid1');
INSERT INTO @TOKEN.TABLE.PREFIX@PERMISSIONS (puid,operation, resource_puid) VALUES (7,0,5);
INSERT INTO @TOKEN.TABLE.PREFIX@ROLE_PERMISSIONS (permission_puid,role_puid) VALUES (7,1);

commit; 

set foreign_key_checks  =  1;