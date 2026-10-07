package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUserHistory;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuthUserHistoryServiceTest {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void returnsPasswordsFromOrderedLimitedHistoryQuery() {
        QueryBuilder queryBuilder = mock(QueryBuilder.class);
        JPAQuery<AuthUserHistory> query = mock(JPAQuery.class);
        AuthUserHistory first = new AuthUserHistory();
        first.setPassword("encoded-first");
        AuthUserHistory second = new AuthUserHistory();
        second.setPassword("encoded-second");

        when(queryBuilder.<AuthUserHistory>createQuery()).thenReturn(query);
        when(query.from(any(EntityPath.class))).thenReturn(query);
        when(query.select(any(Expression.class))).thenReturn(query);
        when(query.where(any(Predicate.class))).thenReturn(query);
        when(query.orderBy(any(OrderSpecifier.class))).thenReturn(query);
        when(query.limit(2)).thenReturn(query);
        when(query.fetch()).thenReturn(List.of(first, second));

        String[] passwords = new AuthUserHistoryService(queryBuilder).getOldPassword(12L, 2);

        assertThat(passwords).containsExactly("encoded-first", "encoded-second");
        verify(query).limit(2);
        verify(query).fetch();
    }
}