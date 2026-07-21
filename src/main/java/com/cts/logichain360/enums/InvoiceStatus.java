package com.cts.logichain360.enums;

public enum InvoiceStatus {
    /** Invoice is valid — the backing order is CONFIRMED, ASSIGNED, IN_TRANSIT, or DELIVERED. */
	ACTIVE,
    /** Invoice is void — the backing order was CANCELLED. */
    VOID
}
