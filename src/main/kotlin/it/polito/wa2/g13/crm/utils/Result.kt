package it.polito.wa2.g13.crm.utils

/**
 * [Result] is a type that represents either success ([Ok]) or failure ([Err]).
 */
@Suppress("unused")
sealed class Result<T, E> {

    fun isOk(): Boolean = this is Ok

    fun isErr(): Boolean = this is Err

    /**
     * Converts from [Result]<T, E> to [T]?.
     *
     * Converts this into a [T]?, consuming this, and discarding the error, if any.
     */
    fun ok(): T? = when (this) {
        is Ok -> this.t
        is Err -> null
    }

    /**
     * Converts from [Result]<T, E> to [E]?.
     *
     * Converts this into an [E]?, consuming this, and discarding the success value, if any.
     */
    fun err(): E? = when (this) {
        is Ok -> null
        is Err -> this.err
    }

    /**
     * Maps a [Result]<T, E> to [Result]<U, E> by applying a function to a contained [Ok] value, leaving an [Err] value untouched.
     *
     * This function can be used to compose the results of two functions.
     */
    fun <R> map(f: (T) -> R): Result<R, E> = when (this) {
        is Ok -> Ok(f(this.t))
        is Err -> Err(this.err)
    }

    /**
     * Returns the provided default (if [Err]), or applies a function to the contained value (if [Ok]).
     *
     * Arguments passed to [mapOr] are eagerly evaluated; if you are passing the result of a function call,
     * it is recommended to use [mapOrElse], which is lazily evaluated
     */
    fun <U> mapOr(default: U, f: (T) -> U): U = when (this) {
        is Ok -> f(this.t)
        is Err -> default
    }

    /**
     * Maps a [Result]<T, E> to [U] by applying fallback function [default] to a contained [Err] value,
     * or function [f] to a contained [Ok] value.
     *
     * This function can be used to unpack a successful result while handling an error.
     */
    fun <U> mapOrElse(default: (E) -> U, f: (T) -> U): U = when (this) {
        is Ok -> f(this.t)
        is Err -> default(this.err)
    }

    /**
     * Maps a [Result]<T, E> to [Result]<T, F> by applying a function to a contained [Err] value,
     * leaving an [Ok] value untouched.
     *
     * This function can be used to pass through a successful result while handling an error.
     */
    fun <F> mapErr(op: (E) -> F): Result<T, F> = when (this) {
        is Ok -> Ok(this.t)
        is Err -> Err(op(this.err))
    }

    /**
     * Returns [res] if the result is [Ok], otherwise returns the [Err] value of `this`.
     *
     * Arguments passed to and are eagerly evaluated; if you are passing the result of a function call,
     * it is recommended to use [andThen], which is lazily evaluated.
     */
    fun <R> and(res: Result<R, E>): Result<R, E> = when (this) {
        is Ok -> res
        is Err -> Err(this.err)
    }

    /**
     * Calls [f] if the result is [Ok], otherwise returns the [Err] value of this.
     *
     * This function can be used for control flow based on [Result] values.
     */
    fun <R> andThen(f: (T) -> Result<R, E>): Result<R, E> = when (this) {
        is Ok -> f(this.t)
        is Err -> Err(this.err)
    }

    /**
     * Returns [res] if the result is [Err], otherwise returns the [Ok] value of this.
     *
     * Arguments passed to [or] are eagerly evaluated; if you are passing the result of a function call,
     * it is recommended to use [orElse], which is lazily evaluated.
     */
    fun <F> or(res: Result<T, F>): Result<T, F> = when (this) {
        is Ok -> Ok(this.t)
        is Err -> res
    }

    /**
     * Calls [f] if the result is [Err], otherwise returns the [Ok] value of this.
     *
     * This function can be used for control flow based on result values.
     */
    fun <F> orElse(f: (E) -> Result<T, F>): Result<T, F> = when (this) {
        is Ok -> Ok(this.t)
        is Err -> f(this.err)
    }

    fun unwrap(): T = (this as Ok).t

    /**
     * Returns the contained [Ok] value or a provided [default].
     *
     * Arguments passed to [unwrapOr] are eagerly evaluated; if you are passing the result of a function call,
     * it is recommended to use [unwrapOrElse], which is lazily evaluated.
     */
    fun unwrapOr(default: T): T = when (this) {
        is Ok -> this.t
        is Err -> default
    }

    /**
     * Returns the contained [Ok] value or computes it from a closure.
     */
    fun unwrapOrElse(f: (E) -> T): T = when (this) {
        is Ok -> this.t
        is Err -> f(this.err)
    }
}

/**
 * Contains the success value
 */
data class Ok<T, E>(val t: T) : Result<T, E>()

/**
 * Contains the error value
 */
data class Err<T, E>(val err: E) : Result<T, E>()
