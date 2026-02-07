package org.sky.haaneul.timesaleservice.service.v1;

import lombok.RequiredArgsConstructor;
import org.sky.haaneul.timesaleservice.domain.Product;
import org.sky.haaneul.timesaleservice.dto.ProductDto;
import org.sky.haaneul.timesaleservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(ProductDto.CreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();  // TODO 실제 서비스에서는 paging 처리가 필요
    }
}
