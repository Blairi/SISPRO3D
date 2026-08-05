package com.sispro3d.unam.workorder.domain;

/**
 * Represents the lifecycle state of a {@link WorkOrder}.
 * Created in {@code PENDING} from an accepted quote; the owning expert
 * starts it ({@code IN_PROGRESS}) and submits work for review
 * ({@code IN_REVIEW}); the client then approves it ({@code COMPLETED}).
 * The client may cancel it while it is still {@code PENDING} or
 * {@code IN_PROGRESS}.
 */
public enum WorkOrderStatus {
    PENDING,
    IN_PROGRESS,
    IN_REVIEW,
    COMPLETED,
    CANCELED
}
