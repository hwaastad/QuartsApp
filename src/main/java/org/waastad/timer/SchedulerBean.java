/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.timer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.omnifaces.util.Faces;
import static org.quartz.JobBuilder.*;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.*;
import org.quartz.TriggerKey;
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
public class SchedulerBean implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerBean.class);
    private static final long serialVersionUID = -6026489540159034940L;
    private Scheduler scheduler;

    private List<QuartzJob> quartzJobList = new ArrayList<>();

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.setProperty("org.quartz.scheduler.instanceName", "TestScheduler");
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
    public void remove() throws SchedulerException {
        LOG.info("Shutting down");
        scheduler.shutdown();
    }

    public void createJob(QuartzJob job) throws SchedulerException {
        System.out.println("Adding in EJB....");
        if (scheduler.checkExists(JobKey.jobKey(job.getJobName(), job.getJobGroup()))) {
            throw new SchedulerException("Job already exists");
        }
        JobDetail newjob = newJob(SayHello.class).withIdentity(job.getJobName(), job.getJobGroup()).requestRecovery().build();
        SimpleTrigger trigger = newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .repeatForever()
                        //.withRepeatCount(20)
                        .withIntervalInSeconds(5))
                .build();
        scheduler.scheduleJob(newjob, trigger);
        quartzJobList.add(job);
    }

    public void removeJob(QuartzJob job) throws SchedulerException {
        JobKey key = new JobKey(job.getJobName(), job.getJobGroup());
        scheduler.deleteJob(key);
    }

    public void startJob(QuartzJob job) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(job.getJobName(), job.getJobGroup()));
        scheduler.rescheduleJob(trigger.getKey(), trigger);
    }

    public void stopJob(QuartzJob job) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(job.getJobName(), job.getJobGroup()));
        //scheduler.unscheduleJob(TriggerKey.triggerKey(job.getJobName(), job.getJobGroup()));
    }

    public void fireNow(QuartzJob job)
            throws SchedulerException {
//        System.out.println("User is: " + sessionContext.getCallerPrincipal().getName());
        if (Faces.isUserInRole("SuperAdmin")) {
            System.out.println("User is SuperAdmin!");
        }
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    public List<QuartzJob> getQuartzJobList() throws SchedulerException {
        quartzJobList = new ArrayList<>();
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
        return quartzJobList;
    }

}
