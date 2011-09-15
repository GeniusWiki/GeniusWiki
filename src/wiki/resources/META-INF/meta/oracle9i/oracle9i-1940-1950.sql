   create table EDG_EXT_CAL (
        PUID number(10,0) not null,
        CREATED_DATE timestamp,
        MODIFIED_DATE timestamp,
        CAL_NAME varchar2(255 char),
        PAGE_UUID varchar2(255 char),
        CREATOR_PUID number(10,0),
        MODIFIER_PUID number(10,0),
        primary key (PUID),
        unique (PAGE_UUID, CAL_NAME)
    );

    create table EDG_EXT_CAL_EVENTS (
        PUID number(10,0) not null,
        CREATED_DATE timestamp,
        MODIFIED_DATE timestamp,
        EVENT_CATEGORY number(10,0),
        EVENT_CHAIR varchar2(255 char),
        EVENT_CONTENT clob,
        END_TIME timestamp,
        EVENT_INVITEES varchar2(255 char),
        ALL_DAY_EVENT number(1,0),
        EVENT_LOCATION varchar2(255 char),
        REPEAT_RULE varchar2(32 char),
        START_TIME timestamp,
        EVENT_SUBJECT varchar2(255 char),
        EVENT_URL varchar2(255 char),
        CREATOR_PUID number(10,0),
        MODIFIER_PUID number(10,0),
        CAL_PUID number(10,0) not null,
        primary key (PUID)
    );

create index CAL_PUUID_INDEX on EDG_EXT_CAL (PAGE_UUID);

alter table EDG_EXT_CAL 
    add constraint FK8DB502B94D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS;

alter table EDG_EXT_CAL 
    add constraint FK8DB502B941EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS;

alter table EDG_EXT_CAL_EVENTS 
    add constraint FK7CC8FD5F4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS;

alter table EDG_EXT_CAL_EVENTS 
    add constraint FK7CC8FD5F41EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS;

alter table EDG_EXT_CAL_EVENTS 
    add constraint FK7CC8FD5FECBE18C4 
    foreign key (CAL_PUID) 
    references EDG_EXT_CAL;
