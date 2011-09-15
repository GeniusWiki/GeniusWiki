alter table EDG_USER_ACCESS_LOG drop foreign key FKA4AEB406C172C1C8;
drop table if exists EDG_USER_ACCESS_LOG;
create table EDG_ACTIVITY_LOG (
    PUID bigint not null auto_increment,
    CREATED_DATE datetime,
    EXTRO_INFO varchar(255),
    SRC_RESOURCE_NAME varchar(255),
    SRC_RESOURCE_TYPE integer,
    ACTIVITY_STATUS integer,
    ACTIVITY_SUB_TYPE integer,
    TGT_RESOURCE_NAME varchar(255),
    TGT_RESOURCE_TYPE integer,
    ACTIVITY_TYPE integer,
    CREATOR_PUID integer,
    primary key (PUID)
) type=InnoDB;
create index ACTIVITY_TYPE_INDEX on EDG_ACTIVITY_LOG (ACTIVITY_TYPE, ACTIVITY_SUB_TYPE);
alter table EDG_ACTIVITY_LOG 
	add index FKBD03754B4D7DC207 (CREATOR_PUID), 
    add constraint FKBD03754B4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS (PUID);
delete from EDG_QRTZ_CRON_TRIGGERS where trigger_name='IndexOptimize-Trigger';
delete from EDG_QRTZ_TRIGGERS where trigger_name='IndexOptimize-Trigger';
delete from EDG_QRTZ_JOB_DETAILS where job_name='IndexOptimize-QuartJob';
delete from EDG_QRTZ_CRON_TRIGGERS where trigger_name='PageCommentNotify-Trigger';
delete from EDG_QRTZ_TRIGGERS where trigger_name='PageCommentNotify-Trigger';
delete from EDG_QRTZ_JOB_DETAILS where job_name='PageCommentNotify-QuartJob';
create table EDG_USER_FOLLOW (
    FOLLOWING_PUID integer not null,
    FOLLOWER_PUID integer not null
) type=InnoDB;
alter table EDG_USER_FOLLOW 
    add index FK3601DC6E8F606C2 (FOLLOWING_PUID), 
    add constraint FK3601DC6E8F606C2 
    foreign key (FOLLOWING_PUID) 
    references EDG_USERS (PUID);
alter table EDG_USER_FOLLOW 
    add index FK3601DC6E250F4955 (FOLLOWER_PUID), 
    add constraint FK3601DC6E250F4955 
    foreign key (FOLLOWER_PUID) 
    references EDG_USERS (PUID);