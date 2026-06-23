package woa.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import woa.app.entity.UserEntity;



public interface UserRepository extends JpaRepository<UserEntity , Long> {
	

}