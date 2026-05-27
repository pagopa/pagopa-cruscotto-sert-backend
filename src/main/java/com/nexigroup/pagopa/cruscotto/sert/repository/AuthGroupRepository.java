package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
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
 * Spring Data  repository for the AuthGroup entity.
 */
@Repository
public interface AuthGroupRepository extends JpaRepository<AuthGroup, Long>, JpaSpecificationExecutor<AuthGroup> {
    @Query(
        value = "select distinct authGroup from AuthGroup authGroup left join fetch authGroup.authFunctions",
        countQuery = "select count(distinct authGroup) from AuthGroup authGroup"
    )
    Page<AuthGroup> findAllWithEagerRelationships(Pageable pageable);

    @Query("select distinct authGroup from AuthGroup authGroup left join fetch authGroup.authFunctions")
    List<AuthGroup> findAllWithEagerRelationships();

    @Query("select authGroup from AuthGroup authGroup left join fetch authGroup.authFunctions where authGroup.id =:id")
    Optional<AuthGroup> findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select max(authGroup.livelloVisibilita) from AuthGroup authGroup")
    Integer getMaxLivelloVisibilita();

    @Query("select authGroup from AuthGroup authGroup where authGroup.nome =:nome")
    Optional<AuthGroup> findOneByNome(@Param("nome") String nome);

    @Query("""
    select authGroup
    from AuthGroup authGroup
    where authGroup.objectId in :objectIds
""")
    List<AuthGroup> findOneByObjectId(@Param("objectIds") List<String> objectIds);


}
