package wolox.training.repositories;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import wolox.training.models.User;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findFirstByUsername(String name);

    Page<User> findAll(Pageable pageable);

    @Query(
        value =
            "SELECT * FROM users WHERE (:startDate IS NULL OR birth_date >= :startDate) AND "
                + "(:endDate IS NULL OR birth_date <= :endDate) AND "
                + "(:name IS NULL OR name LIKE %:name%)",
        nativeQuery = true
    )
    Page<User> findByBirthDateAndName(@Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate, @Param("name") String name, Pageable pageable);
}
