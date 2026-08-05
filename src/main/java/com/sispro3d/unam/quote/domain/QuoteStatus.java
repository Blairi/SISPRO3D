package com.sispro3d.unam.quote.domain;

/**
 * Represents the lifecycle state of a {@link Quote}.
 * A client requests a quote in {@code PENDING} state; the expert
 * responds with a proposal, and the client then accepts or rejects it.
 * {@code EXPIRED} applies when the proposal's {@code validUntil} date passes.
 */
public enum QuoteStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    EXPIRED
}
