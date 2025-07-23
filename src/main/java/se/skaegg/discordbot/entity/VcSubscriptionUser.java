package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "VcSubscriptionUsers",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"memberId", "serverId"})})
public class VcSubscriptionUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    Member member;

    @Column(name = "server_id")
    String serverId;

    @Column(name = "leave_notice", columnDefinition = "BOOLEAN DEFAULT FALSE")
    boolean leaveNotice;

    @Column(name = "subscription_date")
    LocalDate subscriptionDate;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isLeaveNotice() {
        return leaveNotice;
    }

    public void setLeaveNotice(boolean leaveNotice) {
        this.leaveNotice = leaveNotice;
    }

    public LocalDate getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(LocalDate subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }
}
