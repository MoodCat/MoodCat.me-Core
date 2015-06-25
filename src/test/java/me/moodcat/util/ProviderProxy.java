package me.moodcat.util;

import com.google.inject.Provider;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper for a Provider, so Mockito can spy it.
 */
@RequiredArgsConstructor
public class ProviderProxy<T> implements Provider<T> {

	private final Provider<T> provider;

	@Override
	public T get() {
		return provider.get();
	}

}
