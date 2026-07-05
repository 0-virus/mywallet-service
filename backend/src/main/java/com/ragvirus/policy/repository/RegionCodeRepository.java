package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.RegionCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, Long> {

    Optional<RegionCode> findByProvinceAndCity(String province, String city);
}
