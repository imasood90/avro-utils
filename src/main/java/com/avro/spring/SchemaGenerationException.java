package com.avro.spring;

/**
 * Shows that there was error in generating the Avro Schema.
 *
 *
 */
public class SchemaGenerationException extends RuntimeException {

    private static final long serialVersionUID = 3948090887965804027L;

    /**
     * Constructs a new schema generation exception with the specified cause.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <tt>null</tt> value is permitted, and
     *        indicates that the cause is nonexistent or unknown.)
     * @since 1.0
     */
    public SchemaGenerationException(final Throwable cause) {
        super(cause);
    }
}
