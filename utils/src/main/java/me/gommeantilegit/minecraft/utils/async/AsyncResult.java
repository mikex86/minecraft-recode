package me.gommeantilegit.minecraft.utils.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** Returned by {@link AsyncExecutor#submit(AsyncTask)}, allows to poll for the result of the asynch workload.
 * @author badlogic */
public class AsyncResult<T> {

	private final ListenableFuture<T> future;

	AsyncResult (ListenableFuture<T> future) {
		this.future = future;
	}

	/** @return whether the {@link AsyncTask} is done */
	public boolean isDone () {
		return future.isDone();
	}

	/** @return waits if necessary for the computation to complete and then returns the result
	 * @throws RuntimeException if there was an error */
	public T get () {
		try {
			return future.get();
		} catch (InterruptedException ex) {
			return null;
		} catch (ExecutionException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	public ListenableFuture<T> getFuture() {
		return future;
	}

	/**
	 * Cancels the task
	 */
	public void cancel() {
		this.future.cancel(true);
	}
}
