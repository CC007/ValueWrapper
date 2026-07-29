import com.github.cc007.Value;

import static java.util.Comparator.naturalOrder;

/**
 * Demonstrates how to compose computations with {@link Value}.
 *
 * <p>The flow mirrors {@link Optional} and {@link Stream} usage:
 * <ul>
 *     <li>{@link Value#of} creates a wrapped value,</li>
 *     <li>{@link Value#map} transforms the contained value,</li>
 *     <li>{@link Value#filter} conditionally keeps or drops it,</li>
 *     <li>{@link Value#flatMap} chains dependent computations without manual unwrapping,</li>
 *     <li>{@link Value#mapMulti} fans out one wrapped value into multiple candidates,</li>
 *     <li>{@link Value#peek} allows performing side-effects without modifying the wrapped value,</li>
 *     <li>{@link Value#get} extracts the wrapped value for terminal checks and output,</li>
 *     <li>{@link Value#andDo} is a terminal side-effect method: it performs an action on the wrapped value and returns void,
 *     similar to {@link Optional#ifPresent} and {@link Stream#forEach}, and</li>
 *     <li>{@link Value#isMatch} tests the wrapped value against a predicate and returns a boolean, similar to <br>
 *     {@link Stream#anyMatch} and {@link Stream#allMatch} or {@link Optional#filter} followed by {@link Optional#isPresent}</li>
 * </ul>
 *
 * <p>The example also shows interaction with streams and optionals:
 * {@link Value#stream} and {@link Value#flatMapToStream} map the wrapped value to a {@link Stream},
 * while {@link Value#optional} exposes {@link Optional}-style interoperability.
 *
 * <p>{@link Value#of} also works on single-element {@link Stream}s and on {@link Optional}s that are known to be present.
 * The element contained in the stream/optional is then wrapped in a {@link Value}.
 */
void main() {
    var radius = 10;
    var pi = Math.PI;
    var diameter = Value.of(radius)
            .map(r -> r * 2)
            .filter(d -> d > 0);
    var squarePerimeter = Value.of(diameter)
            .map(d -> d * 4);
    var circlePerimeter = Value.of(pi)
            .map(piVal -> piVal * 2)
            .peek(tau -> IO.println("2pi (tau) = " + tau))
            .flatMap(tau -> Value.of(radius * tau));


    if (circlePerimeter.isMatch(perimeter -> perimeter > squarePerimeter.get())) {
        IO.println("Circle perimeter %f is larger than square perimeter %d".formatted(circlePerimeter.get(), squarePerimeter.get()));
    } else {
        IO.println("Circle perimeter %f is smaller than square perimeter %d".formatted(circlePerimeter.get(), squarePerimeter.get()));
    }

    var randomInt = new Random().nextInt(1, squarePerimeter.get() + 1);
    var integerStream = squarePerimeter
            .flatMapToStream(maxsize -> IntStream.rangeClosed(1, maxsize).boxed())
            .sorted(naturalOrder())
            .filter(value -> value == randomInt);

    var randomPerimeter = Value.of(integerStream).get();
    if (circlePerimeter.isMatch(perimeter -> perimeter > randomPerimeter)) {
        IO.println("Circle perimeter %f is larger than random perimeter %d".formatted(circlePerimeter.get(), randomPerimeter));
    } else {
        IO.println("Circle perimeter %f is smaller than random perimeter %d".formatted(circlePerimeter.get(), randomPerimeter));
    }

    var perimeterCandidates = squarePerimeter
            .<Integer>mapMulti((perimeter, downstream) -> {
                downstream.accept(perimeter - 10);
                downstream.accept(perimeter - 5);
                downstream.accept(perimeter);
                downstream.accept(perimeter + 5);
                downstream.accept(perimeter + 10);
            })
            .filter(candidate -> candidate < 75)
            .sorted();

    var mapMultiPerimeter = Value.of(perimeterCandidates.max(naturalOrder())).get();
    if (circlePerimeter.isMatch(perimeter -> perimeter > mapMultiPerimeter)) {
        IO.println("Circle perimeter %f is larger than mapMulti perimeter %d".formatted(circlePerimeter.get(), mapMultiPerimeter));
    } else {
        IO.println("Circle perimeter %f is smaller than mapMulti perimeter %d".formatted(circlePerimeter.get(), mapMultiPerimeter));
    }

    doStuffWithOptional(squarePerimeter.optional());
    doStuffWithStream(squarePerimeter.stream());
}

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
private void doStuffWithOptional(Optional<Integer> optionalPerimeter) {
    optionalPerimeter
            .filter(perimeter1 -> perimeter1 > 50)
            .map(perimeter1 -> perimeter1 + 1).ifPresentOrElse(
                    perimeter -> IO.println("Optional perimeter is %d".formatted(perimeter)),
                    () -> IO.println("Optional perimeter is empty"));
}

private void doStuffWithStream(Stream<Integer> perimeterStream) {
    var streamPerimeterMessage = perimeterStream
            .map("Stream perimeter candidate: %d"::formatted)
            .findFirst()
            .orElse("Stream perimeter is empty");
    IO.println(streamPerimeterMessage);
}
