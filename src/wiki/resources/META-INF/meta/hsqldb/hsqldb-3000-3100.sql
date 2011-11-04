#
# In your Quartz properties file, you'll need to set 
# org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.HSQLDBDelegate
#

DROP TABLE  EDG_rtz_locks IF EXISTS;
DROP TABLE  EDG_rtz_scheduler_state IF EXISTS;
DROP TABLE  EDG_rtz_fired_triggers IF EXISTS;
DROP TABLE  EDG_rtz_paused_trigger_grps IF EXISTS;
DROP TABLE  EDG_rtz_calendars IF EXISTS;
DROP TABLE  EDG_rtz_trigger_listeners IF EXISTS;
DROP TABLE  EDG_rtz_blob_triggers IF EXISTS;
DROP TABLE  EDG_rtz_cron_triggers IF EXISTS;
DROP TABLE  EDG_rtz_simple_triggers IF EXISTS;
DROP TABLE  EDG_rtz_triggers IF EXISTS;
DROP TABLE  EDG_rtz_job_listeners IF EXISTS;
DROP TABLE  EDG_rtz_job_details IF EXISTS;
