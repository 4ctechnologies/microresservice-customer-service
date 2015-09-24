package be.foreseegroup.micro.resourceservice.customer.service;

import be.foreseegroup.micro.resourceservice.customer.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Kaj on 24/09/15.
 */

@RestController
@RequestMapping("/customers")
public class CustomerService {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    CustomerRepository repo;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Iterable<Customer>> getAll() {
        Iterable<Customer> customers = repo.findAll();
        LOG.info("/customers getAll method called, response size: {}", repo.count());
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public ResponseEntity<Customer> getById(@PathVariable String id) {
        LOG.info("/customers getById method called");
        Customer customer = repo.findOne(id);
        if (customer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        LOG.info("/customers create method called");
        Customer createdCustomer = repo.save(customer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{id}")
    public ResponseEntity<Customer> update(@PathVariable String id, @RequestBody Customer customer) {
        LOG.info("/customers update method called");
        Customer update = repo.findOne(id);
        if (update == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        update.setName(customer.getName());
        update.setAddress(customer.getAddress());
        Customer updatedCustomer = repo.save(update);
        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{id}")
    public ResponseEntity<Customer> delete(@PathVariable String id) {
        LOG.info("/customers delete method called");
        Customer customer = repo.findOne(id);
        if (customer == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        repo.delete(customer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
