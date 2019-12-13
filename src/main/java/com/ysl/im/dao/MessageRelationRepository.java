package com.ysl.im.dao;

import com.ysl.im.entity.MessageRelation;
import com.ysl.im.entity.RelationMultiKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRelationRepository extends JpaRepository<MessageRelation, RelationMultiKeys> {

    List<MessageRelation> findAllByOwnerUidAndOtherUidOrderByMid(Long ownerUid, Long otherUid);
}
