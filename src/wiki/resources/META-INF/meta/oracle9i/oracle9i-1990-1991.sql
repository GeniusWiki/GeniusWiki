ALTER TABLE  EDG_USER_PAGE_MARKS ADD (CREATED_DATE timestamp);

create table EDG_EXT_TODOS (
    PUID number(10,0) not null,
    CREATED_DATE timestamp,
    MODIFIED_DATE timestamp,
    TODO_NAME varchar2(255 char),
    PAGE_UUID varchar2(255 char),
    CREATOR_PUID number(10,0),
    MODIFIER_PUID number(10,0),
    primary key (PUID),
    unique (PAGE_UUID, TODO_NAME)
);

create table EDG_EXT_TODO_ITEMS (
    PUID number(10,0) not null,
    CREATED_DATE timestamp,
    MODIFIED_DATE timestamp,
    ITEM_CONTENT long,
    ITEM_PRIORITY number(10,0),
    ITEM_STATUS varchar2(255 char) not null,
    CREATOR_PUID number(10,0),
    MODIFIER_PUID number(10,0),
    TODO_PUID number(10,0) not null,
    primary key (PUID)
);

create index TODOS_PUUID_IDX on EDG_EXT_TODOS (PAGE_UUID);

alter table EDG_EXT_TODOS 
    add constraint FKF57511184D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS;

alter table EDG_EXT_TODOS 
    add constraint FKF575111841EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS;

alter table EDG_EXT_TODO_ITEMS 
    add constraint FK6FA7F4FC4D7DC207 
    foreign key (CREATOR_PUID) 
    references EDG_USERS;

alter table EDG_EXT_TODO_ITEMS 
    add constraint FK6FA7F4FC41EA323C 
    foreign key (MODIFIER_PUID) 
    references EDG_USERS;

alter table EDG_EXT_TODO_ITEMS 
    add constraint FK6FA7F4FC9A11E8CC 
    foreign key (TODO_PUID) 
    references EDG_EXT_TODOS;
