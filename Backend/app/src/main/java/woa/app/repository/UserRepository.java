package woa.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import woa.app.entity.UserEntity;



public interface UserRepository extends JpaRepository<UserEntity , Long> {
	
	@Query("SELECT u FROM UserEntity u WHERE u.user_name = :username AND u.user_password = :password")
    //Optional<UserEntity> findByLogin(@Param("username") String username, @Param("password") String password);

	Optional<UserEntity> findByLogin(@Param("username")String user_name, @Param("password")String user_password);
}