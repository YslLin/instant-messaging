package com.ysl.im.vo;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MessageContactVO {

    private Long ownerUid;
    private String ownerName;
    private String ownerAvatar;
    private Long totalUnread;

    private List<ContactInfo> contactInfoList;

    public MessageContactVO(Long ownerUid, String ownerName, String ownerAvatar, Long totalUnread) {
        this.ownerUid = ownerUid;
        this.ownerName = ownerName;
        this.ownerAvatar = ownerAvatar;
        this.totalUnread = totalUnread;
    }

    @Data
    public class ContactInfo {
        private Long otherUid;
        private String otherName;
        private String otherAvatar;
        private Long mid;
        private Integer type;
        private String content;
        private Long convUnread;
        private Date createTime;

        public ContactInfo(Long otherUid, String otherName, String otherAvatar, Long mid, Integer type, String content, Long convUnread, Date createTime) {
            this.otherUid = otherUid;
            this.otherName = otherName;
            this.otherAvatar = otherAvatar;
            this.mid = mid;
            this.type = type;
            this.content = content;
            this.convUnread = convUnread;
            this.createTime = createTime;
        }

    }

    public void appendContact(ContactInfo contactInfo) {
        if (contactInfoList == null) {
            contactInfoList = Lists.newArrayList();
        }
        contactInfoList.add(contactInfo);
    }
}
