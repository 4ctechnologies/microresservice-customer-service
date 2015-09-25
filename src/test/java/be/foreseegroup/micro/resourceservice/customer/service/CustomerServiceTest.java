package be.foreseegroup.micro.resourceservice.customer.service;

import be.foreseegroup.micro.resourceservice.customer.CustomerServiceApplication;
import be.foreseegroup.micro.resourceservice.customer.model.Customer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Kaj on 25/09/15.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CustomerServiceApplication.class)
@WebIntegrationTest
public class CustomerServiceTest {

    private static final String ROOT_PATH = "http://localhost:8888";
    private static final String UNIT_PATH = "/customers";
    private static final String UNIT_RESOURCE = ROOT_PATH + UNIT_PATH;
    private static final Customer CONSULTANT_1 = new Customer("name1","address1");
    private static final Customer CONSULTANT_2 = new Customer("name2","address2");
    private static final String NON_EXISTING_ID = "nonExistingId";

    @Autowired
    private CustomerRepository repo;

    private RestTemplate restTemplate = new TestRestTemplate();


    @Before
    public void setUp() throws Exception {
        repo.deleteAll();
    }

    @After
    public void tearDown() throws Exception {
        repo.deleteAll();
    }

    /** Test case: getExistingPersonShouldReturnPerson
     *
     * Test if a GET result on an existing entry return the entry itself
     * Also, the Http response should have HttpStatus Code: OK (200)
     */

    @Test
    public void getExistingPersonShouldReturnPerson() {
        //Add the Customer that we will try to GET request to the database
        Customer savedCustomer = repo.save(CONSULTANT_1);

        String url = UNIT_RESOURCE + "/" + savedCustomer.getId();

        //Instantiate the HTTP GET Request
        ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNotNull("Http Request response body did not contain a Customer object", response.getBody());


        Customer receivedCustomer = response.getBody();

        //Finally, match if the values of the received Customer are valid
        assertEquals("ID of the received object did id invalid", savedCustomer.getId(), receivedCustomer.getId());
        assertEquals("name of the received object did id invalid", savedCustomer.getName(), receivedCustomer.getName());
        assertEquals("address of the received object did id invalid", savedCustomer.getAddress(), receivedCustomer.getAddress());
    }

    /** Test case: getUnexistingPersonShouldReturnHttpNotFoundError
     *
     * Test if a GET result on an unexisting entry return an error
     * It should not contain an object in its body
     * It should return a HttpStatus code: NOT_FOUND (404)
     */

    @Test
    public void getUnexistingPersonShouldReturnHttpNotFoundError() {
        String url = UNIT_RESOURCE + "/" + NON_EXISTING_ID;

        //Instantiate the HTTP GET Request
        ResponseEntity<Customer> response = restTemplate.getForEntity(url, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.NOT_FOUND;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNull("Http Request response body did contain a Customer object", response.getBody());
    }

    /** Test case: getPersonsShouldReturnAllPersons
     *
     * Test if a GET results without specifying an ID results all the entries
     * It should contain all the entries in its body
     * It should return HttpStatus code: OK (200)
     */
    @Test
    public void getPersonsShouldReturnAllPersons() {
        //Add the Customer that we will try to GET request to the database
        Customer savedCustomer1 = repo.save(CONSULTANT_1);
        Customer savedCustomer2 = repo.save(CONSULTANT_2);

        String url = UNIT_RESOURCE;

        //Instantiate the HTTP GET Request
        ParameterizedTypeReference<Iterable<Customer>> responseType = new ParameterizedTypeReference<Iterable<Customer>>() {};
        ResponseEntity<Iterable<Customer>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNotNull("Http Request response body did not contain a Customer object", response.getBody());

        //Add the received entries to an ArrayList (has a .size() method to count the entries)
        ArrayList<Customer> responseList = new ArrayList<>();
        if (response.getBody() != null) {
            for (Customer u : response.getBody()) {
                responseList.add(u);
            }

        }

        //Check if the amount of entries is correct
        assertEquals("Response body size did not match", 2, responseList.size());
    }

    /** Test case: createCustomerShouldCreateCustomer
     *
     * Test if a POST result of a Customer instance results in the Customer being saved to the database
     * The Http Request response should return with the HttpStatus code: OK (200)
     */
    @Test
    public void createCustomerShouldCreateCustomer() {
        String url = UNIT_RESOURCE;

        //Instantiate the HTTP POST Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(CONSULTANT_1, requestHeaders);
        ResponseEntity<Customer> response = restTemplate.postForEntity(url, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNotNull("Http Request response body did not contain a Customer object", response.getBody());

        //Check if the returned object is valid in comparison with the published on
        assertEquals("Returned entry is invalid", CONSULTANT_1.getName(), response.getBody().getName());
        assertEquals("Returned entry is invalid", CONSULTANT_1.getAddress(), response.getBody().getAddress());

        //Check if the returned entry contains an ID
        assertNotNull("Returned entry did not contain an ID", response.getBody().getId());

        //Check if the customer was added to the database
        Customer customerFromDb = repo.findOne(response.getBody().getId());

        //Check if the entry that was added is valid
        assertEquals("name did not match",CONSULTANT_1.getName(),customerFromDb.getName());
        assertEquals("address did not match",CONSULTANT_1.getAddress(),customerFromDb.getAddress());

        //Check if only 1 entry was added
        assertEquals("More than one record was added to the database", 1, repo.count());
    }

    /** Test case: createCustomerWithoutBodyShouldNotAddCustomer
     *
     * Test if a POST request without a body does not result in an entry added to the database
     * Also, the Http Request response should have HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void createCustomerWithoutBodyShouldNotAddCustomer() {
        String url = UNIT_RESOURCE;

        //Instantiate the HTTP POST Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Customer> response = restTemplate.postForEntity(url, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNotNull("Http Request response body did not contain a Customer object", response.getBody());

        //Check if a Customer was added to the database
        assertEquals("An entry was added to the database", 0, repo.count());
    }

    /** Test case: editCustomerShouldSaveEditionsAndReturnUpdatedCustomer
     *
     * Test if a PUT request to edit an entry results in the entry being saved
     * The Http Request should respond with an updated entry
     * Also, the Http Request response should have HttpStatus code: OK (200)
     */
    @Test
    public void editCustomerShouldSaveEditionsAndReturnUpdatedCustomer() {
        //Add the Customer that we will try to PUT request to the database
        Customer savedCustomer = repo.save(CONSULTANT_1);

        String url = UNIT_RESOURCE + "/" + savedCustomer.getId();

        //Update the Customer
        savedCustomer.setName("nameEdited");
        savedCustomer.setAddress("addressEdited");


        //Instantiate the HTTP PUT Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(savedCustomer, requestHeaders);
        ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.OK;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNotNull("Http Request response body did not contain a Customer object", response.getBody());

        //Check if the returned entry contains is valid
        assertEquals("Returned entry contained invalid field values", savedCustomer.getId(), response.getBody().getId());
        assertEquals("Returned entry contained invalid field values", savedCustomer.getName(), response.getBody().getName());
        assertEquals("Returned entry contained invalid field values", savedCustomer.getAddress(), response.getBody().getAddress());

        //Fetch the updated entry from the database
        Customer updatedCustomer = repo.findOne(savedCustomer.getId());

        //Check if the update was saved to the database
        assertEquals("Updated entry was not saved to the database", savedCustomer.getName(), updatedCustomer.getName());
        assertEquals("Updated entry was not saved to the database", savedCustomer.getAddress(), updatedCustomer.getAddress());
    }

    /** Test case: editUnexistingCustomerShouldReturnError
     *
     * Test that when we try to update an unexisting entry the Http Request response does not contain an object
     * Also, it should have HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void editUnexistingCustomerShouldReturnError() {
        //Instantiate the HTTP PUT Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(CONSULTANT_1, requestHeaders);
        ResponseEntity<Customer> response = restTemplate.exchange(UNIT_RESOURCE+"/unexistingid", HttpMethod.PUT, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());
    }

    /** Test case: deleteUnexistingCustomerShouldReturnError
     *
     * Test that if we try to delete an unexisting entry, this returns the HttpStatus code: BAD_REQUEST (400)
     */
    @Test
    public void deleteUnexistingCustomerShouldReturnError() {
        String url = UNIT_RESOURCE + "/" + NON_EXISTING_ID;

        //Instantiate the HTTP DELETE Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.BAD_REQUEST;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());
    }

    /** Test case: deleteCustomerShouldReturnError
     *
     * Test if instantiating a DELETE request on an existing entry results in the entry being deleted
     * The Http Request response should have HttpStatus code: NO_CONTENT (204)
     */
    @Test
    public void deleteCustomerShouldReturnError() {
        //Add the Customer that we will try to GET request to the database
        Customer savedCustomer = repo.save(CONSULTANT_1);

        String url = UNIT_RESOURCE + "/" + savedCustomer.getId();

        //Instantiate the HTTP DELETE Request
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Customer> httpEntity = new HttpEntity<>(requestHeaders);
        ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Customer.class);

        //Check if we received a response
        assertNotNull("Http Request response was null", response);

        //Check if we receive the correct HttpStatus code
        HttpStatus expectedCode = HttpStatus.NO_CONTENT;
        assertEquals("HttpStatus code did not match", expectedCode, response.getStatusCode());

        //Check if the response contained a Customer object in its body
        assertNull("Http Request response body did contain an entry object", response.getBody());

        //Check if the entry was deleted in the database
        assertEquals("Customer was not deleted from the database", 0, repo.count());
    }
}