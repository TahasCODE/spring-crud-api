package com.example.CRUD;

import com.example.CRUD.Entity.OrderStatus;
import com.example.CRUD.repo.OrderRepository;
import com.example.CRUD.security.CurrentUser;
import com.example.CRUD.security.OrderAccessPolicy;
import com.example.CRUD.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderAuthorizationTests {
    private final CurrentUser user = mock(CurrentUser.class);
    private final OrderAccessPolicy policy = new OrderAccessPolicy(user);
    private final OrderRepository repository = mock(OrderRepository.class);
    private final OrderServiceImpl service = new OrderServiceImpl(repository, policy, null, null, null);

    @Test
    void customerCannotDeleteOrChangeStatusEvenThroughDirectServiceCalls() {
        when(user.role()).thenReturn("CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> service.delete(10L));
        assertThrows(AccessDeniedException.class, () -> service.updateStatus(10L, OrderStatus.CONFIRMED));
        verifyNoInteractions(repository);
    }

    @Test
    void cashierCannotDeleteOrders() {
        when(user.role()).thenReturn("CASHIER");
        assertThrows(AccessDeniedException.class, () -> service.delete(10L));
        verifyNoInteractions(repository);
    }

    @Test
    void customerCannotReadAnotherPlacerBeforeAnyDatabaseLookup() {
        when(user.role()).thenReturn("CUSTOMER");
        when(user.id()).thenReturn(1L);
        assertThrows(AccessDeniedException.class, () -> service.getByPlacer(2L));
        verifyNoInteractions(repository);
        assertDoesNotThrow(() -> policy.assertCanViewPlacer(1L));
    }

    @Test
    void staffCanViewAnyPlacerAndOnlyManagerCanModify() {
        when(user.role()).thenReturn("CASHIER");
        assertNull(policy.customerPlacerId());
        assertDoesNotThrow(() -> policy.assertCanViewPlacer(2L));
        when(user.role()).thenReturn("MANAGER");
        assertNull(policy.customerPlacerId());
        assertDoesNotThrow(policy::assertManager);
        assertDoesNotThrow(() -> policy.assertCanViewPlacer(2L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void customerListQueryAppliesOwnershipBeforePagination() {
        when(user.role()).thenReturn("CUSTOMER");
        when(user.id()).thenReturn(7L);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(invocation -> {
            Specification specification = invocation.getArgument(0);
            jakarta.persistence.criteria.Root root = mock(jakarta.persistence.criteria.Root.class);
            jakarta.persistence.criteria.Path placer = mock(jakarta.persistence.criteria.Path.class);
            jakarta.persistence.criteria.Path id = mock(jakarta.persistence.criteria.Path.class);
            jakarta.persistence.criteria.CriteriaBuilder builder = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
            when(root.get("placer")).thenReturn(placer);
            when(placer.get("id")).thenReturn(id);
            specification.toPredicate(root, null, builder);
            verify(builder).equal(id, 7L);
            Pageable pageable = invocation.getArgument(1);
            assertEquals(2, pageable.getPageNumber());
            assertEquals(10, pageable.getPageSize());
            return Page.empty(pageable);
        });
        assertTrue(service.getAll(2, 10).isEmpty());
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }
}
