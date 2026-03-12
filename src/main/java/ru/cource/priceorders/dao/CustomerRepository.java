package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

  Optional<Customer> findFirstByInnAndKpp(String inn, String kpp);
}
