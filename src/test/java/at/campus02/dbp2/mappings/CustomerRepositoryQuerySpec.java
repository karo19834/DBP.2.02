package at.campus02.dbp2.mappings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

public class CustomerRepositoryQuerySpec {

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;
    private Customer customer4;
    private Customer customer5;
    private Customer customer6;
    private Customer customer7;

    private void setupCommonTestdata(){
        customer1 = new Customer();
        customer1.setFirstname("Albert");
        customer1.setLastname("Aarhus");
        customer1.setAccountType(AccountType.BASIC);
        customer1.setRegisteredSince(LocalDate.of(2022,1,1));

        customer2 = new Customer();
        customer2.setFirstname("Bernadette");
        customer2.setLastname("Brandtner");
        customer2.setAccountType(AccountType.PREMIUM);
        customer2.setRegisteredSince(LocalDate.of(2022,2,2));

        customer3 = new Customer();
        customer3.setFirstname("Charlie");
        customer3.setLastname("Chandler");
        customer3.setAccountType(AccountType.PREMIUM);
        customer3.setRegisteredSince(LocalDate.of(2022,3,3));

        customer4 = new Customer();
        customer4.setFirstname("Dorli");
        customer4.setLastname("Dornacher");
        customer4.setAccountType(AccountType.BASIC);
        customer4.setRegisteredSince(LocalDate.of(2022,4,4));

        customer5 = new Customer();
        customer5.setFirstname("Emil");
        customer5.setLastname("Eberhard");
        customer5.setAccountType(AccountType.PREMIUM);
        customer5.setRegisteredSince(LocalDate.of(2022,5,5));

        customer6 = new Customer();
        customer6.setFirstname("Charlotte");
        customer6.setLastname("Eberstolz");
        customer6.setAccountType(AccountType.BASIC);
        customer6.setRegisteredSince(LocalDate.of(2022,6,6));

        customer7 = new Customer();
        customer7.setFirstname("Bernhards");
        customer7.setLastname("Hornbacher");
        customer7.setAccountType(AccountType.BASIC);
        customer7.setRegisteredSince(LocalDate.of(2022,7,7));

        manager.getTransaction().begin();
        manager.persist(customer1);
        manager.persist(customer2);
        manager.persist(customer3);
        manager.persist(customer4);
        manager.persist(customer5);
        manager.persist(customer6);
        manager.persist(customer7);
        manager.getTransaction().commit();
    }

    private EntityManagerFactory factory;
    private EntityManager manager;
    private CustomerRepository repository;

    @BeforeEach
    public void beforeEach() {
        factory = Persistence.createEntityManagerFactory("persistenceUnitName");
        manager = factory.createEntityManager();
        repository = new CustomerRepositoryJpa(factory);
    }

    @AfterEach
    public void afterEach() {
        if(manager.isOpen())
            manager.close();
        if(factory.isOpen())
            factory.close();
    }

    @Test
    public void getAllCustomersReturnsAllCustomersFromDbSortedByRegistrationDate() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> sortedCustomers = repository.getAllCustomers();

        // then
        assertThat(sortedCustomers, contains(customer1, customer2, customer3, customer4, customer5, customer6, customer7));
    }

    @Test
    public void getAllCustomersOnEmptyDatabaseReturnsEmptyList() {
        // when
        List<Customer> sortedCustomers = repository.getAllCustomers();

        // then
        assertThat(sortedCustomers, is(empty()));
    }

    @Test
    public void findByAccountTypeReturnsMatchingCustomers() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> basic = repository.findByAccountType(AccountType.BASIC);
        List<Customer> premium = repository.findByAccountType(AccountType.PREMIUM);

        // then
        assertThat(basic, containsInAnyOrder(customer1, customer4, customer6, customer7));
        assertThat(premium, containsInAnyOrder(customer2, customer3, customer5));
    }

    @Test
    public void findByAccountTypeNullReturnsEmptyList() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> result = repository.findByAccountType(null);

        // then
        assertThat(result, is(empty()));
    }

    @Test
    public void findByLastnameReturnsMatchingCustomers() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> matching = repository.findByLastname("orn");

        // then
        assertThat(matching, contains(customer4, customer7));
    }

    @Test
    public void findByLastnameReturnsCaseInsensitivelyMatchingCustomers() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> matching = repository.findByLastname("eBEr");

        // then
        assertThat(matching, contains(customer5, customer6));
    }

    @Test
    public void findByLastnameWithNullOrEmptyStringReturnsEmptyList() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> matching = repository.findByLastname("");

        // then
        assertThat(matching, is(empty()));

        // and when
        matching = repository.findByLastname(null);

        // then
        assertThat(matching, is(empty()));
    }

    @Test
    public void findAllRegisteredAfterReturnsMatchingCustomers() {
        // given
        setupCommonTestdata();

        // when
        List<Customer> matching = repository.findAllRegisteredAfter(LocalDate.of(2022, 4, 4));

        // then
        assertThat(matching, containsInAnyOrder(customer5, customer6, customer7));

        // and when
        matching = repository.findAllRegisteredAfter(LocalDate.of(2022, 4, 3));

        // then
        assertThat(matching, containsInAnyOrder(customer4, customer5, customer6, customer7));
    }
}

