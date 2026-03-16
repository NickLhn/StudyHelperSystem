package com.studyhelper.repository;

import com.studyhelper.entity.UserMaterialAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMaterialActionRepository extends JpaRepository<UserMaterialAction, Long> {

    Optional<UserMaterialAction> findByUserIdAndMaterialIdAndActionType(
            Long userId, Long materialId, UserMaterialAction.ActionType actionType);

    boolean existsByUserIdAndMaterialIdAndActionType(
            Long userId, Long materialId, UserMaterialAction.ActionType actionType);

    long countByMaterialIdAndActionType(Long materialId, UserMaterialAction.ActionType actionType);
}
