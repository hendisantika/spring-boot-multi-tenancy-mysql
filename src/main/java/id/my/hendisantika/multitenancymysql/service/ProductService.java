package id.my.hendisantika.multitenancymysql.service;

import id.my.hendisantika.multitenancymysql.entity.Product;
import id.my.hendisantika.multitenancymysql.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-multi-tenancy-mysql
 * User: hendisantika
 * Link: s.id/hendisantika
 * Email: hendisantika@yahoo.co.id
 * Telegram : @hendisantika34
 * Date: 09/10/25
 * Time: 05.22
 * To change this template use File | Settings | File Templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create a new product
     */
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * Get all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Update an existing product
     */
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with ID: {}", id);
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setQuantity(productDetails.getQuantity());
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    /**
     * Delete a product by ID
     */
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Search products by name
     */
    @Transactional(readOnly = true)
    public List<Product> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Get low stock products
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("Fetching products with quantity less than: {}", threshold);
        return productRepository.findByQuantityLessThan(threshold);
    }
}
