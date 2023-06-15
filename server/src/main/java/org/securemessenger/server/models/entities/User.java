package org.securemessenger.server.models.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "public")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    @Column(name = "totp_secret", length=16)
    private String totpSecret;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "public_key", length = 1024)
    private String publicKey;

    @Column(name = "user_ip", length=15)
    private String userIp;

    public User(String username, String password, String totpSecret, String photoUrl, String publicKey, String userIp, LocalDateTime regDate) {
        this.username = username;
        this.password = password;
        this.totpSecret = totpSecret;
        this.photoUrl = photoUrl;
        this.regDate = regDate;
        this.publicKey = publicKey;
        this.userIp = userIp;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }
}
