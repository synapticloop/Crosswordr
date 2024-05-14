package com.synapticloop.crosswordr.util;

import javafx.scene.control.DatePicker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class FormatHelper {
	public static final SimpleDateFormat SDF_yyyy__MM__dd = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat SDF_dd___MM___yyyy = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat SDF_EE_d = new SimpleDateFormat("EEEE d");
	public static final SimpleDateFormat SDF_MMMM_yyyy_at_ = new SimpleDateFormat(" MMMM yyyy 'at' ");
	public static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance();

	public static String formatNowDate(SimpleDateFormat simpleDateFormat) {
		return(simpleDateFormat.format(new Date(System.currentTimeMillis())));
	}

	public static String formatDate(Date date, SimpleDateFormat simpleDateFormat) {
		return(simpleDateFormat.format(date));
	}
	/**
	 * Get a formatted date from a Datepicker instance
	 *
	 * @param datePicker The DatePicker to use
	 * @param format The SimpleDate format
	 *
	 * @return The formatted date from the DatePicker
	 */
	public static String formatDate(DatePicker datePicker, String format) {
		LocalDate localDate = datePicker.getValue();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, localDate.getYear());
		calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());

		return(new SimpleDateFormat(format).format(calendar.getTime()));
	}

	public static String formatDateMinus(DatePicker datePicker, String format, int numDaysMinus) {
		LocalDate localDate = datePicker.getValue();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, localDate.getYear());
		calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
		calendar.add(Calendar.DAY_OF_MONTH, numDaysMinus);

		return(new SimpleDateFormat(format).format(calendar.getTime()));
	}

	public static String formatDateWithOrdinal(DatePicker datePicker, String format) {
		StringBuffer stringBuffer = new StringBuffer();
		LocalDate localDate = datePicker.getValue();
		if(null != localDate) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, localDate.getYear());
			calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());

			stringBuffer.append(
					new SimpleDateFormat(format)
							.format(
									calendar.getTime())
							.replace(
									"^",
									getDayOfMonthSuffix(
											calendar
													.get(
															Calendar.DAY_OF_MONTH))));
		}

		return(stringBuffer.toString());
	}
	public static String formatDateFullyWithOrdinal(DatePicker datePicker, String time, String location) {
		StringBuffer stringBuffer = new StringBuffer();

		LocalDate localDate = datePicker.getValue();
		if(null != localDate) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, localDate.getYear());
			calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
			calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());

			stringBuffer.append(SDF_EE_d.format(calendar.getTime()));
			stringBuffer.append(getDayOfMonthSuffix(calendar.get(Calendar.DAY_OF_MONTH)));
			stringBuffer.append(SDF_MMMM_yyyy_at_.format(calendar.getTime()));
		}
		if(null != time) {
			stringBuffer.append(time);
		}
		stringBuffer.append(" - ");
		stringBuffer.append(location);

		return(stringBuffer.toString());
	}

	public static String formatCurrency(Double amount) {
		return(CURRENCY.format(amount));
	}

	public static String getDayOfMonthSuffix(final int n) {
		if (n >= 11 && n <= 13) {
			return "th";
		}

		switch (n % 10) {
			case 1:  return "st";
			case 2:  return "nd";
			case 3:  return "rd";
			default: return "th";
		}
	}

	private static final String[] ONES = { " ", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten",
			" Eleven", " Twelve", " Thirteen", " Fourteen", "Fifteen", " Sixteen", " Seventeen", " Eighteen",
			" Nineteen" };

	private static final String[] TENS = { " ", " ", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", "Seventy", " Eighty", " Ninety" };


	public static String formatNumberAsWords(int number) {
		if (number > 19) {
			return(TENS[number / 10] + " " + ONES[number % 10]);
		} else {
			return(ONES[number]);
		}
	}
}
