package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Mono<CustomerDTO> getCustomerById(String customerId);

    Flux<CustomerDTO> getAllCustomers();

    Mono<CustomerDTO> createCustomer(Mono<CustomerDTO> customerDTO);

    Mono<CustomerDTO> updateCustomer(String customerId, Mono<CustomerDTO> customerDTO);

    Mono<Void> deleteCustomer(String customerId);
}
