package com.nexigroup.pagopa.cruscotto.sert.service.qdsl;

import com.querydsl.core.types.OrderSpecifier.NullHandling;
import org.springframework.util.Assert;

public class QdslUtility {

    private QdslUtility() {}

    public static NullHandling toQueryDslNullHandling(org.springframework.data.domain.Sort.NullHandling nullHandling) {
        Assert.notNull(nullHandling, "NullHandling must not be null!");

        return switch (nullHandling) {
            case NULLS_FIRST -> NullHandling.NullsFirst;
            case NULLS_LAST -> NullHandling.NullsLast;
            default -> NullHandling.Default;
        };
    }
}
