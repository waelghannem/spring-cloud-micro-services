package org.wael.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.DiscoveryClient;

@SpringBootApplication
@EnableHystrix
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
    @Bean
    RouteLocator staticRoutes(RouteLocatorBuilder builder) {
    	/*return builder.routes()
    			.route(r -> r.path("/customers/**").uri("http://localhost:8081").id("r1"))
    			.route(r -> r.path("/products/**").uri("http://localhost:8082").id("r2"))
    			.build();*/
    	/*return builder.routes()
    			.route(r -> r.path("/customers/**").uri("lb://CUSTOMER-SERVICE").id("r1"))
    			.route(r -> r.path("/products/**").uri("lb://INVENTORY-SERVICE").id("r2"))
    			.build();*/
    	RouteLocator routeLocator = builder.routes()
    			.route(r -> r
    					.path("/publicCountries/**")
    					.filters(f -> f.addRequestHeader("x-rapidapi-host", "restcountries-v1.p.rapidapi.com")
    							.addRequestHeader("x-rapidapi-key", "41f14ff4e7mshe188ed13118b017p194a1ajsnd313e66e365b")
    							.addRequestHeader("useQueryString", "true")
    							.rewritePath("/publicCountries/(?<segment>.*)", "/${segment}")
    							.hystrix(h -> h.setName("countries").setFallbackUri("forward:/defaultCountries"))
    							)
    					.uri("https://restcountries-v1.p.rapidapi.com").id("r1"))
    			.route(r -> r
    					.path("/muslim/**")
    					.filters(f -> f.addRequestHeader("x-rapidapi-host", "muslimsalat.p.rapidapi.com")
    							.addRequestHeader("x-rapidapi-key", "41f14ff4e7mshe188ed13118b017p194a1ajsnd313e66e365b")
    							.addRequestHeader("useQueryString", "true")
    							.rewritePath("/muslim/(?<segment>.*)", "/${segment}")
    							.hystrix(h -> h.setName("muslimSalat").setFallbackUri("forward:/defaultSalat"))
    							)
    					.uri("https://muslimsalat.p.rapidapi.com").id("r2"))
    			.build();
    	return routeLocator;
    }
    @Bean
    DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp) {
    	return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
    }
}

@RestController
class circuitBreakerrestController {
	@GetMapping("/defaultCountries")
	public Map<String, String> countries() {
		Map<String, String> data = new HashMap<>();
		data.put("message", "defaultCountries");
		data.put("data", "tunisia, maroc, alger .....");
		return data;
	}
	
	@GetMapping("/defaultSalat")
	public Map<String, String> salat() {
		Map<String, String> data = new HashMap<>();
		data.put("message", "defaultSalat");
		data.put("data", "fajr, dhohr, asr .....");
		return data;
	}
}



