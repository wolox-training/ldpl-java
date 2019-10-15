package wolox.training.repositories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.junit4.SpringRunner;
import wolox.training.TestUtils;
import wolox.training.models.User;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String userUsername = "some-username";

    private void persistUser() {
        testUser = TestUtils
            .createUserWithData(null, userUsername, "a name", "password");

        TestUtils.persist(testEntityManager, testUser);
    }

    @Test
    public void givenAnUsername_whenFindByUsernameIsCalled_thenReturnUser() {
        persistUser();

        User userFound = userRepository.findFirstByUsername(userUsername)
            .orElse(new User());

        assertThat(userFound.getId(), is(testUser.getId()));
        assertThat(userFound.getUsername(), is(testUser.getUsername()));
        assertThat(userFound.getName(), is(testUser.getName()));
        assertThat(userFound.getBirthDate(), is(testUser.getBirthDate()));
    }

    @Test
    public void givenUnknownUsername_whenFinByUsernameIsCalled_thenReturnNoResults() {
        Assertions.assertThat(userRepository.findFirstByUsername("some username").isPresent())
            .isFalse();
    }

    @Test
    public void givenUsersInDatabase_whenFindAll_thenReturnUserList() {
        for (int i = 0; i < 20; i++) {
            String name = String.valueOf((i % 2 != 0) ? i - 1 : i);

            User tmpUser = TestUtils
                .createUserWithData(null, String.valueOf(i), name, String.valueOf(i));

            TestUtils.persist(testEntityManager, tmpUser);
        }

        Page<User> userList = userRepository
            .findAll(PageRequest.of(0, 5, Sort.by(Order.asc("name"), Order.desc("username"))));

        Assertions.assertThat(userList.getContent()).hasSize(5);
        Assertions.assertThat(userList.getTotalElements()).isEqualTo(20);
        Assertions.assertThat(userList.getTotalPages()).isEqualTo(4);
        Assertions.assertThat(userList.getNumber()).isEqualTo(0);
        Assertions.assertThat(userList.isEmpty()).isFalse();

        List<User> content = userList.getContent();
        Assertions.assertThat(content).isNotEmpty();
        Assertions.assertThat(content).hasSize(5);

        Assertions
            .assertThat(content.get(0).getName())
            .isEqualTo(content.get(1).getName());

        Assertions
            .assertThat(content.get(0).getUsername())
            .isGreaterThan(content.get(1).getUsername());


    }

    @Test
    public void givenNoUsersInDatabase_whenFindAll_thenReturnEmptyList() {
        Page<User> userList = userRepository.findAll(Pageable.unpaged());

        Assertions.assertThat(userList.getContent()).isEmpty();
    }

    @Test
    public void givenNoUsersInDB_whenFindByBirthDateAndName_thenReturnEmpty() {
        Page<User> userList = userRepository
            .findByBirthDateAndName(LocalDate.now().minusYears(2), LocalDate.now().plusYears(2),
                "nam", PageRequest.of(0, 1));

        Assertions.assertThat(userList.getContent()).isEmpty();
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithBothDatesAndName_thenReturnFoundResults() {
        persistUser();

        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now().plusYears(2);

        Page<User> userList = userRepository
            .findByBirthDateAndName(startDate, endDate, "nam", PageRequest.of(0, 1));

        Assertions
            .assertThat(userList.getContent())
            .isNotEmpty();

        User foundUser = userList.getContent().get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getBirthDate()).isBeforeOrEqualTo(endDate);
        Assertions.assertThat(foundUser.getBirthDate()).isAfterOrEqualTo(startDate);
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithOneDate_thenReturnFoundResults() {
        persistUser();

        LocalDate startDate = LocalDate.now().minusYears(2);

        Page<User> userList = userRepository
            .findByBirthDateAndName(startDate, null, "nam", PageRequest.of(0, 1));

        Assertions
            .assertThat(userList.getContent())
            .isNotEmpty();

        User foundUser = userList.getContent().get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getBirthDate()).isAfterOrEqualTo(startDate);
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    public void givenUsersInDB_whenFindByBirthDateAndNameIsCalledWithPartOfName_thenReturnFoundResults() {
        persistUser();

        Page<User> userList = userRepository
            .findByBirthDateAndName(null, null, "nam", PageRequest.of(0, 1));

        Assertions
            .assertThat(userList.getContent())
            .isNotEmpty();

        User foundUser = userList.getContent().get(0);

        Assertions.assertThat(foundUser.getName()).contains("nam");
        Assertions.assertThat(foundUser.getUsername()).isEqualTo(testUser.getUsername());
        Assertions.assertThat(foundUser.getBirthDate()).isEqualTo(testUser.getBirthDate());
    }
}
