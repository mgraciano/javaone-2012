package j1.model;

import j1.ci.QueryName;
import j1.databeans.Customer;
import java.util.List;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

@Model
public class Index {

    @Inject
    @QueryName("Customer.findAll")
    TypedQuery<Customer> customers;

    public List<Customer> customers() {
        return customers.getResultList();
    }
}
