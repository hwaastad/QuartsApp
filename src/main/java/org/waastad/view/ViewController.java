/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.view;

import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;
import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;
import org.waastad.job.QuartzJob;
import org.waastad.timer.SchedulerBean;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
@Named
@ViewScoped
public class ViewController implements Serializable {

    private static final long serialVersionUID = 2302175898239971184L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ViewController.class);
    @EJB
    private SchedulerBean schedulerBean;

    private QuartzJob job;

    public void prepare(ActionEvent event) {
        System.out.println("Preparing.....");
        job = new QuartzJob();
    }

    public void createJob(ActionEvent event) {
        RequestContext ctx = RequestContext.getCurrentInstance();
        try {
            System.out.println("Adding....");
            schedulerBean.createJob(job);
            Messages.addGlobalInfo("Job added");
            ctx.execute("createView.hide();");
        } catch (SchedulerException ex) {
            LOG.error("Error", ex);
        }
    }

    public void startJob(ActionEvent event) {

    }

    public void stopJob(ActionEvent event) {

    }

    public List<QuartzJob> getQuartzJobs() {
        return schedulerBean.getQuartzJobList();
    }

    public QuartzJob getJob() {
        return job;
    }

    public void setJob(QuartzJob job) {
        this.job = job;
    }

}
