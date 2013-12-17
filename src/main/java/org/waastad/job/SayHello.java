/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.job;

import java.util.Date;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.waastad.ejb.BusinessBean;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
public class SayHello implements Job {

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        Properties props = new Properties();
        props.put("java.naming.security.principal", "admin");
        props.put("java.naming.security.credentials", "admin");
//        props.put("Context.SECURITY_PRINCIPAL", "admin");
//        props.put("Context.SECURITY_CREDENTIALS", "admin");
        props.put("openejb.authentication.realmName", "PropertiesLogin");
        props.put("openejb.ejbd.authenticate-with-request", "true");
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http://127.0.0.1:8081/tomee/ejb");
        try {
            Context ctx = new InitialContext(props);
            BusinessBean businessBean = (BusinessBean) ctx.lookup("java:global/QuartsApp/BusinessBean!org.waastad.ejb.BusinessBean");
            System.out.println("SayHello Job " + new Date());
            System.out.println("From EJB: " + businessBean.sayHello("Helgepelge"));
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

}
