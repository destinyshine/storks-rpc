
package io.destinyshine.storks.core;

import java.util.Objects;

/**
 * Value object to express state of a component or subsystem.
 * <p>
 * Status provides convenient constants for commonly used states like {@link #UP},
 * {@link #DOWN} or {@link #OUT_OF_SERVICE}.
 * <p>
 * Custom states can also be created and used throughout the Spring Boot Health subsystem.
 *
 * @author Christian Dupuis
 * @since 1.1.0
 */
public final class Status {

	/**
	 * {@link Status} indicating that the component or subsystem is in an unknown state.
	 */
	public static final Status UNKNOWN = new Status("UNKNOWN");

	/**
	 * {@link Status} indicating that the component or subsystem is functioning as
	 * expected.
	 */
	public static final Status UP = new Status("UP");

	/**
	 * {@link Status} indicating that the component or subsystem has suffered an
	 * unexpected failure.
	 */
	public static final Status DOWN = new Status("DOWN");

	/**
	 * {@link Status} indicating that the component or subsystem has been taken out of
	 * service and should not be used.
	 */
	public static final Status OUT_OF_SERVICE = new Status("OUT_OF_SERVICE");

	private final String code;

	private final String description;

	/**
	 * Create a new {@link Status} instance with the given code and an empty description.
	 * @param code the status code
	 */
	public Status(String code) {
		this(code, "");
	}

	/**
	 * Create a new {@link Status} instance with the given code and description.
	 * @param code the status code
	 * @param description a description of the status
	 */
	public Status(String code, String description) {
		Objects.requireNonNull(code, "Code must not be null");
		Objects.requireNonNull(description, "Description must not be null");
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return this.code;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj != null && obj instanceof Status) {
			return Objects.equals(this.code, ((Status) obj).code);
		}
		return false;
	}

}
