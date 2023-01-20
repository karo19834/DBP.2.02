package at.campus02.dbp2.mappings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class CustomerRepositoryJpa implements CustomerRepository {

    private EntityManager manager;
    public CustomerRepositoryJpa(EntityManagerFactory factory){
        manager = factory.createEntityManager();
    }

    @Override
    public boolean create(Customer customer) {

        // wenn customer null ist, soll sofort false zurückgeliefert werden
        if (customer == null)
            return false;

        // wenn id != null ist, dann gibts es den customer entweder schon oder es ist fehlerhaft, darum direkt return false;
        if (customer.getId() != null)
            return false;
        manager.getTransaction().begin();
        manager.persist(customer);
        manager.getTransaction().commit();
        return true;
    }

    @Override
    public Customer read(Integer id) {

        // wird keine id übergeben, kann auch nichts gefunden werden, daher direkt return null
        if (id == null)
            return null;
        return manager.find(Customer.class, id);
    }

    @Override
    public Customer update(Customer customer) {

        // geprüft wird ob der zu ändernde Customer auch valide ist, heißt der darf nicht null sein
        if (customer == null)
            return null;
        // geprüft wird ob der zu ändernde Customer auch tatsächlich in der DB vorhanden ist
        if (read(customer.getId()) == null){
            throw new IllegalArgumentException("Customer does not exist, cannot update");
        }
        manager.getTransaction().begin();
        Customer managed = manager.merge(customer);
        manager.getTransaction().commit();


        return managed;
    }

    @Override
    public boolean delete(Customer customer) {
        if (customer == null)
            return false;
        if (read(customer.getId()) == null){
            throw new IllegalArgumentException("Customer does not exist, cannot delete");
        }
        manager.getTransaction().begin();
        manager.remove(manager.merge(customer));
        manager.getTransaction().commit();
        return true;
    }

    @Override
    public List<Customer> getAllCustomers() {
        TypedQuery<Customer> query = manager.createQuery(
                "select c from Customer c " +
                        "order by c.registeredSince",
                Customer.class
        );
        return query.getResultList();
    }

    @Override
    public List<Customer> findByLastname(String lastnamePart) {
        if (lastnamePart == null || lastnamePart.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Customer> query = manager.createNamedQuery(
                "Customer.findByLastnamePart",
                Customer.class
        );
        query.setParameter("wirdSpäterGesetzt", "%" + lastnamePart + "%");
        return query.getResultList();
    }

    @Override
    public List<Customer> findByAccountType(AccountType type) {
        TypedQuery<Customer> query = manager.createQuery(
                "select c from Customer c " +
                        "where c.accountType = :franz",
                Customer.class
        );
        query.setParameter("franz", type);
        return query.getResultList();
    }

    @Override
    public List<Customer> findAllRegisteredAfter(LocalDate date) {
        TypedQuery<Customer> query = manager.createQuery(
                "select c from Customer c " +
                        "where c.registeredSince > :datum",
                Customer.class
        );
        query.setParameter("datum",date);
        return query.getResultList();
    }
}
