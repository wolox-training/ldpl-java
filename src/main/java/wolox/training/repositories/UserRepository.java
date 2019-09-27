package wolox.training.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import wolox.training.models.User;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findFirstByUsername(String name);

    List<User> findAll();

    @Query(
        value =
            "SELECT * FROM users WHERE (:startDate IS NULL OR birth_date BETWEEN :startDate AND :endDate) AND "
                + "(:endDate IS NULL OR name LIKE %:name%)",
        nativeQuery = true
    )
    List<User> findByBirthDateAndName(@Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate, @Param("name") String name);
}
