// repository/NGODetailsRepository.java
package com.pawcare.Pawcare_Backend.repository;

import com.pawcare.Pawcare_Backend.model.NGODetails;
import com.pawcare.Pawcare_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NGODetailsRepository extends JpaRepository<NGODetails, Long> {
    Optional<NGODetails> findByUser(User user);
    Optional<NGODetails> findByNgoName(String ngoName);
    Optional<NGODetails> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
}