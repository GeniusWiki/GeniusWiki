 create table EDG_EXT_CAL (
    PUID integer not null auto_increment,
    CREATED_DATE datetime,
    MODIFIED_DATE datetime,
    CAL_NAME varchar(255),
    PAGE_UUID varchar(255),
    CREATOR_PUID integer,
    MODIFIER_PUID integer,
    primary key (PUID),
    unique (PAGE_UUID, CAL_NAME)
) type=InnoDB;

create table EDG_EXT_CAL_EVENTS (
    PUID integer not null auto_increment,
    CREATED_DATE datetime,
    MODIFIED_DATE datetime,
    EVENT_CATEGORY integer,
    EVENT_CHAIR varchar(255),
    EVENT_CONTENT longtext,
    END_TIME datetime,
    EVENT_INVITEES varchar(255),
    ALL_DAY_EVENT bit,
    EVENT_LOCATION varchar(255),
    REPEAT_RULE varchar(32),
    START_TIME datetime,
    EVENT_SUBJECT varchar(255),
    EVENT_URL varchar(255),
    CREATOR_PUID integer,
    MODIFIER_PUID integer,
    CAL_PUID integer not null,
    primary key (PUID)
) type=InnoDB;

create index CAL_PUUID_INDEX on EDG_EXT_CAL (PAGE_UUID);

alter table EDG_EXT_CAL 
    add index FK8DB502B94D7DC207 (CREATOR_PUID), 
    add constraint FK8DB502B94D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_CAL 
    add index FK8DB502B941EA323C (MODIFIER_PUID), 
    add constraint FK8DB502B941EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_CAL_EVENTS 
    add index FK7CC8FD5F4D7DC207 (CREATOR_PUID), 
    add constraint FK7CC8FD5F4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_CAL_EVENTS 
    add index FK7CC8FD5F41EA323C (MODIFIER_PUID), 
    add constraint FK7CC8FD5F41EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_CAL_EVENTS 
    add index FK7CC8FD5FECBE18C4 (CAL_PUID), 
    add constraint FK7CC8FD5FECBE18C4 
    foreign key (CAL_PUID) 
    references EDG_EXT_CAL (PUID);