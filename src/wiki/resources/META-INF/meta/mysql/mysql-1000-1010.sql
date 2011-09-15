alter table edgenius_page_links DROP COLUMN view;
alter table edgenius_page_links ADD COLUMN  new_link varchar(255);