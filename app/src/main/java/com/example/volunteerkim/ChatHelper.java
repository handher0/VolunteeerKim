package com.example.volunteerkim;

public class ChatHelper {

    /**
     * 닉네임을 기반으로 채팅방 ID 생성
     */
    public static String generateChatRoomId(String userNickname1, String userNickname2) {
        // null 체크 추가
        if (userNickname1 == null || userNickname2 == null) {
            throw new IllegalArgumentException("User nicknames cannot be null");
        }

        // 사전순으로 정렬하여 일관된 채팅방 ID 생성
        if (userNickname1.compareTo(userNickname2) < 0) {
            return userNickname1 + "_" + userNickname2;
        } else {
            return userNickname2 + "_" + userNickname1;
        }
    }
}
