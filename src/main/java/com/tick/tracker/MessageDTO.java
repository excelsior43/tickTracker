package com.tick.tracker;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.tick.tracker.Constants.Threading;

/***
 * This components hold Data Transfer Objects that are required by reactive_stream and statistics components.
 * 
 * @author Yasir <sonu.yasir@gmail.com>
 *
 */
@Component
public final class MessageDTO {

	public static class BasicTick {

		private final String instrument;
		private final Double price;
		private final Status status;
		private final Long timestamp;

		public BasicTick(final String instrument, final Double price, final Status status, final Long timestamp) {
			super();
			this.instrument = instrument;
			this.price = price;
			this.status = status;
			this.timestamp = timestamp;
		}

		public String getInstrument() {
			return instrument;
		}

		public Double getPrice() {
			return price;
		}

		public Status getStatus() {
			return status;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		@Override
		public String toString() {
			return "BasicTick [instrument=" + instrument + ", price=" + price + ", status=" + status + ", timestamp="
					+ timestamp + "]";
		}

	}

	public static abstract class TickModel implements Delayed, Comparable<Delayed> {

		private final BasicTick basicTick;

		protected TickModel(final String instrument, final Double price, final Long timestamp, final Status status) {
			Objects.requireNonNull(instrument);
			Objects.requireNonNull(price);
			Objects.requireNonNull(timestamp);
			Objects.requireNonNull(status);

			this.basicTick = new BasicTick(instrument, price, status, timestamp);

		}

		public String getInstrument() {
			return this.basicTick.getInstrument();
		}

		public Double getPrice() {
			return this.basicTick.getPrice();
		}

		public BasicTick getBasicTick() {
			return basicTick;
		}

		public long getTimestamp() {
			return this.basicTick.getTimestamp();
		}

		public Status getStatus() {
			return this.basicTick.getStatus();
		}

		public int compareTo(Delayed obj) {
			return Long.valueOf(this.getDelay(TimeUnit.MILLISECONDS) - obj.getDelay(TimeUnit.MILLISECONDS)).intValue();

		}

		public long getDelay(TimeUnit unit) {

			long diff = this.basicTick.getTimestamp() + Threading.DELAY_WINDOW - System.currentTimeMillis();
			return unit.convert(diff, TimeUnit.MILLISECONDS);
		}

		@Override
		public String toString() {
			return " [getInstrument()=" + getInstrument() + ", getPrice()=" + getPrice() + ", getTimestamp()="
					+ getTimestamp() + ", getStatus()=" + getStatus() + ", getDelay()="
					+ this.getDelay(TimeUnit.MILLISECONDS) + "]";
		}

	}

	public static final class ReceivedTick extends TickModel {

		public ReceivedTick(final String instrument, final Double price, final Long timestamp) {
			super(instrument, price, timestamp, Status.RECEIVED);
		}
	}

	public static final class ActivatedTick extends TickModel {

		public ActivatedTick(final String instrument, final Double amount, final Long timestamp) {
			super(instrument, amount, timestamp, Status.ACTIVATED);
		}
	}

	public static final class ExitedTick extends TickModel {

		public ExitedTick(final String instrument, final Double price, final Long timestamp) {
			super(instrument, price, timestamp, Status.EXITED);
		}
	}

	public static enum Status {
		RECEIVED, ACTIVATED, EXITED;
	}

	public static interface TickConsumerInterface {
		void consumeTick(BasicTick tick);

	}
	/**
	 * This is the component interface that exposes the component specific methods.
	 * 1. getStatistics()
	 * 2. getStatistics(String instrument)
	 * 
	 * @author Yasir <sonu.yasir@gmail.com>
	 *
	 */
	public static interface StatisticsCalculatorInterface extends TickConsumerInterface {

		StatisticsInterface getStatistics() throws StatisticsNotFoundException;

		boolean checkHealth();

		StatisticsInterface getStatistics(final String instrument) throws StatisticsNotFoundException;
	}

	public static interface StatisticsInterface {
		public int getCount();

		public double getMinPrice();

		public double getMaxPrice();

		public double getAverage();

	}

	public static final class StatisticsNotFoundException extends Exception {

		private static final long serialVersionUID = 1L;

		public StatisticsNotFoundException() {
			super();
		}
	}

	public static final class StatisticsModel implements StatisticsInterface {

		private final int count;
		private final double min;
		private final double max;
		private final double avg;

		public StatisticsModel(final int count, final double minPrice, final double maxPrice, final double average) {

			this.count = count;
			this.min = minPrice;
			this.max = maxPrice;
			this.avg = average;

		}

		@Override
		public int getCount() {

			return this.count;
		}

		@Override
		public double getMinPrice() {
			return this.min;
		}

		@Override
		public double getMaxPrice() {
			return this.max;
		}

		@Override
		public double getAverage() {
			return this.avg;
		}

	}

}
