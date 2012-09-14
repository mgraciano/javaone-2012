package mgt.overview;

import javax.management.MBeanAttributeInfo;
import javax.management.StandardMBean;

public class CDIDeploymentOverview extends StandardMBean
        implements CDIDeploymentOverviewMBean {
    private int qualifiersCount = 0;
    private int interceptorsBindingCount = 0;

    public CDIDeploymentOverview() {
        super(CDIDeploymentOverviewMBean.class, true);
    }

    @Override
    public int getQualifiersCount() {
        return qualifiersCount;
    }

    public void setQualifiersCount(int qualifiersCount) {
        this.qualifiersCount = qualifiersCount;
    }

    @Override
    public int getInterceptorsBindingCount() {
        return interceptorsBindingCount;
    }

    public void setInterceptorsBindingCount(int interceptorsBindingCount) {
        this.interceptorsBindingCount = interceptorsBindingCount;
    }

    @Override
    protected String getDescription(MBeanAttributeInfo info) {
        switch (info.getName()) {
            case "QualifiersCount":
                return "Count of used qualifiers.";
            case "InterceptorsBindingCount":
                return "Count of used interceptors binding.";
            default:
                return super.getDescription(info);
        }
    }
}
