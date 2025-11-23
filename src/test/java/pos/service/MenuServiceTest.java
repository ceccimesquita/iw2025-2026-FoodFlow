package pos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pos.domain.Product;
import pos.repository.ProductRepository;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void list_shouldReturnAllProducts() {
        // Arrange
        Product p1 = new Product();
        p1.setName("Burger");
        when(productRepository.findAll()).thenReturn(List.of(p1));

        // Act
        List<Product> result = menuService.list();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Burger", result.get(0).getName());
        verify(productRepository).findAll();
    }
}
