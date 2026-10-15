package com.example.CRUD.service.impl;
import com.example.CRUD.DTO.OrderSummary;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public void exportCsv(LocalDate from, LocalDate to, Writer writer) throws IOException {
        writer.write("id,type,placerName,supplierName,orderDate,status,totalAmount\n");
        int page = 0;
        Page<OrderResponse> result;
        do {
            result = getByDateRange(from, to, page++, 100);
            for (OrderResponse o : result.getContent()) {
                writer.write(String.join(",",
                        csv(o.id()), csv(o.type()), csv(o.placerName()), csv(o.supplierName()),
                        csv(o.orderDate()), csv(o.status()), csv(o.totalAmount())) + "\n");
            }
        } while (result.hasNext());
        writer.flush();
    }

    private String csv(Object value) {
        if (value == null) return "";
        String s = value.toString();
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;                       // blocks spreadsheet formula injection
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    @Override
    public Page<OrderResponse> getByDateRange(LocalDate from, LocalDate to, int page, int size) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        Pageable pageable =  PageRequest.of(page, Math.min(size, 100), Sort.by("id").descending());
        return orderRepository.findByOrderDateBetween(start, end, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll(int page, int size) {
        if (page < 0 || size < 1) {
            throw new BadRequestException("page must be 0 or more and size must be 1 or more");
        }
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("id").descending());
        return orderRepository.findAll(pageable).stream().map(this::toResponse).toList();
    }

    @Override
    public List<OrderSummary> getSummary() {
        return orderRepository.summarizeByStatus();
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
        Order order = findOrder(id);
        orderAccessPolicy.assertCanView(order);   // 403 if a customer asks for someone else's order
        return toResponse(order);
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