package com.jacky.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class AbstractEntity {

	private Long id;
	private long createdAt;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, updatable = false)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable = false, updatable = false)
	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	@Transient
	public ZonedDateTime getCreatedDateTime() {
		return Instant.ofEpochMilli(this.createdAt).atZone(ZoneId.systemDefault());
	}

	@PrePersist
	public void preInsert() {
		setCreatedAt(System.currentTimeMillis());
	}
}
