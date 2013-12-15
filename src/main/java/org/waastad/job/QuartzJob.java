/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.job;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
public class QuartzJob implements Serializable {

    private static final long serialVersionUID = 2224907519497841120L;

    private String jobName;
    private String jobGroup;
    private Date nextFireTime;

    public QuartzJob(String jobName, String jobGroup, Date nextFireTime) {
        this.jobName = jobName;
        this.jobGroup = jobGroup;
        this.nextFireTime = nextFireTime;
    }

    public QuartzJob() {
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

}
