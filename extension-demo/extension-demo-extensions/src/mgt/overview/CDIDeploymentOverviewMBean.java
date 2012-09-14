package mgt.overview;

import javax.management.MXBean;

@MXBean
public interface CDIDeploymentOverviewMBean {
    int getInterceptorsBindingCount();

    int getQualifiersCount();
}
