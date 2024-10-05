package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.repositories.CustomerRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.URI;

import static guru.springframework.reactivemongo.web.fn.CustomerRouterConfig.CUSTOMER_PATH;
import static guru.springframework.reactivemongo.web.fn.CustomerRouterConfig.CUSTOMER_PATH_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class CustomerEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;

    @Test
    @Order(1)
    void testGetAllCustomers() {
        webTestClient.get().uri(CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.size()").isEqualTo(3);
    }

    @Test
    @Order(2)
    void testGetCustomerById() {
        CustomerDTO testSavedCustomer = getTestSavedCustomer();

        webTestClient.get().uri(CUSTOMER_PATH_ID, testSavedCustomer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class);
    }

    @Test
    void testGetCustomerByIdNotFound() {
        webTestClient.get().uri(CUSTOMER_PATH_ID, "-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateCustomer() {
        getTestSavedCustomer();
    }

    @Test
    void testDeleteCustomer() {
        CustomerDTO savedCustomer = getTestSavedCustomer();

        webTestClient.delete().uri(CUSTOMER_PATH_ID, savedCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testDeleteCustomerNotFound() {
        webTestClient.delete().uri(CUSTOMER_PATH_ID, "-1")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testCreateCustomerInvalid() {
        CustomerDTO invalidCustomer = getTestCustomerDTO();
        invalidCustomer.setCustomerName("");

        webTestClient.post().uri(CUSTOMER_PATH)
                .header("Content-type", "application/json")
                .body(Mono.just(invalidCustomer), CustomerDTO.class)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    CustomerDTO getTestSavedCustomer() {
        FluxExchangeResult<Void> fluxExchangeResult = webTestClient.post().uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .header("Content-Type", "application/json")
                .body(Mono.just(getTestCustomerDTO()), CustomerDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location")
                .returnResult(Void.class);

        URI location = fluxExchangeResult.getResponseHeaders().getLocation();

        return webTestClient.get().uri(location)
                .exchange()
                .returnResult(CustomerDTO.class)
                .getResponseBody()
                .blockFirst();
    }

    CustomerDTO getTestCustomerDTO() {
        return CustomerDTO.builder()
                .customerName("Jonathan")
                .build();
    }
}
