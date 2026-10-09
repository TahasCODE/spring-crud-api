package com.example.CRUD.service.impl;
import com.example.CRUD.security.OrderAccessPolicy;
import com.example.CRUD.DTO.OrderRequest;
import com.example.CRUD.DTO.OrderResponse;
import com.example.CRUD.Entity.Order;
import com.example.CRUD.Entity.OrderPlacer;
import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.Entity.Person;
import com.example.CRUD.Entity.Supplier;
import com.example.CRUD.exception.BadRequestException;
import com.example.CRUD.exception.ResourceNotFoundException;
import com.example.CRUD.repo.OrderRepository;
import com.example.CRUD.repo.PersonRepository;
import com.example.CRUD.repo.SupplierRepository;
import com.example.CRUD.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderAccessPolicy orderAccessPolicy;
    private final PersonRepository personRepository;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getSales() {
        return orderRepository.findAllSales().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getPurchases() {
        return orderRepository.findAllPurchases().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toResponse(findOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByPlacer(Long placerId) {
        if (!personRepository.existsById(placerId)) {
            throw new ResourceNotFoundException("Order placer not found with id " + placerId);
        }
        return orderRepository.findByPlacerId(placerId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getBySupplier(Long supplierId) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier not found with id " + supplierId);
        }
        return orderRepository.findPurchasesBySupplier(supplierId).stream().map(this::toResponse).toList();
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        Person person = personRepository.findById(request.placerId())
                .orElseThrow(() -> new ResourceNotFoundException("Order placer not found with id " + request.placerId()));

        if (!(person instanceof OrderPlacer placer)) {
            throw new BadRequestException("This person cannot place orders");
        }

        Supplier supplier = null;
        if (request.supplierId() != null) {
            supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id " + request.supplierId()));
        }

        // Customer -> SaleOrder, Employee -> PurchaseOrder
        Order order = placer.placeOrder(request.totalAmount(), request.status(), supplier);
        orderAccessPolicy.assertCanPlace(order);   // 403 if the logged-in user may not place this order
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
                o.getPlacer().getId(),
                o.getPlacer().getName(),
                s == null ? null : s.getId(),
                s == null ? null : s.getName(),
                o.getOrderDate(),
                o.getStatus(),
                o.getTotalAmount()
        );
    }
}