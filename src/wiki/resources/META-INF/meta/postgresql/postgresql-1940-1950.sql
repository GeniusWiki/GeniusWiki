 create table EDG_EXT_CAL (
    PUID int4 not null,
    CREATED_DATE timestamp,
    MODIFIED_DATE timestamp,
    CAL_NAME varchar(255),
    PAGE_UUID varchar(255),
    CREATOR_PUID int4,
    MODIFIER_PUID int4,
    primary key (PUID),
    unique (PAGE_UUID, CAL_NAME)
);

create table EDG_EXT_CAL_EVENTS (
    PUID int4 not null,
    CREATED_DATE timestamp,
    MODIFIED_DATE timestamp,
    EVENT_CATEGORY int4,
    EVENT_CHAIR varchar(255),
    EVENT_CONTENT text,
    END_TIME timestamp,
    EVENT_INVITEES varchar(255),
    ALL_DAY_EVENT bool,
    EVENT_LOCATION varchar(255),
    REPEAT_RULE varchar(32),
    START_TIME timestamp,
    EVENT_SUBJECT varchar(255),
    EVENT_URL varchar(255),
    CREATOR_PUID int4,
    MODIFIER_PUID int4,
    CAL_PUID int4 not null,
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