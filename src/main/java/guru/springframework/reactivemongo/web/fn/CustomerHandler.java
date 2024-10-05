package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static guru.springframework.reactivemongo.web.fn.CustomerRouterConfig.CUSTOMER_PATH_ID;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerService customerService;
    private final Validator validator;

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        return ServerResponse.ok().body(customerService.getCustomerById(
                request.pathVariable("customerId")).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                ,CustomerDTO.class);
    }

    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(customerService.getAllCustomers(), CustomerDTO.class);
    }

    public Mono<ServerResponse> createCustomer(ServerRequest request) {
        return customerService.createCustomer(request.bodyToMono(CustomerDTO.class).doOnNext(this::validate))
                .flatMap(customerDTO -> ServerResponse
                        .created(UriComponentsBuilder.fromPath(CUSTOMER_PATH_ID).build(customerDTO.getId()))
                        .build()
                );
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        return customerService.updateCustomer(
                request.pathVariable("customerId"),
                request.bodyToMono(CustomerDTO.class).doOnNext(this::validate)
            )
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        return customerService.getCustomerById(request.pathVariable("customerId"))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> customerService.deleteCustomer(customerDTO.getId()))
                .then(ServerResponse.noContent().build());
    }

    private void validate(CustomerDTO customerDTO) {
        Errors errors = new BeanPropertyBindingResult(customerDTO, "customerDTO");
        validator.validate(customerDTO, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}
