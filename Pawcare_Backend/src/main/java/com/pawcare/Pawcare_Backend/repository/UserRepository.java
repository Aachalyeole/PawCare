
package com.pawcare.Pawcare_Backend.repository;

import com.pawcare.Pawcare_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndIsNGOVerifiedFalse(User.Role role);
}