/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.timer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
//@Singleton
//@Startup
public class StartUpBean {

    SchedulerFactory sf;
    Scheduler sched;
    JobDetail job;

    @PostConstruct
    public void init() throws SchedulerException {
//        Properties props = new Properties();
//        props.setProperty("org.quartz.scheduler.instanceName", "SmartGuestScheduler");
//        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
//        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
//        props.setProperty("org.quartz.threadPool.threadCount", "25");
//        props.setProperty("org.quartz.threadPool.threadPriority", "5");
//        props.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
//        props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
//        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
//        props.setProperty("org.quartz.jobStore.useProperties", "false");
//        props.setProperty("org.quartz.jobStore.dataSource", "QUARTZDS");
//        props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
//        props.setProperty("org.quartz.jobStore.isClustered", "true");
//        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
//        props.setProperty("org.quartz.dataSource.QUARTZDS.jndiURL", "openejb:Resource/QUARTZDS");
//        sf = new StdSchedulerFactory(props);
//        sched = sf.getScheduler();
//        String schedId = sched.getSchedulerInstanceId();
//        int count = 1;
//        Date runTime = DateBuilder.nextGivenSecondDate(null, 10);
//        job = newJob(HelloJob.class).withIdentity("job_" + count, schedId).requestRecovery().build();
//        SimpleTrigger trigger = newTrigger()
//                .withIdentity("trigger1", "group1")
//                .startAt(DateBuilder.futureDate(1, DateBuilder.IntervalUnit.SECOND))
//                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                        .withRepeatCount(20)
//                        .withIntervalInSeconds(5))
//                .build();
//        System.out.println(job.getKey()
//                + " will run at: " + trigger.getNextFireTime()
//                + " and repeat: " + trigger.getRepeatCount()
//                + " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
//
//        sched.scheduleJob(job, trigger);
//        sched.start();

    }

    @PreDestroy
    public void destroy() throws SchedulerException {
        sched.shutdown();
    }

}
