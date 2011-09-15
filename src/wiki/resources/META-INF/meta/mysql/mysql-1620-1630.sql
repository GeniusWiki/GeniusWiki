alter table EDG_ROLES ADD COLUMN  CREATOR_PUID  int(11) default NULL;
alter table EDG_ROLES ADD COLUMN  MODIFIER_PUID  int(11) default NULL;
alter table EDG_ROLES ADD COLUMN  CREATED_DATE datetime default NULL;
alter table EDG_ROLES ADD COLUMN  MODIFIED_DATE  datetime default NULL;