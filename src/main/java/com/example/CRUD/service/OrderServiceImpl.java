package com.example.CRUD.service;

import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.Entity.*;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository;
    private final OrderPlacerRepository orderPlacerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll(OrderType type) {
        List<Order> orders = (type == null) ? orderRepository.findAll() : orderRepository.findByType(type);
        return orders.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByPlacer(Long placerId) {
        if (!orderPlacerRepository.existsById(placerId)) {
            throw new ResourceNotFoundException("Order placer not found with id " + placerId);
        }
        return orderRepository.findByOrderPlacerId(placerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier not found with id " + supplierId);
        }
        return orderRepository.findBySupplierId(supplierId).stream().map(this::toResponse).toList();
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        OrderPlacer placer;
        Supplier supplier = null;

        if (request.type() == OrderType.SALE) {
            if (request.supplierId() != null) {
                throw new BadRequestException("A SALE order cannot have a supplier");
            }
            placer = customerRepository.findById(request.placerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + request.placerId()));
        } else {
            if (request.supplierId() == null) {
                throw new BadRequestException("supplierId is required for a PURCHASE order");
            }
            placer = employeeRepository.findById(request.placerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + request.placerId()));
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + request.supplierId()));
        }

        Order order = new Order();
        order.setType(request.type());
        order.setOrderPlacer(placer);
        order.setSupplier(supplier);
        order.setTotalAmount(request.totalAmount());
        order.setStatus(request.status()); // null falls back to PENDING in @PrePersist
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = findOrder(id);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public void delete(Long id) {
        orderRepository.delete(findOrder(id));
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private OrderResponse toResponse(Order o) {
        Supplier s = o.getSupplier();
        return new OrderResponse(
                o.getId(),
                o.getType(),
                o.getOrderPlacer().getId(),
                o.getOrderPlacer().getName(),
                s == null ? null : s.getId(),
                s == null ? null : s.getName(),
                o.getOrderDate(),
                o.getStatus(),
                o.getTotalAmount()
        );
    }
}