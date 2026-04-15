package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUserHistory;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthUserHistory;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing users.
 */

@Service
@Transactional
public class AuthUserHistoryService {

    private final QueryBuilder queryBuilder;

    public AuthUserHistoryService(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public String[] getOldPassword(Long userId, Integer vecchiePassword) {
        JPQLQuery<AuthUserHistory> jpqlAuthUserHistory = queryBuilder
            .<AuthUserHistory>createQuery()
            .from(QAuthUserHistory.authUserHistory)
            .select(Projections.fields(AuthUserHistory.class, QAuthUserHistory.authUserHistory.password.as("password")));

        jpqlAuthUserHistory.where(QAuthUserHistory.authUserHistory.authUser.id.eq(userId));

        jpqlAuthUserHistory.orderBy(new OrderSpecifier<>(Order.DESC, Expressions.stringPath("dataModifica")));

        jpqlAuthUserHistory.limit(vecchiePassword);

        List<AuthUserHistory> history = jpqlAuthUserHistory.fetch();

        return history.stream().map(AuthUserHistory::getPassword).toArray(String[]::new);
    }
}
