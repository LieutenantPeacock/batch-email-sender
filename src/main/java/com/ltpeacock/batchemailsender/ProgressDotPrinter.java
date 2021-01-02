package com.ltpeacock.batchemailsender;

/**
 * Utility class for printing characters indicating the progress while sending emails.
 * @author LieutenantPeacock
 *
 */
public class ProgressDotPrinter {
	private final int lineLength;
	private int count;

	public ProgressDotPrinter(final int lineLength) {
		this.lineLength = lineLength;
	}

	public void dot() {
		System.out.print('.');
		check();
	}
	
	public void skip() {
		System.out.print('s');
		check();
	}
	
	private void check() {
		if (++count % lineLength == 0) {
			System.out.println();
		} else if (count % 10 == 0) {
			System.out.print(' ');
		}
	}

	public void done() {
		if (count % lineLength != 0) {
			System.out.println();
		}
	}
}