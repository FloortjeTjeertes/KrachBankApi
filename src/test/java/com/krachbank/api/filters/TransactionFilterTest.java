package com.krachbank.api.filters;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;


class TransactionFilterTest {

	@Test
	void testSenderIbanGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		filter.setSenderIban("DE1234567890");
		assertEquals("DE1234567890", filter.getSenderIban());
	}

	@Test
	void testReceiverIbanGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		filter.setReceiverIban("DE0987654321");
		assertEquals("DE0987654321", filter.getReceiverIban());
	}

	@Test
	void testInitiatorIdGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		filter.setInitiatorId(42L);
		assertEquals(42L, filter.getInitiatorId());
	}

	@Test
	void testMinAmountGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		BigDecimal min = new BigDecimal("10.50");
		filter.setMinAmount(min);
		assertEquals(min, filter.getMinAmount());
	}

	@Test
	void testMaxAmountGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		BigDecimal max = new BigDecimal("999.99");
		filter.setMaxAmount(max);
		assertEquals(max, filter.getMaxAmount());
	}

	@Test
	void testBeforeDateGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		String dateStr = "2024-06-01T10:15:30";
		filter.setBeforeDate(dateStr);
		LocalDateTime expected = LocalDateTime.parse(dateStr);
		assertEquals(expected, filter.getBeforeDate());
	}

	@Test
	void testAfterDateGetterSetter() {
		TransactionFilter filter = new TransactionFilter();
		String dateStr = "2024-05-01T08:00:00";
		filter.setAfterDate(dateStr);
		LocalDateTime expected = LocalDateTime.parse(dateStr);
		assertEquals(expected, filter.getAfterDate());
	}

	@Test
	void testBeforeDateNull() {
		TransactionFilter filter = new TransactionFilter();
		filter.setBeforeDate(null);
		assertNull(filter.getBeforeDate());
	}

	@Test
	void testAfterDateNull() {
		TransactionFilter filter = new TransactionFilter();
		filter.setAfterDate(null);
		assertNull(filter.getAfterDate());
	}
}