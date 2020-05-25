package org.wael.billingservice;

import java.nio.MappedByteBuffer;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
class Bill {
	@Id @GeneratedValue( strategy = GenerationType.IDENTITY)
	private Long id;
	private Date billingDate;
	private Long customerID;
	@Transient
	private Customer customer;
	@OneToMany(mappedBy = "bill")
	private Collection<ProductItem> productItems;
}

@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill, Long> {
	
}


@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem, Long> {
	
}
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
class ProductItem {
	@Id @GeneratedValue( strategy = GenerationType.IDENTITY)
	private Long id;
	private Long productID;
	@Transient
	private Product product;
	private double price;
	private double quantity;
	@ManyToOne
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Bill bill;
}

@Projection(name = "fullBill", types = Bill.class)
interface BillProjection {
	
	public Long getId();
	public Date getBillingDate();
	public long getCustomerID();
	public Collection<ProductItem> getProductItems();
	
}

@Data
class Customer {
	private Long id; private String name; private String email;
}

@Data
class Product {
	private Long id; private String name; private double price;
}

@FeignClient(name = "CUSTOMER-SERVICE")
interface CustomerService {
	@GetMapping("/customers/{id}")
	public Customer findCustomerById(@PathVariable(name="id") Long id);
	@GetMapping("/customers")
	public PagedModel<Customer> findAllCustomers();
}

@FeignClient(name = "INVENTORY-SERVICE")
interface InventoryService {
	@GetMapping("/products/{id}")
	public Product findProductById(@PathVariable(name="id") Long id);
	@GetMapping("/products")
	public PagedModel<Product> findAllproducts();
}
@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}
	
	@Bean
	CommandLineRunner start(BillRepository billRepository, ProductItemRepository productItemRepository, RepositoryRestConfiguration restConfiguration, CustomerService customerService, InventoryService inventoryService) {
		return args -> {
			Customer c1 = customerService.findCustomerById(1L);
			System.out.println("***********************");
			System.out.println(c1.getId());
			System.out.println(c1.getName());
			System.out.println(c1.getEmail());
			System.out.println("***********************");
			Bill b1 = billRepository.save(new Bill(null, new Date(), c1.getId(),null,  null));
			
			Product p1 = inventoryService.findProductById(1L);			
			System.out.println("***********************");
			System.out.println(p1.getId());
			System.out.println(p1.getName());
			System.out.println(p1.getPrice());
			System.out.println("***********************");
			
			PagedModel<Product> products = inventoryService.findAllproducts();
			products.getContent().forEach(p -> productItemRepository.save(new ProductItem(null, p.getId(), null, p.getPrice(), 30, b1)));
		};

};
}

@RestController
class BillRestController {
	@Autowired
	private BillRepository billRepository;
	@Autowired
	private ProductItemRepository productItemRepository;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private InventoryService inventoryService;
	
	@GetMapping("/fullBill/{id}")
	public Bill getBill(@PathVariable(name = "id")Long id) {
		Bill bill = billRepository.findById(id).get();
		bill.setCustomer(customerService.findCustomerById(bill.getCustomerID()));
		bill.getProductItems().forEach(pi -> pi.setProduct(inventoryService.findProductById(pi.getProductID())));
		return bill;
	}
}
