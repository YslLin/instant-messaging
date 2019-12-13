package com.ysl.im.dao;

import com.ysl.im.entity.ContactMultiKeys;
import com.ysl.im.entity.MessageContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageContactRepository extends JpaRepository<MessageContact, ContactMultiKeys> {

    List<MessageContact> findMessageContactsByOwnerUidOrderByMidDesc(Long ownerUid);
}
