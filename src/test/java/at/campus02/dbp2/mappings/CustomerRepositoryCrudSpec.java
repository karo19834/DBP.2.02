package at.campus02.dbp2.mappings;

import java.time.LocalDate;
import java.util.ArrayList;

import at.campus02.dbp2.mappings.Customer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRepositoryCrudSpec {

    // Testdaten
    private final String firstname = "Firstname";
    private final String lastname = "Lastname";
    private final AccountType accountType = AccountType.BASIC;
    private final LocalDate registeredSince = LocalDate.of(2022,12,16);

    // Methode zur Anlage eines neuen Kunden
    private Customer initDefaultCustomer(){
        Customer customer = new Customer();
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setAccountType(accountType);
        customer.setRegisteredSince(registeredSince);
        return customer;
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
    public void createNullAsCustomerReturnsFalse() {
        //given
        //when
        boolean result = repository.create(null);

        //then
        assertFalse(result);
    }

    //CREATE
    @Test
    public void createPersistsCustomerInDatabaseAndReturnsTrue(){
        //given
        Customer toCreate = initDefaultCustomer();

        //when
        boolean result = repository.create(toCreate);

        //then
        assertTrue(result);
        // Kontrolle der Werte
        Customer fromDb = manager.find(Customer.class, toCreate.getId());
        assertEquals(firstname, fromDb.getFirstname());
        assertEquals(lastname, fromDb.getLastname());
        assertEquals(accountType, fromDb.getAccountType());
    }

    @Test
    public void createExistingCustomerReturnsFalse() {
        //given
        Customer toCreate = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(toCreate);
        manager.getTransaction().commit();

        //when
        boolean result = repository.create(toCreate);

        //then
        assertFalse(result);
    }

    @Test
    public void createCustomerWithNullAsAccountTypeThrowsException() {
        //given
        Customer notValid = initDefaultCustomer();
        notValid.setAccountType(null);
        //when        //then
        assertThrows(RuntimeException.class, () -> repository.create(notValid));
    }

    //READ
    @Test
    public void readFindsCustomerInDatabse(){
        //given
        Customer existing = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(existing);
        manager.getTransaction().commit();

        //when
        Customer fromRepository = repository.read(existing.getId());

        //then
        assertEquals(existing.getId(),fromRepository.getId());
        assertEquals(firstname, fromRepository.getFirstname());
    }

    @Test
    public void readWithNotExisstingIdReturnsNull(){
        //when
        Customer fromRepository = repository.read(-1);

        //then
        assertNull(fromRepository);
    }

    // Update
    @Test
    public void updateChangesAttributesinDatabase() {
        //given
        Customer existing = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(existing);
        manager.getTransaction().commit();

        String changedFirstname = "changedFirstname";
        String changedLastname = "changedLastname";
        AccountType changedAccountType = AccountType.PREMIUM;
        LocalDate changedRegisteredSince = LocalDate.of(2023,01,13);

        //when
        existing.setFirstname(changedFirstname);
        existing.setLastname(changedLastname);
        existing.setAccountType(changedAccountType);
        existing.setRegisteredSince(changedRegisteredSince);

        Customer updated = repository.update(existing);

        //then
        assertEquals(existing.getId(), updated.getId());
        assertEquals(changedFirstname, updated.getFirstname());

        manager.clear();
        Customer fromDb = manager.find(Customer.class, updated.getId());
        assertEquals(updated.getId(), fromDb.getId());
        assertEquals(changedAccountType, fromDb.getAccountType());
    }

    @Test
    public void updateNotExistingCustomerThrowsIllegalArgumentException() {
        //given
        Customer nonExisting = initDefaultCustomer();

        //when then
        assertThrows(IllegalArgumentException.class, () -> repository.update(nonExisting));
    }

    // delete
    @Test
    public void deleteRemovesCustomerfromDatabaseAndReturnsTrue() {
        //given
        Customer toDelete = initDefaultCustomer();

        manager.getTransaction().begin();
        manager.persist(toDelete);
        manager.getTransaction().commit();

        //when
        boolean result = repository.delete(toDelete);

        //then
        assertTrue(result);
        manager.clear();

        Customer hopefullyDeleted = manager.find(Customer.class, toDelete.getId());
        assertNull(hopefullyDeleted);
    }

}
