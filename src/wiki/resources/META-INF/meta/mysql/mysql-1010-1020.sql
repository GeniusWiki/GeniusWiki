drop table if exists edgenius_widgets;
create table edgenius_widgets (
        uid integer not null auto_increment,
        created_date datetime,
        modified_date datetime,
        content longtext,
        description varchar(255),
        logo_large varchar(255),
        logo_small varchar(255),
        refered_count integer,
        shared bit not null,
        title varchar(255),
        type varchar(255),
        uuid varchar(255) unique,
        creator_uid integer,
        modifier_uid integer,
        primary key (uid)
    ) type=InnoDB;
alter table edgenius_widgets 
    add index FK8F14404688B952DC (modifier_uid), 
    add constraint FK8F14404688B952DC 
    foreign key (modifier_uid) 
    references edgenius_users (uid);

alter table edgenius_widgets 
    add index FK8F144046A1DF1DB1 (creator_uid), 
    add constraint FK8F144046A1DF1DB1 
    foreign key (creator_uid) 
    references edgenius_users (uid);
