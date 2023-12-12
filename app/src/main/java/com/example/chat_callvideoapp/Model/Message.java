package com.example.chat_callvideoapp.Model;

public class Message {
    private String messageId , messageText ,  senderId , messageImageUrl;
    private long timestamp;
    private int fellingIcon = -1;

    public Message() {
    }

    public Message(String messageText, String senderId, long timestamp) {
        this.messageText = messageText;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFellingIcon() {
        return fellingIcon;
    }

    public void setFellingIcon(int fellingIcon) {
        this.fellingIcon = fellingIcon;
    }

    public String getMessageImageUrl() {
        return messageImageUrl;
    }

    public void setMessageImageUrl(String messageImageUrl) {
        this.messageImageUrl = messageImageUrl;
    }
}
