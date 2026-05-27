package com.nexigroup.pagopa.cruscotto.sert.service.qdsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class QueryBuilder {

    @PersistenceContext
    private final EntityManager entityManager;

    QueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> JPAQuery<T> createQuery() {
        return new JPAQuery<T>(entityManager);
    }

    public JPAQueryFactory createQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    public <T> JPADeleteClause deleteQuery(EntityPath<?> entity) {
        return new JPADeleteClause(entityManager, entity);
    }

    public <T> JPAUpdateClause updateQuery(EntityPath<?> entity) {
        return new JPAUpdateClause(entityManager, entity);
    }
}
