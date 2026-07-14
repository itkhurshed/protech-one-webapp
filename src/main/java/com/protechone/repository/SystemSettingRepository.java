package com.protechone.repository;

import com.protechone.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    List<SystemSetting> findByCompanyId(Long companyId);
    Optional<SystemSetting> findByCompanyIdAndSettingKey(Long companyId, String key);
}
