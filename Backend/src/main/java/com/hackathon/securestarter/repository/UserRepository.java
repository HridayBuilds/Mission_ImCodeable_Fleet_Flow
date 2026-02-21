package com.hackathon.securestarter.repository;

import com.hackathon.securestarter.entity.User;
import com.hackathon.securestarter.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByEmployeeId(String employeeId);

    Boolean existsByEmployeeId(String employeeId);

    List<User> findByRole(Role role);

}
