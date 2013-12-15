/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.quartz.DateBuilder;
import static org.quartz.JobBuilder.*;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.waastad.job.QuartzJob;
import org.waastad.job.SayHello;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
@Singleton
@Startup
public class SchedulerBean {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerBean.class);
    private Scheduler scheduler;

    private final List<QuartzJob> quartzJobList = new ArrayList<>();

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.instanceName", "SmartGuestScheduler");
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.setProperty("org.quartz.threadPool.threadCount", "25");
        props.setProperty("org.quartz.threadPool.threadPriority", "5");
        props.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.useProperties", "false");
        props.setProperty("org.quartz.jobStore.dataSource", "QUARTZDS");
        props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        props.setProperty("org.quartz.jobStore.isClustered", "true");
        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        props.setProperty("org.quartz.dataSource.QUARTZDS.jndiURL", "openejb:Resource/QUARTZDS");
        try {
//            scheduler = StdSchedulerFactory.getDefaultScheduler();
//            scheduler =SchedulerFactory.

            StdSchedulerFactory sf = new StdSchedulerFactory();
            sf.initialize(props);
            scheduler = sf.getScheduler();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();

                    // get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler
                            .getTriggersOfJob(jobKey);
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    quartzJobList.add(new QuartzJob(jobName, jobGroup, nextFireTime));
                }
            }
            scheduler.start();
        } catch (SchedulerException ex) {
            LOG.error("Error", ex);
        }
    }

    @PreDestroy
    public void remove() throws SchedulerException{
        LOG.info("Shutting down");
        scheduler.shutdown();
    }

    public void createJob(QuartzJob job) throws SchedulerException {
        System.out.println("Adding in EJB....");
        JobDetail newjob = newJob(SayHello.class).withIdentity(job.getJobName(), job.getJobGroup()).requestRecovery().build();
        SimpleTrigger trigger = newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withRepeatCount(20)
                        .withIntervalInSeconds(5))
                .build();
        scheduler.scheduleJob(newjob, trigger);
        quartzJobList.add(job);
    }

    public void fireNow(String jobName, String jobGroup)
            throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);
    }

    public List<QuartzJob> getQuartzJobList() {
        return quartzJobList;
    }

}
