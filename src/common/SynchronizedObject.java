package common;

import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedObject<T> {
	public volatile T object;

	private ReentrantLock l;

	public void synchronizedSet(T value) {
		if (!l.isHeldByCurrentThread())
			l.lock();
		object = value;
		l.unlock();
	}

	public T synchronizedGetL() {
		if (!l.isHeldByCurrentThread())
			l.lock();
		return object;
	}

	public void release() {
		l.unlock();
	}

	public void forceLock() {
		if (!l.isHeldByCurrentThread())
			l.lock();
	}

	public SynchronizedObject(T object) {
		this.object = object;
		l = new ReentrantLock(true);
	}
}
