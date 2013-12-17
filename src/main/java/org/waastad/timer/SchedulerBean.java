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
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.omnifaces.util.Faces;
import static org.quartz.JobBuilder.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.*;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.jobs.ee.ejb.EJB3InvokerJob;
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
    
    @Resource
    private SessionContext sessionContext;

    private static final Logger LOG = LoggerFactory.getLogger(SchedulerBean.class);
    private static final long serialVersionUID = -6026489540159034940L;
    private Scheduler scheduler;

    public static final String EJB_JNDI_NAME_KEY = "BusinessBean";
    public static final String EJB_METHOD_KEY = "sayHello";
    public static final String EJB_ARG_TYPES_KEY = "argTypes";
    public static final String EJB_ARGS_KEY = "args";
    public static final String INITIAL_CONTEXT_FACTORY
            = "java.naming.factory.initial";
    public static final String PROVIDER_URL = "java.naming.provider.url";
    public static final String PRINCIPAL = "java.naming.security.principal";
    public static final String CREDENTIALS
            = "java.naming.security.credentials";

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

    public void createEJBJob(QuartzJob job) throws SchedulerException {
        System.out.println("Adding in EJB....");
        if (scheduler.checkExists(JobKey.jobKey(job.getJobName(), job.getJobGroup()))) {
            throw new SchedulerException("Job already exists");
        }
        JobDataMap map = new JobDataMap();
        map.put(EJB3InvokerJob.EJB_JNDI_NAME_KEY, "java:global/QuartsApp/BusinessBean!org.waastad.ejb.BusinessBean");
        map.put(EJB3InvokerJob.EJB_METHOD_KEY, "sayHello");
        map.put(EJB3InvokerJob.PROVIDER_URL, "http://127.0.0.1:8081/tomee/ejb");
        map.put(EJB3InvokerJob.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        map.put(EJB3InvokerJob.PRINCIPAL, "admin");
        map.put(EJB3InvokerJob.CREDENTIALS, "admin");
     
        JobDetail detail = newJob(EJB3InvokerJob.class)
                .withIdentity(job.getJobName(), job.getJobGroup())
                .usingJobData(map).build();

        SimpleTrigger trigger = newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .repeatForever()
                        //.withRepeatCount(20)
                        .withIntervalInSeconds(5))
                .build();
        scheduler.scheduleJob(detail, trigger);
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
