package pos.domain;

public enum OrderStatus {
    PENDING,          // Pendiente
    IN_PREPARATION,   // En Preparación
    LISTO,            // Listo (Saiu da cozinha, espera pagamento)
    ON_THE_WAY,       // En Camino (Delivery)
    DELIVERED,        // Entregado
    CANCELED,         // Cancelado
    PAGADO            // <--- ADICIONE ISSO (Pago e finalizado)
}