package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "members",
		uniqueConstraints = {@UniqueConstraint(columnNames = {"memberId", "server_id"})})
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	Integer id;

	@Column(name = "member_id", nullable = false)
	String memberId;

	@Column(name = "server_id", nullable = false)
	String serverId;

	@Column(name = "username", nullable = false)
	String username;

	@Column(name = "nickname")
	String displayName;

	@Column(name = "avatar_url")
	String avatarUrl;

	@Column(name = "color")
	String color;


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
