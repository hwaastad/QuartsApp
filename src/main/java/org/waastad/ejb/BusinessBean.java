/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.waastad.ejb;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

/**
 *
 * @author Helge Waastad <helge.waastad@datametrix.no>
 */
@Stateless
@LocalBean
@DeclareRoles("SuperAdmin")
public class BusinessBean {

    @Resource
    private SessionContext sessionContext;

    //@RolesAllowed("AdminGroup")
    public String sayHello(final String name) {
        System.out.println("BusinessBean User is " + sessionContext.getCallerPrincipal().getName());
        return "Hello " + name;
    }

}
