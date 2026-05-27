package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the AuthFunction entity.
 */
@Repository
public interface AuthFunctionRepository extends JpaRepository<AuthFunction, Long>, JpaSpecificationExecutor<AuthFunction> {
    @Query(
        value = "select distinct authFunction from AuthFunction authFunction left join fetch authFunction.authPermissions",
        countQuery = "select count(distinct authFunction) from AuthFunction authFunction"
    )
    Page<AuthFunction> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct authFunction from AuthFunction authFunction left join fetch authFunction.authPermissions")
    List<AuthFunction> findAllWithEagerRelationships();

    @Query("select authFunction from AuthFunction authFunction left join fetch authFunction.authPermissions where authFunction.id =:id")
    Optional<AuthFunction> findOneWithEagerRelationships(@Param("id") Long id);
}
