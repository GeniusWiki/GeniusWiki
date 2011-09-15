alter table EDG_USER_PAGE_MARKS ADD COLUMN CREATED_DATE datetime;

create table EDG_EXT_TODOS (
    PUID integer not null auto_increment,
    CREATED_DATE datetime,
    MODIFIED_DATE datetime,
    TODO_NAME varchar(255),
    PAGE_UUID varchar(255),
    CREATOR_PUID integer,
    MODIFIER_PUID integer,
    primary key (PUID),
    unique (PAGE_UUID, TODO_NAME)
) type=InnoDB;

create table EDG_EXT_TODO_ITEMS (
    PUID integer not null auto_increment,
    CREATED_DATE datetime,
    MODIFIED_DATE datetime,
    ITEM_CONTENT longtext,
    ITEM_PRIORITY integer,
    ITEM_STATUS varchar(255) not null,
    CREATOR_PUID integer,
    MODIFIER_PUID integer,
    TODO_PUID integer not null,
    primary key (PUID)
) type=InnoDB;


create index TODOS_PUUID_IDX on EDG_EXT_TODOS (PAGE_UUID);

alter table EDG_EXT_TODOS 
    add index FKF57511184D7DC207 (CREATOR_PUID), 
    add constraint FKF57511184D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_TODOS 
    add index FKF575111841EA323C (MODIFIER_PUID), 
    add constraint FKF575111841EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_TODO_ITEMS 
    add index FK6FA7F4FC4D7DC207 (CREATOR_PUID), 
    add constraint FK6FA7F4FC4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_TODO_ITEMS 
    add index FK6FA7F4FC41EA323C (MODIFIER_PUID), 
    add constraint FK6FA7F4FC41EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS (PUID);

alter table EDG_EXT_TODO_ITEMS 
    add index FK6FA7F4FC9A11E8CC (TODO_PUID), 
    add constraint FK6FA7F4FC9A11E8CC 
    foreign key (TODO_PUID) 
    references EDG_EXT_TODOS (PUID);
