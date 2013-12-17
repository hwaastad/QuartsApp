/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.view;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;
import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;
import org.waastad.ejb.BusinessBean;
import org.waastad.job.QuartzJob;
import org.waastad.timer.SchedulerBean;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
@Named
@SessionScoped
public class ViewController implements Serializable {

    private static final long serialVersionUID = 2302175898239971184L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ViewController.class);
    @EJB
    private SchedulerBean schedulerBean;
    @EJB
    private BusinessBean businessBean;

    private QuartzJob job;
    private List<QuartzJob> quartzJobs;

    @PostConstruct
    public void init() throws SchedulerException {
        quartzJobs = schedulerBean.getQuartzJobList();
    }
    
    public void testRemote(ActionEvent event){
        businessBean.sayHello("XXX");
    }

    public void prepare(ActionEvent event) {
        job = new QuartzJob();
    }

    public void createJob(ActionEvent event) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        try {
            System.out.println("Adding....");
            schedulerBean.createJob(job);
            Messages.addGlobalInfo("Job added");
            
        } catch (SchedulerException ex) {
            LOG.error("Error", ex);
        } finally {
            ctx.execute("createView.hide();");
        }
    }

    public void startJob(ActionEvent event) throws SchedulerException {
        schedulerBean.startJob(job);
    }

    public void pauseJob(ActionEvent event) throws SchedulerException {
        schedulerBean.stopJob(job);
    }

    public void removeJob(ActionEvent event) {
        try {
            schedulerBean.removeJob(job);
            Messages.addGlobalInfo("Job is removed");
        } catch (SchedulerException ex) {
            Messages.addGlobalError(ExceptionUtils.getRootCauseMessage(ex));
            LOG.error("Error", ex);
        }
    }
    
    public void fireNow(ActionEvent event){
        try {
            schedulerBean.fireNow(job);
        } catch (SchedulerException ex) {
            Messages.addGlobalError(ExceptionUtils.getRootCauseMessage(ex));
            LOG.error("Error", ex);
        }
    }

    public void quartzJobsListener() {
        try {
            quartzJobs = schedulerBean.getQuartzJobList();
        } catch (SchedulerException ex) {
            LOG.error("Error", ex);
        }
    }

    public QuartzJob getJob() {
        return job;
    }

    public void setJob(QuartzJob job) {
        this.job = job;
    }

    public List<QuartzJob> getQuartzJobs() {
        return quartzJobs;
    }

    public void setQuartzJobs(List<QuartzJob> quartzJobs) {
        this.quartzJobs = quartzJobs;
    }

}
