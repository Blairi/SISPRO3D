package com.sispro3d.unam.offeredservice.domain;

/**
 * Represents the approval workflow state of an {@link OfferedService}.
 * An expert creates a service in {@code PENDING} state; an admin then
 * reviews it and transitions it to {@code APPROVED} or {@code REJECTED}.
 */
public enum ServiceStatus {
    PENDING,
    APPROVED,
    REJECTED
}