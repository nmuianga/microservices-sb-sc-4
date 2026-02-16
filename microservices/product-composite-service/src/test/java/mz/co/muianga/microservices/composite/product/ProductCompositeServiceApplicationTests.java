package mz.co.muianga.microservices.composite.product;

import mz.co.muianga.api.core.product.Product;
import mz.co.muianga.api.core.recommendation.Recommendation;
import mz.co.muianga.api.core.review.Review;
import mz.co.muianga.api.exceptions.InvalidInputException;
import mz.co.muianga.api.exceptions.NotFoundException;
import mz.co.muianga.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;

  @MockitoBean
  private ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {
    Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_OK))
      .thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));

    Mockito.when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
      .thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK,
        1, "author", 1, "content", "mock address")));

    Mockito.when(compositeIntegration.getReviews(PRODUCT_ID_OK))
      .thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author",
        "subject", "content", "mock address")));

    Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
      .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

    Mockito.when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
      .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
  }

  @Autowired
  private WebTestClient client;

  @Test
  void contextLoads() {}

  @Test
  void getProductById() {
    client.get()
      .uri("/product-composite/" + PRODUCT_ID_OK)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
      .jsonPath("$.recommendations.length()").isEqualTo(1)
      .jsonPath("$.reviews.length()").isEqualTo(1)
    ;
  }

  @Test
  void getProductNotFound() {
    client.get()
      .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody()
      .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
      .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND)
    ;
  }

}
