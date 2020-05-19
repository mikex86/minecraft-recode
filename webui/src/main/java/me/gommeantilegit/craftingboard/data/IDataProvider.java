package me.gommeantilegit.craftingboard.data;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a component providing data in respect to provided arguments
 */
public interface IDataProvider {

    /**
     * Retrieves data form a specified data port
     *
     * @param dataPort the data port to retrieve data from
     * @param args     the arguments to supply to the data port. Depends on what the data port expects
     * @param <T>      the type of data supplied
     * @return the data requested from the data port
     */
    @NotNull
    default <T> T getData(@NotNull DataPort<T> dataPort, @NotNull Object... args) {
        return dataPort.respondRequest(args);
    }

    /**
     * Retrieves data form a specified data port
     *
     * @param dataPortName the name of the data port to retrieve data from
     * @param args         the arguments to supply to the data port. Depends on what the data port expects
     * @param <T>          the type of data supplied
     * @return the data requested from the data port
     */
    @NotNull
    default <T> T getData(@NotNull String dataPortName, @NotNull Class<T> dataType, @NotNull Object... args) {
        Optional<DataPort<T>> dataPort = getDataPort(dataPortName, dataType);
        if (!dataPort.isPresent()) {
            throw new IllegalArgumentException("Data port not found: \"" + dataPortName + "\"");
        }
        return dataPort.get().respondRequest(args);
    }

    /**
     * @param dataPortName the name of the data port
     * @param type         the type of data the data port supplies
     * @return the data supplied by the data port
     */
    @NotNull
    <T> Optional<DataPort<T>> getDataPort(@NotNull String dataPortName, @NotNull Class<T> type);

    /**
     * Represents an endpoint for data access
     *
     * @param <T> the type of data that the port provides
     */
    interface DataPort<T> {

        /**
         * Called to respond to requests
         *
         * @param args endpoint specific arguments
         * @return the data the endpoint responded with in regards to the supplied arguments
         */
        @NotNull
        T respondRequest(@NotNull Object... args);

    }

}
