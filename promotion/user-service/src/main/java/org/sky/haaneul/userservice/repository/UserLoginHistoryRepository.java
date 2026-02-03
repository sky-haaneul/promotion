package org.sky.haaneul.userservice.repository;

import org.sky.haaneul.userservice.entity.User;
import org.sky.haaneul.userservice.entity.UserLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {
    List<UserLoginHistory> findByUserOrderByLoginTimeDesc(User user);
}
