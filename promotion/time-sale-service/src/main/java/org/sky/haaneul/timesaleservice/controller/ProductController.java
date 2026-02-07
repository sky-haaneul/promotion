package org.sky.haaneul.timesaleservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sky.haaneul.timesaleservice.domain.Product;
import org.sky.haaneul.timesaleservice.dto.ProductDto;
import org.sky.haaneul.timesaleservice.service.v1.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto.Response> createProduct(@Valid @RequestBody ProductDto.CreateRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductDto.Response.from(product));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto.Response> getProduct(@PathVariable Long productId) {
        Product product = productService.getProduct(productId);
        return ResponseEntity.ok(ProductDto.Response.from(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products.stream()
                .map(ProductDto.Response::from)
                .collect(Collectors.toList()));
    }

}
