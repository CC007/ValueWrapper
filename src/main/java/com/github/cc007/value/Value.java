package com.github.cc007.value;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A wrapper that contains exactly one value.
 *
 * @param <T> the type of the single value
 */
public final class Value<T> {

    /**
     * The single value contained in this wrapper.
     */
    private final T value;

    /**
     * Constructs a {@code Value} with the given value.
     *
     * @param value the single value to be contained in this wrapper
     */
    private Value(T value) {
        this.value = value;
    }

    /**
     * Returns a {@code Value} containing the given value.
     *
     * @param obj the value to be contained in the wrapper
     * @return a {@code Value} containing the given value
     * @param <T> the type of the value
     * @throws NoSuchElementException if the given value is {@code null}
     */
    public static <T> Value<T> of(T obj) {
        if (obj == null) {
            throw new NoSuchElementException("No value present");
        }
        return new Value<T>(obj);
    }

    /**
     * Returns a {@code Value} containing the value from the given {@code Optional}.
     *
     * @param opt the {@code Optional} containing the value
     * @return a {@code Value} containing the value from the {@code Optional}
     * @param <T> the type of the value
     * @throws NoSuchElementException if the {@code Optional} is empty
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Value<T> of(Optional<T> opt) {
        return opt.map(Value::new).orElseThrow();
    }

    /**
     * Returns a {@code Value} containing the single element from the given {@code Stream}.
     *
     * @param stream the {@code Stream} containing the element
     * @return a {@code Value} containing the single element from the {@code Stream}
     * @param <T> the type of the element
     * @throws NoSuchElementException if the {@code Stream} is empty
     * @throws IllegalStateException if the {@code Stream} contains more than one element
     */
    public static <T> Value<T> of(Stream<T> stream) {
        Iterator<T> it = stream.iterator();
        if (!it.hasNext()) throw new NoSuchElementException("No value present");
        T first = it.next();
        if (it.hasNext()) throw new IllegalStateException("Expected exactly one element, but had more");
        return new Value<>(first);
    }

    /**
     * Returns an {@code Optional} describing the value if it matches the given predicate,
     * otherwise returns an empty {@code Optional}.
     *
     * @param filter the predicate to apply to the value
     * @return an {@code Optional} describing the value if it matches the predicate, otherwise an empty {@code Optional}
     */
    public Optional<T> filter(Predicate<? super T> filter) {
        if (filter.test(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    /**
     * Returns a {@code Value} consisting of the results of applying the given
     * function to the value of this {@code Value}.
     *
     * @param mapper the function to apply to the value
     * @return a {@code Value} consisting of the result of applying the function to the value
     * @param <R> the type of the result
     */
    public <R> Value<R> map(Function<? super T, ? extends R> mapper) {
        return new Value<>(mapper.apply(value));
    }

    /**
     * Returns a {@code Value} consisting of the results of applying the given
     * function to the value of this {@code Value}, and then flattening the resulting
     * {@code Value}.
     *
     * @param mapper the function to apply to the value
     * @return a {@code Value} consisting of the result of applying the function to the value
     * @param <R> the type of the result
     */
    public <R> Value<R> flatMap(Function<? super T, ? extends Value<? extends R>> mapper) {
        @SuppressWarnings("unchecked")
        var result = (Value<R>) mapper.apply(value);
        return result;
    }

    /**
     * Returns a {@code Stream} consisting of the results of applying the given
     * function to the value of this {@code Value}, and then flattening the resulting
     * {@code Stream}.
     *
     * @param mapper the function to apply to the value
     * @return a {@code Stream} consisting of the result of applying the function to the value
     * @param <R> the type of the result
     */
    public <R> Stream<R> flatMapToStream(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream().flatMap(mapper);
    }

    /**
     * Returns a {@code Stream} consisting of the results of applying the given
     * function to the value of this {@code Value}, and then flattening the resulting
     * {@code Stream}.
     *
     * @param mapper the function to apply to the value
     * @return a {@code Stream} consisting of the result of applying the function to the value
     * @param <R> the type of the result
     */
    public <R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        return stream().mapMulti(mapper);
    }

    /**
     * Performs the given action on the value of this {@code Value} and returns
     * this {@code Value}.
     *
     * @param action the action to perform on the value
     * @return this {@code Value} after performing the action
     */
    public Value<T> peek(Consumer<? super T> action) {
        action.accept(value);
        return this;
    }

    /**
     * Tests the value of this {@code Value} against the given predicate.
     *
     * @param predicate the predicate to apply to the value
     * @return {@code true} if the value matches the predicate, otherwise {@code false}
     */
    public boolean isMatch(Predicate<? super T> predicate) {
        return predicate.test(value);
    }

    /**
     * Returns the value of this {@code Value}.
     *
     * @return the value of this {@code Value}
     */
    public T get() {
        return value;
    }

    /**
     * Performs the given action on the value of this {@code Value}.
     *
     * @param action the action to perform on the value
     */
    public void andDo(Consumer<? super T> action) {
        action.accept(value);
    }

    /**
     * Returns an {@code Optional} containing the value of this {@code Value}.
     *
     * @return an {@code Optional} containing the value of this {@code Value}
     */
    public Optional<T> optional() {
        return Optional.of(value);
    }

    /**
     * Returns a {@code Stream} containing the value of this {@code Value}.
     *
     * @return a {@code Stream} containing the value of this {@code Value}
     */
    public Stream<T> stream() {
        return Stream.of(value);
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Value}.
     * The other object is considered equal if:
     * <ul>
     * <li>it is also a {@code Value} and;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {@code true} if the other object is "equal to" this object
     *         otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Value<?> other
                && Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code of the value
     *
     * @return hash code value of the value
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this {@code Value}
     * suitable for debugging. The exact presentation format is unspecified and
     * may vary between implementations and versions.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return "Value[" + value + "]";
    }
}
