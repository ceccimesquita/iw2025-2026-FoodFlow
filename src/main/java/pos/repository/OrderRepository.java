  package pos.repository;

  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.data.jpa.repository.Query;
  import org.springframework.data.repository.query.Param;
  import pos.domain.Order;
  import pos.domain.OrderStatus;

  import java.util.List;

  public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o LEFT JOIN FETCH o.serviceSession s LEFT JOIN FETCH s.tableSpot")
    List<Order> findAllWithDetails();

    @Query("SELECT o FROM Order o WHERE o.serviceSession.tableSpot.id = :tableId AND o.status NOT IN :statuses")
    List<Order> findByTableIdAndStatusNotIn(@Param("tableId") Long tableId, @Param("statuses") List<OrderStatus> statuses);
  }
