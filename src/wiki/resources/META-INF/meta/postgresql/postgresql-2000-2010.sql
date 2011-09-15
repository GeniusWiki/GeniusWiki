alter table EDG_USER_ACCESS_LOG drop constraint FKA4AEB406C172C1C8;
drop table EDG_USER_ACCESS_LOG cascade;
drop sequence EDG_USER_ACCESS_LOG_SEQ;
create sequence EDG_TODO_SEQ;
create table EDG_ACTIVITY_LOG (
    PUID int8 not null,
    CREATED_DATE timestamp,
    EXTRO_INFO varchar(255),
    SRC_RESOURCE_NAME varchar(255),
    SRC_RESOURCE_TYPE int4,
    ACTIVITY_STATUS int4,
    ACTIVITY_SUB_TYPE int4,
    TGT_RESOURCE_NAME varchar(255),
    TGT_RESOURCE_TYPE int4,
    ACTIVITY_TYPE int4,
    CREATOR_PUID int4,
    primary key (PUID)
);
create index ACTIVITY_TYPE_INDEX on EDG_ACTIVITY_LOG (ACTIVITY_TYPE, ACTIVITY_SUB_TYPE);
alter table EDG_ACTIVITY_LOG 
    add constraint FKBD03754B4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS;
create sequence EDG_ACTIVITY_LOG_SEQ;
delete from EDG_QRTZ_CRON_TRIGGERS where trigger_name='IndexOptimize-Trigger';
delete from EDG_QRTZ_TRIGGERS where trigger_name='IndexOptimize-Trigger';
delete from EDG_QRTZ_JOB_DETAILS where job_name='IndexOptimize-QuartJob';
delete from EDG_QRTZ_CRON_TRIGGERS where trigger_name='PageCommentNotify-Trigger';
delete from EDG_QRTZ_TRIGGERS where trigger_name='PageCommentNotify-Trigger';
delete from EDG_QRTZ_JOB_DETAILS where job_name='PageCommentNotify-QuartJob';
create table EDG_USER_FOLLOW (
    FOLLOWING_PUID int4 not null,
    FOLLOWER_PUID int4 not null
);
alter table EDG_USER_FOLLOW 
    add constraint FK3601DC6E8F606C2 
    foreign key (FOLLOWING_PUID) 
    references EDG_USERS;
alter table EDG_USER_FOLLOW 
    add constraint FK3601DC6E250F4955 
    foreign key (FOLLOWER_PUID) 
    references EDG_USERS;    