package com.thecodereveal.shopease.config;

import com.thecodereveal.shopease.auth.entities.Authority;
import com.thecodereveal.shopease.auth.entities.User;
import com.thecodereveal.shopease.auth.repositories.AuthorityRepository;
import com.thecodereveal.shopease.auth.repositories.UserDetailRepository;
import com.thecodereveal.shopease.entities.Category;
import com.thecodereveal.shopease.entities.CategoryType;
import com.thecodereveal.shopease.entities.Product;
import com.thecodereveal.shopease.entities.ProductVariant;
import com.thecodereveal.shopease.entities.Resources;
import com.thecodereveal.shopease.repositories.CategoryRepository;
import com.thecodereveal.shopease.repositories.CategoryTypeRepository;
import com.thecodereveal.shopease.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DevelopmentDataInitializer {

    @Bean
    CommandLineRunner initializeDevelopmentData(
            CategoryRepository categoryRepository,
            CategoryTypeRepository categoryTypeRepository,
            ProductRepository productRepository,
            AuthorityRepository authorityRepository,
            UserDetailRepository userRepository,
            PasswordEncoder passwordEncoder,
            PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return args -> transactionTemplate.execute(status -> {
            Authority userRole = findOrCreateAuthority(authorityRepository, "USER", "Customer account");
            Authority adminRole = findOrCreateAuthority(authorityRepository, "ADMIN", "Store administrator");

            createUserIfMissing(userRepository, passwordEncoder, "admin@shopease.local", "Admin", "ShopEase",
                    "Admin123!", adminRole);
            createUserIfMissing(userRepository, passwordEncoder, "cliente@shopease.local", "Cliente", "Demo",
                    "Cliente123!", userRole);

            Category men = findOrCreateCategory(categoryRepository, "Hombre", "MEN", "Ropa para hombre");
            Category women = findOrCreateCategory(categoryRepository, "Mujer", "WOMEN", "Ropa para mujer");

            seedProduct(productRepository, categoryTypeRepository, men, "Remeras", "TSHIRTS", "Remeras", "Essential Cotton T-Shirt",
                    "Remera de algodon suave para todos los dias.", "Northstar", "24.99", 4.7f, true,
                    "essential-cotton-tshirt", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, men, "Camisas", "SHIRTS", "Camisas", "Oxford Button-Down Shirt",
                    "Camisa Oxford clasica de calce comodo.", "Northstar", "45.99", 4.6f, false,
                    "oxford-button-down-shirt", "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, men, "Jeans", "JEANS", "Jeans", "Straight Fit Denim Jeans",
                    "Jean de corte recto con denim resistente.", "Urban Thread", "59.99", 4.8f, true,
                    "straight-fit-denim-jeans", "https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, men, "Buzos", "HOODIES", "Buzos", "Everyday Fleece Hoodie",
                    "Buzo abrigado de frisa con capucha.", "Urban Thread", "64.99", 4.5f, true,
                    "everyday-fleece-hoodie", "https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, women, "Vestidos", "DRESSES", "Vestidos", "Floral Midi Dress",
                    "Vestido midi liviano con estampa floral.", "Luna", "52.99", 4.9f, true,
                    "floral-midi-dress", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, women, "Abrigos", "COATS", "Abrigos", "Classic Wool Coat",
                    "Abrigo de diseno clasico para dias frescos.", "Luna", "119.99", 4.7f, false,
                    "classic-wool-coat", "https://images.unsplash.com/photo-1539533018447-63fcce2678e3?auto=format&fit=crop&w=900&q=80");
            seedProduct(productRepository, categoryTypeRepository, women, "Remeras", "TSHIRTS", "Remeras", "Relaxed Fit T-Shirt",
                    "Remera de calce relajado y tejido suave.", "Luna", "22.99", 4.4f, false,
                    "relaxed-fit-tshirt", "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?auto=format&fit=crop&w=900&q=80");
            return null;
        });
    }

    private Authority findOrCreateAuthority(AuthorityRepository repository, String role, String description) {
        Authority existing = repository.findAll().stream()
                .filter(authority -> role.equals(authority.getRoleCode()))
                .findFirst()
                .orElse(null);
        return existing != null ? existing : repository.save(Authority.builder()
                        .roleCode(role)
                        .roleDescription(description)
                        .build());
    }

    private void createUserIfMissing(UserDetailRepository repository, PasswordEncoder passwordEncoder,
                                     String email, String firstName, String lastName, String rawPassword,
                                     Authority role) {
        if (repository.findByEmail(email) != null) {
            return;
        }

        repository.save(User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .provider("manual")
                .enabled(true)
                .authorities(new ArrayList<>(List.of(role)))
                .build());
    }

    private Category findOrCreateCategory(CategoryRepository repository, String name, String code,
                                          String description) {
        Category existing = repository.findByCode(code);
        return existing != null ? existing : repository.save(Category.builder()
                        .name(name)
                        .code(code)
                        .description(description)
                        .categoryTypes(new ArrayList<>())
                        .build());
    }

    private void seedProduct(ProductRepository repository, CategoryTypeRepository categoryTypeRepository,
                             Category category, String typeName, String typeCode,
                             String typeDescription, String name, String description, String brand, String price,
                             float rating, boolean newArrival, String slug, String imageUrl) {
        Product existingProduct = repository.findBySlug(slug);
        if (existingProduct != null) {
            existingProduct.setPrice(new BigDecimal(price));
            repository.save(existingProduct);
            return;
        }

        CategoryType categoryType = categoryTypeRepository.findByCategoryAndCode(category, typeCode);
        if (categoryType == null) {
            categoryType = categoryTypeRepository.save(CategoryType.builder()
                    .name(typeName)
                    .code(typeCode)
                    .description(typeDescription)
                    .category(category)
                    .build());
        }

        Product product = Product.builder()
                .name(name)
                .description(description)
                .brand(brand)
                .price(new BigDecimal(price))
                .rating(rating)
                .isNewArrival(newArrival)
                .slug(slug)
                .category(category)
                .categoryType(categoryType)
                .productVariants(new ArrayList<>())
                .resources(new ArrayList<>())
                .build();

        for (String size : List.of("S", "M", "L")) {
            product.getProductVariants().add(ProductVariant.builder()
                    .color("Negro")
                    .size(size)
                    .stockQuantity(10)
                    .product(product)
                    .build());
        }

        product.getResources().add(Resources.builder()
                .name(name)
                .url(imageUrl)
                .type("image")
                .isPrimary(true)
                .product(product)
                .build());

        repository.save(product);
    }
}
