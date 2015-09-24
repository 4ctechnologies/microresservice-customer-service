package be.foreseegroup.micro.resourceservice.customer.service;

import be.foreseegroup.micro.resourceservice.customer.model.Customer;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Kaj on 24/09/15.
 */
public interface CustomerRepository extends CrudRepository<Customer, String> {
}
